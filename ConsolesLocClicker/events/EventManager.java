package ConsolesLocClicker.events;

import ConsolesLocClicker.core.GameConfig;
import ConsolesLocClicker.core.Stats;
import ConsolesLocClicker.minigames.StabilityMinigame;
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
 * Agenda mini eventos de la fase de crecimiento. Cada evento es corto y
 * reemplazable: para agregar uno nuevo basta extender showEvent y su enum.
 */
public class EventManager {
    private enum EventType { HACKATHON, CRISIS }

    private final JFrame owner;
    private final Stats stats;
    private final Runnable onChange;
    private final Random random = new Random();
    private Timer schedule;
    private JDialog dialog;
    private boolean running;

    public EventManager(JFrame owner, Stats stats, Runnable onChange) {
        this.owner = owner;
        this.stats = stats;
        this.onChange = onChange;
    }

    public void start() {
        running = true;
        scheduleNext();
    }

    public void stop() {
        running = false;
        if (schedule != null) schedule.stop();
        if (dialog != null) { dialog.dispose(); dialog = null; }
    }

    private void scheduleNext() {
        if (schedule != null) schedule.stop();
        int delay = GameConfig.EVENT_MIN_DELAY_MS
                + random.nextInt(GameConfig.EVENT_MAX_DELAY_MS - GameConfig.EVENT_MIN_DELAY_MS + 1);
        schedule = new Timer(delay, e -> showEvent());
        schedule.setRepeats(false);
        schedule.start();
    }

    private void showEvent() {
        if (!running || dialog != null || stats.getPhase() < 2 || stats.isPhaseComplete()) {
            if (running) scheduleNext();
            return;
        }
        EventType type = random.nextBoolean() ? EventType.HACKATHON : EventType.CRISIS;
        dialog = new JDialog(owner, type == EventType.HACKATHON ? "Evento: Hackathon" : "Evento: Crisis", false);
        dialog.setAlwaysOnTop(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) {
                if (running && dialog != null) {
                    dialog = null;
                    scheduleNext();
                }
            }
        });
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Frame.bgColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(type == EventType.HACKATHON ? Frame.textColor : Frame.warnColor, 2),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        int expense = stats.getUnexpectedExpense();
        String text = type == EventType.HACKATHON
                ? "El equipo entro en modo hackathon. Activa el impulso para ganar LOC y estabilidad."
            : "Una crisis de integracion amenaza la entrega. Paga " + expense
                + " LOC de mitigacion y resuelve la prueba de estabilidad.";
        JLabel message = new JLabel("<html><div style='width:360px'>" + text + "</div></html>");
        Frame.labelify(message, 14);
        JButton action = new JButton(type == EventType.HACKATHON ? "Activar impulso" : "Mitigar crisis");
        Frame.buttonify(action, 16);
        action.addActionListener(e -> {
            if (type == EventType.HACKATHON) {
                stats.addEventBonus();
                closeAndSchedule();
            } else {
                if (!stats.payUnexpectedExpense()) {
                    message.setText("<html><div style='width:360px'>No hay LOC suficientes para pagar la mitigacion. "
                            + "La crisis permanece activa.</div></html>");
                    stats.damageStability(10);
                    onChange.run();
                    return;
                }
                dialog.dispose();
                dialog = null;
                StabilityMinigame.show(owner, stats, onChange, () -> {
                    stats.completeEvent();
                    onChange.run();
                    scheduleNext();
                });
            }
        });
        panel.add(message, BorderLayout.CENTER);
        panel.add(action, BorderLayout.SOUTH);
        dialog.setContentPane(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    private void closeAndSchedule() {
        if (dialog != null) { dialog.dispose(); dialog = null; }
        onChange.run();
        scheduleNext();
    }
}
