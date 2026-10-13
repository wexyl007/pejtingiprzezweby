package dev.wexyl.utils.feature;

import dev.wexyl.utils.config.ModConfig;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Klient przekierowuje kliknięcie w cobweb na ścianę za nim.
 * Serwer dostaje zwykły pakiet użycia przedmiotu na prawdziwym bloku,
 * więc działa to także na serwerach bez moda.
 */
public final class CobwebPaintings {

    private static boolean busy = false;

    public static void init() {
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            if (!level.isClientSide || busy) return InteractionResult.PASS;
            if (!ModConfig.isEnabled(Features.COBWEB_PAINTINGS)) return InteractionResult.PASS;
            if (player.isSpectator()) return InteractionResult.PASS;
            if (!player.getItemInHand(hand).is(Items.PAINTING)) return InteractionResult.PASS;
            if (!level.getBlockState(hit.getBlockPos()).is(Blocks.COBWEB)) return InteractionResult.PASS;

            BlockHitResult real = raycastThroughWebs(player, level);
            if (real == null) return InteractionResult.PASS;

            Minecraft mc = Minecraft.getInstance();
            if (mc.gameMode == null || mc.player == null) return InteractionResult.PASS;

            busy = true;
            try {
                InteractionResult result = mc.gameMode.useItemOn(mc.player, hand, real);
                if (result.consumesAction()) {
                    player.swing(hand);
                    return InteractionResult.CONSUME;
                }
                return InteractionResult.PASS;
            } finally {
                busy = false;
            }
        });
    }

    /** Raycast, który przelatuje przez cobweby i zwraca pierwszy inny blok. */
    private static BlockHitResult raycastThroughWebs(Player player, Level level) {
        Vec3 eye = player.getEyePosition();
        Vec3 dir = player.getLookAngle();
        Vec3 end = eye.add(dir.scale(player.blockInteractionRange()));
        Vec3 start = eye;

        for (int i = 0; i < 32; i++) {
            BlockHitResult r = level.clip(new ClipContext(
                    start, end,
                    ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.NONE,
                    player));

            if (r.getType() == HitResult.Type.MISS) return null;
            if (!level.getBlockState(r.getBlockPos()).is(Blocks.COBWEB)) return r;

            BlockPos web = r.getBlockPos();
            Vec3 p = r.getLocation();
            int guard = 0;
            while (BlockPos.containing(p).equals(web) && guard++ < 100) {
                p = p.add(dir.scale(0.05));
            }
            start = p;
        }
        return null;
    }

    private CobwebPaintings() {
    }
}
