package dev.wexyl.utils;

import com.mojang.blaze3d.platform.InputConstants;
import dev.wexyl.utils.config.ModConfig;
import dev.wexyl.utils.feature.CobwebPaintings;
import dev.wexyl.utils.gui.FeaturesScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;

public class WexylUtilsClient implements ClientModInitializer {

    private static KeyMapping openMenuKey;

    @Override
    public void onInitializeClient() {
        ModConfig.load();

        openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.wexylutils.open_menu",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_O,
                "key.categories.wexylutils"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.consumeClick()) {
                client.setScreen(new FeaturesScreen(client.screen));
            }
        });

        // Tu rejestrujemy kolejne funkcje:
        CobwebPaintings.init();
    }
}
