package AntiBackDoor.Managers;

import org.bukkit.entity.Player;
import org.bukkit.GameMode;
import java.util.HashMap;
import java.util.UUID;

import AntiBackDoor.Main_plugin;

public class CREATE_WarningManager {
    private final Main_plugin plugin;
    private final HashMap<UUID, Integer> warnings = new HashMap<>();

    public CREATE_WarningManager(Main_plugin plugin) {
        this.plugin = plugin;
    }

    public void addWarning(Player player) {
        UUID uuid = player.getUniqueId();
        int current = warnings.getOrDefault(uuid, 0);
        warnings.put(uuid, current + 1);

        // Kiểm tra nếu vượt quá số cảnh báo
        int maxWarnings = plugin.getConfig().getInt("Anti_Create_Abuse.warnings", 10);
        if (current + 1 >= maxWarnings) {
            // Kiểm tra và phục hồi inventory trước khi kick
            if (player.getGameMode() == GameMode.CREATIVE && 
                plugin.getAntiGiveItemManager().hasSavedInventory(player)) {
                
                // So sánh inventory hiện tại với bản lưu
                if (plugin.getAntiGiveItemManager().isInventoryChanged(player)) {
                    plugin.getAntiGiveItemManager().restoreCreativeInventory(player);
                }
                
                // Dọn dẹp item xung quanh
                plugin.getMainManager().cleanupNearbyItems(player);
            }
            
            resetWarning(player);
            player.setGameMode(GameMode.ADVENTURE);
            player.kickPlayer(plugin.getMessenger().get("create_abuse.kick"));
        }
    }

    public void resetWarning(Player player) {
        warnings.remove(player.getUniqueId());
    }
}
