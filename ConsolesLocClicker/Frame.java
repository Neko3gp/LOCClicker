package ConsolesLocClicker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;

/** Ventana principal (Main-Class). Aquí se arma la UI y corre el reloj de 1 segundo. */
public class Frame implements ActionListener, KeyListener {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Frame::new);
    }

    // ---- Tema "terminal oscura" (Samuel: cambia los colores aquí) ----
    protected static final Color bgColor   = new Color(13, 17, 23);
    protected static final Color textColor = new Color(0, 255, 65);
    protected static final Color dimColor  = new Color(80, 120, 90);
    protected static final Color warnColor = new Color(255, 95, 86);
    protected static final Border b = BorderFactory.createLineBorder(textColor, 1);

    private final JFrame frame;
    private final JTabbedPane pane;
    private final javax.swing.Timer refresh;   // reloj del juego: 1 tick por segundo, en el hilo de Swing
    private final Stats stats;
    private final LocTimer time;
    private final ClickerPanel clicker;
    private final StorePanel store;
    private final StatsPanel statsPanel;
    private final DevPanel dev;
    private final DefectManager defects;
    private boolean ended = false;

    public Frame() {
        Image icon = null;
        try {
            icon = ImageIO.read(getClass().getResource("/assets/ide.png"));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "No se encontró assets/ide.png: " + e);
            System.exit(1);
        }
        UIManager.put("TabbedPane.selected", bgColor);
        UIManager.put("TabbedPane.contentAreaColor", bgColor);
        UIManager.put("TabbedPane.light", bgColor);
        UIManager.put("TabbedPane.darkShadow", textColor);
        UIManager.put("TabbedPane.borderHightlightColor", textColor);
        UIManager.put("TabbedPane.tabAreaBackground", bgColor);
        UIManager.put("ToolTip.background", bgColor);
        UIManager.put("ToolTip.foreground", textColor);
        UIManager.put("ToolTip.border", b);
        UIManager.put("OptionPane.background", bgColor);
        UIManager.put("Panel.background", bgColor);
        UIManager.put("OptionPane.messageForeground", textColor);

        frame = new JFrame("LOC Clicker: Ingeniería Edition");
        frame.setSize(960, 620);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setIconImage(icon);

        stats = new Stats();
        time = new LocTimer();
        clicker = new ClickerPanel(stats, icon);
        store = new StorePanel(stats, this::onPlayerAction);
        statsPanel = new StatsPanel(stats, time);
        defects = new DefectManager(frame, stats, this::refreshAll);
        dev = new DevPanel(stats, defects);

        // Pantalla de juego: clicker a la izquierda, tienda a la derecha (sin cambiar de pestaña).
        JPanel game = new JPanel(new BorderLayout(8, 0));
        game.setBackground(bgColor);
        game.add(clicker, BorderLayout.CENTER);
        store.setPreferredSize(new Dimension(470, 0));
        game.add(store, BorderLayout.EAST);

        pane = new JTabbedPane();
        pane.setBackground(bgColor);
        pane.setForeground(textColor);
        pane.setFont(font(14, true));
        pane.addKeyListener(this);
        pane.add("Terminal", game);
        pane.add("Métricas PSP", statsPanel);
        pane.add("Info", new InfoPanel());
        frame.add(pane);
        frame.setVisible(true);

        refresh = new javax.swing.Timer(1000, this);
        refresh.start();
        defects.start();
    }

    /** Un segundo de juego. */
    @Override
    public void actionPerformed(ActionEvent e) {
        stats.tick();
        time.tick();
        refreshAll();
        checkVictory();
    }

    private void onPlayerAction() {
        refreshAll();
        checkVictory();
    }

    private void refreshAll() {
        clicker.refresh();
        store.refresh();
        statsPanel.refresh();
    }

    private void checkVictory() {
        if (!stats.isFinished() || ended) return;
        ended = true;
        refresh.stop();
        defects.stop();
        SwingUtilities.invokeLater(this::showVictory);
    }

    private void showVictory() {
        int secs = Math.max(1, time.getTotalSeconds());
        String msg = String.format(
            "<html><div style='width:380px'><b>PROYECTO ENTREGADO: CMMI Nivel 5 alcanzado</b><br/><br/>"
            + "Tiempo total: %s<br/>LOC producidas: %,d<br/>Productividad (PSP): %,d LOC/hora<br/>"
            + "Defectos detectados: %d (parchados: %d)<br/>Densidad de defectos: %.1f def/KLOC<br/><br/>"
            + "Bitácora PSP guardada en:<br/>%s</div></html>",
            time, stats.getLocMade(), (int) (stats.getLocMade() * 3600.0 / secs),
            stats.getDefectsFound(), stats.getDefectsPatched(), stats.getDefectDensity(),
            DefectLog.FILE.getAbsolutePath());
        Object[] options = {"Jugar de nuevo", "Salir"};
        int r = JOptionPane.showOptionDialog(frame, msg, "Release v1.0", JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        if (r == 0) {
            new Frame();      // primero la nueva ventana, luego se cierra la vieja
            frame.dispose();
        } else {
            System.exit(0);
        }
    }

    // ---- Utilidades de estilo ----
    public static Font font(int size, boolean bold) {
        return new Font(Font.MONOSPACED, bold ? Font.BOLD : Font.PLAIN, size);
    }
    public static void labelify(JLabel label, int size) {
        label.setForeground(textColor);
        label.setBackground(bgColor);
        label.setFont(font(size, true));
        label.setOpaque(true);
    }
    public static void buttonify(JButton button, int size) {
        button.setBorder(b);
        button.setFont(font(size, true));
        button.setBackground(bgColor);
        button.setForeground(textColor);
        button.setFocusPainted(false);
    }

    // ---- Panel oculto de desarrollo: Ctrl+Enter en la pestaña Info ----
    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown() && pane.getSelectedIndex() == pane.indexOfTab("Info")) {
            if (pane.indexOfTab("dev") == -1) {
                pane.add("dev", dev);
                pane.setSelectedIndex(pane.indexOfTab("dev"));
            } else {
                pane.setSelectedIndex(pane.indexOfTab("Terminal"));
                pane.remove(pane.indexOfTab("dev"));
            }
        }
    }
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyPressed(KeyEvent e) {}
}
