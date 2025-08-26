package com.jinzo.commands;

import com.jinzo.KillTracker;
import com.jinzo.commands.admin.set.lastKillerKill;
import com.jinzo.commands.admin.subtractKill;
import com.jinzo.commands.use.infoKill;
import com.jinzo.commands.use.lastKill;
import com.jinzo.commands.use.showKill;
import com.jinzo.commands.use.resetKill;
import com.jinzo.commands.admin.addKill;
import com.jinzo.commands.admin.reloadKill;
import com.jinzo.commands.admin.set.amountKill;
import com.jinzo.commands.admin.set.streakKill;
import com.jinzo.commands.admin.set.lastKilledKill;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class killCommand implements CommandExecutor, TabCompleter {

    private final KillTracker plugin = KillTracker.getInstance();

    // Instances of each subcommand
    private final infoKill infoKillCmd = new infoKill();
    private final lastKill lastKillCmd = new lastKill();
    private final showKill showKillCmd = new showKill();
    private final resetKill resetKillCmd = new resetKill();
    private final addKill addKillCmd = new addKill();
    private final subtractKill subtractKillCmd = new subtractKill();
    private final reloadKill reloadKillCmd = new reloadKill();
    private final amountKill amountKillCmd = new amountKill();
    private final streakKill streakKillCmd = new streakKill();
    private final lastKilledKill lastKilledKillCmd = new lastKilledKill();
    private final lastKillerKill lastKillerKillCmd = new lastKillerKill();

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args
    ) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return false;
        }

        if (args.length == 0) {
            sendUsage(player);
            return false;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "info" -> {
                if (!player.hasPermission("killtracker.use")) {
                    noPerm(player);
                    return false;
                }
                return infoKillCmd.onCommand(sender, command, label, args);
            }
            case "last" -> {
                if (!player.hasPermission("killtracker.use")) {
                    noPerm(player);
                    return false;
                }
                return lastKillCmd.onCommand(sender, command, label, args);
            }
            case "show" -> {
                if (!player.hasPermission("killtracker.show")) {
                    noPerm(player);
                    return false;
                }
                return showKillCmd.onCommand(sender, command, label, args);
            }
            case "reset" -> {
                if (!player.hasPermission("killtracker.reset")) {
                    noPerm(player);
                    return false;
                }
                return resetKillCmd.onCommand(sender, command, label, args);
            }
            case "add" -> {
                if (!player.hasPermission("killtracker.change")) {
                    noPerm(player);
                    return false;
                }
                return addKillCmd.onCommand(sender, command, label, args);
            }
            case "subtract", "sub" -> {
                if (!player.hasPermission("killtracker.change")) {
                    noPerm(player);
                    return false;
                }
                return subtractKillCmd.onCommand(sender, command, label, args);
            }
            case "reload" -> {
                if (!player.hasPermission("killtracker.reload")) {
                    noPerm(player);
                    return false;
                }
                return reloadKillCmd.onCommand(sender, command, label, args);
            }
            case "set" -> {
                if (!player.hasPermission("killtracker.set")) {
                    noPerm(player);
                    return false;
                }
                if (args.length < 3) {
                    player.sendMessage(Component.text("Usage: /kt set {amount/streak/killed/killer} {amount/name} [#force]", NamedTextColor.YELLOW));
                    return false;
                }
                String type = args[1].toLowerCase();
                switch (type) {
                    case "amount" -> {
                        return amountKillCmd.onCommand(sender, command, label, args);
                    }
                    case "streak" -> {
                        return streakKillCmd.onCommand(sender, command, label, args);
                    }
                    case "killed" -> {
                        return lastKilledKillCmd.onCommand(sender, command, label, args);
                    }
                    case "killer" -> {
                        return lastKillerKillCmd.onCommand(sender, command, label, args);
                    }
                    default -> {
                        player.sendMessage(Component.text("Unknown set type: " + type, NamedTextColor.RED));
                        return false;
                    }
                }
            }
            case "help", "?" -> {
                if (!player.hasPermission("killtracker.use")) {
                    noPerm(player);
                    return false;
                }
                sendFullUsage(player);
                return true;
            }
            default -> {
                player.sendMessage(Component.text("Unknown subcommand: " + sub, NamedTextColor.RED));
                sendUsage(player);
                return false;
            }
        }
    }

    private void sendUsage(Player player) {
        player.sendMessage(Component.text("Usage:", NamedTextColor.YELLOW));
        player.sendMessage(Component.text("/kt info", NamedTextColor.GRAY));
        player.sendMessage(Component.text("/kt last", NamedTextColor.GRAY));
        if (player.hasPermission("killtracker.show")) {
            player.sendMessage(Component.text("/kt show", NamedTextColor.GRAY));
        }
        if (player.hasPermission("killtracker.reset")) {
            player.sendMessage(Component.text("/kt reset", NamedTextColor.GRAY));
        }
        if (player.hasPermission("killtracker.change")) {
            player.sendMessage(Component.text("/kt add {amount}", NamedTextColor.GRAY));
            player.sendMessage(Component.text("/kt subtract {amount}", NamedTextColor.GRAY));
            player.sendMessage(Component.text("/kt sub {amount}", NamedTextColor.GRAY));
        }
        if (player.hasPermission("killtracker.reload")) {
            player.sendMessage(Component.text("/kt reload", NamedTextColor.GRAY));
        }
        if (player.hasPermission("killtracker.set")) {
            player.sendMessage(Component.text("/kt set {amount/streak/killed/killer} {amount/name} [#force]", NamedTextColor.GRAY));
        }
        player.sendMessage(Component.text("/kt help", NamedTextColor.GRAY));
    }

    private void sendFullUsage(Player player) {
        player.sendMessage(Component.text("KillTracker Commands:", NamedTextColor.GOLD));
        player.sendMessage(Component.text("/kt info - View your kill stats", NamedTextColor.GRAY));
        player.sendMessage(Component.text("/kt last - View your last kill", NamedTextColor.GRAY));
        if (player.hasPermission("killtracker.show")) {
            player.sendMessage(Component.text("/kt show - Show kills for a specific weapon", NamedTextColor.GRAY));
        }
        if (player.hasPermission("killtracker.reset")) {
            player.sendMessage(Component.text("/kt reset - Reset your kill stats", NamedTextColor.GRAY));
        }
        if (player.hasPermission("killtracker.change")) {
            player.sendMessage(Component.text("/kt add {amount} - Add kills to your stats", NamedTextColor.GRAY));
            player.sendMessage(Component.text("/kt subtract {amount} - Subtract kills from your stats", NamedTextColor.GRAY));
            player.sendMessage(Component.text("/kt sub {amount} - Alias for subtract", NamedTextColor.GRAY));
        }
        if (player.hasPermission("killtracker.reload")) {
            player.sendMessage(Component.text("/kt reload - Reload the plugin configuration", NamedTextColor.GRAY));
        }
        if (player.hasPermission("killtracker.set")) {
            player.sendMessage(Component.text("/kt set {amount/streak/killed/killer} {amount/name} [#force] - Set data to a specific amount", NamedTextColor.GRAY));
        }
        player.sendMessage(Component.text("/kt help - Show this help message", NamedTextColor.GRAY));
    }

    private void noPerm(Player player) {
        player.sendMessage(Component.text("You do not have permission to use this command.", NamedTextColor.RED));
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String alias,
            @NotNull String[] args
    ) {
        if (!(sender instanceof Player player) || !player.hasPermission("killtracker.use")) {
            return List.of();
        }

        if (args.length == 1) {
            return Stream.of(
                            "info", "last", "show", "reset", "help", "?",
                            "add", "subtract", "sub", "reload", "set"
                    )
                    .filter(sub -> {
                        // Map subcommands to real permission nodes
                        String perm;
                        switch (sub) {
                            // Basic user commands
                            case "info", "last", "help", "?" -> perm = "killtracker.use";

                            // Admin commands
                            case "show" -> perm = "killtracker.show";
                            case "reset" -> perm = "killtracker.reset";
                            case "add" -> perm = "killtracker.change";
                            case "subtract", "sub" -> perm = "killtracker.change";
                            case "reload" -> perm = "killtracker.reload";
                            case "set" -> perm = "killtracker.set";
                            default -> perm = "killtracker.use";
                        }
                        return player.hasPermission(perm);
                    })
                    .filter(sub -> sub.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        // Number suggestions for add/subtract commands
        if (args.length == 2 &&
                (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("subtract") || args[0].equalsIgnoreCase("sub"))) {
            if (player.hasPermission("killtracker.change")) {
                return Stream.of("1", "5", "10", "50", "100", "{amount}")
                        .filter(num -> num.startsWith(args[1]))
                        .collect(Collectors.toList());
            }
        }

        // Set command suggestions
        if (args[0].equalsIgnoreCase("set")) {
            if (args.length == 2) {
                return Stream.of("amount", "streak", "killed", "killer")
                        .filter(type -> type.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }

            if (args.length == 3) {
                if (args[1].equalsIgnoreCase("killed") || args[1].equalsIgnoreCase("killer")) {
                    // Provide list of online player names for last killed, also if config.count_mob_kills is true, suggest mob names
                    return plugin.getServer().getOnlinePlayers().stream()
                            .map(Player::getName)
                            .filter(name -> name.startsWith(args[2]))
                            .collect(Collectors.toList());
                } else {
                    return Stream.of("0", "1", "5", "10", "50", "100", "{amount}")
                            .filter(num -> num.startsWith(args[2]))
                            .collect(Collectors.toList());
                }
            }

            if (args.length == 4) {
                return Stream.of("#force")
                        .filter(force -> force.startsWith(args[3].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return List.of();
    }
}
