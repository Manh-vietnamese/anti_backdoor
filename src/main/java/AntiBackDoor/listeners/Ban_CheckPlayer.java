package AntiBackDoor.listeners;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLoginEvent;

public class Ban_CheckPlayer implements Listener {
    /**
     * Xử lý sự kiện đăng nhập của người chơi
     * Kiểm tra xem người chơi có bị cấm trong danh sách cấm của Minecraft không
     * @param event Sự kiện đăng nhập
     */
    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        // Lấy danh sách cấm theo tên người chơi
        BanList banList = Bukkit.getBanList(BanList.Type.NAME);
        
        // Kiểm tra xem người chơi có bị cấm không
        if (banList.isBanned(event.getPlayer().getName())) {
            // Nếu bị cấm, từ chối đăng nhập và hiển thị lý do
            event.disallow(
                PlayerLoginEvent.Result.KICK_BANNED, 
                banList.getBanEntry(event.getPlayer().getName()).getReason()
            );
        }
    }
}