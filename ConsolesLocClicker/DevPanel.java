package ConsolesLocClicker;

import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/** Panel oculto para pruebas (José): Ctrl+Enter estando en la pestaña Info. */
public class DevPanel extends JPanel {
    public DevPanel(Stats stats, DefectManager defects) {
        setLayout(new GridLayout(0, 3));
        SpinnerNumberModel model = new SpinnerNumberModel(0, 0, 2147483647, 1);
        JSpinner spinner = new JSpinner(model);
        spinner.setFont(new Font(Font.MONOSPACED, Font.BOLD, 30));

        List<String> names = new ArrayList<>();
        names.add("LOC"); names.add("LOC totales"); names.add("LOC gastadas"); names.add("Clics");
        for (Item p : stats.getProducers()) names.add(p.getName());
        names.add(stats.getKeyboard().getName());
        JList<String> values = new JList<>(names.toArray(new String[0]));
        values.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));

        JButton setter = new JButton("Set Value");
        setter.addActionListener(e -> {
            int v = (int) model.getValue();
            int sel = values.getSelectedIndex();
            if (sel == 0) stats.setLoc(v);
            else if (sel == 1) stats.setLocMade(v);
            else if (sel == 2) stats.setLocSpent(v);
            else if (sel == 3) stats.setRawClicks(v);
            else if (sel >= 4 && sel < 4 + stats.getProducers().length) stats.getProducers()[sel - 4].setAmount(v);
            else if (sel == 4 + stats.getProducers().length) stats.getKeyboard().setAmount(v);
        });
        JButton forceDefect = new JButton("Forzar defecto ahora");
        forceDefect.addActionListener(e -> defects.triggerNow());
        JPanel right = new JPanel(new GridLayout(0, 1));
        right.add(setter);
        right.add(forceDefect);
        add(values);
        add(spinner);
        add(right);
    }
}
