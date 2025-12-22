package operatedarocket.util.Apps;

public class AppRejistry {
    public String name;
    public String image;
    public Class<?> mainClass;   // <-- supports AppFrame, JPanel, Runnable, anything
    public Boolean onToolbar;

    public AppRejistry(String name, String image, Class<?> mainClass, Boolean onToolbar) {
        this.name = name;
        this.image = image;
        this.mainClass = mainClass;
        this.onToolbar = onToolbar;
    }
}
