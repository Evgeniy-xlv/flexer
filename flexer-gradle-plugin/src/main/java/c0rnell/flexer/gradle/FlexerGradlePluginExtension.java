package c0rnell.flexer.gradle;

public class FlexerGradlePluginExtension {

    private String greeter = "Baeldung";
    private String message = "Message from the plugin!";

    public void setGreeter(String greeter) {
        this.greeter = greeter;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getGreeter() {
        return greeter;
    }

    public String getMessage() {
        return message;
    }
}
