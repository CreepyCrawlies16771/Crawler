package org.firstinspires.ftc.teamcode.annotations.processor;

import org.firstinspires.ftc.teamcode.annotations.Experimental;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedAnnotationTypes("org.firstinspires.ftc.teamcode.annotations.Experimental")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ExperimentalProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (TypeElement ann : annotations) {
            for (Element e : roundEnv.getElementsAnnotatedWith(ann)) {
                Experimental exp = e.getAnnotation(Experimental.class);
                StringBuilder msg = new StringBuilder("Experimental API");
                if (exp != null) {
                    if (!exp.value().isEmpty()) {
                        msg.append(": ").append(exp.value());
                    }
                    if (!exp.since().isEmpty()) {
                        msg.append(" (since ").append(exp.since()).append(")");
                    }
                    if (!exp.mayBeRemoved()) {
                        msg.append(" [may not be removed]");
                    }
                }
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, msg.toString(), e);
            }
        }
        return true;
    }
}
