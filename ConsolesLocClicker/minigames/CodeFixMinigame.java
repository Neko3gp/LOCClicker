package ConsolesLocClicker.minigames;

import ConsolesLocClicker.core.GameConfig;
import ConsolesLocClicker.core.Stats;
import ConsolesLocClicker.ui.Frame;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Reto obligatorio para resolver defectos: seleccionar la linea de codigo
 * defectuosa entre varias lineas plausibles antes de que expire el build.
 */
public final class CodeFixMinigame {
    private static final String[] CODE = {
        "int loc = stats.getLoc();",
        "if (loc < 0) {",
        "    registrarCambio();",
        "return calcularProductividad(loc);",
        "logger.info(\"build estable\");"
    };

    private CodeFixMinigame() {}

    public static void show(JFrame owner, Stats stats, String defectDescription, int errorLine,
                            Runnable onChange, Runnable onSuccess, Runnable onFailure) {
        JDialog dialog = new JDialog(owner, "Correccion asistida", false);
        dialog.setAlwaysOnTop(true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(Frame.bgColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Frame.warnColor, 2),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        JLabel header = new JLabel("<html><b>ERROR DETECTADO</b><br/>" + defectDescription
            + "<br/><br/><b>El compilador senala la linea " + errorLine
            + ". Selecciona exactamente esa linea para corregirla.</b>"
            + "<br/>Si fallas, baja la estabilidad y el build puede detenerse.</html>");
        Frame.labelify(header, 14);
        JPanel lines = new JPanel(new GridLayout(CODE.length, 1, 0, 4));
        lines.setOpaque(false);
        JLabel status = new JLabel("Tiempo restante: " + GameConfig.DEFECT_DEADLINE_MS / 1000 + " s", JLabel.CENTER);
        Frame.labelify(status, 14);
        for (int i = 0; i < CODE.length; i++) {
            int lineNumber = i == 2 ? errorLine : errorLine + (i < 2 ? -(i + 1) * 4 : (i - 1) * 5);
            final boolean correct = lineNumber == errorLine;
            JButton line = new JButton(String.format("line %03d   %s", lineNumber, CODE[i]));
            Frame.buttonify(line, 13);
            line.setHorizontalAlignment(JButton.LEFT);
            line.setPreferredSize(new Dimension(650, 42));
            line.addActionListener(e -> {
                if (correct) {
                    dialog.dispose();
                    onChange.run();
                    onSuccess.run();
                } else {
                    stats.damageStability(5);
                    status.setText("Linea valida, pero no era el defecto. Sigue buscando.");
                    onChange.run();
                }
            });
            lines.add(line);
        }
        Timer timeout = new Timer(100, null);
        long[] remaining = {GameConfig.DEFECT_DEADLINE_MS};
        timeout.addActionListener(e -> {
            remaining[0] -= 100;
            if (remaining[0] <= 0) {
                timeout.stop();
                dialog.dispose();
                onChange.run();
                onFailure.run();
            } else {
                status.setText(String.format("Tiempo restante: %.1f s", remaining[0] / 1000.0));
            }
        });
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent e) { timeout.stop(); }
        });
        panel.add(header, BorderLayout.NORTH);
        panel.add(lines, BorderLayout.CENTER);
        panel.add(status, BorderLayout.SOUTH);
        dialog.setContentPane(panel);
        dialog.setSize(760, 430);
        dialog.setLocationRelativeTo(owner);
        timeout.start();
        dialog.setVisible(true);
    }
}
