package util.extension;

import com.google.testing.compile.JavaFileObjects;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static java.util.Objects.nonNull;

public class MicroProcessorExtension implements InvocationInterceptor {

    @Override
    public void interceptTestMethod(final Invocation<@Nullable Void> invocation,
                                    final ReflectiveInvocationContext<Method> invocationContext,
                                    final ExtensionContext extensionContext) throws Throwable {
        runInCompilerContext(invocation::proceed, extensionContext);
    }

    @Override
    public void interceptTestTemplateMethod(final Invocation<@Nullable Void> invocation,
                                            final ReflectiveInvocationContext<Method> invocationContext,
                                            final ExtensionContext extensionContext) throws Throwable {
        runInCompilerContext(invocation::proceed, extensionContext);
    }

    private void runInCompilerContext(final TestExecutionRunnable testRunnable,
                                      final ExtensionContext context) throws Throwable {
        final var testClass = context.getRequiredTestClass();
        final var testInstance = context.getRequiredTestInstance();

        final var annotation = testClass.getAnnotation(Fixture.class);
        Objects.requireNonNull(annotation, "A target test class must be annotated with @MicroProcessorExtension.Fixture");

        final var fqcn = buildFqcn(annotation.packageName(), annotation.resourcePath());
        final var resource = loadFromResources(annotation.resourcePath(), fqcn);

        final var thrownInTest = new Throwable[1];

        var compilation = javac().withProcessors(new AbstractProcessor() {
            @Override
            public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                if (roundEnv.processingOver()) return false;

                final var fixtureElement = Optional.ofNullable(processingEnv.getElementUtils().getTypeElement(fqcn))
                        .orElseThrow();
                injectFields(testInstance, processingEnv, fixtureElement);

                try {
                    testRunnable.run();
                } catch (Throwable t) {
                    thrownInTest[0] = t;
                }
                return true;
            }

            @Override
            public Set<String> getSupportedAnnotationTypes() { return Set.of("*"); }

            @Override
            public SourceVersion getSupportedSourceVersion() { return SourceVersion.latestSupported(); }
        }).compile(resource);

        if (nonNull(thrownInTest[0])) {
            throw thrownInTest[0];
        }

        assertThat(compilation).succeeded();
    }

    private void injectFields(final Object testInstance,
                              final ProcessingEnvironment processingEnv,
                              final TypeElement fixtureElement) {
        Class<?> clazz = testInstance.getClass();
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                try {
                    if (field.isAnnotationPresent(ProcessingEnv.class)) {
                        field.setAccessible(true);
                        field.set(testInstance, processingEnv);
                    } else if (field.isAnnotationPresent(FixtureElement.class)) {
                        field.setAccessible(true);
                        field.set(testInstance, fixtureElement);
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Error injecting field in test: " + field.getName(), e);
                }
            }
            clazz = clazz.getSuperclass();
        }
    }

    @FunctionalInterface
    private interface TestExecutionRunnable {
        void run() throws Throwable;
    }

    private static String buildFqcn(final String packageName, final String resourcePath) {
        final var fileName = Path.of(resourcePath).getFileName().toString();
        final var className = fileName.substring(0, fileName.lastIndexOf('.'));
        return packageName + "." + className;
    }

    private static JavaFileObject loadFromResources(final String resourcePath, final String fqcn) {
        try {
            final var url = MicroProcessorExtension.class.getClassLoader().getResource(resourcePath);
            Objects.requireNonNull(url, "Fixture not found: " + resourcePath);
            final var content = Files.readString(Path.of(url.toURI()));
            return JavaFileObjects.forSourceLines(fqcn, content);
        } catch (Exception e) {
            throw new RuntimeException("Error to load fixture", e);
        }
    }
}