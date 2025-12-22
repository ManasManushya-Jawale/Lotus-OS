package operatedarocket.ui;

import operatedarocket.OperateDaRocketApplication;
import operatedarocket.Utilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class AppFrame extends JPanel {

    private final JPanel topBar;
    public final JPanel content;
    private final JButton close;
    private final JButton maximize;
    private final JButton minimize;

    private boolean maximized = false;
    private Point initialClick;

    // Default restore size
    private Rectangle restoreBounds = new Rectangle(100, 100, 900, 600);

    // Custom maximize margins
    private final int maxX = 0;
    private final int maxY = 0;
    private final int maxMarginRight = 0;
    private final int maxMarginBottom = 0;

    public AppFrame(String title, String imageResourcePath) {

        setLayout(new BorderLayout());
        setOpaque(true);
        setBackground(UIManager.getColor("Panel.background"));
        setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        setBounds(restoreBounds);
        setDoubleBuffered(true);

        // Top bar
        topBar = new JPanel(new BorderLayout());
        topBar.setBackground(UIManager.getColor("Panel.background").darker());
        topBar.setPreferredSize(new Dimension(0, 36));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        setName(title);

        JLabel icon;
        try {
            icon = new JLabel(
                    new ImageIcon(Utilities.getScaledImageToFill(
                            Utilities.SVGtoBufferedImage(
                                    OperateDaRocketApplication.class.getResourceAsStream(imageResourcePath)),
                            28, 28))
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        left.setOpaque(false);
        left.add(icon);
        left.add(titleLabel);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        controls.setOpaque(false);

        minimize = new JButton("_");
        maximize = new JButton("□");
        close = new JButton("X");

        controls.add(minimize);
        controls.add(maximize);
        controls.add(close);

        topBar.add(left, BorderLayout.WEST);
        topBar.add(controls, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);

        // Content panel
        content = new JPanel(new BorderLayout());
        content.setDoubleBuffered(true);
        add(content, BorderLayout.CENTER);

        // Close
        close.addActionListener(e -> {
            Container parent = getParent();
            if (parent != null) {
                parent.remove(this);
                parent.repaint();
            }
        });

        // Minimize
        minimize.addActionListener(e -> setVisible(false));

        // Maximize
        maximize.addActionListener(e -> toggleMaximize());

        enableDragging();
        enableResizeHandles();
    }

    public void addContent(Component comp) {
        content.add(comp);
        content.revalidate();
        content.repaint();
    }

    public void addContent(Component comp, String constraint) {
        content.add(comp, constraint);
        content.revalidate();
        content.repaint();
    }
    private void enableDragging() {
        topBar.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
                bringToFront();
            }
        });

        topBar.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (maximized) return;

                int x = getX() + e.getX() - initialClick.x;
                int y = getY() + e.getY() - initialClick.y;

                setLocation(x, y);
                repaint(); // fast, no layout
                bringToFront();
            }
        });
    }

    public void bringToFront() {
        Container parent = getParent();
        if (parent != null) {
            parent.setComponentZOrder(this, 0);
            parent.repaint();
        }
    }

    private void enableResizeHandles() {
        int margin = 6;

        MouseAdapter resize = new MouseAdapter() {

            @Override
            public void mouseDragged(MouseEvent e) {
                if (maximized) return;
                if (getParent() == null) return;

                JComponent src = (JComponent) e.getSource();
                Object dir = src.getClientProperty("RESIZE_DIR");
                if (dir == null) return;

                Rectangle b = getBounds();
                Point p = SwingUtilities.convertPoint(src, e.getPoint(), getParent());

                switch (dir.toString()) {
                    case "RIGHT" -> b.width = Math.max(200, p.x - b.x);
                    case "BOTTOM" -> b.height = Math.max(150, p.y - b.y);
                }

                setBounds(b);
                restoreBounds = b;

                repaint(); // fast
                bringToFront();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                revalidate(); // only once
                doLayout();
            }
        };

        // Right resize
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
        right.setPreferredSize(new Dimension(margin, 0));
        right.putClientProperty("RESIZE_DIR", "RIGHT");
        right.addMouseListener(resize);
        right.addMouseMotionListener(resize);
        add(right, BorderLayout.EAST);

        // Bottom resize
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
        bottom.setPreferredSize(new Dimension(0, margin));
        bottom.putClientProperty("RESIZE_DIR", "BOTTOM");
        bottom.addMouseListener(resize);
        bottom.addMouseMotionListener(resize);
        add(bottom, BorderLayout.SOUTH);
    }

    private void toggleMaximize() {
        Container parent = getParent();
        if (parent == null) return;

        if (maximized) {
            setBounds(restoreBounds);
            maximize.setText("□");
        } else {
            restoreBounds = getBounds();
            setBounds(
                    maxX,
                    maxY,
                    parent.getWidth() - maxX - maxMarginRight,
                    parent.getHeight() - maxY - maxMarginBottom
            );
            maximize.setText("⊙");
        }

        maximized = !maximized;

        doLayout();
        repaint();
        bringToFront();
    }
}
