package util;

import com.google.testing.compile.JavaFileObjects;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;
import static java.util.Objects.nonNull;

public class MicroProcessor<T> {

    private final JavaFileObject resource;
    private final String fqcn;
    private final InstantiateTestClass<T> instance;

    public MicroProcessor(final String resourcePath,
                          final String packageName,
                          final InstantiateTestClass<T> instance) {
        this.fqcn = buildFqcn(packageName, resourcePath);
        this.instance = instance;
        this.resource = loadFromResources(this.fqcn, resourcePath);
    }

    public void compile(final CompileAssertion<T> compileAssertion) {
        final var compilation = javac().withProcessors(new AbstractProcessor() {
            @Override
            public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
                final var fixtureElement = processingEnv.getElementUtils().getTypeElement(fqcn);
                if (nonNull(fixtureElement)) {
                    final var testInstance = instance.testInstance(processingEnv);
                    compileAssertion.execAssertions(testInstance, fixtureElement);
                }
                return true;
            }

            @Override
            public Set<String> getSupportedAnnotationTypes() {
                return Set.of("*");
            }

            @Override
            public SourceVersion getSupportedSourceVersion() {
                return SourceVersion.latestSupported();
            }

        }).compile(resource);
        assertThat(compilation).succeeded();
    }

    private static String buildFqcn(final String packageName, final String resourcePath) {
        final var path = Path.of(resourcePath);
        final var fileNameWithExtension = path.getFileName().toString();
        final var className = fileNameWithExtension.substring(0, fileNameWithExtension.lastIndexOf('.'));

        return packageName + "." + className;
    }

    private static JavaFileObject loadFromResources(final String fqcn, final String resourcePath) {
        try {
            final var url = MicroProcessor.class.getClassLoader().getResource(resourcePath);
            Objects.requireNonNull(url, "Fixture não encontrada no caminho: " + resourcePath);

            final var content = Files.readString(Path.of(url.toURI()));

            return JavaFileObjects.forSourceLines(fqcn, content);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao carregar fixture de teste: " + resourcePath, e);
        }
    }

}
