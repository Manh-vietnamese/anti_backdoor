package anntibackdoor.commands;

import anntibackdoor.Main_plugin;

import java.util.Arrays;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class OP_Whitelist implements CommandExecutor {
    private final Main_plugin plugin;
    private final OP_CommandHandler handler;

    public OP_Whitelist(Main_plugin plugin) {
        this.plugin = plugin;
        this.handler = new OP_CommandHandler(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("Sunflower.SP.admin")) {
            sender.sendMessage(plugin.getMessenger().get("no_permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(plugin.getMessenger().get("command_usage"));
            return true;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "add":
                return handler.handleAdd(sender, args);

            case "remove":
                if (args.length < 2) {
                    sender.sendMessage(plugin.getMessenger().get("command_usage"));
                    return false;
                }
                return handler.handleRemove(sender, args[1]); // Đúng vì handleRemove trả về boolean

            case "reload":
                return handler.handleReload(sender);

            case "list":
                handler.listAllowedOps(sender);
                return true;

            case "ban":
                if (args.length < 4) {
                    sender.sendMessage(plugin.getMessenger().get("wop_ban_usage"));
                    return false;
                }
                String targetName = args[1];
                String timeInput = args[args.length - 1];
                String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length - 1));
                return handler.handleBan(sender, targetName, reason, timeInput);

            case "unban":
                if (args.length < 2) {
                    sender.sendMessage(plugin.getMessenger().get("wop_unban_usage"));
                    return false;
                }
                return handler.handleUnban(sender, args);

            default:
                sender.sendMessage(plugin.getMessenger().get("invalid_command"));
                return true;
        }
    }
}