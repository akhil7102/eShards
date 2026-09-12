package site.rudracore.eshards.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import site.rudracore.eshards.eShards;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ShardsCommand implements CommandExecutor, TabCompleter {

    private final eShards plugin;

    public ShardsCommand(eShards plugin) {
        this.plugin = plugin;
    }

    private void sendMessage(CommandSender sender, String path) {
        plugin.sendMessage(sender, path, null, null);
    }

    private void sendMessageReplaced(CommandSender sender, String path, String target, String amount) {
        plugin.sendMessage(sender, path, target, amount);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sendMessage(sender, "console-error");
                return true;
            }
            Player p = (Player) sender;
            if (!p.hasPermission("eshards.use")) {
                sendMessage(sender, "no-permission");
                return true;
            }
            long balance = plugin.getShardManager().getBalance(p.getUniqueId());
            sendMessageReplaced(sender, "balance", null, String.valueOf(balance));
            return true;
        }

        String sub = args[0].toLowerCase();

        if (sub.equals("pay")) {
            if (!(sender instanceof Player)) {
                sendMessage(sender, "console-error");
                return true;
            }
            Player p = (Player) sender;
            if (!p.hasPermission("eshards.pay")) {
                sendMessage(sender, "no-permission");
                return true;
            }
            if (args.length < 3) {
                p.sendMessage(ChatColor.RED + "Usage: /shards pay <player> <amount>");
                return true;
            }

            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                sendMessage(sender, "player-not-found");
                return true;
            }

            if (target.getUniqueId().equals(p.getUniqueId())) {
                sendMessage(sender, "pay-self");
                return true;
            }

            long amount;
            try {
                amount = Long.parseLong(args[2]);
            } catch (NumberFormatException e) {
                sendMessage(sender, "pay-invalid-amount");
                return true;
            }

            if (amount <= 0) {
                sendMessage(sender, "pay-invalid-amount");
                return true;
            }

            long current = plugin.getShardManager().getBalance(p.getUniqueId());
            if (current < amount) {
                sendMessage(sender, "pay-not-enough");
                return true;
            }

            plugin.getShardManager().removeBalance(p.getUniqueId(), amount);
            plugin.getShardManager().addBalance(target.getUniqueId(), amount);

            sendMessageReplaced(p, "pay-sent", target.getName(), String.valueOf(amount));
            sendMessageReplaced(target, "pay-received", p.getName(), String.valueOf(amount));
            return true;
        }

        if (sub.equals("wand") && sender.hasPermission("eshards.admin")) {
            if (!(sender instanceof Player)) {
                sendMessage(sender, "console-error");
                return true;
            }
            plugin.getSelectionManager().giveWand((Player) sender);
            return true;
        }

        if (sub.equals("region") && sender.hasPermission("eshards.admin")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /shards region <create|delete|info>");
                return true;
            }
            String regionAction = args[1].toLowerCase();
            if (regionAction.equals("create")) {
                if (!(sender instanceof Player)) {
                    sendMessage(sender, "console-error");
                    return true;
                }
                Player p = (Player) sender;
                org.bukkit.Location pos1 = plugin.getSelectionManager().getPos1(p.getUniqueId());
                org.bukkit.Location pos2 = plugin.getSelectionManager().getPos2(p.getUniqueId());
                if (pos1 == null || pos2 == null) {
                    sender.sendMessage(ChatColor.RED + "You must select both positions with the wand first!");
                    return true;
                }
                if (!pos1.getWorld().getName().equals(pos2.getWorld().getName())) {
                    sender.sendMessage(ChatColor.RED + "Positions must be in the same world!");
                    return true;
                }
                site.rudracore.eshards.region.AfkRegion region = new site.rudracore.eshards.region.AfkRegion(
                    pos1.getWorld().getName(),
                    pos1.getBlockX(), pos1.getBlockY(), pos1.getBlockZ(),
                    pos2.getBlockX(), pos2.getBlockY(), pos2.getBlockZ()
                );
                plugin.saveAfkRegion(region);
                plugin.getSelectionManager().clear(p.getUniqueId());
                sendMessage(sender, "region-created");
                return true;
            } else if (regionAction.equals("delete")) {
                plugin.saveAfkRegion(null);
                sendMessage(sender, "region-deleted");
                return true;
            } else if (regionAction.equals("info")) {
                site.rudracore.eshards.region.AfkRegion region = plugin.getAfkManager().getRegion();
                if (region == null) {
                    sendMessage(sender, "region-not-set");
                } else {
                    sender.sendMessage(plugin.parseColor("#7A4DFFAFK Region Info:"));
                    sender.sendMessage(plugin.parseColor("<white>World: #7A4DFF" + region.getWorld()));
                    sender.sendMessage(plugin.parseColor("<white>Pos1: #7A4DFF" + region.getMinX() + ", " + region.getMinY() + ", " + region.getMinZ()));
                    sender.sendMessage(plugin.parseColor("<white>Pos2: #7A4DFF" + region.getMaxX() + ", " + region.getMaxY() + ", " + region.getMaxZ()));
                }
                return true;
            }
            sender.sendMessage(ChatColor.RED + "Usage: /shards region <create|delete|info>");
            return true;
        }

        if (!sender.hasPermission("eshards.admin")) {
            sendMessage(sender, "no-permission");
            return true;
        }

        if (sub.equals("reload")) {
            plugin.reloadPlugin();
            sendMessage(sender, "reload");
            return true;
        }

        if (sub.equals("reset")) {
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Usage: /shards reset <player|all>");
                return true;
            }
            if (args[1].equalsIgnoreCase("all")) {
                if (args.length >= 3 && args[2].equalsIgnoreCase("confirm")) {
                    plugin.getShardManager().resetAll();
                    sendMessage(sender, "reset-all");
                } else {
                    sendMessage(sender, "reset-all-confirm");
                }
                return true;
            }

            OfflinePlayer target = resolveTarget(args[1]);
            if (target == null) {
                sendMessage(sender, "player-not-found");
                return true;
            }
            plugin.getShardManager().resetBalance(target.getUniqueId());
            sendMessageReplaced(sender, "reset-balance", target.getName(), null);
            return true;
        }

        if (sub.equals("set") || sub.equals("give") || sub.equals("take")) {
            if (args.length < 3) {
                sender.sendMessage(ChatColor.RED + "Usage: /shards " + sub + " <player> <amount>");
                return true;
            }

            long amount;
            try {
                amount = Long.parseLong(args[2]);
            } catch (NumberFormatException e) {
                sendMessage(sender, "invalid-amount");
                return true;
            }

            if (amount < 0) {
                sendMessage(sender, "invalid-amount");
                return true;
            }

            if (sub.equals("give") && args[1].equalsIgnoreCase("all")) {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    plugin.getShardManager().addBalance(online.getUniqueId(), amount);
                }
                sendMessageReplaced(sender, "give-all", null, String.valueOf(amount));
                return true;
            }

            OfflinePlayer target = resolveTarget(args[1]);
            if (target == null) {
                sendMessage(sender, "player-not-found");
                return true;
            }

            if (sub.equals("set")) {
                plugin.getShardManager().setBalance(target.getUniqueId(), amount);
                sendMessageReplaced(sender, "set-balance", target.getName(), String.valueOf(amount));
            } else if (sub.equals("give")) {
                plugin.getShardManager().addBalance(target.getUniqueId(), amount);
                sendMessageReplaced(sender, "give-balance", target.getName(), String.valueOf(amount));
            } else if (sub.equals("take")) {
                plugin.getShardManager().removeBalance(target.getUniqueId(), amount);
                sendMessageReplaced(sender, "take-balance", target.getName(), String.valueOf(amount));
            }

            return true;
        }

        OfflinePlayer target = resolveTarget(args[0]);
        if (target != null) {
            long balance = plugin.getShardManager().getBalance(target.getUniqueId());
            sendMessageReplaced(sender, "balance-other", target.getName(), String.valueOf(balance));
        } else {
            sendMessage(sender, "player-not-found");
        }

        return true;
    }

    private OfflinePlayer resolveTarget(String name) {
        Player online = Bukkit.getPlayerExact(name);
        if (online != null) return online;

        try {
            return Bukkit.getOfflinePlayerIfCached(name);
        } catch (NoSuchMethodError e) {
            return Bukkit.getOfflinePlayer(name);
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission("eshards.pay")) {
                completions.add("pay");
            }
            if (sender.hasPermission("eshards.admin")) {
                completions.add("set");
                completions.add("give");
                completions.add("take");
                completions.add("reset");
                completions.add("reload");
                completions.add("wand");
                completions.add("region");
                completions.addAll(getOnlinePlayerNames());
            } else {
                completions.addAll(getOnlinePlayerNames());
            }
        } else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("pay") && sender.hasPermission("eshards.pay")) {
                completions.addAll(getOnlinePlayerNames());
            } else if (sender.hasPermission("eshards.admin")) {
                if (sub.equals("set") || sub.equals("take")) {
                    completions.addAll(getOnlinePlayerNames());
                } else if (sub.equals("give") || sub.equals("reset")) {
                    completions.addAll(getOnlinePlayerNames());
                    completions.add("all");
                } else if (sub.equals("region")) {
                    completions.add("create");
                    completions.add("delete");
                    completions.add("info");
                }
            }
        } else if (args.length == 3 && sender.hasPermission("eshards.admin")) {
            String sub = args[0].toLowerCase();
            if (sub.equals("reset") && args[1].equalsIgnoreCase("all")) {
                completions.add("confirm");
            }
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }

    private List<String> getOnlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
    }
}
