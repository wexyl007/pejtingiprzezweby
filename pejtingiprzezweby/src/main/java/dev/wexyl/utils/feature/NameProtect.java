package dev.wexyl.utils.feature;

import dev.wexyl.utils.config.ModConfig;
import net.minecraft.client.Minecraft;

/**
 * Podmienia Twój prawdziwy nick na fałszywy w każdym tekście rysowanym przez grę.
 * Działa tylko u Ciebie (wizualnie) — serwer i inni gracze dalej widzą prawdziwy nick.
 */
public final class NameProtect {

    public static String apply(String text) {
        if (text == null || text.isEmpty()) return text;
        if (!ModConfig.isEnabled(Features.NAME_PROTECT)) return text;

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.getUser() == null) return text;

        String real = mc.getUser().getName();
        if (real == null || real.isEmpty() || !text.contains(real)) return text;

        String fake = ModConfig.getString(Features.FAKE_NAME);
        if (fake == null || fake.isEmpty()) return text;

        return text.replace(real, fake);
    }

    private NameProtect() {
    }
}
