package operatedarocket;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;

import com.formdev.flatlaf.FlatDarkLaf;

import operatedarocket.LotusOS.HomeScreen;
import operatedarocket.LotusOS.LockScreen;
import operatedarocket.util.PlayerData.PlayerDataSaverAndReader;

public class OperateDaRocketApplication {
    public static JFrame frame;
    public static void main(String[] args) throws Exception {
        PlayerDataSaverAndReader.init();

        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Utilities.changeColorTheme(
                new Color(10, 45, 70),
                Color.WHITE
        );

        InputStream fontIS = OperateDaRocketApplication.class.getResourceAsStream("/fonts/JetBrainsMono-Regular.ttf");
        Font customFont = Font.createFont(Font.TRUETYPE_FONT, fontIS).deriveFont(12f);
        for (Object key : UIManager.getLookAndFeelDefaults().keySet()) {
            if (key != null && key.toString().endsWith(".font")) {
                UIManager.put(key, customFont);
            }
        }

        boolean firstLaunch = PlayerDataSaverAndReader.load().firstLaunch;

        GraphicsDevice device = GraphicsEnvironment
                .getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();

        if (firstLaunch) {
            // Lock screen in a JFrame
            frame = new JFrame("Operate Da Rocket - Lock");
            frame.setUndecorated(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new LockScreen(frame));
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            device.setFullScreenWindow(frame);
            frame.setVisible(true);
        } else {
            // Desktop (HomeScreen) in a JFrame with taskbar entry
            frame = new JFrame("Operate Da Rocket - Universe 0");
            frame.setUndecorated(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new HomeScreen(frame));
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            device.setFullScreenWindow(frame);

            // IMPORTANT: not always on top
            frame.setAlwaysOnTop(false);

            frame.setVisible(true);
        }

        File launchNumberFile = ResourceLoader.file("/res/LaunchNumber.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(launchNumberFile))) {
            writer.write(Utilities.getLaunchNumber() + 1);
        }
    }

}
