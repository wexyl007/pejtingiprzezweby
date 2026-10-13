package dev.wexyl.utils.gui;

import dev.wexyl.utils.config.ModConfig;
import dev.wexyl.utils.feature.Feature;
import dev.wexyl.utils.feature.Features;
import dev.wexyl.utils.feature.TextSetting;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

/** Menu otwierane klawiszem O — przyciski ON/OFF dla każdej funkcji. */
public class FeaturesScreen extends Screen {

    private final Screen parent;

    public FeaturesScreen(Screen parent) {
        super(Component.literal("Zjeb Client"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int w = 220;
        int h = 20;
        int x = this.width / 2 - w / 2;
        int y = 50;

        for (Feature feature : Features.all()) {
            Button button = Button.builder(label(feature), btn -> {
                        ModConfig.set(feature, !ModConfig.isEnabled(feature));
                        btn.setMessage(label(feature));
                    })
                    .bounds(x, y, w, h)
                    .tooltip(Tooltip.create(Component.literal(feature.description())))
                    .build();
            this.addRenderableWidget(button);
            y += 24;

            for (TextSetting setting : feature.textSettings()) {
                EditBox box = new EditBox(this.font, x, y, w, h, Component.literal(setting.label()));
                box.setMaxLength(setting.maxLength());
                box.setValue(ModConfig.getString(setting));
                box.setResponder(value -> ModConfig.setString(setting, value));
                box.setHint(Component.literal(setting.label()));
                box.setTooltip(Tooltip.create(Component.literal(setting.label())));
                this.addRenderableWidget(box);
                y += 24;
            }
        }

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, btn -> this.onClose())
                .bounds(x, this.height - 30, w, h)
                .build());
    }

    private static Component label(Feature feature) {
        boolean on = ModConfig.isEnabled(feature);
        return Component.literal(feature.name() + ": ")
                .append(Component.literal(on ? "ON" : "OFF")
                        .withStyle(on ? ChatFormatting.GREEN : ChatFormatting.RED));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}
