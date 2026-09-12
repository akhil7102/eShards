package site.rudracore.eshards.manager;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import site.rudracore.eshards.eShards;
import site.rudracore.eshards.region.AfkRegion;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AfkManager {
    private final eShards plugin;
    private AfkRegion region;
    
    private final Map<UUID, Integer> activeSessions = new HashMap<>();

    private int taskId = -1;

    public AfkManager(eShards plugin) {
        this.plugin = plugin;
    }

    public void startTask() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }

        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::tick, 20L, 20L);
    }

    public void stopTask() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            taskId = -1;
        }
        activeSessions.clear();
    }

    public void setRegion(AfkRegion region) {
        this.region = region;
    }

    public AfkRegion getRegion() {
        return region;
    }

    private void tick() {
        if (!plugin.getConfig().getBoolean("afk.enabled", true) || region == null) {
            return;
        }

        int interval = plugin.getConfig().getInt("afk.reward-interval-seconds", 300);
        long rewardAmount = plugin.getConfig().getLong("afk.shards-per-reward", 1);

        for (Player p : Bukkit.getOnlinePlayers()) {
            UUID uuid = p.getUniqueId();
            if (region.contains(p.getLocation())) {
                int currentSeconds = activeSessions.getOrDefault(uuid, 0) + 1;
                activeSessions.put(uuid, currentSeconds);

                sendActionBar(p, currentSeconds);

                if (currentSeconds % interval == 0) {
                    plugin.getShardManager().addBalance(uuid, rewardAmount);
                    sendRewardNotification(p, rewardAmount);
                }
            } else {
                if (activeSessions.containsKey(uuid)) {
                    activeSessions.remove(uuid);
                }
            }
        }
    }

    private String formatTime(int seconds) {
        if (seconds < 60) return seconds + "s";
        int minutes = seconds / 60;
        int s = seconds % 60;
        if (minutes < 60) {
            return minutes + "m " + s + "s";
        }
        int hours = minutes / 60;
        int m = minutes % 60;
        return hours + "h " + m + "m " + s + "s";
    }

    private void sendActionBar(Player player, int seconds) {
        String raw = plugin.getConfig().getString("messages.afk-actionbar", "#7A4DFFYou have been AFK for <white>%timer%#7A4DFF.");
        raw = raw.replace("%timer%", formatTime(seconds));
        player.sendActionBar(plugin.parseColor(raw));
    }

    private void sendRewardNotification(Player p, long rewardAmount) {
        long totalBalance = plugin.getShardManager().getBalance(p.getUniqueId());

        if (plugin.getConfig().getBoolean("reward.title.enabled", true)) {
            String titleRaw = plugin.getConfig().getString("messages.reward-title", "#7A4DFF+%reward_amount% Shard");
            String subtitleRaw = plugin.getConfig().getString("messages.reward-subtitle", "<white>You Now Own: #7A4DFF%eshards_balance% Shards");

            titleRaw = titleRaw.replace("%reward_amount%", String.valueOf(rewardAmount))
                               .replace("%eshards_balance%", String.valueOf(totalBalance));
            subtitleRaw = subtitleRaw.replace("%reward_amount%", String.valueOf(rewardAmount))
                                     .replace("%eshards_balance%", String.valueOf(totalBalance));

            int fadeIn = plugin.getConfig().getInt("reward.title.fade-in", 10);
            int stay = plugin.getConfig().getInt("reward.title.stay", 35);
            int fadeOut = plugin.getConfig().getInt("reward.title.fade-out", 10);

            Title title = Title.title(
                plugin.parseColor(titleRaw),
                plugin.parseColor(subtitleRaw),
                Title.Times.times(Duration.ofMillis(fadeIn * 50L), Duration.ofMillis(stay * 50L), Duration.ofMillis(fadeOut * 50L))
            );
            p.showTitle(title);
        }

        if (plugin.getConfig().getBoolean("reward.sound.enabled", true)) {
            String type = plugin.getConfig().getString("reward.sound.type", "ENTITY_EXPERIENCE_ORB_PICKUP");
            float volume = (float) plugin.getConfig().getDouble("reward.sound.volume", 1.0);
            float pitch = (float) plugin.getConfig().getDouble("reward.sound.pitch", 1.0);
            try {
                Sound sound = Sound.valueOf(type.toUpperCase());
                p.playSound(p.getLocation(), sound, volume, pitch);
            } catch (IllegalArgumentException ignored) {}
        }
    }
}
