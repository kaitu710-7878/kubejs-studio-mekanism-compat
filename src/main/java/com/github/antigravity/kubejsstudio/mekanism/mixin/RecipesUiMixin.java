package com.github.antigravity.kubejsstudio.mekanism.mixin;

import com.github.antigravity.kubejsstudio.mekanism.recipe.MekanismRecipeFormRegistry;
import com.github.antigravity.kubejsstudio.mekanism.recipe.UniversalMekanismRecipeForm;
import dev.tysontheember.kubejsstudio.client.ui.kit.FilterDropdown;
import dev.tysontheember.kubejsstudio.client.ui.screen.RecipesUi;
import dev.tysontheember.kubejsstudio.core.op.RecipeForm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(value = RecipesUi.class, remap = false)
public abstract class RecipesUiMixin {

    @Shadow
    private void formItem(List<FilterDropdown.Item> items, RecipeForm form) {}

    /**
     * Intercepts recipe form matching so any Mekanism, Astral Mekanism, or universal gas machine recipe
     * cleanly resolves to a dedicated or auto-inferred UniversalMekanismRecipeForm instead of null/custom.
     */
    @Inject(method = "matchForm", at = @At("HEAD"), cancellable = true)
    private void onMatchForm(String typeId, CallbackInfoReturnable<RecipeForm> cir) {
        if (MekanismRecipeFormRegistry.supports(typeId)) {
            UniversalMekanismRecipeForm form = MekanismRecipeFormRegistry.getOrBuildForm(typeId);
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
