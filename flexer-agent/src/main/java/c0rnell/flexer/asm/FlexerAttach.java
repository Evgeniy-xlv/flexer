package c0rnell.flexer.asm;

import com.sun.tools.attach.VirtualMachine;
import net.bytebuddy.agent.ByteBuddyAgent;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.net.URLDecoder;

public final class FlexerAttach {

    private FlexerAttach() {
    }

    /**
     * Attaches flexer javaagent to the current VM using byte-buddy-agent or the simple default way using
     * {@link com.sun.tools.attach.spi.AttachProvider}.
     * Using of byte-buddy-agent may produce some error stack trace complaining about
     * 'java.util.ServiceConfigurationError: com.sun.tools.attach.spi.AttachProvider'.
     * Unfortunately, this cannot be avoided, but there is no reason to worry as it continues to work
     */
    public static void attach() throws Exception {
        try {
            Class.forName("net.bytebuddy.agent.ByteBuddyAgent");
            ByteBuddyAgent.attach(
                    getAgentFileAbsolutePath(),
                    ByteBuddyAgent.ProcessProvider.ForCurrentVm.INSTANCE
            );
        } catch (Exception ignored) {
            loadAgent(getAgentFileAbsolutePath().getAbsolutePath());
        }
    }

    private static void loadAgent(String agentAbsolutePath) throws Exception {
        String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
        String pid = nameOfRunningVM.substring(0, nameOfRunningVM.indexOf('@'));
        VirtualMachine vm = VirtualMachine.attach(pid);
        vm.loadAgent(agentAbsolutePath, "");
        vm.detach();
    }

    private static File getAgentFileAbsolutePath() {
        Class<?> aclass = FlexerInstrumentationAgent.class;
        URL sclClassUrl = aclass.getResource(aclass.getSimpleName() + ".class");
        String part = sclClassUrl == null ? null : sclClassUrl.toString();
        String cp = aclass.getName().replaceAll("\\.", "/").concat(".class");
        if (part == null || !part.endsWith(cp)) {
            ClassLoader cl = aclass.getClassLoader();
            throw new RuntimeException("Class can't find itself as javaagent. ClassLoader: "
                                       + (cl == null ? "*NULL*" : cl.getClass().toString()));
        }
        int selfBaseLength = part.length() - cp.length();
        String decoded = urlDecode(part.substring(0, selfBaseLength));
        File file;
        if (decoded.startsWith("jar:file:") && decoded.endsWith("!/")) {
            file = new File(decoded.substring(9, decoded.length() - 2));
        } else if (decoded.startsWith("file:")) {
            file = new File(decoded.substring(5));
        } else {
            file = new File(decoded);
        }
        return file;
    }

    private static String urlDecode(String in) {
        String plusFixed = in.replaceAll("\\+", "%2B");
        try {
            return URLDecoder.decode(plusFixed, "UTF-8");
        } catch (UnsupportedEncodingException var2) {
            throw new InternalError("UTF-8 not supported");
        }
    }
}
