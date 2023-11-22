package c0rnell.flexer.asm;

import org.jetbrains.capture.org.objectweb.asm.ClassReader;
import org.jetbrains.capture.org.objectweb.asm.ClassWriter;
import org.jetbrains.capture.org.objectweb.asm.Opcodes;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

class AsmClassTransformer implements ClassFileTransformer {

    private final Map<String, Hook> instrumentationByClassNameMap;

    public AsmClassTransformer(Collection<Hook> hooks) {
        this.instrumentationByClassNameMap = hooks.stream()
                .collect(Collectors.toMap(Hook::getClassName, hook -> hook));
    }

    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) {

        var instrumentation = instrumentationByClassNameMap.get(className);
        if (instrumentation != null && classBeingRedefined == null) {
            try {
                var reader = new ClassReader(classfileBuffer);
                var writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES);

                reader.accept(new AsmClassAdapter(Opcodes.API_VERSION, writer, instrumentation), ClassReader.EXPAND_FRAMES);

                return writer.toByteArray();
            }
            catch (Exception e) {
                System.out.println("Capture agent: failed to instrument " + className);
                e.printStackTrace();
            }
        }

        // nothing transformed
        return null;
    }
}
