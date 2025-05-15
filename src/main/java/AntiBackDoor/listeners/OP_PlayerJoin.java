package AntiBackDoor.listeners;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;

import AntiBackDoor.Main_plugin;

public class OP_PlayerJoin implements Listener {
    private final Main_plugin plugin;

    public OP_PlayerJoin(Main_plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Lấy IP
        plugin.logIP(player);
        String ip = player.getAddress().getAddress().getHostAddress();
        UUID uuid = player.getUniqueId();

        // Kiểm tra IP
        if (player.isOp() && !plugin.getWhitelistManager().isValidIP(uuid, ip)) {
            player.kickPlayer(plugin.getMessenger().get("invalid_ip"));
            plugin.getLogger().warning(player.getName() + " tried to login with invalid IP: " + ip);
        }
        
        // Kiểm tra ngay khi join
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOp()) {
                if (!plugin.getWhitelistManager().isAllowed(player.getUniqueId(), player.getName())) {
                    plugin.executeSafetyProtocol(player);
                    
                    // Phòng trường hợp bypass kick
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (player.isOnline()) {
                            player.setOp(false);
                        }
                    });
                }
            }
        }, 40L); // Delay 2 giây để đảm bảo dữ liệu đã load xong

        // Nếu người chơi bị xóa khỏi whitelist nhưng vẫn có OP
        if (player.isOp() && !plugin.getWhitelistManager().isAllowed(player.getUniqueId(), player.getName())) {
            plugin.executeSafetyProtocol(player);
        }
    }
}