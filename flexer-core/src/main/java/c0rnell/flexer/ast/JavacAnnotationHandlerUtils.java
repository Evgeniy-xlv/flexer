package c0rnell.flexer.ast;

import c0rnell.flexer.model.GenerateModel;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Name;
import lombok.core.AST;
import lombok.javac.JavacNode;
import lombok.javac.JavacTreeMaker;
import lombok.javac.handlers.JavacHandlerUtil;

import java.lang.reflect.Modifier;

public class JavacAnnotationHandlerUtils {

    public static JCTree.JCClassDecl addInnerClass(JavacNode singletonClass, JavacTreeMaker singletonTM,
                                                   String innerClassName, long mods) {
        JCTree.JCModifiers modifiers = singletonTM.Modifiers(mods);
        return singletonTM.ClassDef(
                modifiers,
                singletonClass.toName(innerClassName),
                List.nil(),
                null,
                List.nil(),
                List.nil()
        );
    }

    public static JCTree.JCClassDecl genModelInnerClass(JavacNode typeNode, String className) {
        return addInnerClass(typeNode, typeNode.getTreeMaker(), className, Modifier.PUBLIC | Modifier.STATIC);
    }

    public static JCTree.JCMethodDecl genToModelMethod(JavacNode typeNode,
                                                       JavacNode innerClassTypeNode,
                                                       String methodName) {
        JCTree innerClassTypeDeclRaw = innerClassTypeNode.get();
        JCTree.JCClassDecl innerClassTypeDecl = (JCTree.JCClassDecl) innerClassTypeDeclRaw;

        JavacTreeMaker maker = typeNode.getTreeMaker();
        JCTree.JCModifiers mods = maker.Modifiers(Modifier.PUBLIC);

        // args gen
        ListBuffer<JCTree.JCExpression> args = new ListBuffer<>();
        JCTree.JCExpression thiz = maker.Ident(typeNode.toName("this"));
        for (JavacNode fieldNode : findAllFields(innerClassTypeNode, false)) {
            JCTree.JCVariableDecl field = (JCTree.JCVariableDecl) fieldNode.get();
            JCTree.JCExpression val = maker.Select(thiz, field.name);
            args.add(val);
        }

        // return block gen
        JCTree.JCNewClass newClass = maker.NewClass(
                null,
                List.nil(),
                maker.Ident(innerClassTypeDecl.name),
                args.toList(),
                null
        );

        JCTree.JCReturn returnValue = maker.Return(newClass);

        ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();
        statements.append(returnValue);

        JCTree.JCBlock block = maker.Block(0L, statements.toList());

        // method gen
        return maker.MethodDef(
                mods,
                typeNode.toName(methodName),
                maker.Ident(innerClassTypeDecl.name),
                List.nil(),
                List.nil(),
                List.nil(),
                block,
                null
        );
    }

    public static List<JCTree.JCMethodDecl> genGettersByClassFields(JavacNode typeNode) {
        JavacTreeMaker maker = typeNode.getTreeMaker();
        JCTree.JCExpression thiz = maker.Ident(typeNode.toName("this"));
        JCTree.JCModifiers mods = maker.Modifiers(Modifier.PUBLIC);

        ListBuffer<JCTree.JCMethodDecl> methods = new ListBuffer<>();
        for (JavacNode fieldNode : findAllFields(typeNode, false)) {
            JCTree.JCVariableDecl field = (JCTree.JCVariableDecl) fieldNode.get();

            JCTree.JCExpression methodType = copyType(maker, field);
            ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();
            statements.add(maker.Return(maker.Select(thiz, field.name)));
            JCTree.JCBlock methodBody = maker.Block(0, statements.toList());
            methods.add(
                    maker.MethodDef(
                            mods,
                            typeNode.toName(String.format("get%s", capitalize(field.name.toString()))),
                            methodType,
                            List.nil(),
                            List.nil(),
                            List.nil(),
                            methodBody,
                            null
                    )
            );
        }
        return methods.toList();
    }

    private static String capitalize(String string) {
        if (string == null || string.isEmpty()) {
            return string;
        }
        char[] chars = string.toCharArray();
        chars[0] = Character.toUpperCase(chars[0]);
        return new String(chars);
    }

    public static JCTree.JCExpression copyType(JavacTreeMaker treeMaker, JCTree.JCVariableDecl fieldNode) {
        return fieldNode.type != null ? treeMaker.Type(fieldNode.type) : fieldNode.vartype;
    }

//    public static JCTree.JCMethodDecl genToString(JavacNode typeNode) {
//        JavacTreeMaker maker = typeNode.getTreeMaker();
//        ListBuffer<JCTree.JCStatement> statements = new ListBuffer<>();
//        for (JavacNode fieldNode : findAllFieldsIgnore(typeNode, true)) {
//            statements.add(maker.Select(thiz, field.name));
//        }
//        JCTree.JCBlock block = maker.Block(Modifier.PUBLIC, statements.toList());
//        return maker.MethodDef(
//                maker.Modifiers(Modifier.PUBLIC),
//                typeNode.toName(String.format("get%s", capitalize(field.name.toString()))),
//                methodType,
//                List.nil(),
//                List.nil(),
//                List.nil(),
//                methodBody,
//                null
//        );
//    }

    // temporary unused
//    public static JCTree.JCMethodDecl genConstructorWithAllFieldsAsArgs(JavacTreeMaker maker, JavacNode toNode, JavacNode fieldsFromNode) {
//        List<JavacNode> fields = findAllFields(fieldsFromNode, false);
//        ListBuffer<JCTree.JCVariableDecl> params = new ListBuffer<>();
//
//        for (JavacNode fieldNode : fields) {
//            JCTree.JCVariableDecl field = (JCTree.JCVariableDecl) fieldNode.get();
//            Name name = field.name;
//        }
//
//        JCTree.JCBlock block = maker.Block(0L, List.nil());
//        JCTree.JCMethodDecl constructor = maker.MethodDef(
//                maker.Modifiers(Modifier.PUBLIC),
//                toNode.toName("<init>"),
//                null,
//                List.nil(),
//                params.toList(),
//                List.nil(),
//                block,
//                null
//        );
//
//        return constructor;
//    }

    public static List<JavacNode> findAllFields(JavacNode typeNode, boolean includeIgnore) {
        ListBuffer<JavacNode> fields = new ListBuffer<>();
        for (JavacNode child : typeNode.down()) {
            if (child.getKind() != AST.Kind.FIELD) {
                continue;
            }
            JCTree.JCVariableDecl fieldDecl = (JCTree.JCVariableDecl) child.get();
            //Skip fields that start with $
            if (fieldDecl.name.toString().startsWith("$")) {
                continue;
            }
            long fieldFlags = fieldDecl.mods.flags;
            //Skip static fields.
            if ((fieldFlags & Flags.STATIC) != 0) {
                continue;
            }
            if (!includeIgnore && JavacHandlerUtil.hasAnnotation(GenerateModel.Ignore.class, child)) {
                continue;
            }
            //Skip initialized final fields
            boolean isFinal = (fieldFlags & Flags.FINAL) != 0;
            if (!isFinal || fieldDecl.init == null) {
                fields.append(child);
            }
        }
        return fields.toList();
    }

    public static void copyFields(JavacNode fromTypeNode, JavacNode toTypeNode) {
        JavacTreeMaker treeMaker = toTypeNode.getTreeMaker();
        JCTree.JCModifiers fieldMod = treeMaker.Modifiers(Modifier.PRIVATE);

        for (JavacNode fieldNode : findAllFields(fromTypeNode, false)) {
            JCTree.JCVariableDecl sourceField = (JCTree.JCVariableDecl) fieldNode.get();

            Name name = sourceField.name;
            JCTree.JCExpression returnType = sourceField.vartype;

            JCTree.JCVariableDecl fieldDecl = treeMaker.VarDef(
                    fieldMod,
                    name,
                    returnType,
                    null
            );
            JavacHandlerUtil.injectField(toTypeNode, fieldDecl);
        }
    }

//    private void addConstructor(JavacNode singletonClass, JavacTreeMaker singletonTM) {
//        JCTree.JCModifiers modifiers = singletonTM.Modifiers(Modifier.PUBLIC);
//        JCTree.JCBlock block = singletonTM.Block(0L, List.nil());
//        JCTree.JCMethodDecl constructor = singletonTM.MethodDef(
//                modifiers,
//                singletonClass.toName("<init>"),
//                null,
//                List.nil(),
//                List.nil(),
//                List.nil(),
//                block,
//                null
//        );
//
//        JavacHandlerUtil.injectMethod(singletonClass, constructor);
//    }
}
