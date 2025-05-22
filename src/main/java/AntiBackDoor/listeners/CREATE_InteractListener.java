package AntiBackDoor.listeners;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import AntiBackDoor.Main_plugin;

public class CREATE_InteractListener implements Listener {
    private final Main_plugin plugin;

    public CREATE_InteractListener(Main_plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        // Chặn mọi tương tác chuột phải nếu không có quyền
        if (isInCreateMode(player) && !hasCreatePermission(player)) {
            e.setCancelled(true);
            player.sendMessage(plugin.getMessenger().get("create_abuse.interact"));

            // Thêm cảnh báo
            plugin.getWarningManager().addWarning(player);
        }
    }

    private boolean isInCreateMode(Player player) {
        return player.getGameMode() == GameMode.CREATIVE;
    }

    private boolean hasCreatePermission(Player player) {
        return player.hasPermission("antibackdoor.create") 
            || player.hasPermission("antibackdoor.Admin");
    }
}
