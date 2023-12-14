package c0rnell.flexer.asm;

import org.objectweb.asm.MethodVisitor;

import java.util.function.Consumer;

class Hook {

    private final String className;
    private final String methodName;
    private final String methodDescriptor;
    private final Consumer<MethodVisitor> onMethodEnterConsumer;

    Hook(String className, String methodName, String methodDescriptor, Consumer<MethodVisitor> onMethodEnterConsumer) {
        this.className = className;
        this.methodName = methodName;
        this.methodDescriptor = methodDescriptor;
        this.onMethodEnterConsumer = onMethodEnterConsumer;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getMethodDescriptor() {
        return methodDescriptor;
    }

    public Consumer<MethodVisitor> getOnMethodEnterConsumer() {
        return onMethodEnterConsumer;
    }
}
