package br.com.jdbcpp.processor.dto.parameter;

import br.com.jdbcpp.processor.util.ArrayUtil;
import br.com.jdbcpp.processor.util.CollectionUtil;
import br.com.jdbcpp.processor.util.TypeUtil;
import com.palantir.javapoet.TypeName;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.nonNull;

public final class ClassParamInfoFactory extends AbstractParamInfoFactory {

    private ClassParamInfoFactory() {}

    public static List<ParamInfo> create(final VariableElement param,
                                         final Types types) {
        final var paramName = param.getSimpleName().toString();
        final var paramTypeMirror = param.asType();
        return buildClass(types, paramTypeMirror, paramName);
    }

    private static List<ParamInfo> buildClass(final Types types,
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
        return List.of(
                new ClassParamInfo(
                        paramName,
                        TypeName.get(paramTypeMirror),
                        typeContainer,
                        nestedProperties
                )
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
            final var classInfo = buildClass(types, collectionType, field.getSimpleName().toString()).getFirst();
            return new ClassParamInfo(
                    classInfo.getName(),
                    classInfo.getType(),
                    TypeName.get(collectionType),
                    ((ClassParamInfo)classInfo).getNestedProperties()
            );
        }
    }

}
