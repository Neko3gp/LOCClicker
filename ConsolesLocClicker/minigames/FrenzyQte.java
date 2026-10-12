package ConsolesLocClicker.minigames;

import ConsolesLocClicker.core.GameConfig;
import ConsolesLocClicker.core.Stats;
import ConsolesLocClicker.ui.Frame;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Random;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

/** QTE de frenesí: tres círculos amarillos y un círculo dorado final. */
public final class FrenzyQte {
    private FrenzyQte() {}

    public static void show(JFrame owner, Stats stats, Runnable onChange) {
        JDialog dialog = new JDialog(owner, "Oportunidad de frenesí", false);
        dialog.setUndecorated(true);
        dialog.setAlwaysOnTop(true);
        QtePanel qte = new QtePanel();
        dialog.setContentPane(qte);
        dialog.setSize(new Dimension(280, 280));
        Point origin = owner.getLocationOnScreen();
        Random random = new Random();
        int x = origin.x + 20 + random.nextInt(Math.max(1, owner.getWidth() - 300));
        int y = origin.y + 40 + random.nextInt(Math.max(1, owner.getHeight() - 320));
        dialog.setLocation(x, y);

        Timer timer = new Timer(40, null);
        long[] roundStarted = {System.currentTimeMillis()};
        timer.addActionListener(e -> {
            long elapsed = System.currentTimeMillis() - roundStarted[0];
            qte.setElapsed(elapsed);
            if (elapsed >= GameConfig.FRENZY_QTE_MS) fail(dialog, timer, stats, onChange);
        });
        qte.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                if (!qte.isInsideCircle(e.getX(), e.getY())) {
                    fail(dialog, timer, stats, onChange);
                    return;
                }
                if (qte.advanceRound()) {
                    timer.stop();
                    stats.activateFrenzy();
                    dialog.dispose();
                    onChange.run();
                } else {
                    roundStarted[0] = System.currentTimeMillis();
                    qte.setElapsed(0);
                }
            }
        });
        dialog.setVisible(true);
        timer.start();
    }

    private static void fail(JDialog dialog, Timer timer, Stats stats, Runnable onChange) {
        timer.stop();
        stats.missFrenzy();
        dialog.dispose();
        onChange.run();
    }

    private static final class QtePanel extends JPanel {
        private long elapsed;
        private int round;
        private int targetX;
        private int targetY;
        private final Random random = new Random();

        QtePanel() {
            setPreferredSize(new Dimension(280, 280));
            setBackground(new Color(18, 20, 17));
            moveTarget();
        }

        void setElapsed(long elapsed) { this.elapsed = elapsed; repaint(); }
        int radius() {
            double ratio = Math.min(1.0, elapsed / (double) GameConfig.FRENZY_QTE_MS);
            return (int) Math.round(74 - ratio * 52);
        }
        boolean isInsideCircle(int x, int y) {
            int dx = x - targetX;
            int dy = y - targetY;
            return dx * dx + dy * dy <= radius() * radius();
        }
        boolean advanceRound() {
            if (round == GameConfig.FRENZY_QTE_ROUNDS) return true;
            round++;
            moveTarget();
            repaint();
            return false;
        }
        private void moveTarget() {
            targetX = 60 + random.nextInt(160);
            targetY = 70 + random.nextInt(130);
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int radius = radius();
            boolean finalCircle = round == GameConfig.FRENZY_QTE_ROUNDS;
            g.setColor(finalCircle ? new Color(255, 193, 7) : new Color(255, 224, 92));
            g.fillOval(targetX - radius, targetY - radius, radius * 2, radius * 2);
            g.setColor(finalCircle ? new Color(255, 245, 180) : new Color(255, 246, 180));
            g.drawOval(targetX - radius, targetY - radius, radius * 2, radius * 2);
            g.setColor(Color.BLACK);
            g.setFont(Frame.font(14, true));
            String label = finalCircle ? "FRENESI x2" : "CLIC";
            g.drawString(label, targetX - g.getFontMetrics().stringWidth(label) / 2, targetY + 5);
            g.setColor(Color.WHITE);
            g.setFont(Frame.font(12, true));
            String hint = finalCircle ? "ULTIMO CIRCULO" : "CIRCULO " + (round + 1) + "/" + GameConfig.FRENZY_QTE_ROUNDS;
            g.drawString(hint, 10, 22);
            g.drawString(String.format("%.1f s", (GameConfig.FRENZY_QTE_MS - elapsed) / 1000.0), 225, 22);
        }
    }
}
