package com.github.antigravity.kubejsstudio.mekanism.mixin;

import com.github.antigravity.kubejsstudio.mekanism.recipe.MekanismRecipeFormRegistry;
import com.github.antigravity.kubejsstudio.mekanism.recipe.UniversalMekanismRecipeForm;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.tysontheember.kubejsstudio.client.ui.kit.FilterDropdown;
import dev.tysontheember.kubejsstudio.client.ui.screen.RecipesUi;
import dev.tysontheember.kubejsstudio.core.introspect.GameState;
import dev.tysontheember.kubejsstudio.core.op.RecipeForm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = RecipesUi.class, remap = false)
public abstract class RecipesUiMixin {

    @Shadow
    private void formItem(List<FilterDropdown.Item> items, RecipeForm form) {}

    @Shadow
    private String overrideJson;

    @Unique
    private static final ThreadLocal<String> CURRENT_RECIPE_JSON = new ThreadLocal<>();

    @Inject(method = "loadForOverride", at = @At("HEAD"))
    private void onLoadForOverrideHead(String target, CallbackInfo ci) {
        try {
            String jsonStr = GameState.recipeJsonById(target);
            if (jsonStr != null && !jsonStr.isBlank()) {
                CURRENT_RECIPE_JSON.set(jsonStr);
            }
        } catch (Throwable ignored) {}
    }

    @Inject(method = "loadForOverride", at = @At("RETURN"))
    private void onLoadForOverrideReturn(String target, CallbackInfo ci) {
        CURRENT_RECIPE_JSON.remove();
    }

    @Inject(method = "loadForm", at = @At("HEAD"))
    private void onLoadFormHead(String target, String jsonStr, CallbackInfoReturnable<Boolean> cir) {
        if (jsonStr != null && !jsonStr.isBlank()) {
            CURRENT_RECIPE_JSON.set(jsonStr);
        }
    }

    @Inject(method = "loadForm", at = @At("RETURN"))
    private void onLoadFormReturn(String target, String jsonStr, CallbackInfoReturnable<Boolean> cir) {
        CURRENT_RECIPE_JSON.remove();
    }

    /**
     * Intercepts recipe form matching so any Mekanism (including custom mek_data crafting),
     * Astral Mekanism, or universal gas machine recipe cleanly resolves to a dedicated or
     * dynamically inferred UniversalMekanismRecipeForm.
     * With the active JSON properly supplied from CURRENT_RECIPE_JSON, dynamic slot inspection is 100% accurate!
     */
    @Inject(method = "matchForm", at = @At("HEAD"), cancellable = true)
    private void onMatchForm(String typeId, CallbackInfoReturnable<RecipeForm> cir) {
        if (MekanismRecipeFormRegistry.supports(typeId)) {
            JsonObject json = null;
            String rawJson = CURRENT_RECIPE_JSON.get();
            if (rawJson == null || rawJson.isBlank()) {
                rawJson = this.overrideJson;
            }

            if (rawJson != null && !rawJson.isBlank()) {
                try {
                    JsonElement parsed = JsonParser.parseString(rawJson);
                    if (parsed.isJsonObject()) {
                        json = parsed.getAsJsonObject();
                    }
                } catch (Throwable ignored) {}
            }

            UniversalMekanismRecipeForm form = MekanismRecipeFormRegistry.getOrBuildForm(typeId, json);
            if (form != null) {
                cir.setReturnValue(form);
            }
        }
    }

    /**
     * Injects all Mekanism and Astral Mekanism recipe forms into the type picker dropdown menu.
     */
    @ModifyArg(
        method = "openTypeMenu",
        at = @At(value = "INVOKE", target = "Ldev/tysontheember/kubejsstudio/client/ui/kit/FilterDropdown;open(Lcom/lowdragmc/lowdraglib2/gui/ui/UIElement;FFLjava/util/List;)Ldev/tysontheember/kubejsstudio/client/ui/kit/FilterDropdown;"),
        index = 3
    )
    private List<FilterDropdown.Item> onOpenTypeMenuItems(List<FilterDropdown.Item> items) {
        if (items != null) {
            for (UniversalMekanismRecipeForm form : MekanismRecipeFormRegistry.allBuiltins()) {
                formItem(items, form);
            }
        }
        return items;
    }
}
