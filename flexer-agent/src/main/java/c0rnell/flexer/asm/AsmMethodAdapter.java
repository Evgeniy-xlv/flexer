package c0rnell.flexer.asm;

import org.jetbrains.capture.org.objectweb.asm.MethodVisitor;
import org.jetbrains.capture.org.objectweb.asm.commons.AdviceAdapter;

class AsmMethodAdapter extends AdviceAdapter {

    private final Hook hook;

    protected AsmMethodAdapter(int api,
                               MethodVisitor methodVisitor,
                               int access,
                               String name,
                               String descriptor,
                               Hook hook) {
        super(api, methodVisitor, access, name, descriptor);
        this.hook = hook;
    }

    @Override
    protected void onMethodEnter() {
        hook.getOnMethodEnterConsumer().accept(mv);
    }
}
