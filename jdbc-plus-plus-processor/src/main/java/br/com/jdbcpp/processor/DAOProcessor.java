package br.com.jdbcpp.processor;

import br.com.jdbcpp.processor.context.ProcessorContext;
import br.com.jdbcpp.processor.exception.JDBCPlusPlusProcessorException;
import br.com.jdbcpp.processor.exception.ReadDAOFacadeException;
import com.google.auto.service.AutoService;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.Set;

import static javax.lang.model.SourceVersion.RELEASE_21;

@SupportedAnnotationTypes("br.com.jdbcpp.api.DAO")
@AutoService(Processor.class)
@SupportedSourceVersion(RELEASE_21)
public class DAOProcessor extends AbstractProcessor {


    @Override
    public boolean process(final Set<? extends TypeElement> annotations,
                           final RoundEnvironment roundEnv) {
        final var messager = processingEnv.getMessager();
        try {
            final var context = new ProcessorContext(roundEnv, processingEnv);
            final var processorFacade = context.getProcessorFacade();
            processorFacade.process();
        } catch (final ReadDAOFacadeException e){
            return false;
        } catch (final JDBCPlusPlusProcessorException e){
            messager.printWarning(e.getMessage(), e.getElement());
        }catch (IOException ex){
            messager.printError(ex.getMessage());
        }
        return true;
    }

}
