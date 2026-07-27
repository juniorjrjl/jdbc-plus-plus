package util;

import javax.annotation.processing.ProcessingEnvironment;

@FunctionalInterface
public interface InstantiateTestClass<T> {

    T testInstance(ProcessingEnvironment processingEnv);

}
