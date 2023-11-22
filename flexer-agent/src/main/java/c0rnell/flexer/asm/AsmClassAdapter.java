package c0rnell.flexer.asm;

import org.jetbrains.capture.org.objectweb.asm.ClassVisitor;
import org.jetbrains.capture.org.objectweb.asm.MethodVisitor;

class AsmClassAdapter extends ClassVisitor {

    private final Hook hook;

    protected AsmClassAdapter(int api, ClassVisitor classVisitor, Hook hook) {
        super(api, classVisitor);
        this.hook = hook;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (hook.getMethodName().equals(name) && hook.getMethodDescriptor().equals(descriptor)) {
            var mv = super.visitMethod(access, name, descriptor, signature, exceptions);
            return new AsmMethodAdapter(this.api, mv, access, name, descriptor, hook);
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }
}
