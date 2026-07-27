package util;

import javax.lang.model.element.TypeElement;

public interface CompileAssertion<T> {

    void execAssertions(final T instance, final TypeElement fixture);

}
