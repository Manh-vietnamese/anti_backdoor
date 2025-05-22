package AntiBackDoor.listeners;

import org.bukkit.entity.Player;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import AntiBackDoor.Main_plugin;

public class CREATE_CommandBlocker implements Listener {
    private final Main_plugin plugin;

    public CREATE_CommandBlocker(Main_plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent e) {
        if (!plugin.getConfig().getBoolean("Anti_Create_Abuse.enabled", true)) return;
        if (!plugin.getConfig().getBoolean("Anti_Create_Abuse.block_commands", true)) return;

        Player player = e.getPlayer();
        if (isInCreateMode(player) && !hasCreatePermission(player)) {
            e.setCancelled(true);
            player.sendMessage(plugin.getMessenger().get("create_abuse.command"));

            // Thêm cảnh báo
            plugin.getWarningManager().addWarning(player);
        }
    }

    // Thêm phương thức kiểm tra chế độ Create
    private boolean isInCreateMode(Player player) {
        return player.getGameMode() == GameMode.CREATIVE;
    }

    private boolean hasCreatePermission(Player player) {
        return player.hasPermission("antibackdoor.create") 
            || player.hasPermission("antibackdoor.Admin");
    }
}
