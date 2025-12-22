package operatedarocket.apps.FlowerMail;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;

import operatedarocket.LotusOS.HomeScreen;
import operatedarocket.Utilities;
import operatedarocket.ui.AppFrame;
import operatedarocket.util.LocalFonts;
import operatedarocket.util.Mail.Mail;
import operatedarocket.util.PlayerData.PlayerDataSaverAndReader;

public class FlowerMailApplication extends AppFrame {

    private JPanel mailsPanel;
    private JTextArea previewArea;
    private JPanel hyperlinksArea;
    private JSplitPane splitPane;
    private JSplitPane splitPane2;

    public FlowerMailApplication(String title, String imagePath) {
        super(title, imagePath);

        SwingUtilities.invokeLater(() -> {
            initUI();
            loadMails();
            finalizeUI();
        });
    }

    /**
     * Initialize the main UI components before loading content.
     */
    private void initUI() {
        content.setLayout(new BorderLayout());

        // Preview area (right side)
        previewArea = new JTextArea();
        previewArea.setFont(LocalFonts.HELVETICA.deriveFont(16f));
        previewArea.setEditable(false);
        previewArea.setLineWrap(true);
        previewArea.setWrapStyleWord(true);

        JScrollPane previewScroll = new JScrollPane(previewArea);
        previewScroll.setBorder(BorderFactory.createEmptyBorder());

        // Mail list panel (left side)
        mailsPanel = new JPanel();
        mailsPanel.setLayout(new BoxLayout(mailsPanel, BoxLayout.Y_AXIS));

        JScrollPane mailScroll = new JScrollPane(mailsPanel);
        mailScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        hyperlinksArea = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JScrollPane hyperlinkScroll = new JScrollPane(hyperlinksArea);
        hyperlinkScroll.setBorder(BorderFactory.createEmptyBorder());

        splitPane2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, previewScroll, hyperlinkScroll);
        splitPane2.setResizeWeight(0.9);
        splitPane2.setContinuousLayout(true);

        // SplitPane (mail list | preview)
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mailScroll, splitPane2);
        splitPane.setResizeWeight(0.30);
        splitPane.setContinuousLayout(true);

        addContent(splitPane, BorderLayout.CENTER);
    }

    /**
     * Load mails from the backend and create buttons for them.
     */
    private void loadMails() {
        for (Mail mail : FlowerMailBackend.mails) {
            if (mail == null) continue; // not a mail
            if (!mail.send) continue; // not sended yet
            if (mail.date.isAfter(PlayerDataSaverAndReader.load().date)) continue; // far dated

            String sender = mail.sender != null ? mail.sender : "Unknown";
            JButton mailButton = getMailButton(mail, sender);

            mailsPanel.add(mailButton);
            mailsPanel.add(Box.createVerticalStrut(5));

        }
    }

    private JButton getMailButton(Mail mail, String sender) {
        String title = mail.title != null ? mail.title : "(No Title)";
        String text = mail.getText() != null ? mail.getText() : "(No Content)";

        JButton mailButton = new JButton(new AbstractAction(sender + ": " + title) {
            @Override
            public void actionPerformed(ActionEvent e) {
                previewArea.setText(text);
                for (Mail.Hyprlink link : mail.hyprlinks) {
                    JButton linkButton = new JButton(link.displayText());
                    linkButton.addActionListener(f -> openLink(link));
                    hyperlinksArea.add(linkButton);
                }
            }
        });

        mailButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        mailButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return mailButton;
    }

    /**
     * Final window setup after all UI content is loaded.
     */
    private void finalizeUI() {
        setSize(800, 500);
        setVisible(true);

        // After window is visible, adjust the divider for correct proportion
        SwingUtilities.invokeLater(() ->
                splitPane.setDividerLocation(0.30)
        );
    }

    private void openLink(Mail.Hyprlink link) {

        ((HomeScreen) ((JFrame) SwingUtilities.getWindowAncestor(this)).getContentPane()).openAppWindow(
                new AppFrame("Preivew of Hyprlink", "/image/FlowerMail.svg") {{
                    addContent(Utilities.getPort(
                            link.path(),
                            false
                    ));
                }}
        );
    }
}
