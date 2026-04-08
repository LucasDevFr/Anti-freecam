package me.antifreecam;

import me.antifreecam.config.AntiFrecamConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static net.minecraft.commands.Commands.literal;

public class AntiFreecamMod implements ModInitializer {

    public static final String MOD_ID = "antifreecam";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static AntiFrecamConfig CONFIG;

    @Override
    public void onInitialize() {
        CONFIG = AntiFrecamConfig.load();
        LOGGER.info("[AntiFreecam] Loaded. Y-range: ±{} sections, Surface threshold: Y={}",
                CONFIG.sectionRadius, CONFIG.surfaceThreshold);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    literal("antifreecam")
                            .requires(src -> src.permissions().hasPermission(Permissions.COMMANDS_MODERATOR))
                            .then(literal("reload")
                                    .executes(ctx -> {
                                        CONFIG = AntiFrecamConfig.load();
                                        ctx.getSource().sendSuccess(
                                                () -> Component.literal("[AntiFrecam] Config reloaded."), true);
                                        return 1;
                                    })
                            )
                            .then(literal("status")
                                    .executes(ctx -> {
                                        CommandSourceStack src = ctx.getSource();
                                        src.sendSuccess(() -> Component.literal(
                                                "[AntiFrecam] enabled=" + CONFIG.enabled
                                                        + " | sectionRadius=" + CONFIG.sectionRadius
                                                        + " | surfaceThreshold=" + CONFIG.surfaceThreshold
                                                        + " | maskBlock=" + CONFIG.maskBlock), false);
                                        return 1;
                                    })
                            )
            );
        });
    }
}