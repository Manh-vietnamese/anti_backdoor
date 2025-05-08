package anntibackdoor.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import anntibackdoor.SupportServer;

import java.util.UUID;

public class OpHandler implements CommandExecutor {
    private final SupportServer plugin;

    public OpHandler(SupportServer plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) return false;

        // Sử dụng UUID thay vì tên
        Player targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer == null) {
            sender.sendMessage(plugin.getMessenger().get("player_not_online"));
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