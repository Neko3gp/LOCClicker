package ConsolesLocClicker;

import java.awt.BorderLayout;
import java.awt.Point;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.JFrame;

/**
 * Evento de interrupción: aparece un defecto PSP (Tipo 20 Sintaxis o Tipo 90 Sistema).
 * Si no se hace clic en "Parchear" antes del límite, hay penalización.
 *
 * ¿Por qué no rompe el hilo principal?
 *  - Todo usa javax.swing.Timer, que dispara SIEMPRE en el Event Dispatch Thread (EDT),
 *    el mismo hilo de Swing. No hay hilos nuevos ni sincronización.
 *  - El diálogo es NO modal (setModal(false)): el jugador puede seguir haciendo clic mientras decide.
 *  - Nunca se usa Thread.sleep().
 */
public class DefectManager {
    private enum Defect {
        SYNTAX(20, "Sintaxis", "Compilación", "Error de compilación: falta un ';' en la línea %d."),
        SYSTEM(90, "Sistema", "Pruebas de sistema", "Falla de integración: el servidor de build dejó de responder.");
        final int code; final String label, phaseRemoved, text;
        Defect(int code, String label, String phaseRemoved, String text) {
            this.code = code; this.label = label; this.phaseRemoved = phaseRemoved; this.text = text;
        }
    }

    private final JFrame owner;
    private final Stats stats;
    private final Runnable onChange;   // para que la UI se refresque tras un defecto
    private final Random rnd = new Random();
    private Timer spawnTimer, countdownTimer;
    private JDialog dialog;
    private Defect current;
    private String currentText;
    private long shownAt;
    private boolean running;

    public DefectManager(JFrame owner, Stats stats, Runnable onChange) {
        this.owner = owner;
        this.stats = stats;
        this.onChange = onChange;
    }

    public void start() {
        running = true;
        schedule(GameConfig.FIRST_DEFECT_MS);
    }

    public void stop() {
        running = false;
        if (spawnTimer != null) spawnTimer.stop();
        if (countdownTimer != null) countdownTimer.stop();
        if (dialog != null) { dialog.dispose(); dialog = null; }
    }

    /** Para pruebas (DevPanel): lanza un defecto ya mismo. */
    public void triggerNow() {
        if (running && dialog == null) show();
    }

    private void schedule(int delayMs) {
        if (spawnTimer != null) spawnTimer.stop();
        spawnTimer = new Timer(delayMs, e -> show());
        spawnTimer.setRepeats(false);
        spawnTimer.start();
    }

    private void show() {
        if (!running || dialog != null || stats.isFinished()) return;
        if (spawnTimer != null) spawnTimer.stop();
        current = rnd.nextInt(100) < 60 ? Defect.SYNTAX : Defect.SYSTEM;
        currentText = String.format(current.text, 10 + rnd.nextInt(990));
        String penalty = current == Defect.SYNTAX
                ? "Si no lo parchas pierdes el " + (int) (GameConfig.DEFECT20_LOSS_PCT * 100) + "% de tus LOC (o "
                  + GameConfig.DEFECT20_MIN_PRODUCTION_SECONDS + " s de producción, lo que sea mayor)."
                : "Si no lo parchas la producción se detiene " + GameConfig.DEFECT90_FREEZE_SECONDS + " s.";
        stats.defectFound();

        dialog = new JDialog(owner, "[!] Defecto Tipo " + current.code + " (" + current.label + ")", false);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); // solo se cierra parcheando
        dialog.setAlwaysOnTop(true);

        JPanel p = new JPanel(new BorderLayout(8, 10));
        p.setBackground(Frame.bgColor);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Frame.warnColor, 2),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        // JTextArea (no JLabel con HTML): calcula bien su tamaño preferido y nunca recorta el texto
        JTextArea msg = new JTextArea(currentText + "\n\n" + penalty, 6, 34);
        msg.setEditable(false);
        msg.setFocusable(false);
        msg.setLineWrap(true);
        msg.setWrapStyleWord(true);
        msg.setOpaque(false);
        msg.setForeground(Frame.warnColor);
        msg.setFont(Frame.font(14, true));
        JLabel countdown = new JLabel("", SwingConstants.CENTER);
        countdown.setForeground(Frame.textColor);
        countdown.setFont(Frame.font(16, true));
        JButton patch = new JButton("Parchear");
        Frame.buttonify(patch, 18);
        patch.addActionListener(e -> resolve(true));
        JPanel south = new JPanel(new BorderLayout(0, 6));
        south.setOpaque(false);
        south.add(countdown, BorderLayout.NORTH);
        south.add(patch, BorderLayout.CENTER);
        p.add(msg, BorderLayout.CENTER);
        p.add(south, BorderLayout.SOUTH);
        dialog.setContentPane(p);
        dialog.pack();
        // aparece en un lugar aleatorio dentro de la ventana del juego
        Point o = owner.getLocationOnScreen();
        int x = o.x + 20 + rnd.nextInt(Math.max(1, owner.getWidth() - dialog.getWidth() - 40));
        int y = o.y + 40 + rnd.nextInt(Math.max(1, owner.getHeight() - dialog.getHeight() - 80));
        dialog.setLocation(x, y);

        shownAt = System.currentTimeMillis();
        countdownTimer = new Timer(100, e -> {
            long left = GameConfig.DEFECT_DEADLINE_MS - (System.currentTimeMillis() - shownAt);
            if (left <= 0) resolve(false);
            else countdown.setText(String.format("Tiempo restante: %.1f s", left / 1000.0));
        });
        countdownTimer.start();
        dialog.setVisible(true);
    }

    private void resolve(boolean patched) {
        if (dialog == null) return;  // ya se resolvió (doble clic, etc.)
        countdownTimer.stop();
        double fixSeconds = Math.min(System.currentTimeMillis() - shownAt, GameConfig.DEFECT_DEADLINE_MS) / 1000.0;
        dialog.dispose();
        dialog = null;

        String phaseRemoved, result;
        if (patched) {
            stats.defectPatched();
            phaseRemoved = current.phaseRemoved;
            result = "Parchado";
        } else if (current == Defect.SYNTAX) {
            int lost = stats.loseLoc(GameConfig.DEFECT20_LOSS_PCT, GameConfig.DEFECT20_MIN_PRODUCTION_SECONDS);
            phaseRemoved = "Escapó a producción";
            result = "Sin parchar (-" + lost + " LOC)";
        } else {
            stats.freezeProduction(GameConfig.DEFECT90_FREEZE_SECONDS);
            phaseRemoved = "Escapó a producción";
            result = "Sin parchar (producción detenida " + GameConfig.DEFECT90_FREEZE_SECONDS + " s)";
        }
        DefectLog.record(current.code + " " + current.label, "Codificación", phaseRemoved, fixSeconds,
                currentText + " -> " + result);
        onChange.run();
        if (running) {
            schedule(GameConfig.DEFECT_MIN_DELAY_MS
                    + rnd.nextInt(GameConfig.DEFECT_MAX_DELAY_MS - GameConfig.DEFECT_MIN_DELAY_MS + 1));
        }
    }
}
