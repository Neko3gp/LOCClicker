package ConsolesLocClicker;

/** Cronómetro de la partida (se mantiene del repo original, solo se agregó getTotalSeconds). */
public class LocTimer {
    private int days, hours, minutes, seconds;
    public LocTimer() {}
    public void tick() {
        seconds++;
        if (seconds >= 60) { minutes++; seconds = 0; }
        if (minutes >= 60) { hours++; minutes = 0; }
        if (hours >= 24) { days++; hours = 0; }
    }
    public int getTotalSeconds() {
        return ((days * 24 + hours) * 60 + minutes) * 60 + seconds;
    }
    @Override
    public String toString() {
        return String.format("%02d:%02d", minutes + hours * 60 + days * 1440, seconds);
    }
}
