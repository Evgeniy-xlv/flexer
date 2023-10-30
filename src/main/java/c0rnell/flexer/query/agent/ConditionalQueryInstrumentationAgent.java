package c0rnell.flexer.query.agent;

import java.lang.instrument.Instrumentation;

/**
 * An entry point class of instrumentation mechanism.
 * Its premain and agentmain methods are used to transform target classes before their loading into class loader.
 *
 * @see ConditionalQueryTransformer
 * */
public class ConditionalQueryInstrumentationAgent {

    public static void premain(String agentArgs, Instrumentation inst) {
        transformClass(new ConditionalQueryTransformer(), inst);
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        transformClass(new ConditionalQueryTransformer(), inst);
    }

    private static void transformClass(Transformer transformer, Instrumentation instrumentation) {
        transformClass(transformer.getTargetClassName(), instrumentation, transformer);
    }

    private static void transformClass(String className, Instrumentation instrumentation, Transformer transformer) {
        Class<?> targetCls;
        ClassLoader targetClassLoader;
        try {
            targetCls = Class.forName(className);
            targetClassLoader = targetCls.getClassLoader();
            transformer.setTargetClassLoader(targetClassLoader);
            transform(targetCls, instrumentation, transformer);
            return;
        } catch (Exception ex) {
            System.out.println("Class [{}] not found with Class.forName");
        }
        for (Class<?> clazz : instrumentation.getAllLoadedClasses()) {
            if (clazz.getName().equals(className)) {
                targetCls = clazz;
                targetClassLoader = targetCls.getClassLoader();
                transformer.setTargetClassLoader(targetClassLoader);
                transform(targetCls, instrumentation, transformer);
                return;
            }
        }
        throw new RuntimeException("Failed to find class [" + className + "]");
    }

    private static void transform(Class<?> clazz, Instrumentation instrumentation, Transformer transformer) {
        instrumentation.addTransformer(transformer, true);
        try {
            instrumentation.retransformClasses(clazz);
        } catch (Exception ex) {
            throw new RuntimeException("Transform failed for class: [" + clazz.getName() + "]", ex);
        }
    }
}