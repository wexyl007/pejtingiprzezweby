package dev.wexyl.utils.feature;

import dev.wexyl.utils.config.ModConfig;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Klik w zamknięty trapdoor (dolna połowa) → w jednym ticku stawiamy pod nim kolumnę
 * pointed dripstone (stalaktyt), a normalny klik otwiera trapdoor i stalaktyt spada.
 * Działa wyłącznie na klikany trapdoor. Wszystko idzie pakietami, więc dzieje się
 * szybciej, niż zrobiłby to gracz.
 */
public final class AutoDripstone {

    private static final int MAX_LENGTH = 8;

    public static void init() {
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
            if (!(level instanceof ClientLevel clientLevel)) return InteractionResult.PASS;
            if (!ModConfig.isEnabled(Features.AUTO_DRIPSTONE)) return InteractionResult.PASS;
            // Shift = zwykłe zachowanie gry, moduł nic nie robi.
            if (player.isSpectator() || player.isShiftKeyDown()) return InteractionResult.PASS;

            BlockPos trapPos = hit.getBlockPos();
            BlockState trap = level.getBlockState(trapPos);
            if (!(trap.getBlock() instanceof TrapDoorBlock)) {
                return InteractionResult.PASS;
            }
            // Dripstone wisi tylko pod zamkniętym trapdoorem w dolnej połowie bloku.
            if (trap.getValue(BlockStateProperties.OPEN)) return InteractionResult.PASS;
            if (trap.getValue(BlockStateProperties.HALF) != Half.BOTTOM) return InteractionResult.PASS;

            Minecraft mc = Minecraft.getInstance();
            ClientPacketListener connection = mc.getConnection();
            if (connection == null) return InteractionResult.PASS;

            int slot = findHotbarSlot(player);
            if (slot < 0) return InteractionResult.PASS;
            int length = Math.min(configuredLength(), player.getInventory().getItem(slot).getCount());

            // Plan: ile segmentów zmieści się w zasięgu i w wolnym miejscu.
            Vec3 eye = player.getEyePosition();
            double range = player.blockInteractionRange() + 0.5;
            List<BlockPos> supports = new ArrayList<>();
            BlockPos support = trapPos;
            for (int i = 0; i < length; i++) {
                if (new AABB(support).distanceToSqr(eye) >= range * range) break;
                if (!level.getBlockState(support.below()).canBeReplaced()) break;
                supports.add(support);
                support = support.below();
            }
            if (supports.isEmpty()) return InteractionResult.PASS;

            connection.send(new ServerboundSetCarriedItemPacket(slot));
            for (int i = 0; i < supports.size(); i++) {
                // Pierwsze postawienie jest "o trapdoor", więc serwer musi widzieć sneak.
                if (i == 0) sendInput(connection, mc.options, true);
                sendUse(clientLevel, connection, supports.get(i));
                if (i == 0) sendInput(connection, mc.options, mc.options.keyShift.isDown());
            }
            connection.send(new ServerboundSetCarriedItemPacket(player.getInventory().selected));

            // PASS = normalny klik gracza otworzy trapdoor i zrzuci stalaktyt.
            return InteractionResult.PASS;
        });
    }

    private static void sendUse(ClientLevel level, ClientPacketListener connection, BlockPos support) {
        BlockHitResult hit = new BlockHitResult(
                Vec3.atBottomCenterOf(support), Direction.DOWN, support, false);
        try (BlockStatePredictionHandler handler = level.getBlockStatePredictionHandler().startPredicting()) {
            connection.send(new ServerboundUseItemOnPacket(
                    InteractionHand.MAIN_HAND, hit, handler.currentSequence()));
        }
    }

    private static void sendInput(ClientPacketListener connection, Options o, boolean sneak) {
        connection.send(new ServerboundPlayerInputPacket(new Input(
                o.keyUp.isDown(), o.keyDown.isDown(), o.keyLeft.isDown(), o.keyRight.isDown(),
                o.keyJump.isDown(), sneak, o.keySprint.isDown())));
    }

    private static int findHotbarSlot(Player player) {
        Inventory inv = player.getInventory();
        if (inv.getItem(inv.selected).is(Items.POINTED_DRIPSTONE)) return inv.selected;
        for (int i = 0; i < 9; i++) {
            if (inv.getItem(i).is(Items.POINTED_DRIPSTONE)) return i;
        }
        return -1;
    }

    private static int configuredLength() {
        try {
            int value = Integer.parseInt(ModConfig.getString(Features.DRIPSTONE_LENGTH).trim());
            return Math.max(1, Math.min(MAX_LENGTH, value));
        } catch (NumberFormatException e) {
            return 5;
        }
    }

    private AutoDripstone() {
    }
}
