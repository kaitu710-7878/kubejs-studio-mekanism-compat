package com.github.antigravity.kubejsstudio.mekanism.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Configuration for KubeJS Studio Mekanism Compat.
 */
public final class MekanismCompatConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    // Feature Toggles
    public static final ForgeConfigSpec.BooleanValue ENABLE_GAS_BUILDER;
    public static final ForgeConfigSpec.BooleanValue ENABLE_GAS_TAG_MANAGEMENT;
    public static final ForgeConfigSpec.BooleanValue ENABLE_GAS_RECIPES;

    // Default Values
    public static final ForgeConfigSpec.IntValue DEFAULT_GAS_COLOR;
    public static final ForgeConfigSpec.IntValue DEFAULT_GAS_AMOUNT_MB;
    public static final ForgeConfigSpec.ConfigValue<String> DEFAULT_RECIPE_NAMESPACE;

    static {
        BUILDER.push("features");

        ENABLE_GAS_BUILDER = BUILDER
                .comment("Enable Mekanism Gas visual registration builder in KubeJS Studio Create tab.",
                         "KubeJS StudioのCreateタブでMekanismガスのビジュアル登録ビルダーを有効化します。")
                .define("enableGasBuilder", true);

        ENABLE_GAS_TAG_MANAGEMENT = BUILDER
                .comment("Enable Mekanism Gas tag browsing and editing in KubeJS Studio Tags tab.",
                         "KubeJS StudioのTagsタブでMekanismガスのタグ閲覧・編集を有効化します。")
                .define("enableGasTagManagement", true);

        ENABLE_GAS_RECIPES = BUILDER
                .comment("Enable Mekanism Gas-handling machine recipe forms in KubeJS Studio Recipes tab.",
                         "KubeJS StudioのRecipesタブでMekanismのガス取扱機械レシピフォームを有効化します。")
                .define("enableGasRecipes", true);

        BUILDER.pop();

        BUILDER.push("defaults");

        DEFAULT_GAS_COLOR = BUILDER
                .comment("Default integer color for newly created gases (Hex RGB, default: 0xFFFFFF).",
                         "新規作成ガスのデフォルト色コード（Hex RGB、デフォルト: 0xFFFFFF）。")
                .defineInRange("defaultGasColor", 0xFFFFFF, 0x000000, 0xFFFFFF);

        DEFAULT_GAS_AMOUNT_MB = BUILDER
                .comment("Default gas amount in mB (droplets/mB) for recipe inputs/outputs.",
                         "レシピ入出力時のデフォルトガス量（mB）。")
                .defineInRange("defaultGasAmountMb", 100, 1, 100000);

        DEFAULT_RECIPE_NAMESPACE = BUILDER
                .comment("Default namespace prefix for exported KubeJS Mekanism gas recipes.",
                         "生成されるKubeJS Mekanismガスレシピのデフォルト名前空間プレフィックス。")
                .define("defaultRecipeNamespace", "kubejs");

        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    private MekanismCompatConfig() {}
}
