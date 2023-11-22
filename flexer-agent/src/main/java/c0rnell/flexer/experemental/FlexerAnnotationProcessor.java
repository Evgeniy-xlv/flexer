package c0rnell.flexer.experemental;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.Set;

@SupportedAnnotationTypes({"*"})
public class FlexerAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        Class<? extends Annotation> annotation;
        try {
            annotation = (Class<? extends Annotation>) Class.forName("c0rnell.flexer.query.ConditionalQuery");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        for (Element e : roundEnv.getElementsAnnotatedWith(annotation)) {
            Annotation ca = e.getAnnotation(annotation);
            String name = e.getSimpleName().toString();
            char[] c = name.toCharArray();
            c[0] = Character.toUpperCase(c[0]);
            name = new String(name);
            TypeElement clazz = (TypeElement) e.getEnclosingElement();
            try {
                JavaFileObject f = processingEnv.getFiler().
                        createSourceFile(clazz.getQualifiedName() + "Autogenerate");
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE,
                        "Creating " + f.toUri());
                Writer w = f.openWriter();
                try {
                    String pack = clazz.getQualifiedName().toString();
                    PrintWriter pw = new PrintWriter(w);
                    TypeMirror type = e.asType();
                    pw.println("package " + pack.substring(0, pack.lastIndexOf('.')) + ";");
                    pw.println("\npublic class " + clazz.getSimpleName() + "Autogenerate {");
                    pw.println("\n    public java.lang.Object result = \"Zdarova\";");
                    pw.println("    public int type = 1233;");
                    pw.println("\n    protected " + clazz.getSimpleName() + "Autogenerate() {}");
                    pw.println("\n    /** Handle something. */");
                    pw.println("    protected final void handle" + name + "(java.lang.Object value) {");
                    pw.println("\n//" + e);
                    pw.println("//" + ca);
                    pw.println("\n        System.out.println(value);");
                    pw.println("    }");
                    pw.println("}");
                    pw.flush();
                } finally {
                    w.close();
                }
            } catch (IOException x) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                        x.toString());
            }
        }
        return true;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}
