package anntibackdoor.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import anntibackdoor.SupportServer;

public class PlayerJoinListener implements Listener {
    private final SupportServer plugin;

    public PlayerJoinListener(SupportServer plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
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