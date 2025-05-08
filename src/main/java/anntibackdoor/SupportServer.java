package anntibackdoor;

import anntibackdoor.Messenger.Messager;
import anntibackdoor.commands.OpHandler;
import anntibackdoor.commands.WhitelistOP;
import anntibackdoor.config.WhitelistManager;
import anntibackdoor.listeners.BanCheckListener;
import anntibackdoor.listeners.BanListener;
import anntibackdoor.listeners.PlayerJoinListener;
import anntibackdoor.managers.BanManager;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public class SupportServer extends JavaPlugin {
    private WhitelistManager whitelistManager;
    private BanManager banManager;
    private Messager Messenger;
    private int taskId;

    @Override
    public void onEnable() {
        // Tạo file cấu hình mặc định
        saveDefaultConfig();
        saveDefaultMessages();
        
        // Khởi tạo WhitelistManager và load dữ liệu trước
        whitelistManager = new WhitelistManager(this);
        whitelistManager.loadWhitelist(); // ⭐ Load whitelist ngay sau khi khởi tạo
        
        // Gọi scanAndEnforceOpPolicy() sau khi whitelistManager đã sẵn sàng
        scanAndEnforceOpPolicy(); // ⭐ Đã di chuyển xuống dưới
        
        // Khởi tạo các thành phần khác
        Messenger = new Messager(getDataFolder());
        this.banManager = new BanManager(this);
        
        registerMainCommand();
        
        // Lập lịch quét mỗi 5 giây
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
            this, this::scanAndEnforceOpPolicy, // Phương thức này giờ đây an toàn
            100L, 100L
        );

        // Lập lịch quét ban hết hạn mỗi phút
        Bukkit.getScheduler().scheduleSyncRepeatingTask(
            this, () -> banManager.checkExpiredBans(),
            20L * 60,  20L * 60
        );

        getServer().getPluginManager().registerEvents(new BanListener(this), this);
        getServer().getPluginManager().registerEvents(new BanCheckListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        this.getCommand("wop").setExecutor(new WhitelistOP(this));
        overrideOpCommand();
    }

    private void scanAndEnforceOpPolicy() {
        Set<OfflinePlayer> allOps = new HashSet<>(Bukkit.getOperators());
        
        allOps.forEach(op -> {
            boolean isValid = whitelistManager.isAllowed(
                op.getUniqueId(), 
                op.getName() != null ? op.getName() : ""
            );
            
            if (!isValid) {
                op.setOp(false);
                
                if (op.isOnline()) {
                    Player onlinePlayer = Bukkit.getPlayer(op.getUniqueId());
                    if (onlinePlayer != null) {
                        executeSafetyProtocol(onlinePlayer);
                    }
                }
            }
        });
    }

    private void overrideOpCommand() {
        PluginCommand opCommand = getServer().getPluginCommand("op");
        if (opCommand != null) {
            opCommand.setExecutor(new OpHandler(this));
        }
    }

    public WhitelistManager getWhitelistManager() {return whitelistManager;}
    public BanManager getBanManager() {return banManager;}
    public Messager getMessenger() {return Messenger;}

    public void executeSafetyProtocol(Player player) {
        player.setOp(false);
        player.saveData();
    
        // Đọc cấu hình
        String punishmentType = getConfig().getString("punishment_type", "kick").toLowerCase();
        int banDuration = getConfig().getInt("ban_duration", -1);
    
        // Tạo placeholders
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", player.getName());
        placeholders.put("uuid", player.getUniqueId().toString());
        placeholders.put("reason", "Sử dụng OP trái phép");
        placeholders.put("punishment", punishmentType.toUpperCase());
    
        // Xử lý hình phạt
        if ("ban".equals(punishmentType)) {
            banPlayer(player, banDuration, placeholders);
        } else if ("kick".equals(punishmentType)) {
            kickPlayer(player, placeholders);
        }
    
        // Ghi log
        getLogger().warning(getMessenger().get("op_violation_log", placeholders));
    }

    private void kickPlayer(Player player, Map<String, String> placeholders) {
        String kickMessage = getMessenger().get("op_violation_kick", placeholders);
        player.kickPlayer(kickMessage);
    }

    private void banPlayer(Player player, int duration, Map<String, String> placeholders) {
        // Thêm vào hệ thống ban của plugin
        this.getBanManager().banPlayer(
            player,
            placeholders.get("reason"),
            "Hệ thống",
            duration > 0 ? duration : -1
        );
    
        // Lấy thông báo tùy chỉnh từ plugin
        String banMessage = this.getBanManager().getBanMessage(player.getName());
    
        // Thêm vào Bukkit ban list và GHI ĐÈ lý do
        Bukkit.getBanList(BanList.Type.NAME).addBan(
            player.getName(),
            banMessage, // Sử dụng thông báo tùy chỉnh làm lý do
            duration > 0 ? new Date(System.currentTimeMillis() + duration * 1000L) : null,
            null
        );
    
        // Kick người chơi với thông báo tùy chỉnh
        if (player.isOnline()) {
            player.kickPlayer(banMessage);
        }
    }

    private void registerMainCommand() {
        PluginCommand mainCommand = getCommand("supportserver");
        if (mainCommand != null) {
            mainCommand.setExecutor((sender, cmd, label, args) -> {
                if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                    if (!sender.hasPermission("Sunflower.SP.admin")) {
                        sender.sendMessage(getMessenger().get("command-no-permission"));
                        return true;
                    }
                    fullReload();
                    sender.sendMessage(getMessenger().get("full-reload-success"));
                    return true;
                }
                return false;
            });
        }
    }

    public void fullReload() {
        // Hủy task cũ
        Bukkit.getScheduler().cancelTask(taskId);
        
        // Reload toàn bộ cấu hình
        reloadConfig();
        saveDefaultConfig();
        
        // Reload whitelist
        whitelistManager.loadWhitelist();
        
        // Reload messages
        Messenger.reload();
        
        // Khởi tạo lại task
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(
            this,
            this::scanAndEnforceOpPolicy,
            100L,
            100L
        );
        
        // Reload listeners
        reloadListeners();
    }

    private void reloadListeners() {
        // Hủy đăng ký listener cũ
        HandlerList.unregisterAll(this);
        
        // Đăng ký listener mới
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
    }


    @Override
    public void onDisable() {
        // Hủy task định kỳ khi tắt plugin
        Bukkit.getScheduler().cancelTask(taskId);
    }

    @Override
    public void saveDefaultConfig() {
        super.saveDefaultConfig();
        // Đảm bảo file config.yml được tạo từ resources
        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveResource("config.yml", false);
        }
    }

    // kiểm tra và lưu file,tạo file khi còn thiếu
    private void saveDefaultMessages() {
        File msgFile = new File(getDataFolder(), "messages.yml");
        if (!msgFile.exists()) {
            saveResource("messages.yml", false);
        }
    }
}