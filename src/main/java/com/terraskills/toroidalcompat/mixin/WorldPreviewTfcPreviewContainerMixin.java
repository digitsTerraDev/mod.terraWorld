package com.terraskills.toroidalcompat.mixin;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keeps World Preview TFC's resolution selector available in the shaped-world UI. */
@Pseudo
@Mixin(targets = "com.rustysnail.world.preview.tfc.client.gui.screens.PreviewContainer", remap = false)
public abstract class WorldPreviewTfcPreviewContainerMixin {
    private static final int TOOLBAR_HEIGHT = 24;

    @Shadow @Final private Button cycleResolutionButton;

    @Inject(method = "doLayout", at = @At("RETURN"))
    private void tfcToroidal$keepZoomSelectorVisible(ScreenRectangle ignored, CallbackInfo ci) {
        tfcToroidal$showPreviewControls();
        tfcToroidal$movePreviewBelowToolbar();
    }

    @Inject(method = "updateSidePanelVisibility", at = @At("RETURN"))
    private void tfcToroidal$keepZoomSelectorAvailable(CallbackInfo ci) {
        tfcToroidal$showPreviewControls();
    }

    /**
     * Shaped-world creation uses the compact preview layout, which ordinarily
     * hides TFC's additional layers. They remain useful in that layout.
     */
    private void tfcToroidal$showPreviewControls() {
        cycleResolutionButton.visible = true;
        tfcToroidal$show("toggleTFCTemperature");
        tfcToroidal$show("toggleTFCRainfall");
        tfcToroidal$show("toggleTFCLandWater");
        tfcToroidal$show("toggleTFCRockTop");
        tfcToroidal$show("toggleTFCRockMid");
        tfcToroidal$show("toggleTFCRockBot");
        tfcToroidal$show("toggleTFCRockType");
        tfcToroidal$show("toggleKaolinClay");
        tfcToroidal$show("toggleTFCForestType");
        tfcToroidal$show("toggleTFCTreeSpecies");
        tfcToroidal$show("toggleTFCSoilType");
        tfcToroidal$show("toggleTFCHotspot");
    }

    private void tfcToroidal$show(String fieldName) {
        try {
            tfcToroidal$widget(fieldName).visible = true;
        } catch (ReflectiveOperationException ignored) {
            // Older World Preview versions may not have every optional layer.
        }
    }

    private void tfcToroidal$movePreviewBelowToolbar() {
        try {
            if (tfcToroidal$isExpanded()) {
                return;
            }

            final AbstractWidget preview = tfcToroidal$widget("previewDisplay");
            preview.setY(preview.getY() + TOOLBAR_HEIGHT);
            preview.setHeight(Math.max(1, preview.getHeight() - TOOLBAR_HEIGHT));
        } catch (ReflectiveOperationException ignored) {
            // Keep the standard World Preview layout if its internals change.
        }
    }

    private boolean tfcToroidal$isExpanded() throws ReflectiveOperationException {
        final Object expandButton = tfcToroidal$field("toggleExpand").get(this);
        return expandButton.getClass().getField("selected").getBoolean(expandButton);
    }

    private AbstractWidget tfcToroidal$widget(String fieldName) throws ReflectiveOperationException {
        return (AbstractWidget) tfcToroidal$field(fieldName).get(this);
    }

    private java.lang.reflect.Field tfcToroidal$field(String fieldName) throws ReflectiveOperationException {
        final var field = getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field;
    }
}
