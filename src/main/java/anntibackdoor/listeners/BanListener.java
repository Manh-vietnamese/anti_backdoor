package anntibackdoor.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import anntibackdoor.SupportServer;
import anntibackdoor.managers.BanManager;

public class BanListener implements Listener {
    private final BanManager banManager;

    public BanListener(SupportServer plugin) {
        this.banManager = plugin.getBanManager();
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        String playerName = event.getPlayer().getName();
        if (banManager.isBanned(playerName)) {
            String banMessage = banManager.getBanMessage(playerName);
            // Hủy kết nối ngay lập tức
            event.setResult(PlayerLoginEvent.Result.KICK_BANNED);
            event.setKickMessage(banMessage);
        }
    }
}