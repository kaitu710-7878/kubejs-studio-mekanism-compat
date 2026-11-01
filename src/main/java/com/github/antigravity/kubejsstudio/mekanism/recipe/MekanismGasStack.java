package com.github.antigravity.kubejsstudio.mekanism.recipe;

import com.google.gson.JsonObject;

import java.util.Objects;

/**
 * Represents a Mekanism Gas stack or Gas tag ingredient with an amount in mB.
 */
public final class MekanismGasStack {
    private final String idOrTag;
    private final int amount;

    public MekanismGasStack(String idOrTag, int amount) {
        this.idOrTag = Objects.requireNonNull(idOrTag, "Gas ID/Tag cannot be null").trim();
        this.amount = Math.max(1, amount);
    }

    public String idOrTag() { return idOrTag; }
    public int amount() { return amount; }
    public boolean isTag() { return idOrTag.startsWith("#"); }

    public String cleanId() {
        return isTag() ? idOrTag.substring(1) : idOrTag;
    }

    /**
     * Converts to JSON for Mekanism recipe datapacks.
     */
    public JsonObject toJson(String keyName) {
        JsonObject obj = new JsonObject();
        obj.addProperty("amount", amount);
        if (isTag()) {
            obj.addProperty("gasTag", cleanId());
        } else {
            obj.addProperty("gas", cleanId());
        }
        return obj;
    }

    /**
     * Converts to KubeJS recipe script parameter.
     * Example: '100x mekanism:oxygen'
     */
    public String toKubeJsArg() {
        return amount + "x " + idOrTag;
    }

    public static MekanismGasStack parse(String input, int defaultAmount) {
        if (input == null || input.isBlank()) return null;
        String s = input.trim();
        int amount = defaultAmount;
        if (s.contains("x") && s.matches("^\\d+x\\s*.*")) {
            int xIdx = s.indexOf('x');
            try {
                amount = Integer.parseInt(s.substring(0, xIdx).trim());
                s = s.substring(xIdx + 1).trim();
            } catch (NumberFormatException ignored) {}
        }
        return new MekanismGasStack(s, amount);
    }

    @Override
    public String toString() {
        return amount + "x " + idOrTag;
    }
}
