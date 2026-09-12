package site.rudracore.eshards;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import site.rudracore.eshards.command.ShardsCommand;
import site.rudracore.eshards.manager.AfkManager;
import site.rudracore.eshards.manager.ShardManager;
import site.rudracore.eshards.placeholder.ShardsPlaceholder;
import site.rudracore.eshards.region.AfkRegion;
import site.rudracore.eshards.selection.SelectionManager;
import site.rudracore.eshards.storage.ShardStorage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

public class eShards extends JavaPlugin {
    private ShardStorage storage;
    private ShardManager manager;
    private AfkManager afkManager;
    private SelectionManager selectionManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        storage = new ShardStorage(this);
        manager = new ShardManager(this, storage);
        afkManager = new AfkManager(this);
        selectionManager = new SelectionManager(this);

        getServer().getPluginManager().registerEvents(selectionManager, this);

        loadAfkRegion();
        afkManager.startTask();

        getCommand("shards").setExecutor(new ShardsCommand(this));
        getCommand("shards").setTabCompleter(new ShardsCommand(this));

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ShardsPlaceholder(this).register();
            getLogger().info("PlaceholderAPI expansion registered.");
        }

        getLogger().info("eShards has been enabled.");

        int pluginId = 34013;
        org.bstats.bukkit.Metrics metrics = new org.bstats.bukkit.Metrics(this, pluginId);
    }

    @Override
    public void onDisable() {
        if (manager != null) {
            manager.shutdown();
        }
        if (afkManager != null) {
            afkManager.stopTask();
        }
        getLogger().info("eShards has been disabled.");
    }

    public ShardManager getShardManager() {
        return manager;
    }

    public AfkManager getAfkManager() {
        return afkManager;
    }

    public SelectionManager getSelectionManager() {
        return selectionManager;
    }

    public void reloadPlugin() {
        reloadConfig();
        if (manager != null) {
            manager.loadSettings();
        }
        loadAfkRegion();
    }

    public void loadAfkRegion() {
        if (getConfig().getBoolean("afk-region.enabled", false)) {
            String world = getConfig().getString("afk-region.world");
            int minX = getConfig().getInt("afk-region.pos1.x");
            int minY = getConfig().getInt("afk-region.pos1.y");
            int minZ = getConfig().getInt("afk-region.pos1.z");
            int maxX = getConfig().getInt("afk-region.pos2.x");
            int maxY = getConfig().getInt("afk-region.pos2.y");
            int maxZ = getConfig().getInt("afk-region.pos2.z");
            afkManager.setRegion(new AfkRegion(world, minX, minY, minZ, maxX, maxY, maxZ));
        } else {
            afkManager.setRegion(null);
        }
    }

    public void saveAfkRegion(AfkRegion region) {
        if (region == null) {
            getConfig().set("afk-region.enabled", false);
        } else {
            getConfig().set("afk-region.enabled", true);
            getConfig().set("afk-region.world", region.getWorld());
            getConfig().set("afk-region.pos1.x", region.getMinX());
            getConfig().set("afk-region.pos1.y", region.getMinY());
            getConfig().set("afk-region.pos1.z", region.getMinZ());
            getConfig().set("afk-region.pos2.x", region.getMaxX());
            getConfig().set("afk-region.pos2.y", region.getMaxY());
            getConfig().set("afk-region.pos2.z", region.getMaxZ());
        }
        saveConfig();
        loadAfkRegion();
    }

    public Component parseColor(String str) {
        if (str == null) return Component.empty();
        str = str.replaceAll("#([A-Fa-f0-9]{6})", "<#$1>");
        str = str.replace("&0", "<black>").replace("&1", "<dark_blue>").replace("&2", "<dark_green>")
                 .replace("&3", "<dark_aqua>").replace("&4", "<dark_red>").replace("&5", "<dark_purple>")
                 .replace("&6", "<gold>").replace("&7", "<gray>").replace("&8", "<dark_gray>")
                 .replace("&9", "<blue>").replace("&a", "<green>").replace("&b", "<aqua>")
                 .replace("&c", "<red>").replace("&d", "<light_purple>").replace("&e", "<yellow>")
                 .replace("&f", "<white>").replace("&k", "<obfuscated>").replace("&l", "<bold>")
                 .replace("&m", "<strikethrough>").replace("&n", "<underlined>").replace("&o", "<italic>")
                 .replace("&r", "<reset>");
        return MiniMessage.miniMessage().deserialize(str);
    }
    
    public void sendMessage(org.bukkit.command.CommandSender sender, String path, String target, String amount) {
        String prefix = getConfig().getString("messages.prefix", "&8[&bShards&8] ");
        String msg = getConfig().getString("messages." + path, "");
        if (msg.isEmpty()) return;
        
        String full = prefix + msg;
        if (target != null) full = full.replace("<player>", target);
        if (amount != null) full = full.replace("<amount>", amount);

        sender.sendMessage(parseColor(full));
    }
}
