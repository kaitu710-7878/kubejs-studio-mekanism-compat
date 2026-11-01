package com.github.antigravity.kubejsstudio.mekanism.tag;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Representation of a Mekanism Gas Tag modification (add/remove/replace).
 */
public final class MekanismGasTagDefinition {
    public static final String REGISTRY = "mekanism:gas";
    private static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final String tagId;
    private final Set<String> additions;
    private final Set<String> removals;
    private final boolean replace;

    public MekanismGasTagDefinition(String tagId, Collection<String> additions, Collection<String> removals, boolean replace) {
        this.tagId = Objects.requireNonNull(tagId, "Tag id cannot be null");
        this.additions = additions != null ? new LinkedHashSet<>(additions) : new LinkedHashSet<>();
        this.removals = removals != null ? new LinkedHashSet<>(removals) : new LinkedHashSet<>();
        this.replace = replace;
    }

    public String tagId() { return tagId; }
    public Set<String> additions() { return Collections.unmodifiableSet(additions); }
    public Set<String> removals() { return Collections.unmodifiableSet(removals); }
    public boolean replace() { return replace; }

    /**
     * Generates KubeJS ServerEvents tag statements.
     * Example:
     * event.add('forge:gases/clean_iron', 'kubejs:super_gas')
     * event.remove('forge:gases/clean_iron', 'mekanism:hydrogen')
     */
    public List<String> toKubeJsStatements() {
        List<String> list = new ArrayList<>();
        if (replace) {
            list.add("  event.removeAll('" + tagId + "');");
        }
        for (String id : additions) {
            list.add("  event.add('" + tagId + "', '" + id + "');");
        }
        for (String id : removals) {
            list.add("  event.remove('" + tagId + "', '" + id + "');");
        }
        return list;
    }

    /**
     * Generates Minecraft Datapack JSON for this gas tag.
     */
    public String toDatapackJson() {
        JsonObject root = new JsonObject();
        root.addProperty("replace", replace);
        JsonArray vals = new JsonArray();
        for (String add : additions) {
            vals.add(add);
        }
        root.add("values", vals);
        if (!removals.isEmpty()) {
            JsonArray rems = new JsonArray();
            for (String rem : removals) {
                rems.add(rem);
            }
            root.add("remove", rems);
        }
        return PRETTY.toJson(root);
    }
}
