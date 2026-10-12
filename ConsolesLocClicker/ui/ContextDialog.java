package ConsolesLocClicker.ui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Image;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Contexto narrativo de cada partida. Es una ventana independiente para que
 * la introduccion no ocupe una pestana de la interfaz principal.
 */
public final class ContextDialog {
    private ContextDialog() {}

    /** Muestra la situacion inicial y arranca el reloj cuando el jugador entra. */
    public static void show(JFrameOwner owner, Image icon, Runnable onStart) {
        JDialog dialog = new JDialog(owner.frame(), "Inicio del proyecto", Dialog.ModalityType.MODELESS);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setAlwaysOnTop(true);
        dialog.setIconImage(icon);

        JPanel content = new JPanel(new BorderLayout(12, 12));
        content.setBackground(Frame.bgColor);
        content.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Frame.textColor, 1),
                BorderFactory.createEmptyBorder(18, 22, 18, 22)));
        JLabel context = new JLabel("<html><div style='width:560px'>"
                + "<b>OPERACION: ESCALAR LA EMPRESA</b><br/><br/>"
                + "Tu equipo acaba de recibir un proyecto critico. Debes producir LOC, "
                + "invertir en calidad y mantener la estabilidad mientras los defectos "
                + "amenazan la entrega.<br/><br/>"
                + "Al alcanzar CMMI Nivel 5 no termina la partida: la empresa entra en "
                + "una nueva fase, con mas equipos, mayor capacidad y problemas mas dificiles.<br/><br/>"
                + "Cada decision representa una parte del trabajo de ingenieria de software. "
                + "Crece rapido, pero no olvides construir un proceso sostenible.</div></html>");
        Frame.labelify(context, 15);
        context.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JButton start = new JButton("Comenzar proyecto");
        Frame.buttonify(start, 16);
        start.addActionListener(e -> {
            dialog.dispose();
            onStart.run();
        });
        content.add(context, BorderLayout.CENTER);
        content.add(start, BorderLayout.SOUTH);
        dialog.setContentPane(content);
        dialog.pack();
        dialog.setLocationRelativeTo(owner.frame());
        dialog.setVisible(true);
    }

    /** Minima interfaz para evitar acoplar el dialogo a toda la ventana del juego. */
    public interface JFrameOwner {
        java.awt.Frame frame();
    }
}
