package com.github.antigravity.kubejsstudio.mekanism.mixin;

import com.github.antigravity.kubejsstudio.mekanism.recipe.MekanismRegistryLookup;
import mekanism.api.chemical.Chemical;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Pseudo
@Mixin(targets = "dev.tysontheember.kubejsstudio.client.ui.kit.SlotGrid$Slot", remap = false)
public abstract class SlotGridSlotMixin {

    @Shadow
    private String value;

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

    /**
     * Renders chemical (gas, slurry, infusion, pigment) textures and tints directly in recipe slots
     * instead of falling back to empty tilde ("~") or missing bucket item stacks.
     */
    @Inject(method = "drawFluid", at = @At("HEAD"), cancellable = true)
    private void onDrawChemicalFluid(GuiGraphics graphics, Font font, int x, int y, CallbackInfo ci) {
        if (this.value == null || this.value.isBlank()) {
            return;
        }

        String id = this.value.trim();
        boolean isTag = id.startsWith("#");

        if (MekanismRegistryLookup.isValidChemicalId(id) || MekanismRegistryLookup.isValidChemicalTag(id)) {
            int tint = MekanismRegistryLookup.getChemicalColor(id);
            float r = ((tint >> 16) & 0xFF) / 255.0f;
            float g = ((tint >> 8) & 0xFF) / 255.0f;
            float b = (tint & 0xFF) / 255.0f;

            Chemical<?> chem = MekanismRegistryLookup.getChemical(id);
            TextureAtlasSprite sprite = null;

            if (chem != null && chem.getIcon() != null) {
                try {
                    sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(chem.getIcon());
                } catch (Throwable ignored) {}
            }

            if (sprite != null && !sprite.contents().name().getPath().contains("missingno")) {
                graphics.blit(x + 1, y + 1, 0, 16, 16, sprite, r, g, b, 1.0f);
            } else {
                // Colored chemical swatch
                graphics.fill(x + 1, y + 1, x + 17, y + 17, (0xFF << 24) | (tint & 0xFFFFFF));
                if (isTag) {
                    graphics.drawString(font, "#", x + 5, y + 4, 0xFFFFFFFF, false);
                }
            }

            ci.cancel();
        }
    }

    /**
     * Appends readable chemical name to slot tooltip.
     */
    @Inject(method = "tooltip", at = @At("RETURN"), cancellable = true)
    private void onChemicalTooltip(CallbackInfoReturnable<List<Component>> cir) {
        if (this.value == null || this.value.isBlank()) return;
        String id = this.value.trim();
        if (MekanismRegistryLookup.isValidChemicalId(id)) {
            Chemical<?> chem = MekanismRegistryLookup.getChemical(id);
            if (chem != null) {
                List<Component> tip = cir.getReturnValue();
                if (tip != null && !tip.isEmpty()) {
                    tip.set(0, chem.getTextComponent());
                }
            }
        }
    }
}
