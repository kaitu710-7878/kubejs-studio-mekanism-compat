package com.github.antigravity.kubejsstudio.mekanism.gas;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Representation of a custom Mekanism Gas to be registered via KubeJS.
 */
public final class MekanismGasDefinition {
    private final String id;
    private final String displayName;
    private final int color;
    private final String texture;
    private final double radioactivity;
    private final List<String> tags;

    public MekanismGasDefinition(String id, String displayName, int color, String texture, double radioactivity, List<String> tags) {
        this.id = Objects.requireNonNull(id, "Gas id cannot be null");
        this.displayName = displayName != null ? displayName : "";
        this.color = color;
        this.texture = texture != null ? texture : "";
        this.radioactivity = Math.max(0.0, radioactivity);
        this.tags = tags != null ? List.copyOf(tags) : List.of();
    }

    public String id() { return id; }
    public String displayName() { return displayName; }
    public int color() { return color; }
    public String texture() { return texture; }
    public double radioactivity() { return radioactivity; }
    public List<String> tags() { return tags; }

    /**
     * Converts to KubeJS startup registry script snippet.
     * Example:
     * event.create('super_gas')
     *      .displayName('Super Gas')
     *      .color(0x33FF99)
     *      .radioactivity(0.5)
     */
    public String toKubeJsStatement() {
        String path = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
        StringBuilder sb = new StringBuilder();
        sb.append("  event.create('").append(path).append("')");

        if (!displayName.isBlank()) {
            sb.append("\n    .displayName('").append(escapeJs(displayName)).append("')");
        }

        if (color != 0xFFFFFF) {
            sb.append("\n    .color(0x").append(String.format("%06X", color)).append(")");
        }

        if (!texture.isBlank()) {
            sb.append("\n    .texture('").append(escapeJs(texture)).append("')");
        }

        if (radioactivity > 0.0) {
            sb.append("\n    .radioactivity(").append(radioactivity).append(")");
        }

        for (String tag : tags) {
            if (!tag.isBlank()) {
                sb.append("\n    .tag('").append(escapeJs(tag)).append("')");
            }
        }

        return sb.toString();
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", id);
        obj.addProperty("displayName", displayName);
        obj.addProperty("color", String.format("#%06X", color));
        obj.addProperty("texture", texture);
        obj.addProperty("radioactivity", radioactivity);
        JsonArray tagArr = new JsonArray();
        for (String tag : tags) {
            tagArr.add(tag);
        }
        obj.add("tags", tagArr);
        return obj;
    }

    public static MekanismGasDefinition fromJson(JsonObject obj) {
        String id = obj.has("id") ? obj.get("id").getAsString() : "kubejs:custom_gas";
        String displayName = obj.has("displayName") ? obj.get("displayName").getAsString() : "";
        int color = 0xFFFFFF;
        if (obj.has("color")) {
            String colStr = obj.get("color").getAsString().replace("#", "").replace("0x", "");
            try {
                color = Integer.parseInt(colStr, 16);
            } catch (NumberFormatException ignored) {}
        }
        String texture = obj.has("texture") ? obj.get("texture").getAsString() : "";
        double radioactivity = obj.has("radioactivity") ? obj.get("radioactivity").getAsDouble() : 0.0;
        List<String> tags = new ArrayList<>();
        if (obj.has("tags") && obj.get("tags").isJsonArray()) {
            obj.getAsJsonArray("tags").forEach(el -> tags.add(el.getAsString()));
        }
        return new MekanismGasDefinition(id, displayName, color, texture, radioactivity, tags);
    }

    private static String escapeJs(String str) {
        return str.replace("\\", "\\\\").replace("'", "\\'");
    }

    public static Builder builder(String id) {
        return new Builder(id);
    }

    public static class Builder {
        private final String id;
        private String displayName = "";
        private int color = 0xFFFFFF;
        private String texture = "";
        private double radioactivity = 0.0;
        private final List<String> tags = new ArrayList<>();

        public Builder(String id) {
            this.id = id;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder color(int color) {
            this.color = color;
            return this;
        }

        public Builder texture(String texture) {
            this.texture = texture;
            return this;
        }

        public Builder radioactivity(double radioactivity) {
            this.radioactivity = radioactivity;
            return this;
        }

        public Builder tag(String tag) {
            if (tag != null && !tag.isBlank()) {
                this.tags.add(tag);
            }
            return this;
        }

        public Builder tags(Iterable<String> tags) {
            if (tags != null) {
                for (String t : tags) {
                    tag(t);
                }
            }
            return this;
        }

        public MekanismGasDefinition build() {
            return new MekanismGasDefinition(id, displayName, color, texture, radioactivity, tags);
        }
    }
}
