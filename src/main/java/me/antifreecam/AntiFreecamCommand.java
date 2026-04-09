package me.antifreecam;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.Permissions;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AntiFreecamCommand {
    private static final Set<UUID> exemptPlayers = new HashSet<>();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("antifreecam")
                        .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_MODERATOR)) // op level 2
                        .then(Commands.literal("exempt")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.literal("add")
                                                .executes(ctx -> {
                                                    ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                                    exemptPlayers.add(player.getUUID());
                                                    ctx.getSource().sendSuccess(
                                                            () -> Component.literal(player.getName().getString() + " is now exempt from AntiFreecam"), true);
                                                    return 1;
                                                }))
                                        .then(Commands.literal("remove")
                                                .executes(ctx -> {
                                                    ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                                    exemptPlayers.remove(player.getUUID());
                                                    ctx.getSource().sendSuccess(
                                                            () -> Component.literal(player.getName().getString() + " is no longer exempt from AntiFreecam"), true);
                                                    return 1;
                                                }))
                                        .then(Commands.literal("check")
                                                .executes(ctx -> {
                                                    ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                                    boolean exempt = exemptPlayers.contains(player.getUUID());
                                                    ctx.getSource().sendSuccess(
                                                            () -> Component.literal(player.getName().getString() + " is " + (exempt ? "exempt" : "not exempt")), false);
                                                    return 1;
                                                }))
                                )
                        )
        );
    }

    public static boolean isExempt(ServerPlayer player) {
        return exemptPlayers.contains(player.getUUID());
    }
}