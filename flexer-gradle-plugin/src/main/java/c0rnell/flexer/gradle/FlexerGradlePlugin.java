package c0rnell.flexer.gradle;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.build.gradle.ByteBuddyPlugin;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.matcher.ElementMatchers;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class FlexerGradlePlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        FlexerGradlePluginExtension extension = project.getExtensions()
                .create("flexer", FlexerGradlePluginExtension.class);
//        project.task("hello")
//                .doLast(task -> {
//                    System.out.println(
//                            "Hello, " + extension.getGreeter());
//                    System.out.println(
//                            "I have a message for You: " + extension.getMessage());
//                });


//        ByteBuddyPlugin byteBuddyPlugin = new ByteBuddyPlugin();
//        byteBuddyPlugin.apply(project);
//        try {
//            new ByteBuddy()
//                    .redefine(Class.forName("org.springframework.data.jpa.repository.query.NativeJpaQuery", false, project.getClass().getClassLoader()))
//                    .method(ElementMatchers.named("createJpaQuery"))
//                    .intercept(MethodCall.call(() -> {
//                        System.out.println("jopa");
//                        return null;
//                    }))
//            ;
//        } catch (ClassNotFoundException e) {
//            throw new RuntimeException(e);
//        }

        System.out.println("zdarova");
    }
}