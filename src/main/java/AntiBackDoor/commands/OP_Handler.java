package AntiBackDoor.Commands;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import AntiBackDoor.Main_plugin;
import AntiBackDoor.Messenger.Messager;

public class OP_Handler implements CommandExecutor {
    private final Main_plugin plugin;

    public OP_Handler(Main_plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Xử lý lệnh /op tùy chỉnh với cơ chế whitelist
     * @param sender Người gửi lệnh
     * @param cmd Lệnh được thực thi
     * @param label Tên lệnh
     * @param args Tham số lệnh (tên người chơi)
     * @return true nếu xử lý thành công, ngược lại false
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) return false;

        // Sử dụng UUID thay vì tên
        Player targetPlayer = Bukkit.getPlayer(args[0]);
        if (targetPlayer == null) {
            // Sử dụng Messager trực tiếp
            Messager.get("command.error.player_offline");
            return true;
        }

        UUID uuid = targetPlayer.getUniqueId();
        String name = targetPlayer.getName();

        // Kiểm tra whitelist
        if (!plugin.getWhitelistManager().isAllowed(uuid, name)) {
            // Sử dụng Messager trực tiếp
            Messager.get("player_not_op");
            return true;
        }

        // Thực thi lệnh OP gốc
        return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "op " + args[0]);
    }
}
