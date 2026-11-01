package com.github.antigravity.kubejsstudio.mekanism.mixin;

import com.github.antigravity.kubejsstudio.mekanism.recipe.MekanismRegistryLookup;
import dev.tysontheember.kubejsstudio.core.introspect.GameState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GameState.class, remap = false)
public abstract class GameStateMixin {

    /**
     * Ensures gas, slurry, infusion, and pigment IDs are recognized as valid registry entries,
     * preventing exclamation error marks on input/output slots in KubeJS Studio.
     */
    @Inject(method = "idInRegistry", at = @At("HEAD"), cancellable = true)
    private static void onIdInRegistry(String registry, String id, CallbackInfoReturnable<Boolean> cir) {
        if (MekanismRegistryLookup.isValidChemicalId(id)) {
            cir.setReturnValue(true);
        }
    }

    /**
     * Validates gas and chemical tags.
     */
    @Inject(method = "tagExists(Ljava/lang/String;Ljava/lang/String;)Z", at = @At("HEAD"), cancellable = true)
    private static void onTagExists(String registry, String tag, CallbackInfoReturnable<Boolean> cir) {
        if (MekanismRegistryLookup.isValidChemicalTag(tag)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "anyTagExists", at = @At("HEAD"), cancellable = true)
    private static void onAnyTagExists(String tag, CallbackInfoReturnable<Boolean> cir) {
        if (MekanismRegistryLookup.isValidChemicalTag(tag)) {
            cir.setReturnValue(true);
        }
    }
}
