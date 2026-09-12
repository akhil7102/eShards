package site.rudracore.eshards.manager;

import org.bukkit.Bukkit;
import site.rudracore.eshards.eShards;
import site.rudracore.eshards.storage.ShardStorage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ShardManager {
    private final eShards plugin;
    private final ShardStorage storage;
    private final Map<UUID, Long> balances;
    private long defaultBalance;
    private boolean saveScheduled = false;

    public ShardManager(eShards plugin, ShardStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
        this.balances = new ConcurrentHashMap<>(storage.loadAll());
        loadSettings();
    }

    public void loadSettings() {
        this.defaultBalance = plugin.getConfig().getLong("settings.default-balance", 0);
    }

    public void shutdown() {
        storage.saveAll(balances);
    }

    public long getBalance(UUID uuid) {
        return balances.getOrDefault(uuid, defaultBalance);
    }

    public void setBalance(UUID uuid, long amount) {
        if (amount < 0) amount = 0;
        balances.put(uuid, amount);
        saveAsync();
    }

    public void addBalance(UUID uuid, long amount) {
        if (amount <= 0) return;
        long current = getBalance(uuid);
        if (Long.MAX_VALUE - amount < current) {
            setBalance(uuid, Long.MAX_VALUE);
        } else {
            setBalance(uuid, current + amount);
        }
    }

    public void removeBalance(UUID uuid, long amount) {
        if (amount <= 0) return;
        long current = getBalance(uuid);
        if (current - amount < 0) {
            setBalance(uuid, 0);
        } else {
            setBalance(uuid, current - amount);
        }
    }

    public void resetBalance(UUID uuid) {
        balances.remove(uuid);
        saveAsync();
    }

    public void resetAll() {
        balances.clear();
        saveAsync();
    }
    
    private void saveAsync() {
        if (saveScheduled) return;
        saveScheduled = true;
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            storage.saveAll(balances);
            saveScheduled = false;
        }, 60L);
    }
}
