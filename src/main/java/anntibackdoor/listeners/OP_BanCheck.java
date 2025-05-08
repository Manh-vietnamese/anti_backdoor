package anntibackdoor.listeners;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerLoginEvent;

public class OP_BanCheck implements Listener {
    
    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        BanList banList = Bukkit.getBanList(BanList.Type.NAME);
        if (banList.isBanned(event.getPlayer().getName())) {
            event.disallow(
                PlayerLoginEvent.Result.KICK_BANNED, 
                banList.getBanEntry(event.getPlayer().getName()).getReason()
            );
        }
    }
}