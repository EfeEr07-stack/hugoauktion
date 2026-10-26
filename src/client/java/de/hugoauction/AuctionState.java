package de.hugoauction;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AuctionState {
    // Die Nachricht muss GENAU so aussehen, davor/dahinter darf nichts stehen.
    private static final Pattern PAYMENT = Pattern.compile(
            "^\\[HugoSMP\\] Du hast \\$([0-9][0-9.,]*) von ([A-Za-z0-9_]{3,16}) erhalten\\.$");

    public static final int[] DURATIONS = {30, 60, 120, 300, 600};
    public static int durationIndex = 1;

    public static ItemStack item = ItemStack.EMPTY;
    public static boolean running = false;
    public static long endTime = 0;
    public static long bestAmount = 0;
    public static String bestPlayer = null;
    public static int bidCount = 0;

    public static int durationSec() { return DURATIONS[durationIndex]; }

    public static void scanHand(MinecraftClient mc) {
        if (mc.player == null || running) return;
        ItemStack s = mc.player.getMainHandStack();
        item = s.isEmpty() ? ItemStack.EMPTY : s.copy();
    }

    public static void start() {
        if (running || item.isEmpty()) return;
        running = true;
        bestAmount = 0;
        bestPlayer = null;
        bidCount = 0;
        endTime = System.currentTimeMillis() + durationSec() * 1000L;
    }

    public static void stop(MinecraftClient mc) {
        if (!running) return;
        running = false;
        if (mc.player == null) return;
        if (bestPlayer == null) {
            mc.player.sendMessage(Text.literal("§8[§dAuktion§8] §7Beendet - keine Gebote."), false);
        } else {
            mc.player.sendMessage(Text.literal("§8[§dAuktion§8] §7Gewinner: §f" + bestPlayer
                    + " §7mit §a$" + money(bestAmount) + " §7(" + bidCount + " Zahlungen)"), false);
        }
    }

    public static void tick(MinecraftClient mc) {
        if (running && System.currentTimeMillis() >= endTime) stop(mc);
    }

    public static long secondsLeft() {
        return Math.max(0, (endTime - System.currentTimeMillis() + 999) / 1000);
    }

    public static void handleChat(String raw) {
        if (!running) return;
        Matcher m = PAYMENT.matcher(raw.trim());
        if (!m.matches()) return;
        long amount;
        try {
            amount = Long.parseLong(m.group(1).replace(".", "").replace(",", ""));
        } catch (NumberFormatException e) {
            return;
        }
        bidCount++;
        // Hoechste Einzelzahlung gewinnt
        if (amount > bestAmount) {
            bestAmount = amount;
            bestPlayer = m.group(2);
        }
    }

    public static String money(long v) {
        return String.format("%,d", v).replace(',', '.');
    }
}
