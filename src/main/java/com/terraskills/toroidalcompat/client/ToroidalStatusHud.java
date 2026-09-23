package com.terraskills.toroidalcompat.client;

import com.terraskills.toroidalcompat.config.ToroidalCompatClientConfig;
import com.toroidalworld.api.v1.ToroidalShape;
import com.toroidalworld.api.v1.ToroidalWorldApi;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.Optional;

public final class ToroidalStatusHud {
    private static final int WIDTH = 154;
    private static final int HEIGHT = 41;

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        final Minecraft minecraft = Minecraft.getInstance();
        if (!ToroidalCompatClientConfig.SHOW_HUD.get() || minecraft.options.hideGui
                || minecraft.player == null || minecraft.level == null) return;

        final Optional<ToroidalShape> optionalShape = ToroidalWorldApi.shapeOf(minecraft.level).or(() -> {
            final var server = minecraft.getSingleplayerServer();
            return server == null ? Optional.empty() : ToroidalWorldApi.shapeOf(server.overworld());
        });
        if (optionalShape.isEmpty()) return;

        final float scale = ToroidalCompatClientConfig.HUD_SCALE.get().floatValue();
        graphics.pose().pushPose();
        graphics.pose().translate(ToroidalCompatClientConfig.LEFT_OFFSET.get(),
                graphics.guiHeight() - ToroidalCompatClientConfig.BOTTOM_OFFSET.get(), 0);
        graphics.pose().scale(scale, scale, 1);
        graphics.pose().translate(0, -HEIGHT, 0);

        drawPanel(graphics);
        graphics.drawString(minecraft.font,
                Component.literal("Location").withStyle(ChatFormatting.GOLD), 6, 5, 0xFFFFFFFF, true);

        final ToroidalShape shape = optionalShape.orElseThrow();
        final double latitude = ToroidalAngles.latitude(shape, minecraft.player.getZ());
        final double longitude = ToroidalAngles.longitude(shape, minecraft.player.getX());
        graphics.drawString(minecraft.font, "Latitude   " + latitudeAngle(latitude,
                        ToroidalAngles.latitudeBranch(shape, minecraft.player.getZ())),
                6, 17, 0xFFE7E7E7, true);
        graphics.drawString(minecraft.font, "Longitude  " + unsignedAngle(longitude),
                6, 29, 0xFFE7E7E7, true);
        graphics.pose().popPose();
    }

    private static String latitudeAngle(double value, char branch) {
        if (branch == '\0') {
            return String.format(Locale.ROOT, "%+.2f\u00B0", value);
        }
        final double magnitude = Math.abs(value) < 0.005 ? 0.0 : Math.abs(value);
        return String.format(Locale.ROOT, "%c%.2f\u00B0 %s", branch, magnitude, value >= 0 ? "N" : "S");
    }

    private static String unsignedAngle(double value) {
        return String.format(Locale.ROOT, "%.2f\u00B0", value);
    }

    private static void drawPanel(GuiGraphics graphics) {
        graphics.fill(0, 0, WIDTH, HEIGHT, 0xFF000000);
        graphics.fill(1, 1, WIDTH - 1, HEIGHT - 1, 0xFF373737);
        graphics.fill(1, 1, WIDTH - 2, 2, 0xFFFFFFFF);
        graphics.fill(1, 1, 2, HEIGHT - 2, 0xFFFFFFFF);
        graphics.fill(2, HEIGHT - 2, WIDTH - 1, HEIGHT - 1, 0xFF555555);
        graphics.fill(WIDTH - 2, 2, WIDTH - 1, HEIGHT - 1, 0xFF555555);
    }

    private ToroidalStatusHud() {}
}
