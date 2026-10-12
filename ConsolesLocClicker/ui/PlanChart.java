package ConsolesLocClicker.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JPanel;

/** Grafica compacta de valor ganado: LOC reales frente al plan acumulado. */
public class PlanChart extends JPanel {
    private int earned;
    private int planned;

    public PlanChart() {
        setPreferredSize(new Dimension(0, 112));
        setBackground(Frame.bgColor);
        setBorder(Frame.b);
    }

    public void setValues(int earned, int planned) {
        this.earned = earned;
        this.planned = planned;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int width = getWidth();
        int barWidth = Math.max(1, width - 210);
        int maximum = Math.max(1, Math.max(earned, planned));
        int earnedWidth = earned * barWidth / maximum;
        int plannedWidth = planned * barWidth / maximum;

        g.setFont(Frame.font(13, true));
        g.setColor(Frame.textColor);
        g.drawString("VALOR GANADO: AVANCE CONTRA PLAN", 12, 20);
        g.setColor(Frame.dimColor);
        g.drawString("Plan", 12, 52);
        g.drawString("Real", 12, 87);
        g.setColor(new Color(45, 65, 50));
        g.fillRect(70, 39, barWidth, 18);
        g.fillRect(70, 74, barWidth, 18);
        g.setColor(new Color(255, 195, 70));
        g.fillRect(70, 39, plannedWidth, 18);
        g.setColor(Frame.textColor);
        g.fillRect(70, 74, earnedWidth, 18);
        g.drawString(String.format("%,d LOC", planned), 80 + barWidth, 53);
        g.drawString(String.format("%,d LOC", earned), 80 + barWidth, 88);
    }
}
