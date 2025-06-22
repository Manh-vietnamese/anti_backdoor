package AntiBackDoor.Commands;

import java.util.Arrays;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import AntiBackDoor.Main_plugin;

public class OP_Whitelist implements CommandExecutor {
    private final Main_plugin plugin;
    private final OP_CommandHandler handler;

    public OP_Whitelist(Main_plugin plugin) {
        this.plugin = plugin;
        this.handler = new OP_CommandHandler(plugin);
    }

    /**
     * Xử lý lệnh /opwhitelist và các sub-command
     * @param sender Người gửi lệnh
     * @param cmd Lệnh được thực thi
     * @param label Tên lệnh
     * @param args Tham số lệnh (add/remove/reload/list/ban/unban)
     * @return true nếu xử lý thành công, ngược lại false
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Kiểm tra quyền ADMIN
        if (!sender.hasPermission("antibackdoor.Admin")) {
            sender.sendMessage(plugin.getMessenger().get("command.error.no_permission"));
            return true;
        }

        // Hiển thị hướng dẫn nếu thiếu tham số
        if (args.length < 1) {
            sender.sendMessage(plugin.getMessenger().get("command.usage"));
            return true;
        }

        String action = args[0].toLowerCase();

        // Định tuyến sub-command
        switch (action) {
            // Thêm người chơi vào whitelist OP
            case "add":
                return handler.handleAdd(sender, args);

            // Xóa người chơi khỏi whitelist OP
            case "remove":
                if (args.length < 2) {
                    sender.sendMessage(plugin.getMessenger().get("command.usage"));
                    return false;
                }
                return handler.handleRemove(sender, args[1]); // Đúng vì handleRemove trả về boolean

            // Tải lại danh sách OP từ file
            case "reload":
                return handler.handleReload(sender);

            // Liệt kê OP trong whitelist OP
            case "list":
                handler.listAllowedOps(sender);
                return true;

            // ban người chơi
            case "ban":
                if (args.length < 4) {
                    sender.sendMessage(plugin.getMessenger().get("command.usage"));
                    return false;
                }

                String targetName = args[1];
                String timeInput = args[args.length - 1];
                String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length - 1));
                return handler.handleBan(sender, targetName, reason, timeInput);

            // Gỡ ban người chơi
            case "unban":
                if (args.length < 2) {
                    sender.sendMessage(plugin.getMessenger().get("command.usage"));
                    return false;
                }
                return handler.handleUnban(sender, args);

            // Xử lý lệnh không hợp lệ
            default:
                sender.sendMessage(plugin.getMessenger().get("command.error.invalid_command"));
                return true;
        }
    }
}
