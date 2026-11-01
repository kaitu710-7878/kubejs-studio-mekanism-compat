package com.github.antigravity.kubejsstudio.mekanism.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Accurately parses Mekanism's custom crafting serializer (`mekanism:mek_data`),
 * which is used for crafting recipes that retain NBT/gas/energy data (e.g. Jetpack, Laser, Energy Cubes, Gas Tanks).
 * Converts them directly into KubeJS Studio's 3x3 shaped or shapeless grid model.
 */
public final class MekDataRecipeParser {

    private MekDataRecipeParser() {}

    public record ParsedMekData(boolean isShaped, String[] grid, String result, int resultCount) {}

    public static ParsedMekData parse(JsonObject json) {
        if (json == null) return null;
        if (!json.has("type") || !"mekanism:mek_data".equals(json.get("type").getAsString())) {
            return null;
        }

        // 1. Result parsing
        String resultItem = "";
        int resultCount = 1;
        if (json.has("result")) {
            JsonElement resElem = json.get("result");
            if (resElem.isJsonObject()) {
                JsonObject resObj = resElem.getAsJsonObject();
                if (resObj.has("item")) resultItem = resObj.get("item").getAsString();
                if (resObj.has("count")) resultCount = resObj.get("count").getAsInt();
            } else if (resElem.isJsonPrimitive()) {
                resultItem = resElem.getAsString();
            }
        }

        // 2. Shaped crafting: "pattern" and "key"
        if (json.has("pattern") && json.has("key")) {
            String[] grid = new String[9];
            Arrays.fill(grid, "");

            Map<Character, String> keyMap = new HashMap<>();
            JsonObject keyObj = json.getAsJsonObject("key");
            for (Map.Entry<String, JsonElement> entry : keyObj.entrySet()) {
                if (entry.getKey().isEmpty()) continue;
                char c = entry.getKey().charAt(0);
                String val = parseIngredientElement(entry.getValue());
                if (val != null) {
                    keyMap.put(c, val);
                }
            }

            JsonArray patternArr = json.getAsJsonArray("pattern");
            int numRows = Math.min(3, patternArr.size());
            for (int r = 0; r < numRows; r++) {
                String rowStr = patternArr.get(r).getAsString();
                int numCols = Math.min(3, rowStr.length());
                for (int c = 0; c < numCols; c++) {
                    char ch = rowStr.charAt(c);
                    if (ch != ' ' && keyMap.containsKey(ch)) {
                        grid[r * 3 + c] = keyMap.get(ch);
                    }
                }
            }
            return new ParsedMekData(true, grid, resultItem, resultCount);
        }

        // 3. Shapeless crafting: "ingredients"
        if (json.has("ingredients")) {
            String[] grid = new String[9];
            Arrays.fill(grid, "");
            JsonArray ingArr = json.getAsJsonArray("ingredients");
            int limit = Math.min(9, ingArr.size());
            for (int i = 0; i < limit; i++) {
                String val = parseIngredientElement(ingArr.get(i));
                grid[i] = (val != null) ? val : "";
            }
            return new ParsedMekData(false, grid, resultItem, resultCount);
        }

        return null;
    }

    public static String parseIngredientElement(JsonElement elem) {
        if (elem == null || elem.isJsonNull()) return null;
        if (elem.isJsonObject()) {
            JsonObject obj = elem.getAsJsonObject();
            if (obj.has("item")) return obj.get("item").getAsString();
            if (obj.has("tag")) return "#" + obj.get("tag").getAsString();
            if (obj.has("ingredient")) return parseIngredientElement(obj.get("ingredient"));
        } else if (elem.isJsonArray()) {
            JsonArray arr = elem.getAsJsonArray();
            if (!arr.isEmpty()) return parseIngredientElement(arr.get(0));
        } else if (elem.isJsonPrimitive()) {
            return elem.getAsString();
        }
        return null;
    }
}
