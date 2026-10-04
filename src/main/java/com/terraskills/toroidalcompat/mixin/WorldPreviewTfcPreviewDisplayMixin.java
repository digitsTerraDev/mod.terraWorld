package com.terraskills.toroidalcompat.mixin;

import com.terraskills.toroidalcompat.worldgen.ActiveTfcFold;
import com.toroidalworld.core.WorldFold;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Draws every visible seam of the selected wrapped world over World Preview TFC's map. */
@Pseudo
@Mixin(targets = "com.rustysnail.world.preview.tfc.client.gui.widgets.PreviewDisplay", remap = false)
public abstract class WorldPreviewTfcPreviewDisplayMixin {
    private static final int BORDER_COLOR = 0xE000E5FF;
    private static final int BORDER_WIDTH = 2;

    @Shadow private int texWidth;
    @Shadow private int texHeight;
    @Shadow private int scaleBlockPos;
    @Shadow public abstract BlockPos center();

    @Inject(method = "renderWidget", at = @At("RETURN"))
    private void tfcToroidal$drawWorldBorders(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        final WorldFold fold = ActiveTfcFold.get();
        if (fold == null || scaleBlockPos <= 0 || texWidth <= 0 || texHeight <= 0) return;

        final AbstractWidget widget = (AbstractWidget) (Object) this;
        final int left = widget.getX();
        final int top = widget.getY();
        final int right = left + widget.getWidth();
        final int bottom = top + widget.getHeight();
        final BlockPos center = center();
        final int minX = center.getX() - texWidth * scaleBlockPos / 2 - 1;
        final int minZ = center.getZ() - texHeight * scaleBlockPos / 2 - 1;

        graphics.enableScissor(left, top, right, bottom);
        if (fold.bounds().loops(Direction.Axis.X)) {
            drawVerticalSeams(graphics, fold.blockDomain(Direction.Axis.X).lowerBound,
                    fold.blockDomain(Direction.Axis.X).domainLength, minX, left, top, bottom, widget.getWidth());
        }
        if (fold.bounds().loops(Direction.Axis.Z)) {
            drawHorizontalSeams(graphics, fold.blockDomain(Direction.Axis.Z).lowerBound,
                    fold.blockDomain(Direction.Axis.Z).domainLength, minZ, left, right, top, widget.getHeight());
        }
        graphics.disableScissor();
    }

    private void drawVerticalSeams(GuiGraphics graphics, int domainStart, int domainLength, int visibleStart,
                                   int left, int top, int bottom, int widgetWidth) {
        final long first = firstVisibleSeam(domainStart, domainLength, visibleStart);
        final long visibleEnd = (long) visibleStart + texWidth * scaleBlockPos;
        for (long seam = first; seam <= visibleEnd; seam += domainLength) {
            final int x = left + (int) (((seam - visibleStart) / (double) (texWidth * scaleBlockPos)) * widgetWidth);
            graphics.fill(x - BORDER_WIDTH / 2, top, x + (BORDER_WIDTH + 1) / 2, bottom, BORDER_COLOR);
        }
    }

    private void drawHorizontalSeams(GuiGraphics graphics, int domainStart, int domainLength, int visibleStart,
                                     int left, int right, int top, int widgetHeight) {
        final long first = firstVisibleSeam(domainStart, domainLength, visibleStart);
        final long visibleEnd = (long) visibleStart + texHeight * scaleBlockPos;
        for (long seam = first; seam <= visibleEnd; seam += domainLength) {
            final int y = top + (int) (((seam - visibleStart) / (double) (texHeight * scaleBlockPos)) * widgetHeight);
            graphics.fill(left, y - BORDER_WIDTH / 2, right, y + (BORDER_WIDTH + 1) / 2, BORDER_COLOR);
        }
    }

    private static long firstVisibleSeam(int domainStart, int domainLength, int visibleStart) {
        final long repeats = Math.floorDiv((long) visibleStart - domainStart, domainLength);
        return (long) domainStart + repeats * domainLength;
    }
}
