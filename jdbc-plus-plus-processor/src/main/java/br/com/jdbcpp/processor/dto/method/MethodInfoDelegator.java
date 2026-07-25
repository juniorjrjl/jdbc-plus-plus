package br.com.jdbcpp.processor.dto.method;

import br.com.jdbcpp.api.Command;
import br.com.jdbcpp.api.Query;
import br.com.jdbcpp.processor.dto.parameter.ClassParamInfo;
import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.dto.parameter.ParamPathExtractor;
import br.com.jdbcpp.processor.dto.parameter.ParameterInfoDelegator;
import br.com.jdbcpp.processor.exception.InvalidInputParamException;
import br.com.jdbcpp.processor.exception.InvalidMethodSignatureException;
import br.com.jdbcpp.processor.exception.MoreParamsThanStatementNeedException;
import br.com.jdbcpp.processor.util.LambdaUtil;
import br.com.jdbcpp.processor.util.MethodValidator;
import br.com.jdbcpp.processor.util.TypeUtil;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MethodInfoDelegator {


    private final ParameterInfoDelegator parameterInfoDelegator;
    private final ParamPathExtractor paramPathExtractor;
    private final WriteMethodInfoFactory writeMethodInfoFactory;
    private final MethodValidator methodValidator;
    private final ReadMethodInfoFactory readMethodInfoFactory;
    private final TypeUtil typeUtil;
    private final Types types;

    public MethodInfoDelegator(final ParameterInfoDelegator parameterInfoDelegator,
                               final ParamPathExtractor paramPathExtractor,
                               final WriteMethodInfoFactory writeMethodInfoFactory,
                               final MethodValidator methodValidator,
                               final ReadMethodInfoFactory readMethodInfoFactory,
                               final TypeUtil typeUtil,
                               final Types types) {
        this.parameterInfoDelegator = parameterInfoDelegator;
        this.paramPathExtractor = paramPathExtractor;
        this.writeMethodInfoFactory = writeMethodInfoFactory;
        this.methodValidator = methodValidator;
        this.readMethodInfoFactory = readMethodInfoFactory;
        this.typeUtil = typeUtil;
        this.types = types;
    }

    public MethodInfo build(final ExecutableElement method) throws InvalidInputParamException,
            InvalidMethodSignatureException, MoreParamsThanStatementNeedException {
        final var params = parameterInfoDelegator.create(method);

        final Map<String, List<ParamInfo>> classPropertyMap =
                (params.size() == 1 && params.getFirst() instanceof ClassParamInfo classParamInfo) ?
                        paramPathExtractor.build(classParamInfo) :
                        Collections.emptyMap();

        final var commandOptional = Optional.ofNullable(method.getAnnotation(Command.class))
                .map(LambdaUtil.unchecked(command -> {
                    final var packException = typeUtil.getTypeMirrorFromClass(command::packException);
                    final var methodInfo = writeMethodInfoFactory.create(
                            method,
                            params,
                            classPropertyMap,
                            command,
                            packException
                    );
                    switch (command.commandType()){
                        case INSERT, UPDATE -> {
                            final List<TypeMirror> validReturns = classPropertyMap.isEmpty() ?
                                    List.of(types.getNoType(TypeKind.VOID)) :
                                    List.of(types.getNoType(TypeKind.VOID), params.getFirst().getType());
                            methodValidator.validateReturn(
                                    method,
                                    command.returnRowsAffected(),
                                    methodInfo.getReturnType(),
                                    command.commandType().name(),
                                    validReturns);
                        }
                        case DELETE ->
                                methodValidator.validateReturn(
                                        method,
                                        command.returnRowsAffected(),
                                        methodInfo.getReturnType(),
                                        command.commandType().name(),
                                        List.of(types.getNoType(TypeKind.VOID))
                                );
                    }
                    return  methodInfo;
                }));

        return Optional.ofNullable(method.getAnnotation(Query.class))
                .map(LambdaUtil.unchecked(query -> readMethodInfoFactory.create(
                        method,
                        params,
                        classPropertyMap,
                        query,
                        typeUtil.getTypeMirrorFromClass(query::packException)
                )))
                .or(() -> commandOptional)
                .orElseThrow(LambdaUtil.unchecked(() -> {
                    final var message = String.format("Fail to get info from method %s", method.getSimpleName());
                    return new InvalidMethodSignatureException(message, method);
                }));
    }

}
