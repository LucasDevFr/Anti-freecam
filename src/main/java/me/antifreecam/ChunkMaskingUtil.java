package me.antifreecam;

import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

import java.util.Optional;

import static me.antifreecam.AntiFreecamMod.CONFIG;

public class ChunkMaskingUtil {
    private static LevelChunkSection fake;

    public static ClientboundLevelChunkWithLightPacket buildMaskedPacket(
            LevelChunk chunk, ServerLevel level, ServerPlayer player) {

        int playerY = (int) Math.floor(player.getY());
        playerY = Math.min(playerY, CONFIG.surfaceThreshold);

        int playerSection = Math.floorDiv(playerY - chunk.getMinY(), 16);
        int revealDownToSection = playerSection - 1;

        BlockState mask = getMaskState();
        LevelChunkSection[] realSections = chunk.getSections();
        LevelChunkSection[] backup = realSections.clone();

        if (CONFIG.engineMode == 1) {
            try {
                for (int i = 0; i < realSections.length; i++) {
                    if (i < revealDownToSection) {
                        if (fake == null) {
                            fake = realSections[i].copy();
                            for (int x = 0; x < 16; x++)
                                for (int y = 0; y < 16; y++)
                                    for (int z = 0; z < 16; z++)
                                        fake.setBlockState(x, y, z, mask, false);
                        }
                        realSections[i] = fake.copy();
                    }
                }
                return new ClientboundLevelChunkWithLightPacket(chunk, level.getLightEngine(), null, null);
            } finally {
                System.arraycopy(backup, 0, realSections, 0, backup.length);
            }
        } else  {
            // Find the reference section to copy — the lowest visible one
            // This is the section just at the cutoff, which is typically stone/ground
            LevelChunkSection referenceSectionToCopy = realSections[Math.max(revealDownToSection, 0)];

            try {
                for (int i = 0; i < revealDownToSection; i++) {
                    realSections[i] = referenceSectionToCopy.copy();
                }
                return new ClientboundLevelChunkWithLightPacket(chunk, level.getLightEngine(), null, null);
            } finally {
                System.arraycopy(backup, 0, realSections, 0, backup.length);
            }
        }

    }

    private static BlockState getMaskState() {
        String blockId = CONFIG.maskBlock;
        Optional<Holder.Reference<Block>> optional =
                BuiltInRegistries.BLOCK.get(Identifier.parse(blockId));
        if (optional.isEmpty()) {
            AntiFreecamMod.LOGGER.warn("[AntiFreecam] maskBlock '{}' not found, falling back to stone", blockId);
            return Blocks.STONE.defaultBlockState();
        }
        return optional.get().value().defaultBlockState();
    }

    public static boolean isEntityInMaskedSection(Entity entity, ServerPlayer player) {
        // reuse your existing player Y / surface threshold logic
        int effectiveY = Math.min((int) Math.floor(player.getY()), AntiFreecamMod.CONFIG.surfaceThreshold);
        int currentPlayerSection = Math.floorDiv(effectiveY, 16);
        int effectiveEntityY = Math.min((int) Math.floor(entity.getY()), AntiFreecamMod.CONFIG.surfaceThreshold);
        int currentEntitySection = Math.floorDiv(effectiveEntityY, 16);
        return currentEntitySection < currentPlayerSection-1;
    }
}