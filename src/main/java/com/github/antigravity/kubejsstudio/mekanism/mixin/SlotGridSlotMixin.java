package com.github.antigravity.kubejsstudio.mekanism.mixin;

import com.github.antigravity.kubejsstudio.mekanism.recipe.MekanismRegistryLookup;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Pseudo
@Mixin(targets = "dev.tysontheember.kubejsstudio.client.ui.kit.SlotGrid$Slot", remap = false)
public abstract class SlotGridSlotMixin {

    /**
     * Filters out any Mekanism or Astral Mekanism chemical IDs from the unresolved list,
     * removing the red exclamation mark error icon from input/output slots.
     */
    @Inject(method = "unresolved", at = @At("RETURN"))
    private void onFilterUnresolved(String val, CallbackInfoReturnable<List<String>> cir) {
        List<String> list = cir.getReturnValue();
        if (list != null && !list.isEmpty()) {
            list.removeIf(id -> MekanismRegistryLookup.isValidChemicalId(id) || MekanismRegistryLookup.isValidChemicalTag(id));
        }
    }
}
