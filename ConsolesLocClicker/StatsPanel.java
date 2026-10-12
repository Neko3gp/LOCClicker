package ConsolesLocClicker;

import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** Métricas al estilo PSP: tamaño (LOC), tiempo, productividad y densidad de defectos. */
public class StatsPanel extends JPanel {
    private final Stats stats;
    private final LocTimer timer;
    private final JLabel[] rows = new JLabel[14];

    public StatsPanel(Stats stats, LocTimer timer) {
        this.stats = stats;
        this.timer = timer;
        setBackground(Frame.bgColor);
        setLayout(new GridLayout(rows.length, 1));
        for (int i = 0; i < rows.length; i++) {
            rows[i] = new JLabel("", SwingConstants.LEFT);
            Frame.labelify(rows[i], 15);
            rows[i].setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 20, 0, 0));
            add(rows[i]);
        }
        refresh();
    }

    public void refresh() {
        int r = 0;
        rows[r++].setText("== MÉTRICAS DEL PROYECTO (PSP) ==");
        rows[r++].setText(String.format("LOC disponibles:           %,d", stats.getLoc()));
        rows[r++].setText(String.format("LOC totales producidas:    %,d", stats.getLocMade()));
        rows[r++].setText(String.format("LOC invertidas en proceso: %,d", stats.getLocSpent()));
        rows[r++].setText(String.format("Clics (raw):               %,d", stats.getRawClicks()));
        for (Item p : stats.getProducers()) {
            rows[r++].setText(p.getName() + ": " + p.getAmount());
        }
        rows[r++].setText(String.format("%s: %d/%d", stats.getKeyboard().getName(), stats.getKeyboard().getAmount(), GameConfig.CLICK_MAX));
        int secs = Math.max(1, timer.getTotalSeconds());
        rows[r++].setText("Tiempo:  " + timer + "   |   Productividad: " + (int) (stats.getLocMade() * 3600.0 / secs) + " LOC/hora");
        rows[r++].setText(String.format("Defectos: %d detectados, %d parchados  |  %.1f def/KLOC",
                stats.getDefectsFound(), stats.getDefectsPatched(), stats.getDefectDensity()));
    }
}
