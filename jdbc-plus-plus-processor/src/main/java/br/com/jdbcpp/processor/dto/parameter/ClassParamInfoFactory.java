package br.com.jdbcpp.processor.dto.parameter;

import br.com.jdbcpp.api.InputParam;
import br.com.jdbcpp.processor.exception.InvalidInputParamException;
import br.com.jdbcpp.processor.util.ArrayUtil;
import br.com.jdbcpp.processor.util.CollectionUtil;
import br.com.jdbcpp.processor.util.StringUtil;
import br.com.jdbcpp.processor.util.TypeUtil;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class ClassParamInfoFactory {

    private final Types types;
    private final Elements elements;
    private final ArrayUtil arrayUtil;
    private final CollectionUtil collectionUtil;
    private final TypeUtil typeUtil;

    public ClassParamInfoFactory(final Types types,
                                 final Elements elements,
                                 final ArrayUtil arrayUtil,
                                 final CollectionUtil collectionUtil,
                                 final TypeUtil typeUtil) {
        this.types = types;
        this.elements = elements;
        this.arrayUtil = arrayUtil;
        this.collectionUtil = collectionUtil;
        this.typeUtil = typeUtil;
    }

    public List<ParamInfo> create(final VariableElement param) {
        final var paramName = param.getSimpleName().toString();
        final var paramTypeMirror = param.asType();
        final var paramInfo = buildClass(paramTypeMirror, paramName, null);
        return List.of(paramInfo);
    }

    private ParamInfo buildClass(final TypeMirror paramTypeMirror,
                                 final String paramName,
                                 @Nullable
                                 final TypeMirror parentTypeMirror) {
        final var collectionType = collectionUtil.getCollectionElementType(paramTypeMirror);
        final var arrayType = arrayUtil.getArrayElementType(paramTypeMirror);
        final TypeMirror typeContainer;
        final List<ParamInfo> nestedProperties;
        final TypeElement typeElement;
        if (nonNull(collectionType)) {
            typeElement = (TypeElement) types.asElement(collectionType);
            typeContainer = collectionType;
        } else if (nonNull(arrayType)) {
            typeElement = (TypeElement) types.asElement(arrayType);
            typeContainer = arrayType;
        } else {
            typeElement = (TypeElement) types.asElement(paramTypeMirror);
            typeContainer = null;
        }
        nestedProperties = extractFieldsFromType(typeElement);
        return new ClassParamInfo(
                paramName,
                paramTypeMirror,
                typeContainer,
                nestedProperties,
                typeUtil.isRecord(paramTypeMirror),
                isNull(parentTypeMirror) ?
                        paramName :
                        findMethod(parentTypeMirror, paramName, paramTypeMirror)
        );
    }

    private List<ParamInfo> extractFieldsFromType(final TypeElement typeElement) {
        final var fields = typeElement.getEnclosedElements()
                .stream()
                .filter(element -> element.getKind() == ElementKind.FIELD)
                .map(VariableElement.class::cast)
                .filter(field -> field.getModifiers().contains(Modifier.PRIVATE))
                .filter(field -> !field.getModifiers().contains(Modifier.STATIC))
                .toList();

        final List<ParamInfo> paramInfos = new ArrayList<>();
        for (final var field : fields) {
            final var collectionType = collectionUtil.getCollectionElementType(field.asType());
            final var arrayType = arrayUtil.getArrayElementType(field.asType());
            final ParamInfo paramInfo;
            if (nonNull(collectionType)) {
                paramInfo = buildContainerInfo(field, collectionType, typeElement.asType());
            } else if (nonNull(arrayType)) {
                paramInfo = buildContainerInfo(field, arrayType, typeElement.asType());
            } else {
                paramInfo = buildSimpleParamInfo(field, null, typeElement.asType());
            }
            paramInfos.add(paramInfo);
        }
        return paramInfos;
    }

    private ParamInfo buildContainerInfo(final VariableElement field,
                                         final TypeMirror collectionType,
                                         final TypeMirror parentType) {
        if (typeUtil.isSimpleType(collectionType)) {
            return buildSimpleParamInfo(field, collectionType, parentType);
        } else {
            return buildClass(collectionType, field.getSimpleName().toString(), parentType);
        }
    }

    private ParamInfo buildSimpleParamInfo(final VariableElement param,
                                           @Nullable
                                           final TypeMirror collectionType,
                                           final TypeMirror parentType) {
        final var paramName = param.getSimpleName().toString();
        return Optional.ofNullable(param.getAnnotation(InputParam.class))
                .map(i -> {
                    final String convertMethod;
                    TypeMirror enumMethodType = null;
                    if (typeUtil.isEnum(param.asType())) {
                        convertMethod = String.format("%s().%s", i.value(), i.enumMethodValue());
                        final var enumType = (TypeElement) types.asElement(param.asType());
                        enumMethodType = ElementFilter.methodsIn(elements.getAllMembers(enumType)).stream()
                                .filter(m -> m.getSimpleName().contentEquals(i.enumMethodValue()))
                                .findFirst()
                                .orElseThrow(() -> {
                                    final var message = String.format(
                                            "An enum %s does not contains method %s",
                                            enumType.getQualifiedName(),
                                            i.enumMethodValue()
                                    );
                                    return new InvalidInputParamException(message);
                                }).getReturnType();
                    } else {
                        convertMethod = i.value().isBlank() ?
                                findMethod(parentType, paramName, param.asType()) :
                                i.value();
                    }
                    return new SimpleParamInfo(
                            paramName,
                            param.asType(),
                            typeUtil.isEnum(param.asType()),
                            collectionType,
                            i.statementField().isBlank() ?
                                    StringUtil.camelToSnakeCase(paramName) :
                                    i.statementField(),
                            convertMethod,
                            i.ignore(),
                            enumMethodType
                    );
                }).orElseGet(() -> new SimpleParamInfo(
                        paramName,
                        param.asType(),
                        typeUtil.isEnum(param.asType()),
                        collectionType,
                        StringUtil.camelToSnakeCase(paramName),
                        findMethod(parentType, paramName, param.asType()),
                        null
                ));
    }

    public String findMethod(final TypeMirror typeMirror,
                             final String propertyName,
                             final TypeMirror expectedReturnType) {

        final var typeElement = (TypeElement) types.asElement(typeMirror);

        final var methods = typeElement.getEnclosedElements()
                .stream()
                .filter(e -> e.getKind() == ElementKind.METHOD)
                .map(ExecutableElement.class::cast)
                .filter(m -> m.getParameters().isEmpty())
                .filter(m -> types.isSameType(m.getReturnType(), expectedReturnType))
                .toList();

        ExecutableElement recordMatch = null;
        ExecutableElement javaBeanMatch = null;
        ExecutableElement endsWithMatch = null;
        for (final var method : methods) {
            final var methodName = method.getSimpleName().toString();
            final var capitalizedProperty = propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
            final var javaBeanGet = "get" + capitalizedProperty;
            final var javaBeanIs = "is" + capitalizedProperty;

            if (methodName.equalsIgnoreCase(propertyName)) {
                recordMatch = method;
                break;
            }

            if (methodName.equals(javaBeanGet) ||
                    methodName.equals(javaBeanIs) ||
                    methodName.equalsIgnoreCase("get" + propertyName)){
                javaBeanMatch = method;
            }

            if (methodName.toLowerCase().endsWith(propertyName.toLowerCase())) {
                endsWithMatch = method;
            }
        }

        final var finalGetter = nonNull(recordMatch) ? recordMatch :
                (nonNull(javaBeanMatch) ? javaBeanMatch : endsWithMatch);


        return Optional.ofNullable(finalGetter)
                .map(m -> m.getSimpleName().toString())
                .orElseThrow(() -> {
                    final var message = String.format(
                            "A class %s has none valid public method to access property %s",
                            typeElement.getQualifiedName(),
                            propertyName
                    );
                    return new InvalidInputParamException(message);
                });
    }

}
