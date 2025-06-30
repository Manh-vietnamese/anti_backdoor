package AntiBackDoor.listeners;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

import AntiBackDoor.Main_plugin;
import AntiBackDoor.Messenger.Messager;

public class Listener_ANTI_GiveItem implements Listener {
    private final Main_plugin plugin;

    public Listener_ANTI_GiveItem(Main_plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Xử lý sự kiện thay đổi chế độ chơi
     * Quản lý kho đồ khi chuyển vào/ra khỏi chế độ Creative
     * @param event Sự kiện thay đổi chế độ chơi
     */
    @EventHandler
    public void onGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        
        // Bỏ qua người chơi có quyền bypass
        if (player.hasPermission("antibackdoor.bypass.giveitem")) {
            return;
        }
        
        GameMode newMode = event.getNewGameMode();
        GameMode oldMode = player.getGameMode();
        
        // Khi chuyển SANG Creative: Lưu kho đồ hiện tại
        if (newMode == GameMode.CREATIVE) {
            plugin.getAntiGiveItemManager().saveCreativeInventory(player);
        } 
        // Khi chuyển RA KHỎI Creative: Xóa kho đồ đã lưu
        else if (oldMode == GameMode.CREATIVE) {
            plugin.getAntiGiveItemManager().removeSavedInventory(player);
        }
    }

    /**
     * Xử lý sự kiện nhặt vật phẩm
     * Ngăn chặn việc nhặt vật phẩm trong chế độ Creative nếu không có quyền
     * @param event Sự kiện nhặt vật phẩm
     */
    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        // Kiểm tra xem tính năng có được bật không
        if (!plugin.getConfig().getBoolean("Anti_Give_Item.enabled", true)) return;
        
        // Chỉ xử lý nếu entity là người chơi
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        
        // Bỏ qua người chơi có quyền bypass
        if (player.hasPermission("antibackdoor.bypass.giveitem")) {
            return;
        }
        
        // Chỉ xử lý khi ở chế độ Creative
        if (player.getGameMode() == GameMode.CREATIVE) {
            event.setCancelled(true); // Hủy sự kiện nhặt vật phẩm
        }
    }

    /**
     * Xử lý sự kiện thả vật phẩm của người chơi
     * Ngăn chặn việc thả vật phẩm trong chế độ Creative nếu không có quyền
     * @param event Sự kiện thả vật phẩm
     */
    @EventHandler
    public void onItemDrop(PlayerDropItemEvent event) {
        // Kiểm tra xem tính năng chống Give Item có được bật không
        if (!plugin.getConfig().getBoolean("Anti_Give_Item.enabled", true)) return;
        
        Player player = event.getPlayer();
        
        // Cho phép người chơi có quyền bypass tiếp tục thả vật phẩm
        // if (player.hasPermission("antibackdoor.bypass.giveitem")) {
        //     return;
        // }
        
        // Chỉ xử lý khi người chơi ở chế độ Creative
        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE) {
            // Hủy sự kiện thả vật phẩm
            event.setCancelled(true);
            // Cảnh báo người chơi
            Messager.send(player,"anti_give_item.drop_warning");
            
            // Ghi nhận cảnh báo vào hệ thống
            plugin.getWarningManager().addWarning(player);
            
            // Kiểm tra inventory ngay lập tức
            if (plugin.getAntiGiveItemManager().isInventoryChanged(player)) {
                plugin.getAntiGiveItemManager().restoreCreativeInventory(player);
            }
            
            // Dọn dẹp item gần đó ngay lập tức
            plugin.getMainManager().cleanupNearbyItems(player);
        }
    }
}