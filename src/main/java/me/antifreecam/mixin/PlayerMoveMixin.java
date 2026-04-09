package me.antifreecam.mixin;

import me.antifreecam.AntiFreecamCommand;
import me.antifreecam.AntiFreecamMod;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

import static me.antifreecam.ChunkMaskingUtil.buildMaskedPacket;

@Mixin(ServerGamePacketListenerImpl.class)
public class PlayerMoveMixin {
    private int lastTrackedSection = Integer.MAX_VALUE;
    private final Queue<LevelChunk> pendingChunkResends = new ArrayDeque<>();
    private static final int CHUNKS_PER_TICK = 5; // tune this
    private long lastResendTime = 0;
    private static final long RESEND_COOLDOWN_MS = 250;

    @Inject(method = "handleMovePlayer", at = @At("TAIL"))
    private void onPlayerMove(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
        if (!AntiFreecamMod.CONFIG.enabled) return;

        ServerPlayer player = ((ServerGamePacketListenerImpl)(Object)this).player;
        if (AntiFreecamCommand.isExempt(player)) return;
        if (player.level().dimension().equals(Level.NETHER)) return;

        int effectiveY = Math.min((int) Math.floor(player.getY()), AntiFreecamMod.CONFIG.surfaceThreshold);
        int currentSection = Math.floorDiv(effectiveY, 16);

        long now = System.currentTimeMillis();
        if (currentSection != lastTrackedSection && now - lastResendTime > RESEND_COOLDOWN_MS) {
            lastResendTime = now;
            lastTrackedSection = currentSection;
            queueNearbyChunks(player);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        if (pendingChunkResends.isEmpty()) return;
        if (!AntiFreecamMod.CONFIG.enabled) return;
        ServerPlayer player = ((ServerGamePacketListenerImpl)(Object)this).player;
        ServerLevel level = (ServerLevel) player.level();

        int sent = 0;
        while (!pendingChunkResends.isEmpty() && sent < CHUNKS_PER_TICK) {
            LevelChunk chunk = pendingChunkResends.poll();
            ClientboundLevelChunkWithLightPacket pkt =
                    buildMaskedPacket(chunk, level, player);
            ((ServerGamePacketListenerImpl)(Object)this).send(pkt);
            sent++;
        }
    }

    private void queueNearbyChunks(ServerPlayer player) {
        pendingChunkResends.clear(); // discard stale queue if section changed again
        ServerLevel level = (ServerLevel) player.level();
        int chunkX = (int) Math.floor(player.getX()) >> 4;
        int chunkZ = (int) Math.floor(player.getZ()) >> 4;
        int viewDistance = level.getServer().getPlayerList().getViewDistance();

        //int effectiveY = Math.min((int) Math.floor(player.getY()), AntiFreecamMod.CONFIG.surfaceThreshold);
        //boolean underground = effectiveY < AntiFreecamMod.CONFIG.surfaceThreshold;
        //int viewDistance = underground ? 4 : level.getServer().getPlayerList().getViewDistance();

        // Collect and sort by distance so nearest chunks send first
        List<LevelChunk> chunks = new ArrayList<>();
        for (int dx = -viewDistance; dx <= viewDistance; dx++) {
            for (int dz = -viewDistance; dz <= viewDistance; dz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX + dx, chunkZ + dz);
                if (chunk != null) chunks.add(chunk);
            }
        }

        chunks.sort(Comparator.comparingInt(c ->
                Math.abs(c.getPos().x - chunkX) + Math.abs(c.getPos().z - chunkZ)));

        pendingChunkResends.addAll(chunks);
    }

//    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;)V", at = @At("HEAD"), cancellable = true)
//    private void onSendPacket(Packet<?> packet, CallbackInfo ci) {
//        if (!AntiFreecamMod.CONFIG.enabled) return;
//        if (!(packet instanceof ClientboundAddEntityPacket entityPacket)) return;
//
//        ServerPlayer player = ((ServerGamePacketListenerImpl)(Object)this).player;
//        int playerY = (int) Math.floor(player.getY());
//        playerY = Math.min(playerY, AntiFreecamMod.CONFIG.surfaceThreshold);
//
//        int playerSection = Math.floorDiv(playerY - player.level().getMinY(), 16);
//        int entitySection = Math.floorDiv((int) Math.floor(entityPacket.getY()) - player.level().getMinY(), 16);
//
//        // Cancel if entity is in a section that would be masked
//        if (entitySection < playerSection - 1) {
//            ci.cancel();
//        }
//    }
}