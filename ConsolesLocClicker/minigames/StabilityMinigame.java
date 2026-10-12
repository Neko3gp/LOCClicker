package ConsolesLocClicker.minigames;

import ConsolesLocClicker.core.GameConfig;
import ConsolesLocClicker.core.Stats;
import ConsolesLocClicker.ui.Frame;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Minijuego rapido de estabilidad: localizar el boton FALLA antes de que
 * termine el contador. Los equipos pueden reemplazarlo por otro reto sin
 * tocar DefectManager ni el reloj principal.
 */
public final class StabilityMinigame {
    private StabilityMinigame() {}

    public static void show(JFrame owner, Stats stats, Runnable onChange, Runnable onSuccess) {
        show(owner, stats, onChange, onSuccess, null);
    }

    public static void show(JFrame owner, Stats stats, Runnable onChange,
                            Runnable onSuccess, Runnable onFailure) {
        JDialog dialog = new JDialog(owner, "Prueba de estabilidad", false);
        dialog.setAlwaysOnTop(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Frame.bgColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Frame.textColor, 2),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        JLabel status = new JLabel("Encuentra FALLA antes de que se caiga el build", JLabel.CENTER);
        Frame.labelify(status, 14);
        JPanel grid = new JPanel(new GridLayout(3, 3, 5, 5));
        grid.setOpaque(false);
        int target = new Random().nextInt(9);
        for (int i = 0; i < 9; i++) {
            JButton button = new JButton(i == target ? "FALLA" : "OK");
            Frame.buttonify(button, 14);
            final boolean correct = i == target;
            button.addActionListener(e -> {
                if (correct) {
                    stats.recoverStability(GameConfig.STABILITY_MINIGAME_REWARD);
                    dialog.dispose();
                    onChange.run();
                    onSuccess.run();
                } else {
                    stats.damageStability(5);
                    status.setText("Ese componente estaba sano. Sigue buscando.");
                    onChange.run();
                }
            });
            grid.add(button);
        }
        Timer timeout = new Timer(GameConfig.DEFECT_DEADLINE_MS, e -> {
            if (!dialog.isVisible()) return;
            stats.damageStability(GameConfig.STABILITY_DEFECT_LOSS);
            dialog.dispose();
            onChange.run();
            if (onFailure != null) onFailure.run();
        });
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) { timeout.stop(); }
        });
        panel.add(status, BorderLayout.NORTH);
        panel.add(grid, BorderLayout.CENTER);
        dialog.setContentPane(panel);
        dialog.pack();
        Point location = owner.getLocationOnScreen();
        dialog.setLocation(location.x + 80, location.y + 80);
        timeout.start();
        dialog.setVisible(true);
    }
}
