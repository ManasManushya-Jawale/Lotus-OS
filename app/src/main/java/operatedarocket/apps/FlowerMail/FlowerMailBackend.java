package operatedarocket.apps.FlowerMail;

import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import operatedarocket.util.Mail.Mail;

public class FlowerMailBackend {
    public static ArrayList<Mail> mails = new ArrayList<>();

    public static void init() throws Exception {
        mails = new ArrayList<>();

        URL url = FlowerMailBackend.class.getResource("/mails");
        if (url == null) {
            throw new IllegalStateException("Resource folder /mails not found");
        }

        if (url.getProtocol().equals("file")) {
            // ✅ Dev mode: mails is a real folder on disk
            File dir = new File(url.toURI());
            for (File file : dir.listFiles()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    processMailFile(file.getName(), reader);
                }
            }
        } else if (url.getProtocol().equals("jar")) {
            // ✅ Packaged JAR: enumerate entries inside the JAR
            String jarPath = FlowerMailBackend.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath();

            try (JarFile jar = new JarFile(jarPath)) {
                jar.stream()
                        .filter(e -> e.getName().startsWith("mails/") && e.getName().endsWith(".txt"))
                        .forEach(entry -> {
                            try (InputStream is = jar.getInputStream(entry);
                                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                                String filename = entry.getName().substring("mails/".length());
                                processMailFile(filename, reader);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        });
            }
        }

        System.out.println("Loaded " + mails.size() + " mails");
    }

    private static void processMailFile(String filename, BufferedReader reader) throws IOException {
        String[] info = filename.substring(0, filename.length() - 4).split("_");
        int yr = Integer.parseInt(info[0]);
        int mt = Integer.parseInt(info[1]);
        int dy = Integer.parseInt(info[2]);
        String sender = info[3];
        String title = info[4];

        ArrayList<Mail.Hyprlink> hyprlinks = new ArrayList<>();

        StringBuilder bodyBuilder = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            if (bodyBuilder.length() > 0) bodyBuilder.append('\n');
            if (line.startsWith(":link:")) {
                String[] linkInfo = line.split(":");
                hyprlinks.add(new Mail.Hyprlink(
                        linkInfo[2],
                        linkInfo[3]
                ));
                continue;
            }
            bodyBuilder.append(line);
        }

        LocalDate date = LocalDate.of(yr, mt, dy);
        mails.add(new Mail(
                sender,
                title,
                date,
                bodyBuilder.toString(),
                hyprlinks.toArray(new Mail.Hyprlink[0]))
        {{
            send=true;
        }});
    }

}
