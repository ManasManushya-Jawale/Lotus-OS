package operatedarocket;

import operatedarocket.ui.AppFrame;
import operatedarocket.util.Apps.AppRejistry;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Image;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ResourceLoader {

    public static Image image(String resourcePath) {
        try (InputStream is = OperateDaRocketApplication.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.out.println("Image resource not found: " + resourcePath);
                return null;
            }
            return ImageIO.read(is);
        } catch (IOException e) {
            System.out.println("Failed to load image: " + resourcePath);
            return null;
        }
    }

    public static Font font(String resourcePath) {
        try (InputStream fontIS = OperateDaRocketApplication.class.getResourceAsStream(resourcePath)) {
            if (fontIS == null) {
                System.out.println("Font resource not found: " + resourcePath);
                return null;
            }
            return Font.createFont(Font.TRUETYPE_FONT, fontIS).deriveFont(12f);
        } catch (Exception e) {
            System.out.println("Failed to load font: " + e.getMessage());
            return null;
        }
    }

    public static List<AppRejistry> apps() throws Exception {
        List<AppRejistry> apps = new ArrayList<>();
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        try (InputStream is = OperateDaRocketApplication.class.getResourceAsStream("/Apps.xml")) {
            if (is == null) {
                throw new IllegalArgumentException("Apps.xml not found in resources!");
            }
            Document document = db.parse(is);

            NodeList nodes = document.getElementsByTagName("app");
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                NodeList children = node.getChildNodes();

                String name = null, image = null, appClass = null;
                Boolean onToolbar = node.getAttributes().getNamedItem("onToolbar").getTextContent().equals("1");

                for (int j = 0; j < children.getLength(); j++) {
                    Node child = children.item(j);
                    if (child.getNodeType() != Node.ELEMENT_NODE) continue;

                    switch (child.getNodeName()) {
                        case "name" -> name = child.getTextContent().trim();
                        case "image" -> image = child.getTextContent().trim();
                        case "class" -> appClass = child.getTextContent().trim();
                    }
                }
                Class<?> rawClass = Class.forName(appClass);
                Class<?> frameClass;
                if (AppFrame.class.isAssignableFrom(rawClass)) {
                    frameClass = rawClass.asSubclass(AppFrame.class);
                } else if (Runnable.class.isAssignableFrom(rawClass)) {
                    frameClass = rawClass.asSubclass(Runnable.class);
                } else {
                    throw new IllegalArgumentException();
                }
                apps.add(new AppRejistry(name, image, frameClass, onToolbar));
            }
        }
        return apps;
    }

    public static File file(String resourcePath) {
        URL url = OperateDaRocketApplication.class.getResource(resourcePath);
        if (url == null) {
            System.out.println("Resource not found: " + resourcePath);
            return null;
        }

        try {
            // Works if resource is a real file (IDE/dev mode)
            return new File(url.toURI());
        } catch (Exception e) {
            // Inside JAR: not a real file
            System.out.println("Resource is inside JAR, cannot convert to File: " + e.getMessage());
            return null;
        }
    }

    public static InputStream stream(String resourcePath) {
        InputStream is = OperateDaRocketApplication.class.getResourceAsStream(resourcePath);
        if (is == null) {
            System.out.println("Resource not found: " + resourcePath);
        }
        return is;
    }

}
