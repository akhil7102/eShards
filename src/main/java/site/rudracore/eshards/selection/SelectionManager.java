package site.rudracore.eshards.selection;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import site.rudracore.eshards.eShards;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SelectionManager implements Listener {
    private final eShards plugin;

    private final Map<UUID, Location> pos1Map = new HashMap<>();
    private final Map<UUID, Location> pos2Map = new HashMap<>();

    public SelectionManager(eShards plugin) {
        this.plugin = plugin;
    }

    public void giveWand(Player player) {
        ItemStack wand = new ItemStack(Material.GOLDEN_AXE);
        ItemMeta meta = wand.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.AQUA + "eShards AFK Wand");
            wand.setItemMeta(meta);
        }
        player.getInventory().addItem(wand);
        plugin.sendMessage(player, "wand-received", null, null);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("eshards.admin")) return;

        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.GOLDEN_AXE) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName() || !meta.getDisplayName().equals(ChatColor.AQUA + "eShards AFK Wand")) {
            return;
        }

        Action action = event.getAction();
        if (action == Action.LEFT_CLICK_BLOCK) {
            event.setCancelled(true);
            Location loc = event.getClickedBlock().getLocation();
            pos1Map.put(player.getUniqueId(), loc);
            player.sendMessage(ChatColor.GREEN + "Position 1 selected: " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            event.setCancelled(true);
            Location loc = event.getClickedBlock().getLocation();
            pos2Map.put(player.getUniqueId(), loc);
            player.sendMessage(ChatColor.GREEN + "Position 2 selected: " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
        }
    }

    public Location getPos1(UUID uuid) {
        return pos1Map.get(uuid);
    }

    public Location getPos2(UUID uuid) {
        return pos2Map.get(uuid);
    }

    public void clear(UUID uuid) {
        pos1Map.remove(uuid);
        pos2Map.remove(uuid);
    }
}
