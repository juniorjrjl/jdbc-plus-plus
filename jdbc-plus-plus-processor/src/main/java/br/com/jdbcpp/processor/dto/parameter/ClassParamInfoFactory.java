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

public final class ClassParamInfoFactory {

    public List<ParamInfo> create(final VariableElement param,
                                  final Types types,
                                  final Elements elements) {
        final var paramName = param.getSimpleName().toString();
        final var paramTypeMirror = param.asType();
        final var paramInfo = buildClass(types, elements, paramTypeMirror, paramName, null);
        return List.of(paramInfo);
    }

    private static ParamInfo buildClass(final Types types,
                                        final Elements elements,
                                        final TypeMirror paramTypeMirror,
                                        final String paramName,
                                        @Nullable
                                        final TypeMirror parentTypeMirror) {
        final var collectionType = CollectionUtil.getCollectionElementType(paramTypeMirror);
        final var arrayType = ArrayUtil.getArrayElementType(paramTypeMirror);
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
        nestedProperties = extractFieldsFromType(typeElement, elements, types);
        return new ClassParamInfo(
                paramName,
                paramTypeMirror,
                typeContainer,
                nestedProperties,
                TypeUtil.isRecord(paramTypeMirror, types),
                isNull(parentTypeMirror) ?
                        paramName :
                        findMethod(parentTypeMirror, types, paramName, paramTypeMirror)
        );
    }

    private static List<ParamInfo> extractFieldsFromType(final TypeElement typeElement,
                                                         final Elements elements,
                                                         final Types types) {
        final var fields = typeElement.getEnclosedElements()
                .stream()
                .filter(element -> element.getKind() == ElementKind.FIELD)
                .map(VariableElement.class::cast)
                .filter(field -> field.getModifiers().contains(Modifier.PRIVATE))
                .filter(field -> !field.getModifiers().contains(Modifier.STATIC))
                .toList();

        final List<ParamInfo> paramInfos = new ArrayList<>();
        for (final var field : fields) {
            final var collectionType = CollectionUtil.getCollectionElementType(field.asType());
            final var arrayType = ArrayUtil.getArrayElementType(field.asType());
            final ParamInfo paramInfo;
            if (nonNull(collectionType)) {
                paramInfo = buildContainerInfo(types, elements, field, collectionType, typeElement.asType());
            } else if (nonNull(arrayType)) {
                paramInfo = buildContainerInfo(types, elements, field, arrayType, typeElement.asType());
            } else {
                paramInfo = buildSimpleParamInfo(types, elements, field, null, typeElement.asType());
            }
            paramInfos.add(paramInfo);
        }
        return paramInfos;
    }

    private static ParamInfo buildContainerInfo(final Types types,
                                                final Elements elements,
                                                final VariableElement field,
                                                final TypeMirror collectionType,
                                                final TypeMirror parentType) {
        if (TypeUtil.isSimpleType(collectionType, types)) {
            return buildSimpleParamInfo(types, elements, field, collectionType, parentType);
        } else {
            return buildClass(types, elements, collectionType, field.getSimpleName().toString(), parentType);
        }
    }

    private static ParamInfo buildSimpleParamInfo(final Types types,
                                                  final Elements elements,
                                                  final VariableElement param,
                                                  @Nullable
                                                  final TypeMirror collectionType,
                                                  final TypeMirror parentType) {
        final var paramName = param.getSimpleName().toString();
        return Optional.ofNullable(param.getAnnotation(InputParam.class))
                .map(i -> {
                    final String convertMethod;
                    TypeMirror enumMethodType = null;
                    if (TypeUtil.isEnum(param.asType(), types)) {
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
                                findMethod(parentType, types, paramName, param.asType()) :
                                i.value();
                    }
                    return new SimpleParamInfo(
                            paramName,
                            param.asType(),
                            TypeUtil.isEnum(param.asType(), types),
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
                        TypeUtil.isEnum(param.asType(), types),
                        collectionType,
                        StringUtil.camelToSnakeCase(paramName),
                        findMethod(parentType, types, paramName, param.asType()),
                        null
                ));
    }

    public static String findMethod(final TypeMirror typeMirror,
                                    final Types types,
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
