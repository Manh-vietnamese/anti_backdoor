package AntiBackDoor.listeners;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import AntiBackDoor.Main_plugin;
import AntiBackDoor.Messenger.Messager;

public class Listener_ANTI_Create implements Listener {
    private final Main_plugin plugin;

    public Listener_ANTI_Create(Main_plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Xử lý sự kiện khi người chơi thực hiện lệnh
     * Chặn lệnh nếu người chơi ở chế độ Creative mà không có quyền
     * @param e Sự kiện thực hiện lệnh
     */
    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        // Kiểm tra xem tính năng chống lạm dụng Creative có được bật không
        if (!plugin.getConfig().getBoolean("Anti_Create_Abuse.enabled", true)) return;
        if (!plugin.getConfig().getBoolean("Anti_Create_Abuse.block_commands", true)) return;

        Player player = e.getPlayer();
        
        // Kiểm tra điều kiện chặn lệnh
        if (isInCreateMode(player) && !hasCreatePermission(player)) {
            // Hủy lệnh và thông báo cho người chơi
            e.setCancelled(true);
            Messager.send(player, "create_abuse.command");

            // Ghi nhận cảnh báo
            plugin.getWarningManager().addWarning(player);
        }
    }

    /**
     * Xử lý sự kiện tương tác của người chơi
     * Chặn tương tác nếu người chơi ở chế độ Creative mà không có quyền
     * @param e Sự kiện tương tác
     */
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        // Chặn tương tác nếu không có quyền
        if (isInCreateMode(player) && !hasCreatePermission(player)) {
            e.setCancelled(true);
            Messager.send(player,"create_abuse.interact");

            // Ghi nhận cảnh báo
            plugin.getWarningManager().addWarning(player);
        }
    }

    /**
     * Xử lý sự kiện di chuyển của người chơi
     * Hạn chế di chuyển nếu người chơi ở chế độ Creative mà không có quyền
     * @param e Sự kiện di chuyển
     */
    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        // Kiểm tra xem tính năng có được bật không
        if (!plugin.getConfig().getBoolean("Anti_Create_Abuse.enabled", true)) return;
        if (!plugin.getConfig().getBoolean("Anti_Create_Abuse.restrict_movement", true)) return;

        Player player = e.getPlayer();
        if (isInCreateMode(player) && !hasCreatePermission(player)) {
            Location from = e.getFrom();
            Location to = e.getTo();
            
            // Kiểm tra nếu người chơi thực sự di chuyển
            if (to != null && to.distanceSquared(from) > 0.0001) {
                // Đưa người chơi về vị trí cũ
                e.setTo(from);
                Messager.send(player,"create_abuse.movement");

                // Ghi nhận cảnh báo
                plugin.getWarningManager().addWarning(player);
            }
        }
    }
    
    // Kiểm tra xem người chơi có đang ở chế độ Creative không
    private boolean isInCreateMode(Player player) {
        return player.getGameMode() == GameMode.CREATIVE;
    }
    
    // Kiểm tra quyền sử dụng chế độ Creative
    private boolean hasCreatePermission(Player player) {
        return player.hasPermission("antibackdoor.create") || 
               player.hasPermission("antibackdoor.Admin");
    }
}
