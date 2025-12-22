package operatedarocket.util.Mail;

import java.time.LocalDate;
import java.util.HashMap;

public class Mail {
    public static record Hyprlink(String path, String displayText){}

    public String sender, title, body;
    public LocalDate date;
    public boolean send;
    public Hyprlink[] hyprlinks;

    public Mail(String sender, String title, LocalDate date, String body, Hyprlink[] hyprlinks) {
        this.title = title;
        this.sender = sender;
        this.body = body;
        this.date = date;
        this.hyprlinks = hyprlinks;

    }

    public String getText() {
        return 
        "From " + sender +
        "\nTitle - " + title +
        "\nDate - " +
                date.getDayOfMonth() + "-" +
                date.getMonth().toString() + "-" +
                date.getYear() + "\n" + body;
    }
}
