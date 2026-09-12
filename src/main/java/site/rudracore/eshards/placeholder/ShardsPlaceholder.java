package site.rudracore.eshards.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import site.rudracore.eshards.eShards;

public class ShardsPlaceholder extends PlaceholderExpansion {
    private final eShards plugin;

    public ShardsPlaceholder(eShards plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return "eshards";
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player == null) {
            return "";
        }

        if (params.equalsIgnoreCase("balance")) {
            return String.valueOf(plugin.getShardManager().getBalance(player.getUniqueId()));
        }

        if (params.equalsIgnoreCase("timer")) {
            return "";
        }

        return null;
    }
}
