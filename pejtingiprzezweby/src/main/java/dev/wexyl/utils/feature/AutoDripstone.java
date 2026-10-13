package dev.wexyl.utils.feature;

import dev.wexyl.utils.config.ModConfig;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Klik w trapdoor → w tym samym ticku stawiamy pod nim jeden pointed dripstone,
 * a zwykły klik otwiera trapdoor i dripstone spada. Działa tylko na klikany trapdoor.
 *
 * Sposób stawiania:
 *  1) jeśli obok miejsca pod trapdoorem jest zwykły pełny blok, stawiamy "o niego" (bez sneaka),
 *  2) w przeciwnym razie stawiamy o spód trapdoora i na chwilę wysyłamy sneak.
 */
public final class AutoDripstone {

    public static void init() {
        UseBlockCallback.EVENT.register((player, level, hand, hit) -> {
            if (hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
            if (!level.isClientSide) return InteractionResult.PASS;
            if (!ModConfig.isEnabled(Features.AUTO_DRIPSTONE)) return InteractionResult.PASS;
            // Shift = zwykłe zachowanie gry, moduł nic nie robi.
            if (player.isSpectator() || player.isShiftKeyDown()) return InteractionResult.PASS;

            BlockPos trapPos = hit.getBlockPos();
            if (!(level.getBlockState(trapPos).getBlock() instanceof TrapDoorBlock)) {
                return InteractionResult.PASS;
            }

            Minecraft mc = Minecraft.getInstance();
            ClientPacketListener connection = mc.getConnection();
            if (connection == null) return InteractionResult.PASS;

            int slot = findHotbarSlot(player);
            if (slot < 0) {
                say(player, "AutoDripstone: brak pointed dripstone w hotbarze");
                return InteractionResult.PASS;
            }

            BlockPos target = trapPos.below();
            if (!level.getBlockState(target).canBeReplaced()) {
                say(player, "AutoDripstone: pod trapdoorem nie ma wolnego miejsca");
                return InteractionResult.PASS;
            }

            Vec3 eye = player.getEyePosition();
            double range = player.blockInteractionRange() + 0.5;
            double rangeSq = range * range;

            // 1) kotwica: sąsiedni zwykły blok obok miejsca docelowego
            BlockPos anchor = null;
            Direction face = null;
            for (Direction d : Direction.Plane.HORIZONTAL) {
                BlockPos neighbour = target.relative(d);
                if (!isPlainSolid(level, neighbour)) continue;
                if (new AABB(neighbour).distanceToSqr(eye) >= rangeSq) continue;
                anchor = neighbour;
                face = d.getOpposite();
                break;
            }

            // 2) plan B: spód trapdoora + sneak
            boolean sneak = false;
            if (anchor == null) {
                if (new AABB(trapPos).distanceToSqr(eye) >= rangeSq) {
                    say(player, "AutoDripstone: trapdoor poza zasięgiem");
                    return InteractionResult.PASS;
                }
                anchor = trapPos;
                face = Direction.DOWN;
                sneak = true;
            }

            connection.send(new ServerboundSetCarriedItemPacket(slot));
            if (sneak) sendInput(connection, mc.options, true);
            sendUse(connection, anchor, face);
            if (sneak) sendInput(connection, mc.options, mc.options.keyShift.isDown());
            connection.send(new ServerboundSetCarriedItemPacket(player.getInventory().selected));

            say(player, sneak
                    ? "AutoDripstone: wysłano (przez sneak)"
                    : "AutoDripstone: wysłano (przez sąsiedni blok)");

            // PASS = normalny klik gracza otworzy trapdoor i zrzuci dripstone.
            return InteractionResult.PASS;
        });
    }

    private static void sendUse(ClientPacketListener connection, BlockPos anchor, Direction face) {
        Vec3 location = new Vec3(
                anchor.getX() + 0.5 + face.getStepX() * 0.5,
                anchor.getY() + 0.5 + face.getStepY() * 0.5,
                anchor.getZ() + 0.5 + face.getStepZ() * 0.5);
        BlockHitResult hit = new BlockHitResult(location, face, anchor, false);
        // Serwer przyjmuje dowolny numer sekwencji >= 0.
        connection.send(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND, hit, 0));
    }

    private static void sendInput(ClientPacketListener connection, Options o, boolean sneak) {
        connection.send(new ServerboundPlayerInputPacket(new Input(
                o.keyUp.isDown(), o.keyDown.isDown(), o.keyLeft.isDown(), o.keyRight.isDown(),
                o.keyJump.isDown(), sneak, o.keySprint.isDown())));
    }

    /** Pełny blok, z którym nie da się wejść w interakcję (żeby klik go nie "użył"). */
    private static boolean isPlainSolid(Level level, BlockPos pos) {
        BlockState s = level.getBlockState(pos);
        if (s.isAir() || s.hasBlockEntity() || !s.isCollisionShapeFullBlock(level, pos)) return false;
        return !(s.is(Blocks.CRAFTING_TABLE) || s.is(Blocks.FLETCHING_TABLE)
                || s.is(Blocks.SMITHING_TABLE) || s.is(Blocks.CARTOGRAPHY_TABLE)
                || s.is(Blocks.LOOM) || s.is(Blocks.NOTE_BLOCK)
                || s.is(Blocks.RESPAWN_ANCHOR) || s.is(Blocks.TNT));
    }

    private static int findHotbarSlot(Player player) {
        Inventory inv = player.getInventory();
        if (inv.getItem(inv.selected).is(Items.POINTED_DRIPSTONE)) return inv.selected;
        for (int i = 0; i < 9; i++) {
            if (inv.getItem(i).is(Items.POINTED_DRIPSTONE)) return i;
        }
        return -1;
    }

    private static void say(Player player, String text) {
        player.displayClientMessage(Component.literal(text), true);
    }

    private AutoDripstone() {
    }
}
