package c0rnell.flexer.ast;

import c0rnell.flexer.model.GenerateModel;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import lombok.AccessLevel;
import lombok.core.AnnotationValues;
import lombok.core.HandlerPriority;
import lombok.javac.JavacAnnotationHandler;
import lombok.javac.JavacNode;
import lombok.javac.handlers.HandleConstructor;
import lombok.javac.handlers.JavacHandlerUtil;
import org.kohsuke.MetaInfServices;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@MetaInfServices(JavacAnnotationHandler.class)
@HandlerPriority(0)
public class GenerateModelHandler extends JavacAnnotationHandler<GenerateModel> {

    private final HandleConstructor handleConstructor = new HandleConstructor();

    @Override
    public void handle(AnnotationValues<GenerateModel> annotationValues,
                       JCTree.JCAnnotation ast,
                       JavacNode annotationNode) {
        // todo define lombok version to resolve reflections
        handleForNewestVersion(annotationValues, annotationNode);
    }

    private void handleForNewestVersion(AnnotationValues<GenerateModel> annotationValues,
                                        JavacNode annotationNode) {
        JavacNode typeNode = annotationNode.up();

        GenerateModel generateModel = annotationValues.getInstance();
        String className = generateModel.className().isEmpty() ? "Model" : generateModel.className();
        String methodName = generateModel.methodName().isEmpty() ? "toModel" : generateModel.methodName();

        JavacHandlerUtil.deleteAnnotationIfNeccessary(annotationNode, GenerateModel.class);
        JavacHandlerUtil.deleteImportFromCompilationUnit(annotationNode, GenerateModel.class.getName());

        try {
            // generation of all args constructor for annotated class
            if (JavacHandlerUtil.constructorExists(typeNode) == JavacHandlerUtil.MemberExistsResult.NOT_EXISTS) {
                genAllArgsConstructor(typeNode, annotationNode);
            }

            // generation of inner class Model
            JCTree.JCClassDecl innerClass = JavacAnnotationHandlerUtils.genModelInnerClass(typeNode, className);
            JavacNode innerClassTypeNode = JavacHandlerUtil.injectType(typeNode, innerClass);
            // copying of all annotated type fields into model inner class
            JavacAnnotationHandlerUtils.copyFields(typeNode, innerClassTypeNode);
            // generation of all args constructor for model inner class
            if (JavacHandlerUtil.constructorExists(innerClassTypeNode) == JavacHandlerUtil.MemberExistsResult.NOT_EXISTS) {
                genAllArgsConstructor(innerClassTypeNode, annotationNode);
            }

            if (JavacHandlerUtil.methodExists(methodName, typeNode, 0) == JavacHandlerUtil.MemberExistsResult.NOT_EXISTS) {
                // generation of 'public Model toModel();' method inside annotated class
                // it'll create a new model inner class instance and pass all field values into its constructor
                JCTree.JCMethodDecl method = JavacAnnotationHandlerUtils.genToModelMethod(typeNode, innerClassTypeNode, methodName);
                JavacHandlerUtil.injectMethod(typeNode, method);
            }

            // generation of getters for model inner class
            List<JCTree.JCMethodDecl> getters = JavacAnnotationHandlerUtils.genGettersByClassFields(innerClassTypeNode);
            for (JCTree.JCMethodDecl getter : getters) {
                JavacHandlerUtil.injectMethod(innerClassTypeNode, getter);
            }
        } catch (Throwable e) {
            annotationNode.addError(e.getMessage());
            System.err.println(e.getMessage());
//            File file = new File("C:\\Users\\evgen\\IdeaProjects\\flexer\\flexer-test-project\\FLEXER-ERROR-REPORT.txt");
//            if (!file.exists()) {
//                try {
//                    file.createNewFile();
//                } catch (IOException ex) {
//                    throw new RuntimeException(ex);
//                }
//            } else {
//                file.delete();
//            }
//            FileOutputStream fileOutputStream;
//            try {
//                fileOutputStream = new FileOutputStream(file);
//                fileOutputStream.write(("GENERATED AT " + LocalDateTime.now() + "\n\n\n").getBytes(StandardCharsets.UTF_8));
//                e.printStackTrace(new PrintStream(fileOutputStream));
//                fileOutputStream.close();
//            } catch (IOException ex) {
//                throw new RuntimeException(ex);
//            }
        }
    }

    private void genAllArgsConstructor(JavacNode typeNode, JavacNode annotationNode) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> handleConstructorClass = handleConstructor.getClass();

        Method generateConstructorMethod = handleConstructorClass.getDeclaredMethod(
                "generateConstructor",
                JavacNode.class, AccessLevel.class, List.class, List.class, boolean.class,
                String.class, HandleConstructor.SkipIfConstructorExists.class, JavacNode.class
        );

        List<JavacNode> fields = JavacAnnotationHandlerUtils.findAllFields(typeNode, false);

        generateConstructorMethod.invoke(
                this.handleConstructor,
                typeNode,
                AccessLevel.PUBLIC,
                null,
                fields,
                false,
                null,
                HandleConstructor.SkipIfConstructorExists.NO,
                annotationNode
        );
    }
}
