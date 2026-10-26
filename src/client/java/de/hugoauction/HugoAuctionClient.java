package de.hugoauction;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class HugoAuctionClient implements ClientModInitializer {
    private static KeyBinding openKey;

    @Override
    public void onInitializeClient() {
        openKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hugoauction.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_G,
                KeyBinding.Category.create(Identifier.of("hugoauction", "main"))));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (openKey.wasPressed()) {
                if (mc.currentScreen == null) {
                    AuctionState.scanHand(mc);
                    mc.setScreen(new AuctionScreen());
                }
            }
            AuctionState.tick(mc);
        });

        // Server-Nachrichten (kein Overlay/Actionbar)
        ClientReceiveMessageEvents.GAME.register((msg, overlay) -> {
            if (!overlay) AuctionState.handleChat(msg.getString());
        });

        HudRenderCallback.EVENT.register((ctx, tick) -> {
            if (!AuctionState.running) return;
            MinecraftClient mc = MinecraftClient.getInstance();
            int w = 170, h = 44;
            int x = ctx.getScaledWindowWidth() / 2 - w / 2, y = 8;
            ctx.fill(x, y, x + w, y + h, 0xC0141028);
            ctx.fill(x, y, x + w, y + 2, 0xFF8B5CF6);
            ctx.drawItem(AuctionState.item, x + 6, y + 8);
            ctx.drawTextWithShadow(mc.textRenderer, AuctionState.item.getName(), x + 28, y + 6, 0xFFFFFFFF);
            ctx.drawTextWithShadow(mc.textRenderer, "Zeit: " + AuctionState.secondsLeft() + "s", x + 28, y + 17, 0xFFC4B5FD);
            String best = AuctionState.bestPlayer == null ? "noch kein Gebot"
                    : AuctionState.bestPlayer + "  $" + AuctionState.money(AuctionState.bestAmount);
            ctx.drawTextWithShadow(mc.textRenderer, best, x + 6, y + 31, 0xFF86EFAC);
        });
    }
}
