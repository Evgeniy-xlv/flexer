package c0rnell.flexer.asm;

import java.lang.instrument.Instrumentation;


/**
 * An entry point class of instrumentation mechanism.
 * Its premain and agentmain methods are used to transform target classes before their loading into class loader.
 * */
public class FlexerInstrumentationAgent {

    public static void premain(String agentArgs, Instrumentation inst) {
        process(inst);
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        process(inst);
    }

    private static void process(Instrumentation inst) {
        inst.addTransformer(new AsmClassTransformer(Hooks.HOOKS), false);
    }
}
