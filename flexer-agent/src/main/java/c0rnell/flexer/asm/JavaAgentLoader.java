package c0rnell.flexer.asm;

import com.sun.tools.attach.VirtualMachine;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.nio.file.Path;

public final class JavaAgentLoader {

    private JavaAgentLoader() {
    }

    public static void loadAgent(String agentAbsolutePath) throws Exception {
        var nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
        var pid = nameOfRunningVM.substring(0, nameOfRunningVM.indexOf('@'));
        var vm = VirtualMachine.attach(pid);
        vm.loadAgent(agentAbsolutePath, "");
        vm.detach();
    }

    public static String getClassJarAbsolutePath(Class<?> aClass) throws URISyntaxException {
        var prefix = "jar:file:/";
        var end = ".jar";
        var path = aClass.getProtectionDomain().getCodeSource().getLocation().toURI().toString();
        return path.substring(path.indexOf(prefix) + prefix.length(), path.lastIndexOf(end) + end.length());
    }
}
