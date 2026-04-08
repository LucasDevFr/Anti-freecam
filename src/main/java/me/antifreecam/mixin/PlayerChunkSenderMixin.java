package me.antifreecam.mixin;

import me.antifreecam.AntiFreecamMod;
import net.fabricmc.fabric.mixin.event.interaction.ServerPlayNetworkHandlerInteractEntityHandlerMixin;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.PlayerChunkSender;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.network.protocol.game.ClientGamePacketListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static me.antifreecam.ChunkMaskingUtil.buildMaskedPacket;

@Mixin(PlayerChunkSender.class)
public class PlayerChunkSenderMixin {
    private static final ThreadLocal<Boolean> SENDING_MASKED = ThreadLocal.withInitial(() -> false);

    @Inject(method = "sendChunk", at = @At("HEAD"), cancellable = true)
    private static void onSendChunk(ServerGamePacketListenerImpl connection,
                                    ServerLevel level,
                                    LevelChunk chunk,
                                    CallbackInfo ci) {
        if (!AntiFreecamMod.CONFIG.enabled) return;

        ServerPlayer player = connection.player;
        int playerY = (int) Math.floor(player.getY());

        // Build and send the masked packet manually
        ClientboundLevelChunkWithLightPacket fakePacket = buildMaskedPacket(chunk, level, player);
        connection.send(fakePacket);
        System.out.println("sent fake");
        ci.cancel();
    }
}