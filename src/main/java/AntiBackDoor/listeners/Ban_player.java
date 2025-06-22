package AntiBackDoor.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLoginEvent;

import AntiBackDoor.Main_plugin;
import AntiBackDoor.Managers.OP_BanManager;

public class Ban_player implements Listener {
    private final OP_BanManager banManager;

    public Ban_player(Main_plugin plugin) {
        this.banManager = plugin.getBanManager();
    }

    /**
     * Xử lý sự kiện đăng nhập của người chơi
     * Kiểm tra xem người chơi có bị cấm bởi hệ thống cấm của plugin không
     * @param event Sự kiện đăng nhập
     */
    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        String playerName = event.getPlayer().getName();
        
        // Kiểm tra xem người chơi có bị cấm không
        if (banManager.isBanned(playerName)) {
            // Lấy thông báo cấm
            String banMessage = banManager.getBanMessage(playerName);
            
            // Từ chối đăng nhập và hiển thị thông báo
            event.setResult(PlayerLoginEvent.Result.KICK_BANNED);
            event.setKickMessage(banMessage);
        }
    }
}