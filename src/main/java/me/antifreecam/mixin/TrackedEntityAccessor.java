package me.antifreecam.mixin;

import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

// Create this interface
@Mixin(targets = "net.minecraft.server.level.ChunkMap$TrackedEntity")
public interface TrackedEntityAccessor {
    @Accessor("serverEntity")
    ServerEntity getServerEntity();

    @Accessor("entity")
    Entity getEntity();

    @Accessor("seenBy")
    Set<ServerPlayerConnection> getSeenBy();
}