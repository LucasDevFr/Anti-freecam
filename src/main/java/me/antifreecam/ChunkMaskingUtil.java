package me.antifreecam;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.Optional;

public class ChunkMaskingUtil {
    public static ClientboundLevelChunkWithLightPacket buildMaskedPacket(
            LevelChunk chunk, ServerLevel level, ServerPlayer player) {

        int playerY = (int) Math.floor(player.getY());
        playerY = Math.min(playerY, AntiFreecamMod.CONFIG.surfaceThreshold);

        int playerSection = Math.floorDiv(playerY - chunk.getMinY(), 16);
        int revealDownToSection = playerSection - 1;

        BlockState mask = getMaskState();
        LevelChunkSection[] realSections = chunk.getSections();
        LevelChunkSection[] backup = realSections.clone();
        try {
            for (int i = 0; i < realSections.length; i++) {
                if (i < revealDownToSection) {
                    LevelChunkSection fake = realSections[i].copy();
                    for (int x = 0; x < 16; x++)
                        for (int y = 0; y < 16; y++)
                            for (int z = 0; z < 16; z++)
                                fake.setBlockState(x, y, z, mask, false);
                    realSections[i] = fake;
                }
            }
            return new ClientboundLevelChunkWithLightPacket(chunk, level.getLightEngine(), null, null);
        } finally {
            System.arraycopy(backup, 0, realSections, 0, backup.length);
        }
    }

    private static BlockState getMaskState() {
        String blockId = AntiFreecamMod.CONFIG.maskBlock;
        Optional<Holder.Reference<Block>> optional =
                BuiltInRegistries.BLOCK.get(Identifier.parse(blockId));
        if (optional.isEmpty()) {
            AntiFreecamMod.LOGGER.warn("[AntiFreecam] maskBlock '{}' not found, falling back to stone", blockId);
            return Blocks.STONE.defaultBlockState();
        }
        return optional.get().value().defaultBlockState();
    }
}