package ConsolesLocClicker.events;

import ConsolesLocClicker.core.Stats;
import ConsolesLocClicker.core.GameConfig;
import ConsolesLocClicker.events.catalog.NegativeEvent;
import ConsolesLocClicker.events.catalog.NegativeEventCatalog;
import ConsolesLocClicker.ui.Frame;

import java.awt.BorderLayout;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Eventos operativos independientes de los minijuegos. No se pueden evitar:
 * representan gastos, incidentes o retrabajo que la empresa debe absorber.
 */
public class UnexpectedEventManager {
    private final JFrame owner;
    private final Stats stats;
    private final Runnable onChange;
    private final Random random = new Random();
    private Timer schedule;
    private JDialog dialog;
    private boolean running;

    public UnexpectedEventManager(JFrame owner, Stats stats, Runnable onChange) {
        this.owner = owner;
        this.stats = stats;
        this.onChange = onChange;
    }

    public void start() {
        running = true;
        if (stats.getPhase() >= 2) scheduleFirstPhaseEvent();
    }

    /** Reinicia el ciclo al escalar: el primer incidente es obligatorio y cercano. */
    public void startPhase() {
        running = true;
        schedule(GameConfig.UNEXPECTED_FIRST_DELAY_MS);
    }

    public void stop() {
        running = false;
        if (schedule != null) schedule.stop();
        if (dialog != null) { dialog.dispose(); dialog = null; }
    }

    private void scheduleNext() {
        schedule(GameConfig.UNEXPECTED_MIN_DELAY_MS
            + random.nextInt(GameConfig.UNEXPECTED_MAX_DELAY_MS
            - GameConfig.UNEXPECTED_MIN_DELAY_MS + 1));
        }

        private void scheduleFirstPhaseEvent() { schedule(GameConfig.UNEXPECTED_FIRST_DELAY_MS); }

        private void schedule(int delay) {
        if (schedule != null) schedule.stop();
        schedule = new Timer(delay, e -> showEvent());
        schedule.setRepeats(false);
        schedule.start();
    }

    private void showEvent() {
        if (!running || dialog != null || stats.getPhase() < 2 || stats.isPhaseComplete()) {
            if (running) scheduleNext();
            return;
        }
        NegativeEvent event = NegativeEventCatalog.random(random);
        String consequence = event.apply(stats);
        int stability = stats.getStability();
        dialog = new JDialog(owner, "Evento inesperado: " + event.title(), false);
        dialog.setAlwaysOnTop(true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Frame.bgColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Frame.warnColor, 2),
                BorderFactory.createEmptyBorder(18, 20, 18, 20)));
        JLabel message = new JLabel("<html><div style='width:520px'><b>" + event.title() + "</b><br/><br/>"
            + event.description() + "<br/><br/>" + consequence + "<br/>"
            + "Estabilidad actual: <b>" + stability + "%</b><br/><br/>"
            + "La consecuencia es automatica y no se puede evitar. Los minijuegos sirven para recuperarte, "
            + "pero no cancelan este evento.</div></html>");
        Frame.labelify(message, 15);
        JButton acknowledge = new JButton("Registrar incidente");
        Frame.buttonify(acknowledge, 16);
        acknowledge.addActionListener(e -> closeAndSchedule());
        panel.add(message, BorderLayout.CENTER);
        panel.add(acknowledge, BorderLayout.SOUTH);
        dialog.setContentPane(panel);
        dialog.setSize(650, 350);
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
        onChange.run();
    }

    private void closeAndSchedule() {
        if (dialog != null) { dialog.dispose(); dialog = null; }
        onChange.run();
        if (running) scheduleNext();
    }
}
