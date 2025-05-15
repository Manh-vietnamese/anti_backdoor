package AntiBackDoor.managers;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.text.SimpleDateFormat;

import org.bukkit.Bukkit;
import org.bukkit.BanList;
import org.bukkit.entity.Player;

import AntiBackDoor.Main_plugin;

import org.bukkit.configuration.file.YamlConfiguration;

public class OP_BanManager {
    private final Main_plugin plugin;
    private final File bansFile;
    private YamlConfiguration bansConfig;

    public OP_BanManager(Main_plugin plugin) {
        this.plugin = plugin;
        this.bansFile = new File(plugin.getDataFolder(), "bans.yml");
        loadBans();
    }

    public void loadBans() {
        if (!bansFile.exists()) {
            plugin.saveResource("bans.yml", false);
            bansConfig = YamlConfiguration.loadConfiguration(bansFile);
            // Thêm "banned_players" nếu chưa có
            if (!bansConfig.contains("banned_players")) {
                bansConfig.createSection("banned_players");
                saveBans();
            }
        } else {
            bansConfig = YamlConfiguration.loadConfiguration(bansFile);
        }
    }

    public void saveBans() {
        try {
            bansConfig.save(bansFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Lỗi khi lưu bans.yml: " + e.getMessage());
        }
    }

    public void banPlayer(Player player, String reason, String bannedBy, long duration) {
        long currentTime = Instant.now().getEpochSecond();
        long unbanTime = duration > 0 ? currentTime + duration : -1;
    
        // Lưu thông tin ban
        bansConfig.set("banned_players." + player.getName() + ".ip", player.getAddress().getAddress().getHostAddress());
        bansConfig.set("banned_players." + player.getName() + ".reason", reason);
        bansConfig.set("banned_players." + player.getName() + ".banned_by", bannedBy);
        bansConfig.set("banned_players." + player.getName() + ".ban_time", currentTime);
        bansConfig.set("banned_players." + player.getName() + ".unban_time", unbanTime);
    
        // Đồng bộ với Bukkit ban list (CHỈ CẬP NHẬT LÝ DO TỪ PLUGIN)
        Date expiryDate = (duration > 0) ? new Date(System.currentTimeMillis() + duration * 1000L) : null;
        String banMessage = getBanMessage(player.getName());
        updateBukkitBanEntry(player.getName(), banMessage, expiryDate);
    
        // Kick người chơi nếu đang online (CHỈ SỬ DỤNG THÔNG BÁO TỪ PLUGIN)
        if (player.isOnline()) {
            player.kickPlayer(banMessage); // Sử dụng banMessage đã định dạng
        }
    
        saveBans();
    }

    // private Map<String, String> createBanPlaceholders(String reason, String bannedBy, long duration) {
    //     Map<String, String> placeholders = new HashMap<>();
    //     placeholders.put("reason", reason);
    //     placeholders.put("admin", bannedBy);
    //     placeholders.put("time", formatTime(duration > 0 ? (Instant.now().getEpochSecond() + duration) : -1));
    //     return placeholders;
    // }

    public boolean isBanned(String playerName) {
        if (!bansConfig.contains("banned_players." + playerName)) {
            return false;
        }
    
        long unbanTime = bansConfig.getLong("banned_players." + playerName + ".unban_time");
        if (unbanTime != -1 && Instant.now().getEpochSecond() > unbanTime) {
            // Tự động gỡ ban nếu đã hết hạn
            bansConfig.set("banned_players." + playerName, null);
            saveBans();
            return false;
        }
        return true;
    }   

    public String getBanMessage(String playerName) {
        if (!isBanned(playerName)) return null;
    
        long unbanTime = bansConfig.getLong("banned_players." + playerName + ".unban_time");
        String reason = bansConfig.getString("banned_players." + playerName + ".reason", "Không rõ lý do");
        String bannedBy = bansConfig.getString("banned_players." + playerName + ".banned_by", "Hệ thống");
    
        Map<String, String> args = new HashMap<>();
        args.put("reason", reason);
        args.put("admin", bannedBy);
    
        // Thêm thời điểm unban vào thông báo
        args.put("time", formatTime(unbanTime));
    
        if (unbanTime == -1) {
            return plugin.getMessenger().get("ban.permanent", args);
        } else {
            return plugin.getMessenger().get("ban.temporary", args);
        }
    }
    
    public boolean unbanPlayer(String playerName) {
        if (!isBanned(playerName)) return false;
        
        bansConfig.set("banned_players." + playerName, null);
        saveBans();
        
        // Corrected BanList reference
        Bukkit.getBanList(BanList.Type.NAME).pardon(playerName);
        return true;
    }

    private String formatTime(long unbanTimestamp) {
        if (unbanTimestamp == -1) {
            return "Vĩnh viễn";
        }

        // Chuyển timestamp thành Date (đơn vị giây → mili giây)
        Date unbanDate = new Date(unbanTimestamp * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Ho_Chi_Minh")); // Điều chỉnh múi giờ nếu cần

        return sdf.format(unbanDate);
    }

    public void checkExpiredBans() {
        // Kiểm tra xem "banned_players" có tồn tại không
        if (bansConfig.getConfigurationSection("banned_players") == null) {
            return; // Không có dữ liệu, thoát sớm
        }
        
        // Lặp qua các key nếu tồn tại
        for (String playerName : bansConfig.getConfigurationSection("banned_players").getKeys(false)) {
            isBanned(playerName); // Tự động xóa nếu hết hạn
        }
    }

    public void updateBukkitBanEntry(String playerName, String reason, Date expiryDate) {
        BanList banList = Bukkit.getBanList(BanList.Type.NAME);
        // Xóa ban entry cũ nếu tồn tại
        if (banList.isBanned(playerName)) {
            banList.pardon(playerName);
        }
        banList.addBan(playerName, reason, expiryDate, null);
    }
}