package ConsolesLocClicker.ui;

import ConsolesLocClicker.core.Stats;
import ConsolesLocClicker.persistence.SaveManager;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.nio.file.Path;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** Pestana para guardar y cargar campañas identificadas por empresa. */
public class SavePanel extends JPanel {
    private final Stats stats;
    private final Runnable onChange;
    private final JTextField companyName = new JTextField("Mi_empresa");
    private final JComboBox<String> savedGames = new JComboBox<>();
    private final JLabel status = new JLabel(" ");

    public SavePanel(Stats stats, Runnable onChange) {
        this.stats = stats;
        this.onChange = onChange;
        setBackground(Frame.bgColor);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        setLayout(new BorderLayout(0, 12));

        JLabel title = new JLabel("== ARCHIVO DE CAMPAÑAS ==");
        Frame.labelify(title, 18);
        JPanel form = new JPanel(new GridLayout(0, 1, 0, 8));
        form.setBackground(Frame.bgColor);
        JLabel hint = new JLabel("Nombre de empresa para guardar en partidas/");
        Frame.labelify(hint, 14);
        companyName.setFont(Frame.font(14, true));
        companyName.setForeground(Frame.textColor);
        companyName.setBackground(Frame.bgColor);
        JButton save = new JButton("Guardar partida");
        Frame.buttonify(save, 15);
        save.addActionListener(e -> save());
        JButton load = new JButton("Cargar seleccionada");
        Frame.buttonify(load, 15);
        load.addActionListener(e -> load());
        JButton refresh = new JButton("Actualizar lista");
        Frame.buttonify(refresh, 15);
        refresh.addActionListener(e -> refreshList());
        savedGames.setFont(Frame.font(14, true));
        savedGames.setForeground(Frame.textColor);
        savedGames.setBackground(Frame.bgColor);
        form.add(hint);
        form.add(companyName);
        form.add(save);
        form.add(new JLabel("Campañas disponibles:"));
        form.add(savedGames);
        form.add(load);
        form.add(refresh);
        add(title, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        Frame.labelify(status, 13);
        add(status, BorderLayout.SOUTH);
        refreshList();
    }

    private void save() {
        try {
            Path file = SaveManager.save(companyName.getText(), stats);
            status.setText("Guardada: " + file);
            refreshList();
        } catch (IOException e) {
            status.setText("No se pudo guardar: " + e.getMessage());
        }
    }

    private void load() {
        Object selected = savedGames.getSelectedItem();
        if (selected == null) {
            status.setText("Selecciona una campaña primero.");
            return;
        }
        try {
            String name = selected.toString();
            SaveManager.load(name, stats);
            companyName.setText(name);
            onChange.run();
            status.setText("Cargada: " + SaveManager.DIRECTORY.resolve(name + ".properties"));
        } catch (IOException e) {
            status.setText("No se pudo cargar: " + e.getMessage());
        }
    }

    private void refreshList() {
        savedGames.removeAllItems();
        try {
            for (String name : SaveManager.list()) savedGames.addItem(name);
        } catch (IOException e) {
            status.setText("No se pudo leer partidas/: " + e.getMessage());
        }
    }
}
