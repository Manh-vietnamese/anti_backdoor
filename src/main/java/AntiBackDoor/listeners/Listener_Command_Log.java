package AntiBackDoor.listeners;

import AntiBackDoor.Main_plugin;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class Listener_Command_Log implements Listener {
    private final Main_plugin plugin;

    public Listener_Command_Log(Main_plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage();
        
        // Lấy thông tin người chơi
        String ip = player.getAddress().getAddress().getHostAddress();
        String uuid = player.getUniqueId().toString();
        
        // Ghi log lệnh
        plugin.getCommandLogger().logCommand(
            player.getName(), 
            uuid, 
            ip, 
            command
        );
    }
}