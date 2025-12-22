package operatedarocket.ui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import operatedarocket.LotusOS.HomeScreen;
import operatedarocket.ResourceLoader;
import operatedarocket.util.Apps.AppRejistry;
import org.apache.batik.swing.JSVGCanvas;

public class DesktopIcon extends JSVGCanvas {

    private final String name;
    private final Runnable action;

    /**
     * Dock icon that launches an AppFrame panel inside HomeScreen.
     */
    public DesktopIcon(AppRejistry registry, HomeScreen homeScreen) {
        this(
                registry.name,
                registry.image,
                () -> {
                    try {
                        Class<?> cls = registry.mainClass;

// 1. AppFrame apps (internal windows)
                        if (AppFrame.class.isAssignableFrom(cls)) {
                            AppFrame appPanel = (AppFrame) cls
                                    .getDeclaredConstructor(String.class, String.class)
                                    .newInstance(registry.name, registry.image);

                            homeScreen.openAppWindow(appPanel);
                            return;
                        }

// 2. JPanel apps (embedded UI)
                        if (JPanel.class.isAssignableFrom(cls)) {
                            JPanel panel = (JPanel) cls.getDeclaredConstructor().newInstance();

                            AppFrame window = new AppFrame(registry.name, registry.image);
                            window.addContent(panel);

                            homeScreen.openAppWindow(window);
                            return;
                        }

// 3. Runnable apps (background logic)
                        if (Runnable.class.isAssignableFrom(cls)) {
                            Runnable r = (Runnable) cls.getDeclaredConstructor().newInstance();
                            new Thread(r).start();
                            return;
                        }

// 4. Unknown type
                        throw new RuntimeException("Unknown app type: " + cls);

                    } catch (Exception ex) {
                        showLaunchError(homeScreen, registry.name,
                                ex instanceof java.lang.reflect.InvocationTargetException
                                        ? ((java.lang.reflect.InvocationTargetException) ex).getTargetException()
                                        : ex);
                    }
                }
        );
    }

    /**
     * Generic icon with custom action.
     */
    public DesktopIcon(String name, String image, Runnable action) {
        this.name = name;
        this.action = action;
        initIcon(image);
    }

    private void initIcon(String imagePath) {
        setDisableInteractions(true);
        setEnableZoomInteractor(false);
        setEnablePanInteractor(false);

        setURI(getClass().getResource(imagePath).toExternalForm());
        setToolTipText(name);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        Dimension size = new Dimension(60, 60);
        setPreferredSize(size);
        setMaximumSize(size);
        setMinimumSize(size);
        setSize(size);

        setOpaque(false);
        setBackground(new Color(0, 0, 0, 0));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SwingUtilities.invokeLater(action);
            }
        });
    }

    private static void showLaunchError(Component parent, String appName, Throwable ex) {
        JOptionPane.showMessageDialog(parent,
                "Failed to launch " + appName + ":\n" + ex.getMessage(),
                "Launch Error",
                JOptionPane.ERROR_MESSAGE);
    }
}
