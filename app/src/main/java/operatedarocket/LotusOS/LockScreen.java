package operatedarocket.LotusOS;

import operatedarocket.util.LocalFonts;
import operatedarocket.util.PlayerData.PlayerData;
import operatedarocket.util.PlayerData.PlayerDataSaverAndReader;
import org.apache.batik.swing.JSVGCanvas;

import javax.print.attribute.standard.JobMessageFromOperator;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LockScreen extends JPanel {
    public JSVGCanvas icon;
    public JLabel name;
    public JPanel passwordPanel;
    public JPasswordField passwordField;
    public JButton submit;
    public JPanel showPassword;
    public JLabel showPasswordText;
    public JRadioButton showPasswordButton;

    public JLabel passwordHint;

    public LockScreen(JFrame frame) {
        setLayout(new GridBagLayout());
        setAlignmentY(CENTER_ALIGNMENT);
        setBackground(new Color(16, 28, 13, 255));

        icon = new JSVGCanvas() {{
            setURI(LockScreen.class.getResource("/image/Pfp.svg").toExternalForm());

            Dimension size = new Dimension(360, 360);
            setPreferredSize(size);
            setMaximumSize(size);
            setMinimumSize(size);
            setSize(size);

            setOpaque(false);
            setBackground(new Color(0, 0, 0, 0));
        }};
        name = new JLabel(
                String.format(
                """
                        <html>
                            <font style="font-size: 50px; color: white;"><b>Hello! %s</font></b></html>
                        """,
                        PlayerDataSaverAndReader.load().name)
        );

        passwordPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        passwordField = new JPasswordField();
        passwordField.setFont(LocalFonts.HELVETICA.deriveFont(16f));
        passwordField.setSize(200, 75);
        passwordField.setPreferredSize(new Dimension(200, 25));

        submit = new JButton(">");
        {
            Dimension size = new Dimension(25, 25);

            submit.setMaximumSize(size);
            submit.setPreferredSize(size);
            submit.setMinimumSize(size);
            submit.setSize(size);
        }

        submit.addActionListener(e -> {
            String pswrd = "Manas%Manus&shya";

            String pswrdGiven = new String(passwordField.getPassword());

            if (pswrdGiven.equals(pswrd)) {
                try {
                    ((JFrame) SwingUtilities.getWindowAncestor(this)).setContentPane(new HomeScreen(frame));
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }

                if (PlayerDataSaverAndReader.load().firstLaunch) {
                    PlayerData newData = PlayerDataSaverAndReader.load();
                    newData.firstLaunch = false;
                    PlayerDataSaverAndReader.save(newData);
                }
            } else {
                JOptionPane.showMessageDialog(
                        frame,
                        "Wrong Password");
            }
        });
        passwordPanel.add(passwordField);
        passwordPanel.add(submit);

        showPassword = new JPanel(new FlowLayout(FlowLayout.CENTER)){
            {
                setOpaque(false);
                setBackground(new Color(0, 0, 0, 0));
            }
        };
        showPasswordText = new JLabel("Show Password");
        showPasswordButton = new JRadioButton(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                passwordField.setEchoChar(showPasswordButton.isSelected() ? ((char) 0) : '•');
            }
        });

        showPassword.add(showPasswordText);
        showPassword.add(showPasswordButton);

        passwordHint = new JLabel("\"4D 61 6E 61 73 25 4D 61 6E 75 73 26 73 68 79 61\" is it's hex value");

        addComponent(icon, 0, 0);
        addComponent(name, 0, 1);
        addComponent(passwordPanel, 0, 2);
        addComponent(showPassword, 0, 3);
        addComponent(passwordHint, 0, 4);

    }

    private void addComponent(Component comp, int gridx, int gridy) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.anchor = GridBagConstraints.CENTER; // center in both axis
        add(comp, gbc);
    }

}
