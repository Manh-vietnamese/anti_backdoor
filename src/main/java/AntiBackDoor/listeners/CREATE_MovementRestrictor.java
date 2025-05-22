package AntiBackDoor.listeners;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import AntiBackDoor.Main_plugin;

public class CREATE_MovementRestrictor implements Listener {
    private final Main_plugin plugin;

    public CREATE_MovementRestrictor(Main_plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!plugin.getConfig().getBoolean("Anti_Create_Abuse.enabled", true)) return;
        if (!plugin.getConfig().getBoolean("Anti_Create_Abuse.restrict_movement", true)) return;

        Player player = e.getPlayer();
        if (isInCreateMode(player) && !hasCreatePermission(player)) {
            Location from = e.getFrom();
            Location to = e.getTo();
            if (to != null && to.distanceSquared(from) > 0.01) {
                e.setTo(from);
                player.sendMessage(plugin.getMessenger().get("create_abuse.movement"));

                // Thêm cảnh báo
                plugin.getWarningManager().addWarning(player);
            }
        }
    }

    // Thêm phương thức kiểm tra chế độ Create
    private boolean isInCreateMode(Player player) {
        return player.getGameMode() == GameMode.CREATIVE;
    }

    // Thêm phương thức kiểm tra quyền
    private boolean hasCreatePermission(Player player) {
        return player.hasPermission("antibackdoor.create") 
            || player.hasPermission("antibackdoor.Admin");
    }
}
