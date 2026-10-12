package ConsolesLocClicker.ui;

import ConsolesLocClicker.core.GameConfig;
import ConsolesLocClicker.core.Item;
import ConsolesLocClicker.core.LocTimer;
import ConsolesLocClicker.core.Stats;

import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JProgressBar;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/** Métricas al estilo PSP: tamaño (LOC), tiempo, productividad y densidad de defectos. */
public class StatsPanel extends JPanel {
    private final Stats stats;
    private final LocTimer timer;
    private final JLabel[] rows = new JLabel[20];
    private final JProgressBar companyProgress;
    private final PlanChart planChart;

    public StatsPanel(Stats stats, LocTimer timer) {
        this.stats = stats;
        this.timer = timer;
        setBackground(Frame.bgColor);
        setLayout(new BorderLayout(0, 6));
        JPanel metrics = new JPanel(new GridLayout(rows.length, 1));
        metrics.setBackground(Frame.bgColor);
        for (int i = 0; i < rows.length; i++) {
            rows[i] = new JLabel("", SwingConstants.LEFT);
            Frame.labelify(rows[i], 15);
            rows[i].setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 20, 0, 0));
            metrics.add(rows[i]);
        }
        add(metrics, BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout(0, 5));
        footer.setBackground(Frame.bgColor);
        companyProgress = new JProgressBar(0, 100);
        companyProgress.setStringPainted(true);
        companyProgress.setForeground(Frame.textColor);
        companyProgress.setBackground(new Color(35, 45, 38));
        footer.add(companyProgress, BorderLayout.NORTH);
        planChart = new PlanChart();
        footer.add(planChart, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
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
        rows[r++].setText(String.format("Calidad: %d/%d niveles | Riesgo de defecto: %d%%",
            stats.getQuality().getAmount(), GameConfig.QUALITY_MAX, stats.getDefectChancePercent()));
        rows[r++].setText(String.format("Empresa nivel %d | %,d / %,d LOC para el siguiente nivel",
            stats.getCompanyLevel(), stats.getLocMade(), stats.getCompanyNextLevel()));
        rows[r++].setText(String.format("Bonus positivos: %d (+%,d LOC)", stats.getBonusesAwarded(), stats.getBonusLoc()));
        rows[r++].setText(String.format("Fase %d: %s", stats.getPhase(), stats.getPhaseName()));
        rows[r++].setText(String.format("Estabilidad: %d%% | Rendimiento: x%.2f | Deuda: %d",
            stats.getStability(), stats.getStabilityMultiplier(), stats.getTechnicalDebt()));
        rows[r++].setText("Mini eventos completados: " + stats.getEventsCompleted());
        int secs = Math.max(1, timer.getTotalSeconds());
        rows[r++].setText("Tiempo:  " + timer + "   |   Productividad: " + (int) (stats.getLocMade() * 3600.0 / secs) + " LOC/hora");
        rows[r++].setText(String.format("Defectos: %d detectados, %d parchados  |  %.1f def/KLOC",
                stats.getDefectsFound(), stats.getDefectsPatched(), stats.getDefectDensity()));
        companyProgress.setValue(stats.getCompanyProgressPercent());
        companyProgress.setString("Empresa nivel " + stats.getCompanyLevel());
        planChart.setValues(stats.getLocMade(), stats.getPlannedLoc(secs));
    }
}
