package br.com.jdbcpp.processor.service.method;

import br.com.jdbcpp.api.Command;
import br.com.jdbcpp.api.Query;
import br.com.jdbcpp.processor.dto.method.InsertMethod;
import br.com.jdbcpp.processor.dto.method.MethodInfo;
import br.com.jdbcpp.processor.dto.parameter.ClassParamInfo;
import br.com.jdbcpp.processor.dto.parameter.ParamInfo;
import br.com.jdbcpp.processor.exception.InvalidInputParamException;
import br.com.jdbcpp.processor.exception.InvalidMethodSignatureException;
import br.com.jdbcpp.processor.exception.InvalidSelectResultMappingException;
import br.com.jdbcpp.processor.exception.MoreParamsThanStatementNeedException;
import br.com.jdbcpp.processor.service.parameter.ParamPathExtractor;
import br.com.jdbcpp.processor.service.parameter.ParameterInfoDelegator;
import br.com.jdbcpp.processor.service.validation.MethodValidator;
import br.com.jdbcpp.processor.util.TypeUtil;

import javax.lang.model.element.ExecutableElement;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class MethodInfoDelegator {


    private final ParameterInfoDelegator parameterInfoDelegator;
    private final ParamPathExtractor paramPathExtractor;
    private final WriteMethodInfoFactory writeMethodInfoFactory;
    private final MethodValidator methodValidator;
    private final ReadMethodInfoFactory readMethodInfoFactory;
    private final TypeUtil typeUtil;

    public MethodInfoDelegator(final ParameterInfoDelegator parameterInfoDelegator,
                               final ParamPathExtractor paramPathExtractor,
                               final WriteMethodInfoFactory writeMethodInfoFactory,
                               final MethodValidator methodValidator,
                               final ReadMethodInfoFactory readMethodInfoFactory,
                               final TypeUtil typeUtil) {
        this.parameterInfoDelegator = parameterInfoDelegator;
        this.paramPathExtractor = paramPathExtractor;
        this.writeMethodInfoFactory = writeMethodInfoFactory;
        this.methodValidator = methodValidator;
        this.readMethodInfoFactory = readMethodInfoFactory;
        this.typeUtil = typeUtil;
    }

    public MethodInfo build(final ExecutableElement method) throws InvalidInputParamException,
            InvalidMethodSignatureException, MoreParamsThanStatementNeedException, InvalidSelectResultMappingException {
        final var params = parameterInfoDelegator.create(method);

        final Map<String, List<ParamInfo>> classPropertyMap =
                (params.size() == 1 && params.getFirst() instanceof ClassParamInfo classParamInfo) ?
                        paramPathExtractor.build(classParamInfo) :
                        Collections.emptyMap();

        final var builder = MethodInfo.builder()
                .withName(method.getSimpleName().toString())
                .withParams(params)
                .withClassPropertyMap(classPropertyMap);

        final var command= method.getAnnotation(Command.class);
        final var query = method.getAnnotation(Query.class);

        if (isNull(command) && isNull(query)) {
            final var message = String.format("Fail to get info from method %s", method.getSimpleName());
            throw  new InvalidMethodSignatureException(message, method);
        }

        final MethodInfo methodInfo;

        if (nonNull(command)){
            methodInfo = buildWriteMethod(builder, method, command);
        } else {
            final var packException = typeUtil.getTypeMirrorFromClass(query::packException);
            methodInfo = readMethodInfoFactory.create(builder, method, query, packException);
            methodValidator.validateReadReturn(method);
        }

        methodValidator.validateParams(
                method,
                methodInfo.getParams(),
                methodInfo.getClassPropertyMap(),
                methodInfo.getStatement().params()
        );
        methodValidator.validateExceptionThrow(
                method,
                methodInfo.getPackException()
        );
        return methodInfo;
    }

    private MethodInfo buildWriteMethod(final MethodInfo.MethodInfoBuilder builder,
                                        final ExecutableElement method,
                                        final Command command
    ) throws InvalidMethodSignatureException{
        final var packException = typeUtil.getTypeMirrorFromClass(command::packException);
        final var methodInfo = writeMethodInfoFactory.create(
                builder.withReturnType(method.getReturnType()),
                command,
                packException
        );

        methodValidator.validateWriteReturn(
                method,
                methodInfo.getClassPropertyMap(),
                command.returnRowsAffected(),
                command.commandType().name(),
                methodInfo instanceof InsertMethod
        );
        return methodInfo;
    }

}
