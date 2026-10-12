import ConsolesLocClicker.core.Stats;
import ConsolesLocClicker.core.GameConfig;
import ConsolesLocClicker.events.DefectManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;

/** Prueba de humo con interfaz real (requiere pantalla o Xvfb). Hace clics con Robot y toma capturas. */
public class UiSmokeTest {
    static Robot robot;
    static void shot(String name) throws Exception {
        BufferedImage im = robot.createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));
        ImageIO.write(im, "png", new File("/tmp/shots/" + name + ".png"));
    }
    static void find(Container c, List<JButton> out) {
        for (Component k : c.getComponents()) {
            if (k instanceof JButton) out.add((JButton) k);
            if (k instanceof Container) find((Container) k, out);
        }
    }
    static List<JButton> buttons(Window w) { List<JButton> l = new ArrayList<>(); find(w, l); return l; }
    static void click(JButton b) throws Exception {
        Point p = b.getLocationOnScreen();
        robot.mouseMove(p.x + b.getWidth() / 2, p.y + b.getHeight() / 2);
        robot.mousePress(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);
        robot.delay(40);
    }
    static JFrame mainFrame() {
        for (Window w : Window.getWindows()) if (w instanceof JFrame && w.isVisible()) return (JFrame) w;
        return null;
    }
    static void check(boolean cond, String msg) { System.out.println((cond ? "PASS  " : "FAIL  ") + msg); if (!cond) failed = true; }
    static boolean failed = false;

    public static void main(String[] a) throws Exception {
        new File("/tmp/shots").mkdirs();
        new File("bitacora_defectos_psp.csv").delete();
        robot = new Robot();
        SwingUtilities.invokeAndWait(() -> ConsolesLocClicker.ui.Frame.main(new String[0]));
        Thread.sleep(1500);
        JFrame f = mainFrame();
        check(f != null && f.isVisible(), "ventana principal visible");
        JButton start = null;
        for (JButton b : buttons(f)) if (b.getText().contains("Comenzar proyecto")) start = b;
        check(start != null, "contexto inicial visible");
        click(start);
        Thread.sleep(300);

        // 1) clics reales en el icono
        JButton clicker = null;
        for (JButton b : buttons(f)) if (b.getIcon() != null) clicker = b;
        for (int i = 0; i < 120; i++) click(clicker);
        Thread.sleep(300);
        Field sf = ConsolesLocClicker.ui.Frame.class.getDeclaredField("stats"); sf.setAccessible(true);
        ConsolesLocClicker.ui.Frame frameObj = null;
        // obtener la instancia de Frame a través del KeyListener registrado en el JTabbedPane
        JTabbedPane pane = null;
        for (Component k : f.getContentPane().getComponents()) if (k instanceof JTabbedPane) pane = (JTabbedPane) k;
        frameObj = (ConsolesLocClicker.ui.Frame) pane.getKeyListeners()[0];
        Stats stats = (Stats) sf.get(frameObj);
        check(stats.getRawClicks() == 120, "120 clics registrados (" + stats.getRawClicks() + ")");
        int expectedLoc = 120 + (120 / GameConfig.BONUS_CLICKS) * GameConfig.BONUS_LOC;
        check(stats.getLoc() == expectedLoc, expectedLoc + " LOC tras 120 clics (" + stats.getLoc() + ")");
        shot("01_juego");

        // 2) comprar un Becario desde la tienda
        JButton becario = null;
        for (JButton b : buttons(f)) if (b.getText().contains("Becario")) becario = b;
        click(becario);
        check(stats.getProducers()[0].getAmount() == 1, "compra de Becario por clic en la tienda");
        check(stats.getLoc() == expectedLoc - 15, "LOC descontadas tras comprar Becario: " + (expectedLoc - 15) + " (" + stats.getLoc() + ")");

        // 3) comprar teclado y verificar poder de clic
        JButton teclado = null;
        for (JButton b : buttons(f)) if (b.getText().contains("Teclado")) teclado = b;
        click(teclado);
        check(stats.getClickPower() == 2, "Teclado Mecánico sube el clic a +2 (" + stats.getClickPower() + ")");

        // 4) defecto aleatorio: esperar el primero (a los ~20 s) y NO parchar
        System.out.println("... esperando el primer defecto (~20 s) ...");
        JDialog dlg = null;
        for (int i = 0; i < 300 && dlg == null; i++) {
            Thread.sleep(100);
            for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isVisible()) dlg = (JDialog) w;
        }
        check(dlg != null, "apareció el diálogo de defecto: " + (dlg != null ? dlg.getTitle() : "-"));
        Thread.sleep(500);
        shot("02_defecto");
        JButton analizar = null;
        for (JButton b : buttons(dlg)) if (b.getText().startsWith("Iniciar:")) analizar = b;
        check(analizar != null, "el defecto asigna un minijuego obligatorio");
        click(analizar);
        // el jugador puede seguir haciendo clic con el diálogo abierto (no modal)
        int antes = stats.getRawClicks();
        click(clicker);
        check(stats.getRawClicks() == antes + 1, "no modal: se puede seguir haciendo clic con el defecto abierto");
        Thread.sleep(10500);
        boolean cerrado = true;
        for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isVisible()) cerrado = false;
        check(cerrado, "el diálogo se cierra solo a los 5 s");
        check(stats.getDefectsFound() == 1 && stats.getDefectsPatched() == 0, "defecto contado como NO parchado");
        String logTxt = new String(java.nio.file.Files.readAllBytes(new File("bitacora_defectos_psp.csv").toPath()), "UTF-8");
        check(logTxt.contains("Sin parchar"), "la bitácora registra el defecto como 'Sin parchar' con su penalización");
        check(!logTxt.contains("-0 LOC"), "la penalización del Tipo 20 no es 0 LOC");
        shot("03_penalizacion");

        // 5) forzar otro defecto y resolverlo seleccionando la línea correcta
        Field df = ConsolesLocClicker.ui.Frame.class.getDeclaredField("defects"); df.setAccessible(true);
        DefectManager dm = (DefectManager) df.get(frameObj);
        SwingUtilities.invokeAndWait(dm::triggerNow);
        Thread.sleep(600);
        JButton analizarSegundo = null;
        for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isVisible())
            for (JButton b : buttons(w)) if (b.getText().startsWith("Iniciar:")) analizarSegundo = b;
        check(analizarSegundo != null, "segundo defecto abre el minijuego de código");
        click(analizarSegundo);
        for (int i = 0; i < 5 && stats.getDefectsPatched() == 0; i++) {
            for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isVisible()) {
                List<JButton> lines = new ArrayList<>();
                for (JButton b : buttons(w)) if (b.getText().startsWith("line ")) lines.add(b);
                if (i < lines.size()) click(lines.get(i));
                else for (JButton b : buttons(w)) if (b.getText().equals("FALLA")) click(b);
            }
        }
        Thread.sleep(300);
        check(stats.getDefectsFound() == 2 && stats.getDefectsPatched() == 1, "segundo defecto parchado a tiempo (" + stats.getDefectsPatched() + "/2)");

        // 6) victoria: dar LOC y comprar CMMI 5
        stats.setLoc(100000);
        JButton meta = null;
        for (JButton b : buttons(f)) if (b.getText().contains("Certificación")) meta = b;
        click(meta);
        Thread.sleep(800);
        JButton escalar = null;
        for (Window w : Window.getWindows()) if (w instanceof JDialog && w.isVisible()) {
            for (JButton b : buttons(w)) if (b.getText().contains("Escalar empresa")) escalar = b;
        }
        check(escalar != null, "apareció la transición a la fase 2");
        click(escalar);
        check(stats.getPhase() == 2 && !stats.isPhaseComplete(), "la empresa continúa en la fase 2");
        shot("04_victoria");

        // 7) bitácora PSP
        File log = new File("bitacora_defectos_psp.csv");
        check(log.exists(), "se generó bitacora_defectos_psp.csv");
        System.out.println("--- bitácora ---");
        System.out.println(new String(java.nio.file.Files.readAllBytes(log.toPath()), "UTF-8"));
        System.out.println(failed ? "RESULTADO: HAY FALLAS" : "RESULTADO: TODO OK");
        System.exit(failed ? 1 : 0);
    }
}
