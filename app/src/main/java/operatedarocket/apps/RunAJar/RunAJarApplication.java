package operatedarocket.apps.RunAJar;

import net.miginfocom.swing.MigLayout;
import operatedarocket.ui.AppFrame;
import operatedarocket.util.LocalFonts;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class RunAJarApplication extends AppFrame {

    public JLabel titleText;
    public JButton runAJar;

    public RunAJarApplication(String title, String imagePath) {
        super(title, imagePath);

        content.setLayout(new MigLayout());

        titleText = new JLabel(title);
        titleText.setFont(LocalFonts.INTER.deriveFont(Font.BOLD, 50));
        titleText.setAlignmentX(Component.CENTER_ALIGNMENT);
        addContent(titleText, "grow");

        runAJar = new JButton(new AbstractAction("Run A Jar") {
            @Override
            public void actionPerformed(ActionEvent e) {
                runAJar();
            }
        });
        runAJar.setAlignmentX(Component.CENTER_ALIGNMENT);
        addContent(runAJar, "grow");
    }

    public void runAJar() {
        JFileChooser fileChooser = new JFileChooser();
        int res = fileChooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String path = file.getPath();

            try {
                ProcessBuilder pb = new ProcessBuilder("java", "-jar", path);
                Process pbs = pb.start();

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }
}
