package ConsolesLocClicker.ui;

import ConsolesLocClicker.core.GameConfig;
import ConsolesLocClicker.core.Item;
import ConsolesLocClicker.core.Stats;

import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JPanel;

/** Tienda generada a partir de los datos de GameConfig: agregar una mejora = agregar una línea allá. */
public class StorePanel extends JPanel {
    private final Stats stats;
    private final JButton[] producerButtons;
    private final JButton keyboardButton;
    private final JButton qualityButton;

    public StorePanel(Stats stats, Runnable onPurchase) {
        this.stats = stats;
        setBackground(Frame.bgColor);
        setLayout(new GridLayout(0, 1, 0, 6));
        setBorder(Frame.b);

        Item[] producers = stats.getProducers();
        producerButtons = new JButton[producers.length];
        for (int i = 0; i < producers.length; i++) {
            final int index = i;
            JButton b = new JButton();
            Frame.buttonify(b, 13);
            b.setToolTipText(tooltip(producers[i].getDescription()));
            b.addActionListener(e -> { stats.buyProducer(index); onPurchase.run(); });
            producerButtons[i] = b;
            add(b);
        }
        keyboardButton = new JButton();
        Frame.buttonify(keyboardButton, 13);
        keyboardButton.setToolTipText(tooltip(stats.getKeyboard().getDescription()));
        keyboardButton.addActionListener(e -> { stats.buyKeyboard(); onPurchase.run(); });
        add(keyboardButton);
        qualityButton = new JButton();
        Frame.buttonify(qualityButton, 13);
        qualityButton.setToolTipText(tooltip(stats.getQuality().getDescription()));
        qualityButton.addActionListener(e -> { stats.buyQuality(); onPurchase.run(); });
        add(qualityButton);
        refresh();
    }

    private static String tooltip(String text) {
        return "<html><div style='width:260px'>" + text + "</div></html>";
    }

    public void refresh() {
        Item[] producers = stats.getProducers();
        for (int i = 0; i < producers.length; i++) {
            Item it = producers[i];
            String goal = (i == GameConfig.WIN_ITEM) ? " &nbsp;<b>[META]</b>" : "";
            producerButtons[i].setText(String.format(
                "<html><b>%s</b>%s<br/>+%,d LOC/s &nbsp;|&nbsp; Costo: %,d LOC &nbsp;|&nbsp; Tienes: %d</html>",
                it.getName(), goal, it.getLocPerSecond(), stats.getPrice(it), it.getAmount()));
            producerButtons[i].setForeground(stats.getLoc() >= stats.getPrice(it) ? Frame.textColor : Frame.dimColor);
        }
        Item k = stats.getKeyboard();
        boolean max = k.getAmount() >= GameConfig.CLICK_MAX;
        keyboardButton.setText(max
            ? "<html><b>" + k.getName() + "</b><br/>NIVEL MÁXIMO (" + k.getAmount() + "/" + GameConfig.CLICK_MAX + ")</html>"
            : String.format("<html><b>%s</b><br/>+%d LOC/clic &nbsp;|&nbsp; Costo: %,d LOC &nbsp;|&nbsp; Nivel: %d/%d</html>",
                k.getName(), GameConfig.CLICK_POWER_PER_LEVEL, stats.getPrice(k), k.getAmount(), GameConfig.CLICK_MAX));
        keyboardButton.setForeground(!max && stats.getLoc() >= stats.getPrice(k) ? Frame.textColor : Frame.dimColor);
        Item q = stats.getQuality();
        boolean qualityMax = q.getAmount() >= GameConfig.QUALITY_MAX;
        qualityButton.setText(qualityMax
            ? "<html><b>" + q.getName() + "</b><br/>NIVEL MAXIMO (" + q.getAmount() + "/" + GameConfig.QUALITY_MAX + ")</html>"
            : String.format("<html><b>%s</b><br/>Defectos: -%d%% &nbsp;|&nbsp; Costo: %,d LOC &nbsp;|&nbsp; Nivel: %d/%d</html>",
                q.getName(), GameConfig.QUALITY_DEFECT_REDUCTION_PERCENT, stats.getPrice(q), q.getAmount(), GameConfig.QUALITY_MAX));
        qualityButton.setForeground(!qualityMax && stats.getLoc() >= stats.getPrice(q) ? Frame.textColor : Frame.dimColor);
    }
}
