package c0rnell.flexer.query.agent;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public abstract class Transformer implements ClassFileTransformer {

    private final String targetClassName;
    private final String methodName;

    private ClassLoader targetClassLoader;

    public Transformer(String targetClassName, String methodName) {
        this.targetClassName = targetClassName;
        this.methodName = methodName;
    }

    protected abstract void transform(ClassPool classPool, CtClass clazz, CtMethod method) throws NotFoundException, CannotCompileException, IOException;

    public void setTargetClassLoader(ClassLoader classLoader) {
        this.targetClassLoader = classLoader;
    }

    /**
     * Filters the target method of class and calls {@link #transform(ClassPool, CtClass, CtMethod)}
     * */
    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {
        var byteCode = classfileBuffer;
        var finalTargetClassName = this.targetClassName.replaceAll("\\.", "/");
        if (!className.equals(finalTargetClassName)) {
            return byteCode;
        }

        if (loader.equals(targetClassLoader)) {
            try {
                var classPool = ClassPool.getDefault();
                var clazz = classPool.get(targetClassName);
                var method = clazz.getDeclaredMethod(methodName);

                transform(classPool, clazz, method);

                byteCode = clazz.toBytecode();
                clazz.detach();
            } catch (NotFoundException | CannotCompileException | IOException e) {
                e.printStackTrace();
            }
        }
        return byteCode;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getTargetClassName() {
        return targetClassName;
    }
}
