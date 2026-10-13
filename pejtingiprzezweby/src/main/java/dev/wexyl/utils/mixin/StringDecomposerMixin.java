package dev.wexyl.utils.mixin;

import dev.wexyl.utils.feature.NameProtect;
import net.minecraft.util.StringDecomposer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Cały tekst w grze (czat, tab, scoreboard, GUI) przechodzi przez StringDecomposer,
 * więc tu podmieniamy nick — zarówno przy rysowaniu, jak i przy liczeniu szerokości.
 */
@Mixin(StringDecomposer.class)
public class StringDecomposerMixin {

    @ModifyVariable(method = "iterate", at = @At("HEAD"), argsOnly = true)
    private static String wexylutils$protectPlain(String text) {
        return NameProtect.apply(text);
    }

    @ModifyVariable(
            method = "iterateFormatted(Ljava/lang/String;ILnet/minecraft/network/chat/Style;Lnet/minecraft/network/chat/Style;Lnet/minecraft/util/FormattedCharSink;)Z",
            at = @At("HEAD"),
            argsOnly = true)
    private static String wexylutils$protectFormatted(String text) {
        return NameProtect.apply(text);
    }
}
