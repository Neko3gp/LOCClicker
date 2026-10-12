package ConsolesLocClicker;

/** Estado y reglas del juego. No contiene nada de Swing: se puede probar sin interfaz. */
public class Stats {
    private int loc, locMade, locSpent, rawClicks;
    private int frozenSeconds;
    private int defectsFound, defectsPatched;
    private final Item[] producers;
    private final Item keyboard;

    public Stats() {
        int n = GameConfig.NAMES.length;
        producers = new Item[n];
        for (int i = 0; i < n; i++) {
            producers[i] = new Item(GameConfig.NAMES[i], GameConfig.DESCRIPTIONS[i],
                    GameConfig.LOC_PER_SEC[i], GameConfig.BASE_PRICE[i], GameConfig.GROWTH);
        }
        keyboard = new Item(GameConfig.CLICK_NAME, GameConfig.CLICK_DESCRIPTION,
                0, GameConfig.CLICK_BASE_PRICE, GameConfig.CLICK_GROWTH);
    }

    /** Se llama cada segundo. Si hay un defecto sin parchear, la producción está detenida. */
    public void tick() {
        if (isFinished()) return;
        if (frozenSeconds > 0) { frozenSeconds--; return; }
        int produced = getBaseProduction();
        loc += produced;
        locMade += produced;
    }
    public void click() {
        if (isFinished()) return;
        int amount = getClickPower();
        loc += amount;
        locMade += amount;
        rawClicks++;
    }
    public boolean buyProducer(int index) {return buy(producers[index]);}
    public boolean buyKeyboard() {
        if (keyboard.getAmount() >= GameConfig.CLICK_MAX) return false;
        return buy(keyboard);
    }
    private boolean buy(Item item) {
        if (isFinished()) return false;
        int price = item.getPrice();
        if (loc < price) return false;
        loc -= price;
        locSpent += price;
        item.setAmount(item.getAmount() + 1);
        return true;
    }

    // ---------- Defectos ----------
    public void freezeProduction(int seconds) {frozenSeconds = Math.max(frozenSeconds, seconds);}
    /**
     * Pierde un porcentaje de las LOC actuales, pero como mínimo "minSeconds" de producción
     * (así la penalización también duele al inicio del juego). Nunca baja de 0. Devuelve lo perdido.
     */
    public int loseLoc(double pct, int minSeconds) {
        int lost = Math.max((int) (loc * pct), getBaseProduction() * minSeconds);
        lost = Math.min(lost, loc);
        loc -= lost;
        return lost;
    }
    public void defectFound() {defectsFound++;}
    public void defectPatched() {defectsPatched++;}

    // ---------- Consultas ----------
    public boolean isFinished() {return producers[GameConfig.WIN_ITEM].getAmount() >= GameConfig.WIN_AMOUNT;}
    public boolean isFrozen() {return frozenSeconds > 0;}
    public int getFrozenSeconds() {return frozenSeconds;}
    public int getClickPower() {return 1 + keyboard.getAmount() * GameConfig.CLICK_POWER_PER_LEVEL;}
    /** LOC/s que producirían los productores (ignora si hay congelamiento). */
    public int getBaseProduction() {
        int sum = 0;
        for (Item p : producers) sum += p.getProduction();
        return sum;
    }
    /** LOC/s reales en este momento. */
    public int getLocPerSecond() {return isFrozen() ? 0 : getBaseProduction();}
    public double getDefectDensity() {return locMade == 0 ? 0 : defectsFound * 1000.0 / locMade;}
    public int getLoc() {return loc;}
    public int getLocMade() {return locMade;}
    public int getLocSpent() {return locSpent;}
    public int getRawClicks() {return rawClicks;}
    public int getDefectsFound() {return defectsFound;}
    public int getDefectsPatched() {return defectsPatched;}
    public Item[] getProducers() {return producers;}
    public Item getKeyboard() {return keyboard;}
    public void setLoc(int v) {loc = v;}
    public void setLocMade(int v) {locMade = v;}
    public void setLocSpent(int v) {locSpent = v;}
    public void setRawClicks(int v) {rawClicks = v;}
}
