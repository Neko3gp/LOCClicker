package ConsolesLocClicker.core;

import java.util.Properties;
import java.util.Random;

/** Estado y reglas del juego. No contiene nada de Swing: se puede probar sin interfaz. */
public class Stats {
    private int loc, locMade, locSpent, rawClicks;
    private int frozenSeconds;
    private int defectsFound, defectsPatched;
    private int bonusesAwarded, bonusLoc;
    private int phase = 1;
    private int phaseStartLoc;
    private int phaseTargetLoc;
    private int stability = GameConfig.STABILITY_START;
    private int technicalDebt;
    private int eventsCompleted;
    private int frenzySeconds;
    private int frenzyCooldownSeconds;
    private boolean frenzyOpportunity;
    private int priceMultiplierPercent;
    private int productionPenaltyPercent;
    private final Random random = new Random();
    private final Item[] producers;
    private final Item keyboard;
    private final Item quality;
    private final Item computer;
    private final Item training;
    private final Item retention;

    public Stats() {
        int n = GameConfig.NAMES.length;
        producers = new Item[n];
        for (int i = 0; i < n; i++) {
            producers[i] = new Item(GameConfig.NAMES[i], GameConfig.DESCRIPTIONS[i],
                    GameConfig.LOC_PER_SEC[i], GameConfig.BASE_PRICE[i], GameConfig.GROWTH);
        }
        keyboard = new Item(GameConfig.CLICK_NAME, GameConfig.CLICK_DESCRIPTION,
                0, GameConfig.CLICK_BASE_PRICE, GameConfig.CLICK_GROWTH);
        quality = new Item(GameConfig.QUALITY_NAME, GameConfig.QUALITY_DESCRIPTION,
            0, GameConfig.QUALITY_BASE_PRICE, GameConfig.QUALITY_GROWTH);
        computer = new Item(GameConfig.COMPUTER_NAME, GameConfig.COMPUTER_DESCRIPTION,
            0, GameConfig.COMPUTER_BASE_PRICE, GameConfig.COMPUTER_GROWTH);
        training = new Item(GameConfig.TRAINING_NAME, GameConfig.TRAINING_DESCRIPTION,
            0, GameConfig.TRAINING_BASE_PRICE, GameConfig.TRAINING_GROWTH);
        retention = new Item(GameConfig.RETENTION_NAME, GameConfig.RETENTION_DESCRIPTION,
            0, GameConfig.RETENTION_BASE_PRICE, GameConfig.RETENTION_GROWTH);
        phaseTargetLoc = GameConfig.PHASE_TWO_GOAL_LOC;
    }

    /** Se llama cada segundo. Si hay un defecto sin parchear, la producción está detenida. */
    public void tick() {
        if (isPhaseComplete()) return;
        if (frenzyCooldownSeconds > 0) frenzyCooldownSeconds--;
        if (frozenSeconds > 0) { frozenSeconds--; return; }
        if (stability >= GameConfig.FRENZY_MIN_STABILITY && frenzySeconds == 0
                && frenzyCooldownSeconds == 0
                && random.nextInt(100) < GameConfig.FRENZY_START_CHANCE_PERCENT) {
            frenzySeconds = GameConfig.FRENZY_SECONDS;
            frenzyCooldownSeconds = GameConfig.FRENZY_SECONDS + GameConfig.FRENZY_COOLDOWN_SECONDS;
            frenzyOpportunity = true;
        }
        int produced = getEffectiveProduction();
        loc += produced;
        locMade += produced;
        if (frenzySeconds > 0) frenzySeconds--;
    }
    public void click() {
        if (isPhaseComplete()) return;
        int amount = getClickPower();
        loc += amount;
        locMade += amount;
        rawClicks++;
        if (rawClicks % GameConfig.BONUS_CLICKS == 0) {
            loc += GameConfig.BONUS_LOC;
            locMade += GameConfig.BONUS_LOC;
            bonusesAwarded++;
            bonusLoc += GameConfig.BONUS_LOC;
        }
    }
    public boolean buyProducer(int index) {return buy(producers[index]);}
    public boolean buyKeyboard() {
        if (keyboard.getAmount() >= GameConfig.CLICK_MAX) return false;
        return buy(keyboard);
    }
    public boolean buyQuality() {
        if (quality.getAmount() >= GameConfig.QUALITY_MAX) return false;
        return buy(quality);
    }
    public boolean buyComputer() {return buy(computer);}
    public boolean buyTraining() {return buy(training);}
    public boolean buyRetention() {return buy(retention);}
    /** Precio final de una compra, incluyendo el costo de escalar la empresa. */
    public int getPrice(Item item) {
        double multiplier = phase == 1 ? 1.0
                : GameConfig.PHASE_TWO_COST_MULTIPLIER
                    * Math.pow(GameConfig.PHASE_COST_GROWTH, phase - 2);
        multiplier *= 1.0 + priceMultiplierPercent / 100.0;
        return (int) Math.min(Integer.MAX_VALUE, Math.ceil(item.getPrice() * multiplier));
    }
    public int getUnexpectedExpense() {
        return (int) Math.min(Integer.MAX_VALUE,
                (long) GameConfig.EVENT_EXPENSE_BASE * phase
                    + (long) technicalDebt * GameConfig.EVENT_EXPENSE_DEBT_FACTOR);
    }
    public boolean payUnexpectedExpense() {
        int expense = getUnexpectedExpense();
        if (loc < expense) return false;
        loc -= expense;
        locSpent += expense;
        return true;
    }
    private boolean buy(Item item) {
        if (isPhaseComplete()) return false;
        int price = getPrice(item);
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
    public void defectPatched() {
        defectsPatched++;
        recoverStability(GameConfig.STABILITY_PATCH_REWARD);
        technicalDebt = Math.max(0, technicalDebt - 1);
    }
    public void defectEscaped() {
        damageStability(GameConfig.STABILITY_DEFECT_LOSS);
        technicalDebt += GameConfig.DEBT_DEFECT_PENALTY;
    }
    /** Pierde el empleado de mayor nivel disponible durante una crisis de escala. */
    public String applyEmployeeTurnover() {
        for (int i = producers.length - 1; i >= 0; i--) {
            if (producers[i].getAmount() > 0) {
                producers[i].setAmount(producers[i].getAmount() - 1);
                int damage = Math.max(5, GameConfig.UNEXPECTED_STABILITY_LOSS
                        - retention.getAmount() * GameConfig.RETENTION_STABILITY_PROTECTION);
                damageStability(damage);
                technicalDebt += GameConfig.DEBT_DEFECT_PENALTY;
                return producers[i].getName();
            }
        }
        damageStability(GameConfig.UNEXPECTED_STABILITY_LOSS);
        return "un puesto vacante";
    }
    /** Penalizacion automatica de un evento inesperado de operacion. */
    public int applyUnexpectedEvent() {
        int lost = Math.max((int) (loc * GameConfig.UNEXPECTED_LOC_LOSS_PCT),
                getBaseProduction() * GameConfig.UNEXPECTED_MIN_PRODUCTION_SECONDS);
        lost = Math.min(lost, loc);
        loc -= lost;
        damageStability(GameConfig.UNEXPECTED_STABILITY_LOSS);
        technicalDebt += GameConfig.DEBT_DEFECT_PENALTY;
        return lost;
    }
    public int applyMarketShock() {
        int lost = Math.min(loc, Math.max((int) (loc * GameConfig.MARKET_SHOCK_LOC_LOSS_PCT),
                getBaseProduction() * 8));
        loc -= lost;
        damageStability(GameConfig.MARKET_SHOCK_STABILITY_LOSS);
        technicalDebt += GameConfig.DEBT_DEFECT_PENALTY;
        return lost;
    }
    public void applyPriceSurge() {priceMultiplierPercent += GameConfig.PRICE_SURGE_PERCENT; damageStability(20);}
    public void applyBudgetCut() {
        productionPenaltyPercent = Math.min(80, productionPenaltyPercent + GameConfig.BUDGET_CUT_PRODUCTION_PERCENT);
        damageStability(25);
    }
    public void completeEvent() {eventsCompleted++;}
    public void damageStability(int amount) {stability = Math.max(0, stability - Math.max(0, amount));}
    public void recoverStability(int amount) {stability = Math.min(GameConfig.STABILITY_MAX, stability + Math.max(0, amount));}
    public void reduceTechnicalDebt(int amount) {technicalDebt = Math.max(0, technicalDebt - Math.max(0, amount));}
    public void addEventBonus() {
        loc += GameConfig.EVENT_BONUS_LOC;
        locMade += GameConfig.EVENT_BONUS_LOC;
        recoverStability(GameConfig.STABILITY_MINIGAME_REWARD);
        completeEvent();
    }

    // ---------- Consultas ----------
    /** Indica que la fase actual alcanzo su objetivo y espera una transicion. */
    public boolean isPhaseComplete() {
        if (phase == 1) return producers[GameConfig.WIN_ITEM].getAmount() >= GameConfig.WIN_AMOUNT;
        return locMade >= phaseTargetLoc && stability >= GameConfig.STABILITY_MIN_FOR_PHASE;
    }
    /** Se conserva como alias para pruebas y codigo de la primera version. */
    public boolean isFinished() {return isPhaseComplete();}
    public void advancePhase() {
        phase++;
        phaseStartLoc = locMade;
        long nextTarget = phase == 2
                ? (long) phaseStartLoc + GameConfig.PHASE_TWO_GOAL_LOC
                : (long) phaseStartLoc + Math.round(GameConfig.PHASE_TWO_GOAL_LOC
                    * Math.pow(GameConfig.PHASE_GOAL_GROWTH, phase - 2));
        phaseTargetLoc = (int) Math.min(Integer.MAX_VALUE, nextTarget);
        stability = GameConfig.STABILITY_START;
        technicalDebt = Math.max(0, technicalDebt - GameConfig.DEBT_QUALITY_REDUCTION);
        frozenSeconds = 0;
    }
    public boolean isPhaseOne() {return phase == 1;}
    public int getPhase() {return phase;}
    public int getPhaseStartLoc() {return phaseStartLoc;}
    public int getPhaseTargetLoc() {return phaseTargetLoc;}
    public int getPhaseProgressPercent() {
        if (phase == 1) return isPhaseComplete() ? 100 : 0;
        if (phaseTargetLoc <= phaseStartLoc) return 100;
        return (int) Math.min(100, Math.max(0,
                (long) (locMade - phaseStartLoc) * 100 / (phaseTargetLoc - phaseStartLoc)));
    }
    public String getPhaseName() {return phase == 1 ? "Proyecto controlado" : "Empresa en escala";}
    public boolean isFrozen() {return frozenSeconds > 0;}
    public int getFrozenSeconds() {return frozenSeconds;}
        public int getClickPower() {return 1 + keyboard.getAmount() * GameConfig.CLICK_POWER_PER_LEVEL
            + computer.getAmount() * GameConfig.COMPUTER_CLICK_POWER;}
    public int getDefectChancePercent() {
        return Math.max(20, 100 - quality.getAmount() * GameConfig.QUALITY_DEFECT_REDUCTION_PERCENT);
    }
    /** LOC/s que producirían los productores (ignora si hay congelamiento). */
    public int getBaseProduction() {
        int sum = 0;
        for (Item p : producers) sum += p.getProduction();
        return sum;
    }
    /** LOC/s reales en este momento. */
    public int getLocPerSecond() {return getEffectiveProduction();}
    public int getEffectiveProduction() {
        if (isFrozen()) return 0;
        double budgetMultiplier = 1.0 - productionPenaltyPercent / 100.0;
        double staffMultiplier = 1.0 + computer.getAmount() * GameConfig.COMPUTER_PRODUCTION_PERCENT / 100.0
            + training.getAmount() * GameConfig.TRAINING_PRODUCTION_PERCENT / 100.0;
        return (int) Math.round(getBaseProduction() * getStabilityMultiplier() * budgetMultiplier * staffMultiplier
                * (frenzySeconds > 0 ? GameConfig.FRENZY_PRODUCTION_MULTIPLIER : 1.0));
    }
    public double getStabilityMultiplier() {
        if (stability >= GameConfig.STABILITY_START) return 1.0 + (stability - GameConfig.STABILITY_START) / 100.0;
        return 0.5 + stability / 200.0;
    }
    public double getDefectDensity() {return locMade == 0 ? 0 : defectsFound * 1000.0 / locMade;}
    public int getLoc() {return loc;}
    public int getLocMade() {return locMade;}
    public int getLocSpent() {return locSpent;}
    public int getRawClicks() {return rawClicks;}
    public int getDefectsFound() {return defectsFound;}
    public int getDefectsPatched() {return defectsPatched;}
    public int getBonusesAwarded() {return bonusesAwarded;}
    public int getBonusLoc() {return bonusLoc;}
    public int getStability() {return stability;}
    public int getTechnicalDebt() {return technicalDebt;}
    public int getEventsCompleted() {return eventsCompleted;}
    public int getFrenzySeconds() {return frenzySeconds;}
    public boolean consumeFrenzyOpportunity() {
        boolean available = frenzyOpportunity;
        frenzyOpportunity = false;
        return available;
    }
    public void activateFrenzy() {
        stability = GameConfig.STABILITY_START;
        frenzySeconds = GameConfig.FRENZY_SECONDS;
        frenzyCooldownSeconds = GameConfig.FRENZY_SECONDS + GameConfig.FRENZY_COOLDOWN_SECONDS;
    }
    public void missFrenzy() {frenzyCooldownSeconds = GameConfig.FRENZY_MISSED_COOLDOWN_SECONDS;}
    public int getPriceMultiplierPercent() {return priceMultiplierPercent;}
    public int getProductionPenaltyPercent() {return productionPenaltyPercent;}
    public Item[] getProducers() {return producers;}
    public Item getKeyboard() {return keyboard;}
    public Item getQuality() {return quality;}
    public Item getComputer() {return computer;}
    public Item getTraining() {return training;}
    public Item getRetention() {return retention;}
    public int getEmployeeCount() {
        int total = 0;
        for (Item producer : producers) total += producer.getAmount();
        return total;
    }
    public int getCompanyLevel() {
        long next = GameConfig.COMPANY_FIRST_LEVEL_LOC;
        int level = 1;
        while (locMade >= next && next <= Integer.MAX_VALUE / GameConfig.COMPANY_LEVEL_GROWTH) {
            level++;
            next = Math.round(next * GameConfig.COMPANY_LEVEL_GROWTH);
        }
        return level;
    }
    public int getCompanyLevelStart() {
        long threshold = GameConfig.COMPANY_FIRST_LEVEL_LOC;
        for (int level = 1; level < getCompanyLevel(); level++) threshold = Math.round(threshold * GameConfig.COMPANY_LEVEL_GROWTH);
        return (int) Math.min(Integer.MAX_VALUE, threshold);
    }
    public int getCompanyNextLevel() {
        long threshold = Math.round((long) getCompanyLevelStart() * GameConfig.COMPANY_LEVEL_GROWTH);
        return (int) Math.min(Integer.MAX_VALUE, threshold);
    }
    public int getCompanyProgressPercent() {
        long start = getCompanyLevelStart();
        long next = getCompanyNextLevel();
        if (next <= start) return 100;
        return (int) Math.min(100, Math.max(0, (locMade - start) * 100 / (next - start)));
    }
    public int getPlannedLoc(int seconds) {
        return (int) Math.min(Integer.MAX_VALUE, (long) Math.max(0, seconds) * GameConfig.PLAN_LOC_PER_SECOND);
    }
    public void setLoc(int v) {loc = v;}
    public void setLocMade(int v) {locMade = v;}
    public void setLocSpent(int v) {locSpent = v;}
    public void setRawClicks(int v) {rawClicks = v;}

    /** Exporta solo datos de juego; la interfaz y los timers se reconstruyen al cargar. */
    public void saveProperties(Properties p) {
        p.setProperty("loc", String.valueOf(loc));
        p.setProperty("locMade", String.valueOf(locMade));
        p.setProperty("locSpent", String.valueOf(locSpent));
        p.setProperty("rawClicks", String.valueOf(rawClicks));
        p.setProperty("frozenSeconds", String.valueOf(frozenSeconds));
        p.setProperty("defectsFound", String.valueOf(defectsFound));
        p.setProperty("defectsPatched", String.valueOf(defectsPatched));
        p.setProperty("bonusesAwarded", String.valueOf(bonusesAwarded));
        p.setProperty("bonusLoc", String.valueOf(bonusLoc));
        p.setProperty("phase", String.valueOf(phase));
        p.setProperty("phaseStartLoc", String.valueOf(phaseStartLoc));
        p.setProperty("phaseTargetLoc", String.valueOf(phaseTargetLoc));
        p.setProperty("stability", String.valueOf(stability));
        p.setProperty("technicalDebt", String.valueOf(technicalDebt));
        p.setProperty("eventsCompleted", String.valueOf(eventsCompleted));
        p.setProperty("frenzySeconds", String.valueOf(frenzySeconds));
        p.setProperty("frenzyCooldownSeconds", String.valueOf(frenzyCooldownSeconds));
        p.setProperty("priceMultiplierPercent", String.valueOf(priceMultiplierPercent));
        p.setProperty("productionPenaltyPercent", String.valueOf(productionPenaltyPercent));
        for (int i = 0; i < producers.length; i++) p.setProperty("producer." + i, String.valueOf(producers[i].getAmount()));
        p.setProperty("keyboard", String.valueOf(keyboard.getAmount()));
        p.setProperty("quality", String.valueOf(quality.getAmount()));
        p.setProperty("computer", String.valueOf(computer.getAmount()));
        p.setProperty("training", String.valueOf(training.getAmount()));
        p.setProperty("retention", String.valueOf(retention.getAmount()));
    }

    /** Restaura un estado validado por SaveManager. Valores ausentes conservan el valor actual. */
    public void loadProperties(Properties p) {
        loc = read(p, "loc", loc); locMade = read(p, "locMade", locMade);
        locSpent = read(p, "locSpent", locSpent); rawClicks = read(p, "rawClicks", rawClicks);
        frozenSeconds = read(p, "frozenSeconds", frozenSeconds);
        defectsFound = read(p, "defectsFound", defectsFound); defectsPatched = read(p, "defectsPatched", defectsPatched);
        bonusesAwarded = read(p, "bonusesAwarded", bonusesAwarded); bonusLoc = read(p, "bonusLoc", bonusLoc);
        phase = Math.max(1, read(p, "phase", phase)); phaseStartLoc = read(p, "phaseStartLoc", phaseStartLoc);
        phaseTargetLoc = Math.max(1, read(p, "phaseTargetLoc", phaseTargetLoc));
        stability = Math.max(0, Math.min(GameConfig.STABILITY_MAX, read(p, "stability", stability)));
        technicalDebt = Math.max(0, read(p, "technicalDebt", technicalDebt));
        eventsCompleted = read(p, "eventsCompleted", eventsCompleted);
        frenzySeconds = Math.max(0, read(p, "frenzySeconds", frenzySeconds));
        frenzyCooldownSeconds = Math.max(0, read(p, "frenzyCooldownSeconds", frenzyCooldownSeconds));
        priceMultiplierPercent = Math.max(0, read(p, "priceMultiplierPercent", priceMultiplierPercent));
        productionPenaltyPercent = Math.max(0, Math.min(80,
            read(p, "productionPenaltyPercent", productionPenaltyPercent)));
        for (int i = 0; i < producers.length; i++) producers[i].setAmount(read(p, "producer." + i, producers[i].getAmount()));
        keyboard.setAmount(read(p, "keyboard", keyboard.getAmount()));
        quality.setAmount(read(p, "quality", quality.getAmount()));
        computer.setAmount(read(p, "computer", computer.getAmount()));
        training.setAmount(read(p, "training", training.getAmount()));
        retention.setAmount(read(p, "retention", retention.getAmount()));
    }

    private static int read(Properties p, String key, int fallback) {
        try { return Integer.parseInt(p.getProperty(key, String.valueOf(fallback))); }
        catch (NumberFormatException e) { return fallback; }
    }
}
