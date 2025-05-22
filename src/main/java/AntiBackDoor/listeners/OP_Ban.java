package AntiBackDoor.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLoginEvent;

import AntiBackDoor.Main_plugin;
import AntiBackDoor.managers.OP_BanManager;

public class OP_Ban implements Listener {
    private final OP_BanManager banManager;

    public OP_Ban(Main_plugin plugin) {
        this.banManager = plugin.getBanManager();
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        String playerName = event.getPlayer().getName();
        if (banManager.isBanned(playerName)) {
            String banMessage = banManager.getBanMessage(playerName);
            // Huỷ kết nối ngay lập tức
            event.setResult(PlayerLoginEvent.Result.KICK_BANNED);
            event.setKickMessage(banMessage); // Chỉ sử dụng thông báo từ plugin
        }
    }
}
