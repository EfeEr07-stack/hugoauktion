package de.hugoauction;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class AuctionScreen extends Screen {
    private static final int W = 320, H = 190, SIDE = 84;
    private int left, top;
    private ButtonWidget durationBtn, startBtn;

    public AuctionScreen() {
        super(Text.literal("Auktion"));
    }

    @Override
    protected void init() {
        left = (width - W) / 2;
        top = (height - H) / 2;
        int cx = left + SIDE + 16, cw = W - SIDE - 32;

        addDrawableChild(ButtonWidget.builder(Text.literal("Item einscannen"), b -> {
            AuctionState.scanHand(MinecraftClient.getInstance());
        }).dimensions(cx, top + 78, cw, 20).build());

        durationBtn = addDrawableChild(ButtonWidget.builder(durationText(), b -> {
            if (AuctionState.running) return;
            AuctionState.durationIndex = (AuctionState.durationIndex + 1) % AuctionState.DURATIONS.length;
            b.setMessage(durationText());
        }).dimensions(cx, top + 102, cw, 20).build());

        startBtn = addDrawableChild(ButtonWidget.builder(startText(), b -> {
            if (AuctionState.running) AuctionState.stop(MinecraftClient.getInstance());
            else AuctionState.start();
            b.setMessage(startText());
        }).dimensions(cx, top + 126, cw, 20).build());
    }

    private Text durationText() {
        return Text.literal("Dauer: " + AuctionState.durationSec() + " s");
    }

    private Text startText() {
        return Text.literal(AuctionState.running ? "Auktion stoppen" : "Auktion starten");
    }

    /** Fake-Rundung: Rechteck ohne Ecken. */
    private static void card(DrawContext c, int x, int y, int w, int h, int color) {
        c.fill(x + 2, y, x + w - 2, y + h, color);
        c.fill(x, y + 2, x + w, y + h - 2, color);
        c.fill(x + 1, y + 1, x + w - 1, y + h - 1, color);
    }

    // Weichzeichner kommt vom Standard-Hintergrund (Blur), Panel zeichnen wir drauf.
    @Override
    public void renderBackground(DrawContext c, int mx, int my, float delta) {
        super.renderBackground(c, mx, my, delta);

        card(c, left - 1, top - 1, W + 2, H + 2, 0x558B5CF6);   // Glas-Rand
        card(c, left, top, W, H, 0xC0141028);                   // Panel
        card(c, left + 6, top + 6, SIDE - 6, H - 12, 0x60241B4B); // Sidebar
        card(c, left + SIDE + 8, top + 6, W - SIDE - 14, 62, 0x60241B4B); // Item-Karte

        Text title = Text.literal("Auktion").formatted(Formatting.ITALIC);
        c.drawTextWithShadow(textRenderer, title, left + 14, top + 14, 0xFFC4B5FD);
        c.drawTextWithShadow(textRenderer, "Hugo SMP", left + 14, top + 28, 0xFF9CA3AF);
        card(c, left + 12, top + 46, SIDE - 18, 16, 0x808B5CF6);
        c.drawTextWithShadow(textRenderer, "Streamer", left + 18, top + 50, 0xFFFFFFFF);

        int ix = left + SIDE + 18;
        if (AuctionState.item.isEmpty()) {
            c.drawTextWithShadow(textRenderer, "Kein Item - nimm eins in die Hand", ix, top + 32, 0xFFFCA5A5);
        } else {
            c.drawItem(AuctionState.item, ix, top + 16);
            c.drawTextWithShadow(textRenderer, AuctionState.item.getName(), ix + 24, top + 20, 0xFFFFFFFF);
        }
        String status = AuctionState.running
                ? "Läuft: " + AuctionState.secondsLeft() + "s"
                : "Bereit";
        c.drawTextWithShadow(textRenderer, status, ix, top + 42, 0xFFC4B5FD);
        if (AuctionState.bestPlayer != null) {
            c.drawTextWithShadow(textRenderer,
                    AuctionState.bestPlayer + "  $" + AuctionState.money(AuctionState.bestAmount),
                    ix, top + 53, 0xFF86EFAC);
        }
    }

    @Override
    public void tick() {
        if (startBtn != null) startBtn.setMessage(startText());
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
