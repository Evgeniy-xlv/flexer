package c0rnell.flexer.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

class AsmClassTransformer implements ClassFileTransformer {

    private final Map<String, Hook> hooksByClassNameMap;

    public AsmClassTransformer(Collection<Hook> hooks) {
        this.hooksByClassNameMap = hooks.stream()
                .collect(Collectors.toMap(Hook::getClassName, hook -> hook));
    }

    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {

        Hook hook = hooksByClassNameMap.get(className);
        if (hook != null && classBeingRedefined == null) {
            try {
                ClassReader reader = new ClassReader(classfileBuffer);
                ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);

                reader.accept(new AsmClassAdapter(Opcodes.ASM5, writer, hook), ClassReader.EXPAND_FRAMES);

                return writer.toByteArray();
            } catch (Exception e) {
                System.out.println("Flexer JavaAgent: failed to instrument " + className);
                e.printStackTrace();
            }
        }

        // nothing transformed
        return null;
    }
}
