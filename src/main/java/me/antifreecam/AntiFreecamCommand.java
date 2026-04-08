package me.antifreecam;

public class AntiFreecamCommand {
    private static final Set<UUID> exemptPlayers = new HashSet<>();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("antifreecam")
                        .requires(source -> source.hasPermission(2)) // op level 2
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