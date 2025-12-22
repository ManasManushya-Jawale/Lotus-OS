package operatedarocket.LotusOS;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;

import operatedarocket.OperateDaRocketApplication;
import operatedarocket.ResourceLoader;
import operatedarocket.apps.FlowerMail.FlowerMailBackend;
import operatedarocket.ui.AppFrame;
import operatedarocket.ui.DesktopIcon;
import operatedarocket.util.Apps.AppRejistry;
import operatedarocket.Utilities;
import operatedarocket.util.LocalFonts;
import operatedarocket.util.PlayerData.PlayerData;
import operatedarocket.util.PlayerData.PlayerDataSaverAndReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * HomeScreen:
 * - Wallpaper with slideshow
 * - Dock on the left
 * - Top bar with clock + power
 * - Internal app windows (AppFrame panels) managed inside a JLayeredPane
 */
public class HomeScreen extends JPanel {

    private static final int DOCK_WIDTH = 80;
    private static final Log log = LogFactory.getLog(HomeScreen.class);

    public final JPanel dockPanel;
    private final JLayeredPane contentPane;
    private final JPanel doorsPanel;
    public final JPanel topBar = new JPanel(new BorderLayout());
    private final JLabel backgroundLabel;

    // Layer that holds AppFrame windows
    private final JLayeredPane windowLayer = new JLayeredPane();

    private String[] wallpapers;
    private Timer wallpaperTimer;
    private int wallpaperNumber = 0;
    private Window loading;

    private ArrayList<String> appsOpened;

    private LocalDate date;

    public HomeScreen(JFrame owner) throws Exception {
        super(new BorderLayout());

        loading = new JWindow();
        loading.setLayout(new BorderLayout());
        loading.add(
                new JLabel("Loading the homescreen") {{
                    setFont(LocalFonts.YOUNG20S.deriveFont(36f));
                }},
                BorderLayout.CENTER
        );
        loading.setSize(300, 100);
        loading.setLocationRelativeTo(null);
        loading.setVisible(true);

        FlowerMailBackend.init();

        appsOpened = new ArrayList<>();

        // Create UI parts
        dockPanel = createDockPanel(owner);
        contentPane = createContentPane();
        doorsPanel = new JPanel(null);
        doorsPanel.setOpaque(false);

        // background label stored so we can update its icon on resize
        backgroundLabel = new JLabel();

        // Add layers to contentPane
        contentPane.add(backgroundLabel, Integer.valueOf(0)); // wallpaper
        contentPane.add(doorsPanel, Integer.valueOf(1));      // doors / extra content

        windowLayer.setLayout(null);
        contentPane.add(windowLayer, Integer.valueOf(2));     // app windows

        setupTopBar();

        revalidate();
        SwingUtilities.invokeLater(() -> {
            add(topBar, BorderLayout.NORTH);
            add(dockPanel, BorderLayout.WEST);
            add(contentPane, BorderLayout.CENTER);

            try {
                updateSizesAndBackground();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            loading.dispose();
            SwingUtilities.getWindowAncestor(this).setVisible(true);
        });

        // listen for resize so wallpaper and cpSize update
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                try {
                    updateSizesAndBackground();
                    // Resize maximized app windows to fit new content area
                    for (Component c : windowLayer.getComponents()) {
                        if (c instanceof AppFrame af && af.isVisible()) {
                            // If currently maximized, re-maximize to new bounds
                            // AppFrame internally keeps track of maximized state;
                            // here we just ensure it doesn't overflow.
                            Rectangle b = af.getBounds();
                            if (b.x == 0 && b.y == 0 &&
                                    b.width >= windowLayer.getWidth() - 5 &&
                                    b.height >= windowLayer.getHeight() - 5) {
                                af.setBounds(0, 0, windowLayer.getWidth(), windowLayer.getHeight());
                            }
                        }
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    private void setupTopBar() {
        topBar.setOpaque(true);
        topBar.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        topBar.setBackground(UIManager.getColor("Panel.background").darker());

        // Clock + Date labels
        JLabel clockLabel = new JLabel();
        clockLabel.setForeground(Color.WHITE);
        assert LocalFonts.INTER != null;
        clockLabel.setFont(LocalFonts.INTER.deriveFont(15f));

        JLabel dateLabel = new JLabel();
        dateLabel.setForeground(Color.WHITE);
        dateLabel.setFont(LocalFonts.INTER.deriveFont(15f));

        JPanel clockPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        clockPanel.setOpaque(false);
        clockPanel.add(dateLabel);
        clockPanel.add(clockLabel);

        date = PlayerDataSaverAndReader.load().date;

        Timer timer = new Timer(1000, e -> {
            String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
            clockLabel.setText(time);

            LocalDate gameDate = date;
            String dateText = DateTimeFormatter.ofPattern("dd/MM/yyyy").format(gameDate);
            dateLabel.setText(dateText);
        });
        timer.start();

        JButton powerButton = getPowerButton();

        topBar.add(powerButton, BorderLayout.WEST);
        topBar.add(clockPanel, BorderLayout.CENTER);
    }

    private JButton getPowerButton() {
        JButton powerButton = new JButton("Power");
        powerButton.setFocusable(false);

        JPopupMenu powerPopup = new JPopupMenu();
        JMenuItem sleep = new JMenuItem("Sleep");
        JMenuItem restart = new JMenuItem("Restart");
        JMenuItem shutdown = new JMenuItem("Shutdown");

        sleep.addActionListener(e -> {
            ((JFrame) SwingUtilities.getWindowAncestor(this)).setState(JFrame.ICONIFIED);
        });

        shutdown.addActionListener(e -> {
            SwingUtilities.getWindowAncestor(this).dispose();
            System.exit(0);
        });

        restart.addActionListener(e -> {
            SwingUtilities.getWindowAncestor(this).setVisible(false);
            try {
                loading.setVisible(true);
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            loading.setVisible(false);
            PlayerData newData = PlayerDataSaverAndReader.load();
            newData.date = newData.date.plusDays(2);
            PlayerDataSaverAndReader.save(newData);
            date = newData.date;
            SwingUtilities.getWindowAncestor(this).setVisible(true);
        });

        powerPopup.add(sleep);
        powerPopup.add(restart);
        powerPopup.add(shutdown);

        powerButton.addActionListener(e -> powerPopup.show(powerButton, 0, powerButton.getHeight()));
        return powerButton;
    }

    /**
     * Build dock with DesktopIcons from AppRegistry list.
     */
    private JPanel createDockPanel(JFrame owner) throws Exception {
        JPanel dock = new JPanel();
        dock.setLayout(new BoxLayout(dock, BoxLayout.Y_AXIS));
        dock.setBackground(UIManager.getColor("Panel.background").darker());
        dock.setPreferredSize(new Dimension(DOCK_WIDTH, 100));
        dock.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        List<AppRejistry> apps = ResourceLoader.apps();

        DesktopIcon appsMenu = getDesktopIcon();
        appsMenu.setAlignmentX(Component.CENTER_ALIGNMENT);
        appsMenu.setToolTipText("Apps Menu");
        appsMenu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        appsMenu.setMaximumSize(new Dimension(60, 60));
        appsMenu.setMinimumSize(new Dimension(40, 40));
        appsMenu.setOpaque(false);

        MouseListener[] listeners = appsMenu.getMouseListeners();
        if (listeners.length > 0) {
            appsMenu.removeMouseListener(listeners[0]);
        }

        appsMenu.addMouseListener(new MouseAdapter() {
            private final Dimension original = appsMenu.getPreferredSize();

            @Override
            public void mouseEntered(MouseEvent e) {
                appsMenu.setOpaque(true);
                appsMenu.setPreferredSize(new Dimension(
                        Math.min(80, original.width + 12),
                        Math.min(80, original.height + 12)));
                appsMenu.revalidate();
                appsMenu.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                appsMenu.setOpaque(false);
                appsMenu.setPreferredSize(original);
                appsMenu.revalidate();
                appsMenu.repaint();
            }
        });

        dock.add(appsMenu);
        dock.add(Box.createVerticalStrut(10));

        for (AppRejistry app : apps) {
            if (!app.onToolbar) continue;
            DesktopIcon icon = new DesktopIcon(app, this);
            dock.add(icon);
            dock.add(Box.createVerticalStrut(10));
        }

        return dock;
    }

    /**
     * Apps + files + windows menu.
     * Now "Windows" lists internal AppFrame windows from windowLayer.
     */
    private DesktopIcon getDesktopIcon() {
        JPopupMenu entities = new JPopupMenu(),
                appsMenu = new JPopupMenu(),
                filesMenu = new JPopupMenu(),
                windowsMenu = new JPopupMenu();

        JMenuItem filesButton = new JMenuItem("Files");
        JMenuItem windowsButtons = new JMenuItem("Windows");
        JMenuItem appsButton = new JMenuItem("Apps");

        windowsButtons.addActionListener(e -> {
            windowsMenu.removeAll();

            appsOpened = new ArrayList<>();
            for (Component c : windowLayer.getComponents()) {
                if (!(c instanceof AppFrame af)) continue;

                appsOpened.add(c.getName());

                JMenuItem item = new JMenuItem("(" + windowLayer.getComponentZOrder(c) + ")" + c.getName());
                item.addActionListener(ev -> {
                    af.setVisible(true);
                    ((AppFrame)c).bringToFront();
                    bringWindowToFront(af);
                });
                windowsMenu.add(item);
            }

            windowsMenu.show(this, 100, 50);
        });

        entities.add(filesButton);
        entities.add(windowsButtons);
        entities.add(appsButton);

        filesMenu.add(new JLabel("It is under development"));

        try {
            for (AppRejistry app : ResourceLoader.apps()) {
                JMenuItem appBtn = new JMenuItem(
                        app.name,
                        new ImageIcon(Utilities.getScaledImageToFill(
                                Utilities.SVGtoBufferedImage(
                                        OperateDaRocketApplication.class.getResourceAsStream(app.image)),
                                50, 50))
                );
                appBtn.addActionListener(e -> {
                    try {
                        Class<?> cls = app.mainClass;

                        if (AppFrame.class.isAssignableFrom(cls)) {
                            AppFrame win = (AppFrame) cls
                                    .getDeclaredConstructor(String.class, String.class)
                                    .newInstance(app.name, app.image);
                            openAppWindow(win);
                            return;
                        }

                        if (JPanel.class.isAssignableFrom(cls)) {
                            JPanel panel = (JPanel) cls.getDeclaredConstructor().newInstance();
                            AppFrame win = new AppFrame(app.name, app.image);
                            win.addContent(panel);
                            openAppWindow(win);
                            return;
                        }

                        if (Runnable.class.isAssignableFrom(cls)) {
                            Runnable r = (Runnable) cls.getDeclaredConstructor().newInstance();
                            new Thread(r).start();
                            return;
                        }

                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                });

                appsMenu.add(appBtn);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        appsButton.addActionListener(e -> {
            appsMenu.show(this, 100, 50);
        });

        filesButton.addActionListener(e -> {
            filesMenu.show(this, 100, 50);
        });

        return new DesktopIcon("Apps Menu", "/image/Logo.svg", () -> {
            entities.show(this, 90, 50);
        });
    }

    private JLayeredPane createContentPane() {
        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);
        return layeredPane;
    }

    /**
     * Open an internal AppFrame window on the desktop.
     */
    public void openAppWindow(AppFrame app) {
        int w = Math.min(900, Math.max(400, contentPane.getWidth() - DOCK_WIDTH - 50));
        int h = Math.min(600, Math.max(300, contentPane.getHeight() - 150));
        app.setBounds(120, 120, w, h);
        windowLayer.add(app, Integer.valueOf(10));
        bringWindowToFront(app);
        windowLayer.revalidate();
        windowLayer.repaint();
    }

    private void bringWindowToFront(AppFrame app) {
        windowLayer.setComponentZOrder(app, 0);
        windowLayer.repaint();
    }

    private void updateSizesAndBackground() throws IOException {
        if (wallpapers == null) {
            loadWallpapers();
            if (wallpapers.length == 0) {
                System.err.println("No valid wallpapers found.");
                return;
            }
        }

        Dimension full = getSize();
        if (full.width <= 0 || full.height <= 0) {
            full = Toolkit.getDefaultToolkit().getScreenSize();
        }

        int contentWidth = Math.max(100, full.width - dockPanel.getPreferredSize().width);
        int contentHeight = Math.max(100, full.height);

        contentPane.setBounds(0, 0, contentWidth, contentHeight);
        contentPane.setPreferredSize(new Dimension(contentWidth, contentHeight));
        doorsPanel.setBounds(0, 0, contentWidth, contentHeight);
        windowLayer.setBounds(0, 0, contentWidth, contentHeight);

        if (wallpaperTimer == null) {
            wallpaperTimer = new Timer(10000, e -> showNextWallpaper(contentWidth, contentHeight));
            wallpaperTimer.start();
        }

        showNextWallpaper(contentWidth, contentHeight);
    }

    private void loadWallpapers() throws IOException {
        File registry = ResourceLoader.file("/Settings/Wallpapers.txt");
        wallpapers = Files.readString(registry.toPath()).split("\n");
    }

    private void showNextWallpaper(int w, int h) {
        wallpaperNumber = wallpaperNumber < 13 ? wallpaperNumber++ : 0;
        Image img = ResourceLoader.image(wallpapers[wallpaperNumber]);

        Image scaled = Utilities.getScaledImageToFill(img, w, h);

        backgroundLabel.setIcon(new ImageIcon(scaled));
        backgroundLabel.setBounds(0, 0, w, h);
        backgroundLabel.repaint();
        contentPane.repaint();
    }
}
