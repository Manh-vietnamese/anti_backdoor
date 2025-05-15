package anntibackdoor.commands;

import anntibackdoor.Main_plugin;
import anntibackdoor.managers.OP_BanManager;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;

public class OP_CommandHandler {
    public final Main_plugin plugin;

    public OP_CommandHandler(Main_plugin plugin) {
        this.plugin = plugin;
    }

    // phương thức xử lý ADD OP
    public boolean handleAdd(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.getMessenger().get("command_usage"));
            return false;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.getMessenger().get("player_not_online"));
            return true;
        }
    
        UUID uuid = target.getUniqueId();
        String name = target.getName();
    
        // Thêm IP khi người chơi online
        String ip = target.getAddress().getAddress().getHostAddress();
        // Thêm vào whitelist của plugin
        plugin.getWhitelistManager().addOP(uuid, name);
        plugin.getWhitelistManager().addIP(uuid, ip);
    
        // Thêm vào ops.json thông qua Bukkit API
        target.setOp(true); // Nếu người chơi đang online
        Bukkit.getOfflinePlayer(uuid).setOp(true); // Đảm bảo cả offline
    
        sender.sendMessage("§aĐã thêm " + name + " vào whitelist OP và cấp quyền OP!");
        return true;
    }

    // phương thức xử lý list OP
    public void listAllowedOps(CommandSender sender) {
        List<String> ops = plugin.getWhitelistManager().getAllowedOPs();
        if (ops.isEmpty()) {
            sender.sendMessage("§7Danh sách OP trống!");
            return;
        }
        
        sender.sendMessage("§6Danh sách OP hợp lệ:");
        ops.forEach(opEntry -> {
            String[] parts = opEntry.split(":");
            String uuid = parts[0];
            String name = parts.length > 1 ? parts[1] : "Unknown";
            sender.sendMessage("§7- §e" + name + " §7(UUID: §f" + uuid + "§7)");
        });
    }

    // phương thức xử lý remove OP
    public boolean handleRemove(CommandSender sender, String targetName) {
        UUID targetUUID = findUUID(targetName);
    
        Player onlinePlayer = Bukkit.getPlayer(targetName);
        if (onlinePlayer != null) {
            targetUUID = onlinePlayer.getUniqueId();
        } else {
            // Duyệt qua danh sách OP dạng UUID:Name
            for (String opEntry : plugin.getWhitelistManager().getAllowedOPs()) {
                String[] parts = opEntry.split(":");
                if (parts.length > 1 && parts[1].equalsIgnoreCase(targetName)) {
                    try {
                        targetUUID = UUID.fromString(parts[0]);
                        break;
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid UUID format: " + parts[0]);
                    }
                }
            }
        }

        if (targetUUID == null) {
            Map<String, String> params = new HashMap<>();
            params.put("player", targetName);
            sender.sendMessage(plugin.getMessenger().get("player_not_found", params));
            return false; // Trả về false nếu không tìm thấy
        }
    
        plugin.getWhitelistManager().removeOP(targetUUID);
        Map<String, String> params = new HashMap<>();
        params.put("player", targetName);
        sender.sendMessage(plugin.getMessenger().get("player_remove_op", params));
        return true; // Trả về true nếu xóa thành công
    }

    // phương thức xử lý UnBan
    public boolean handleUnban(CommandSender sender, String[] args) {
        if (args.length != 2) { // Kiểm tra args phải có 2 phần tử: "unban" và "tên người chơi"
            sender.sendMessage(plugin.getMessenger().get("command_usage"));
            return false;
        }
    
        String playerName = args[1]; // Lấy tên người chơi từ args[1]
        OP_BanManager banManager = plugin.getBanManager();
    
        if (banManager.unbanPlayer(playerName)) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", playerName);
            sender.sendMessage(plugin.getMessenger().get("unban_success", placeholders));
        } else {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("player", playerName);
            sender.sendMessage(plugin.getMessenger().get("unban_fail", placeholders));
        }
    
        return true;
    }
    
    // phương thức xử lý reload
    public boolean handleReload(CommandSender sender) {
        plugin.fullReload();
        sender.sendMessage(plugin.getMessenger().get("reload_success"));
        return true;
    }

    // phương thức xử lý Ban
    public boolean handleBan(CommandSender sender, String targetName, String reason, String timeInput) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(plugin.getMessenger().get("player_not_online"));
            return true;
        }
    
        long duration = parseBanDuration(timeInput);
        if (duration == -1) {
            sender.sendMessage(plugin.getMessenger().get("wop_ban_invalid_time"));
            return false;
        }
    
        // Gọi BanManager với lý do từ người dùng
        plugin.getBanManager().banPlayer(
            target,
            reason, // <-- Sử dụng lý do từ tham số
            sender.getName(),
            duration
        );
    
        // Thêm lý do vào thông báo
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", targetName);
        placeholders.put("time", formatDuration(duration));
        placeholders.put("reason", reason); // <-- Thêm placeholder mới
        sender.sendMessage(plugin.getMessenger().get("ban_success", placeholders));
    
        return true;
    }
    
    // Chuyển đổi thời gian từ chuỗi (vd: 1d) → giây
    public long parseBanDuration(String input) {
        try {
            // Lấy số lượng và đơn vị (ví dụ: "1d" → 1 và 'd')
            long value = Long.parseLong(input.substring(0, input.length() - 1));
            char unit = input.charAt(input.length() - 1);
    
            switch (unit) {
                case 'd': return value * 86400; // 1 ngày = 86400 giây
                case 'h': return value * 3600;  // 1 giờ = 3600 giây
                case 'm': return value * 60;    // 1 phút = 60 giây
                case 's': return value;        // Giây
                default: return -1;
            }
        } catch (Exception e) {
            return -1;
        }
    }
    
    // Định dạng thời gian thành chuỗi dễ đọc (ví dụ: 2 giờ 30 phút)
    public String formatDuration(long seconds) {
        if (seconds == -1) return "Vĩnh viễn";
    
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
    
        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append(" ngày ");
        if (hours > 0) sb.append(hours).append(" giờ ");
        if (minutes > 0) sb.append(minutes).append(" phút ");
        if (secs > 0) sb.append(secs).append(" giây");
    
        return sb.toString().trim();
    }

    // Tìm UUID từ tên người chơi
    public UUID findUUID(String targetName) {
        Player onlinePlayer = Bukkit.getPlayer(targetName);
        if (onlinePlayer != null) return onlinePlayer.getUniqueId();
    
        for (String opEntry : plugin.getWhitelistManager().getAllowedOPs()) {
            String[] parts = opEntry.split(":");
            if (parts.length > 1 && parts[1].equalsIgnoreCase(targetName)) {
                try {
                    return UUID.fromString(parts[0]);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID: " + parts[0]);
                }
            }
        }
        return null;
    }
}