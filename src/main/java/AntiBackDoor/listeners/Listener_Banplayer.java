package AntiBackDoor.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLoginEvent;

import AntiBackDoor.Main_plugin;
import AntiBackDoor.Managers.Managers_OP_Ban;

public class Listener_Banplayer implements Listener {
    private final Managers_OP_Ban banManager;

    public Listener_Banplayer(Main_plugin plugin) {
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
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(banMessage);
        }
    }
}