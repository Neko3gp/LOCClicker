package ConsolesLocClicker.ui;

import ConsolesLocClicker.audio.SoundManager;
import ConsolesLocClicker.core.GameConfig;
import ConsolesLocClicker.core.Item;
import ConsolesLocClicker.core.Stats;

import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Tienda separada para retencion, productividad y herramientas de trabajo. */
public class StaffPanel extends JPanel {
    private final Stats stats;
    private final JButton[] upgradeButtons = new JButton[3];
    private final JLabel employees = new JLabel();

    public StaffPanel(Stats stats, Runnable onPurchase) {
        this.stats = stats;
        setBackground(Frame.bgColor);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        setLayout(new GridLayout(0, 1, 0, 8));
        Frame.labelify(employees, 17);
        add(employees);
        Item[] upgrades = {stats.getComputer(), stats.getTraining(), stats.getRetention()};
        for (int i = 0; i < upgrades.length; i++) {
            final int index = i;
            JButton button = new JButton();
            Frame.buttonify(button, 14);
            button.setToolTipText("Mejora permanente para la fase actual");
            button.addActionListener(e -> {
                boolean bought;
                if (index == 0) bought = stats.buyComputer();
                else if (index == 1) bought = stats.buyTraining();
                else bought = stats.buyRetention();
                if (bought) {
                    SoundManager.playEquipmentUpgrade();
                }
                onPurchase.run();
            });
            upgradeButtons[i] = button;
            add(button);
        }
        refresh();
    }

    public void refresh() {
        employees.setText("== EQUIPO Y RETENCION ==  Empleados activos: " + stats.getEmployeeCount());
        Item[] upgrades = {stats.getComputer(), stats.getTraining(), stats.getRetention()};
        for (int i = 0; i < upgrades.length; i++) {
            Item item = upgrades[i];
            upgradeButtons[i].setText(String.format("<html><b>%s</b><br/>%s &nbsp;|&nbsp; Costo: %,d LOC &nbsp;|&nbsp; Nivel: %d</html>",
                    item.getName(), effect(i), stats.getPrice(item), item.getAmount()));
            upgradeButtons[i].setForeground(stats.getLoc() >= stats.getPrice(item) ? Frame.textColor : Frame.dimColor);
        }
    }

    private String effect(int index) {
        if (index == 0) return "+" + GameConfig.COMPUTER_PRODUCTION_PERCENT + "% produccion, +"
                + GameConfig.COMPUTER_CLICK_POWER + " LOC/clic";
        if (index == 1) return "+" + GameConfig.TRAINING_PRODUCTION_PERCENT + "% produccion";
        return "reduce dano de rotacion en " + GameConfig.RETENTION_STABILITY_PROTECTION;
    }
}
