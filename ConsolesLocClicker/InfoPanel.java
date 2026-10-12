package ConsolesLocClicker;

import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class InfoPanel extends JPanel {
    public InfoPanel() {
        JLabel info = new JLabel("<html><div style='width:600px'><b>LOC Clicker: Ingeniería Edition</b><br/><br/>"
            + "Produce Líneas de Código (LOC), contrata procesos de calidad y llega a CMMI Nivel 5. "
            + "Cuidado con los defectos: si no los parchas a tiempo, pierdes LOC o la producción se detiene.<br/><br/>"
            + "Equipo (roles TSP): Nicolás (Líder Técnico), Samuel (UI), Carlos (Lógica de Negocio), José (Gestor de Calidad).<br/><br/>"
            + "Basado en SwingLocClicker v1.1 de Console (2022), github.com/Consolel/SwingLocClicker.</div></html>");
        Frame.labelify(info, 16);
        info.setVerticalAlignment(JLabel.TOP);
        info.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 0, 20));
        setBackground(Frame.bgColor);
        setLayout(new BorderLayout());
        add(info, BorderLayout.CENTER);
    }
}
