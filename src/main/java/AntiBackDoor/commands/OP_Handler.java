package AntiBackDoor.commands;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import AntiBackDoor.Main_plugin;

public class OP_Handler implements CommandExecutor {
    private final Main_plugin plugin;

    public OP_Handler(Main_plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) return false;

        // Sử dụng UUID thay vì tên
        Player targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer == null) {
            sender.sendMessage(plugin.getMessenger().get("command.error.player_offline"));
            return true;
        }

        UUID uuid = targetPlayer.getUniqueId();
        String name = targetPlayer.getName();

        if (!plugin.getWhitelistManager().isAllowed(uuid, name)) {
            sender.sendMessage(plugin.getMessenger().get("player_not_op"));
            return true;
        }

        return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "op " + args[0]);
    }
}
