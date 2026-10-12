package ConsolesLocClicker;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

public class ClickerPanel extends JPanel {
    protected JButton clicker;
    private final JLabel locLabel, statusLabel;
    private final Stats stats;

    public ClickerPanel(Stats stats, Image image) {
        this.stats = stats;
        setBackground(Frame.bgColor);
        setBorder(Frame.b);
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);

        locLabel = new JLabel("", JLabel.CENTER);
        Frame.labelify(locLabel, 26);
        statusLabel = new JLabel(" ", JLabel.CENTER);
        Frame.labelify(statusLabel, 15);

        ImageIcon bigIcon = new ImageIcon(image.getScaledInstance(220, 215, Image.SCALE_SMOOTH));
        ImageIcon smallIcon = new ImageIcon(image.getScaledInstance(205, 200, Image.SCALE_SMOOTH));
        clicker = new JButton(bigIcon);
        clicker.setOpaque(false);
        clicker.setContentAreaFilled(false);
        clicker.setBorderPainted(false);
        clicker.setFocusPainted(false);
        // Swing Timer (no java.util.Timer): se ejecuta en el hilo de Swing, es seguro tocar la UI.
        Timer restoreIcon = new Timer(100, e -> clicker.setIcon(bigIcon));
        restoreIcon.setRepeats(false);
        clicker.addActionListener(e -> {
            stats.click();
            clicker.setIcon(smallIcon);
            restoreIcon.restart();
            refresh();
        });
        JLabel hint = new JLabel("Haz clic para escribir código", JLabel.CENTER);
        Frame.labelify(hint, 13);
        hint.setForeground(Frame.dimColor);

        c.gridy = 0; add(locLabel, c);
        c.gridy = 1; add(statusLabel, c);
        c.gridy = 2; add(clicker, c);
        c.gridy = 3; add(hint, c);
        refresh();
    }

    public void refresh() {
        locLabel.setText("<html><center>" + String.format("%,d", stats.getLoc()) + " LOC<br/>"
                + "<span style='font-size:16px'>Producción: " + String.format("%,d", stats.getLocPerSecond())
                + " LOC/s &nbsp;|&nbsp; Clic: +" + stats.getClickPower() + "</span></center></html>");
        if (stats.isFrozen()) {
            statusLabel.setForeground(Frame.warnColor);
            statusLabel.setText("[!] PRODUCCIÓN DETENIDA (" + stats.getFrozenSeconds() + " s)");
        } else {
            statusLabel.setForeground(Frame.dimColor);
            statusLabel.setText("build: OK");
        }
    }
}
