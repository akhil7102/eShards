package site.rudracore.eshards.storage;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import site.rudracore.eshards.eShards;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class ShardStorage {
    private final eShards plugin;
    private File dataFile;
    private FileConfiguration dataConfig;

    public ShardStorage(eShards plugin) {
        this.plugin = plugin;
        init();
    }

    private void init() {
        dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create data.yml", e);
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public Map<UUID, Long> loadAll() {
        Map<UUID, Long> balances = new HashMap<>();
        if (dataConfig.contains("players")) {
            for (String key : dataConfig.getConfigurationSection("players").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(key);
                    long balance = dataConfig.getLong("players." + key);
                    balances.put(uuid, balance);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in data.yml: " + key);
                }
            }
        }
        return balances;
    }

    public void saveAll(Map<UUID, Long> balances) {
        dataConfig.set("players", null);
        for (Map.Entry<UUID, Long> entry : balances.entrySet()) {
            dataConfig.set("players." + entry.getKey().toString(), entry.getValue());
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save data.yml", e);
        }
    }
}
