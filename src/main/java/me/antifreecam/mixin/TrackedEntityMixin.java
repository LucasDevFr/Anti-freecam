package me.antifreecam.mixin;

import me.antifreecam.ChunkMaskingUtil;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(targets = "net.minecraft.server.level.ChunkMap$TrackedEntity")
public abstract class TrackedEntityMixin {
    @Inject(
            method = "updatePlayer(Lnet/minecraft/server/level/ServerPlayer;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onUpdatePlayer(ServerPlayer player, CallbackInfo ci) {
        TrackedEntityAccessor self = (TrackedEntityAccessor)(Object)this;
        Entity entity = self.getEntity();
        ServerEntity serverEntity = self.getServerEntity();
        Set<ServerPlayerConnection> seenBy = self.getSeenBy();

        // Never interfere with a player's own entity
        if (entity == player) return;
        // Never interfere with players at all (optional, but safer)
        //if (entity instanceof ServerPlayer) return;

        if (ChunkMaskingUtil.isEntityInMaskedSection(entity, player)) {
            seenBy.remove(player.connection);
            serverEntity.removePairing(player);
            ci.cancel();
        } else if (!seenBy.contains(player.connection)) {
            // Only re-add if vanilla would normally be tracking this
            // (i.e. player is within range)
            seenBy.add(player.connection);
            serverEntity.addPairing(player);
            ci.cancel();
        }
    }
}