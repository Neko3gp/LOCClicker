package ConsolesLocClicker.ui;

import ConsolesLocClicker.audio.SoundManager;
import ConsolesLocClicker.core.LocTimer;
import ConsolesLocClicker.core.Stats;
import ConsolesLocClicker.events.DefectManager;
import ConsolesLocClicker.events.EventManager;
import ConsolesLocClicker.events.UnexpectedEventManager;
import ConsolesLocClicker.minigames.FrenzyQte;

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
public class Frame implements ActionListener, KeyListener, ContextDialog.JFrameOwner {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Frame::new);
    }

    // ---- Tema "terminal oscura" (Samuel: cambia los colores aquí) ----
    public static final Color bgColor   = new Color(13, 17, 23);
    public static final Color textColor = new Color(0, 255, 65);
    public static final Color dimColor  = new Color(80, 120, 90);
    public static final Color warnColor = new Color(255, 95, 86);
    public static final Border b = BorderFactory.createLineBorder(textColor, 1);

    private final JFrame frame;
    private final JTabbedPane pane;
    private final javax.swing.Timer refresh;   // reloj del juego: 1 tick por segundo, en el hilo de Swing
    private final Stats stats;
    private final LocTimer time;
    private final ClickerPanel clicker;
    private final StorePanel store;
    private final StaffPanel staffPanel;
    private final StatsPanel statsPanel;
    private final DevPanel dev;
    private final DefectManager defects;
        private final EventManager events;
    private final UnexpectedEventManager unexpectedEvents;
    private boolean ended = false;
    private boolean gameStarted = false;

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
        staffPanel = new StaffPanel(stats, this::onPlayerAction);
        statsPanel = new StatsPanel(stats, time);
        defects = new DefectManager(frame, stats, this::refreshAll);
            events = new EventManager(frame, stats, this::refreshAll);
        unexpectedEvents = new UnexpectedEventManager(frame, stats, this::refreshAll);
        dev = new DevPanel(stats, defects);

        // Terminal de escritura a la izquierda, todas las pestañas concentradas a la derecha
        clicker.setPreferredSize(new Dimension(440, 0));

        pane = new JTabbedPane();
        pane.setBackground(bgColor);
        pane.setForeground(textColor);
        pane.setFont(font(14, true));
        pane.addKeyListener(this);
        pane.add("Mejoras", store);
        pane.add("Equipo", staffPanel);
        pane.add("Métricas PSP", statsPanel);
        pane.add("Partida", new SavePanel(stats, this::onPlayerAction));
        pane.addChangeListener(e -> SoundManager.playClick());

        JPanel contentPane = (JPanel) frame.getContentPane();
        contentPane.setLayout(new BorderLayout(8, 0));
        contentPane.setBackground(bgColor);
        contentPane.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        contentPane.add(clicker, BorderLayout.WEST);
        contentPane.add(pane, BorderLayout.CENTER);
        frame.setVisible(true);

        SoundManager.init();
        refresh = new javax.swing.Timer(1000, this);
        ContextDialog.show(this, icon, this::startGame);
    }

    /** Activa el reloj y los eventos despues de que el jugador lea el contexto. */
    private void startGame() {
        if (gameStarted) return;
        gameStarted = true;
        refresh.start();
        defects.start();
        events.start();
        unexpectedEvents.start();
        SoundManager.startBgm();
    }

    @Override
    public java.awt.Frame frame() { return frame; }

    /** Un segundo de juego. */
    @Override
    public void actionPerformed(ActionEvent e) {
        stats.tick();
        time.tick();
        if (stats.consumeFrenzyOpportunity()) FrenzyQte.show(frame, stats, this::refreshAll);
        refreshAll();
        checkVictory();
    }

    private void onPlayerAction() {
        refreshAll();
        checkVictory();
    }

    private void refreshAll() {
        SoundManager.setFever(stats.getFrenzySeconds() > 0);
        clicker.refresh();
        store.refresh();
        staffPanel.refresh();
        statsPanel.refresh();
    }

    private void checkVictory() {
        if (!stats.isFinished() || ended) return;
        ended = true;
        refresh.stop();
        defects.stop();
        events.stop();
        unexpectedEvents.stop();
        SoundManager.setFever(false);
        SoundManager.setBugEvent(false);
        SwingUtilities.invokeLater(this::showPhaseTransition);
    }

    /** Presenta el ascenso de fase; CMMI Nivel 5 es un hito, no el final. */
    private void showPhaseTransition() {
        int secs = Math.max(1, time.getTotalSeconds());
        String msg = String.format(
            "<html><div style='width:400px'><b>FASE %d COMPLETADA: %s</b><br/><br/>"
            + "LOC producidas: %,d<br/>Productividad (PSP): %,d LOC/hora<br/>"
            + "Defectos: %d detectados, %d parchados<br/>Estabilidad: %d%%<br/><br/>"
            + "La empresa escala: el siguiente proyecto exigira mas LOC y estabilidad."
            + "</div></html>",
            stats.getPhase(), stats.getPhaseName(),
            stats.getLocMade(), (int) (stats.getLocMade() * 3600.0 / secs),
            stats.getDefectsFound(), stats.getDefectsPatched(), stats.getStability());
        Object[] options = {"Escalar empresa", "Salir"};
        int r = JOptionPane.showOptionDialog(frame, msg, "Nueva fase", JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
        if (r == 0) {
            stats.advancePhase();
            ended = false;
            refreshAll();
            refresh.start();
            defects.start();
            events.start();
            unexpectedEvents.startPhase();
        } else {
            SoundManager.stopBgm();
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

    // ---- Panel oculto de desarrollo: Ctrl+Enter en la pestaña de metricas ----
    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER && e.isControlDown() && pane.getSelectedIndex() == pane.indexOfTab("Métricas PSP")) {
            if (pane.indexOfTab("dev") == -1) {
                pane.add("dev", dev);
                pane.setSelectedIndex(pane.indexOfTab("dev"));
            } else {
                pane.setSelectedIndex(pane.indexOfTab("Mejoras"));
                pane.remove(pane.indexOfTab("dev"));
            }
        }
    }
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyPressed(KeyEvent e) {}
}
