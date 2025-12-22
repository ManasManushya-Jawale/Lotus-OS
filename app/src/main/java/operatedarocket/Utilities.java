package operatedarocket;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import operatedarocket.LotusOS.HomeScreen;
import operatedarocket.ui.AppFrame;
import operatedarocket.util.Web.WebController;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import com.sun.net.httpserver.HttpServer;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class Utilities {

    public static void showLaunchError(Component parent, String appName, Throwable err) {
        err.printStackTrace();
        JOptionPane.showMessageDialog(parent,
                "Failed to launch " + appName + ":\n" +
                        err.getClass().getSimpleName() + ": " + err.getMessage(),
                "Launch Error",
                JOptionPane.ERROR_MESSAGE);
    }

    static {
        new JFXPanel(); // Start JavaFX once
        Platform.setImplicitExit(false);
    }

    public record PortResult(JFXPanel panel, int port, HttpServer server) {
    }

    // ------------------------------------------------------------
    // 1. PUBLIC API
    // ------------------------------------------------------------

    public static JFXPanel getPort(String htmlResourcePath,
            boolean showFrame) {
        String html = buildHtml(htmlResourcePath);
        if (html == null) {
            return loadUrlIntoPanel(htmlResourcePath, showFrame);
        }
        return loadHtmlIntoPanel(html, showFrame);
    }

    public static PortResult getPort(String htmlResourcePath,
            boolean showFrame,
            Integer requestedPort) throws IOException {

        int port = (requestedPort != null)
                ? findFreePortStartingAt(requestedPort)
                : findFreePortStartingAt(3000);

        String html = buildHtml(htmlResourcePath);
        byte[] response = html.getBytes(StandardCharsets.UTF_8);

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", exchange -> {
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        });
        server.start();

        JFXPanel panel = loadUrlIntoPanel("http://localhost:" + port + "/", showFrame);
        return new PortResult(panel, port, server);
    }

    public static JFXPanel getLink(String httpLink, boolean showFrame) {
        return loadUrlIntoPanel(httpLink, showFrame);
    }

    private static String buildHtml(String htmlResourcePath) {
        File file = ResourceLoader.file(htmlResourcePath);
        String html;

        if (file == null) {
            return null;
        }

        try {
            html = Files.readString(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read HTML resource: " + htmlResourcePath, e);
        }

        // Apply Swing color substitutions
        Color f = UIManager.getColor("Label.foreground");
        Color b = UIManager.getColor("Panel.background");

        String foreground = f.getRed() + ", " + f.getGreen() + ", " + f.getBlue();
        String background = b.getRed() + ", " + b.getGreen() + ", " + b.getBlue();

        try {
            for (String str : html.split("\n")) {
                if (!str.startsWith("link"))
                    continue;
                String controllerPath = str.substring(5);
                Class<?> controllerClass = Class.forName(controllerPath);

                if (WebController.class.isAssignableFrom(controllerClass)) {
                    WebController controller = ((WebController) controllerClass
                            .getDeclaredConstructor(String.class)
                            .newInstance(html));

                    html = controller.getInnerHTML();
                }
                html = html.replace(str, "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return html.replace("$b", foreground).replace("$f", background);
    }

    // ------------------------------------------------------------
    // 3. PANEL LOADING HELPERS
    // ------------------------------------------------------------

    private static JFXPanel loadHtmlIntoPanel(String html, boolean showFrame) {
        try {
            File temp = File.createTempFile("lotus_", ".html");
            temp.deleteOnExit();
            Files.writeString(temp.toPath(), html);
            return loadUrlIntoPanel(temp.toURI().toString(), showFrame);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create temp HTML file", e);
        }
    }

    private static JFXPanel loadUrlIntoPanel(String url, boolean showFrame) {
        JFXPanel panel = new JFXPanel();

        Platform.runLater(() -> {
            WebView webView = new WebView();
            webView.getEngine().load(url);

            BorderPane root = new BorderPane(webView);
            Scene scene = new Scene(root, 600, 400);
            panel.setScene(scene);

            if (showFrame) {
                ((HomeScreen) OperateDaRocketApplication.frame.getContentPane())
                        .openAppWindow(
                                new AppFrame(url, "/image/Apps/LocalhostBrowser.svg") {
                                    {
                                        addContent(panel, BorderLayout.CENTER);
                                    }
                                });
            }

        });

        SwingUtilities.invokeLater(panel::repaint);
        return panel;
    }

    private static boolean isPortInUse(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            return false;
        } catch (IOException e) {
            return true;
        }
    }

    private static int findFreePortStartingAt(int start) {
        int port = start;
        while (port < 65535 && isPortInUse(port)) {
            port++;
        }
        if (port >= 65535) {
            throw new RuntimeException("No free ports available.");
        }
        return port;
    }

    // Convert SVG InputStream to BufferedImage
    public static BufferedImage SVGtoBufferedImage(InputStream svgStream) throws Exception {
        class BufferedImageTranscoder extends ImageTranscoder {
            private BufferedImage image;

            @Override
            public BufferedImage createImage(int w, int h) {
                image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                return image;
            }

            @Override
            public void writeImage(BufferedImage img, TranscoderOutput out) {
                this.image = img;
            }

            public BufferedImage getImage() {
                return image;
            }
        }

        BufferedImageTranscoder transcoder = new BufferedImageTranscoder();
        TranscoderInput input = new TranscoderInput(svgStream);
        transcoder.transcode(input, null);
        return transcoder.getImage();
    }

    // Convenience wrapper: return ImageIcon directly
    public static ImageIcon SVGtoIcon(InputStream svgStream) throws Exception {
        return new ImageIcon(SVGtoBufferedImage(svgStream));
    }

    public static Image getScaledImageToFill(Image src, int targetW, int targetH) {
        int srcW = src.getWidth(null);
        int srcH = src.getHeight(null);

        if (srcW <= 0 || srcH <= 0) {
            return src.getScaledInstance(targetW, targetH, Image.SCALE_SMOOTH);
        }

        double scale = Math.max((double) targetW / srcW, (double) targetH / srcH);
        int newW = (int) Math.round(srcW * scale);
        int newH = (int) Math.round(srcH * scale);

        Image tmp = src.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);

        BufferedImage cropped = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = cropped.createGraphics();
        try {
            int x = (newW - targetW) / 2;
            int y = (newH - targetH) / 2;
            g2.drawImage(tmp, -x, -y, null);
        } finally {
            g2.dispose();
        }
        return cropped;
    }

    // Static method to change the global color theme
    public static void changeColorTheme(Color background, Color foreground) {
        for (Object key : UIManager.getLookAndFeelDefaults().keySet()) {
            if (key != null && key.toString().endsWith(".background")) {
                UIManager.put(key, background);
            } else if (key != null && key.toString().endsWith(".foreground")) {
                UIManager.put(key, foreground);
            }
        }

        // Text fields
        UIManager.put("TextField.background", background.brighter());

        // Optionally refresh existing components
        for (Frame frame : Frame.getFrames()) {
            SwingUtilities.updateComponentTreeUI(frame);
        }
    }

    public static int getLaunchNumber() throws IOException {
        File file = ResourceLoader.file("/res/LaunchNumber.txt");

        try (Scanner sc = new Scanner(file)) {
            if (sc.hasNext()) {
                String word = sc.nextLine();
                System.out.println("First word: " + word);
                return Integer.parseInt(word);
            } else {
                System.out.println("No word found");
            }
        }
        return 0;
    }

}
