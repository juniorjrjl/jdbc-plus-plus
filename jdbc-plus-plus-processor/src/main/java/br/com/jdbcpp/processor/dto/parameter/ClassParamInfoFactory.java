package br.com.jdbcpp.processor.dto.parameter;

import br.com.jdbcpp.api.InputParam;
import br.com.jdbcpp.processor.util.ArrayUtil;
import br.com.jdbcpp.processor.util.CollectionUtil;
import br.com.jdbcpp.processor.util.StringUtil;
import br.com.jdbcpp.processor.util.TypeUtil;
import com.palantir.javapoet.TypeName;
import org.jspecify.annotations.Nullable;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;

public final class ClassParamInfoFactory {

    public List<ParamInfo> create(final VariableElement param,
                                  final Types types) {
        final var paramName = param.getSimpleName().toString();
        final var paramTypeMirror = param.asType();
        final var paramInfo = buildClass(types, paramTypeMirror, paramName);
        return List.of(paramInfo);
    }

    private static ParamInfo buildClass(final Types types,
                                        final TypeMirror paramTypeMirror,
                                        final String paramName) {
        final var collectionType = CollectionUtil.getCollectionElementType(paramTypeMirror);
        final var arrayType = ArrayUtil.getArrayElementType(paramTypeMirror);
        final TypeName typeContainer;
        final List<ParamInfo> nestedProperties;
        final TypeElement typeElement;
        if (nonNull(collectionType)) {
            typeElement = (TypeElement) types.asElement(collectionType);
            typeContainer = TypeName.get(collectionType);
        } else if (nonNull(arrayType)) {
            typeElement = (TypeElement) types.asElement(arrayType);
            typeContainer = TypeName.get(arrayType);
        } else {
            typeElement = (TypeElement) types.asElement(paramTypeMirror);
            typeContainer = null;
        }
        nestedProperties = extractFieldsFromType(typeElement, types);
        return new ClassParamInfo(
                paramName,
                TypeName.get(paramTypeMirror),
                typeContainer,
                nestedProperties,
                TypeUtil.isRecord(paramTypeMirror, types)
        );
    }

    private static List<ParamInfo> extractFieldsFromType(final TypeElement typeElement,
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
                paramInfo = buildContainerInfo(types, field, collectionType);
            } else if (nonNull(arrayType)) {
                paramInfo = buildContainerInfo(types, field, arrayType);
            } else {
                paramInfo = buildSimpleParamInfo(types, field, null);
            }
            paramInfos.add(paramInfo);
        }
        return paramInfos;
    }

    private static ParamInfo buildContainerInfo(final Types types,
                                                final VariableElement field,
                                                final TypeMirror collectionType) {
        if (TypeUtil.isSimpleType(collectionType, types)) {
            return buildSimpleParamInfo(types, field, TypeName.get(collectionType));
        } else {
            return buildClass(types, collectionType, field.getSimpleName().toString());
        }
    }

    private static ParamInfo buildSimpleParamInfo(final Types types,
                                                    final VariableElement param,
                                                    @Nullable
                                                    final TypeName collectionType) {
        final var paramName = param.getSimpleName().toString();
        return Optional.ofNullable(param.getAnnotation(InputParam.class))
                .map(i -> {
                    final String convertMethod;
                    if (TypeUtil.isEnum(param.asType(), types)) {
                        convertMethod = switch (i.strategy()){
                            case STRING -> String.format("%s.toString()", paramName);
                            case ORDINAL -> String.format("%s.ordinal()", paramName);
                            case CUSTOM_METHOD -> String.format("%s.%s", paramName, i.strategy());
                        };
                    } else {
                        convertMethod = i.value().isBlank() ? paramName : i.value();
                    }
                    return new SimpleParamInfo(
                            paramName,
                            TypeName.get(param.asType()),
                            TypeUtil.isEnum(param.asType(), types),
                            collectionType,
                            i.statementField().isBlank() ?
                                    StringUtil.camelToSnakeCase(paramName) :
                                    i.statementField(),
                            convertMethod
                    );
                }).orElseGet(() -> new SimpleParamInfo(
                        paramName,
                        TypeName.get(param.asType()),
                        TypeUtil.isEnum(param.asType(), types),
                        collectionType,
                        StringUtil.camelToSnakeCase(paramName),
                        paramName
                ));
    }

}
