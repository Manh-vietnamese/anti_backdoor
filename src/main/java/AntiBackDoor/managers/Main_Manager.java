package AntiBackDoor.managers;

import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import AntiBackDoor.Main_plugin;
import AntiBackDoor.Messenger.Messager;
import AntiBackDoor.commands.OP_Handler;
import AntiBackDoor.config.OP_Manager;
import AntiBackDoor.listeners.OP_PlayerJoin;

import org.bukkit.command.PluginCommand;

public class Main_Manager {
    private final Main_plugin plugin;
    private final OP_Manager whitelistManager;
    private final OP_BanManager banManager;
    private final Messager messenger;

    public Main_Manager(Main_plugin plugin) {
        this.plugin = plugin;
        this.whitelistManager = plugin.getWhitelistManager();
        this.banManager = plugin.getBanManager();
        this.messenger = plugin.getMessenger();
    }

    // Các phương thức được chuyển từ Main_plugin.java
    public void executeSafetyProtocol(Player player) {
        player.setOp(false);
        player.saveData();
    
        // Đọc thời gian ban từ config (mặc định -1 nếu không có)
        int banDuration = plugin.getConfig().getInt("ban_duration", -1); 
    
        // Tạo placeholders cho log
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", player.getName());
        placeholders.put("uuid", player.getUniqueId().toString());
        placeholders.put("reason", "Sử dụng OP trái phép");
    
        // Luôn thực hiện ban (không còn xử lý kick)
        banManager.banPlayer(
            player,
            placeholders.get("reason"),
            "Hệ thống AuToBan",
            banDuration > 0 ? banDuration : -1
        );
    
        // Ghi log
        plugin.getLogger().warning(messenger.get("op_violation_log", placeholders));
    }

    public void registerMainCommand() {
        PluginCommand mainCommand = plugin.getCommand("supportserver");
        if (mainCommand != null) {
            mainCommand.setExecutor((sender, cmd, label, args) -> {
                if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                    if (!sender.hasPermission("Sunflower.SP.admin")) {
                        sender.sendMessage(messenger.get("command-no-permission"));
                        return true;
                    }
                    plugin.fullReload();
                    sender.sendMessage(messenger.get("full-reload-success"));
                    return true;
                }
                return false;
            });
        }
    }

    public void reloadListeners() {
        HandlerList.unregisterAll(plugin);
        plugin.getServer().getPluginManager().registerEvents(new OP_PlayerJoin(plugin), plugin);
    }

    public void scanAndEnforceOpPolicy() {
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

    public void overrideOpCommand() {
        PluginCommand opCommand = plugin.getServer().getPluginCommand("op");
        if (opCommand != null) {
            opCommand.setExecutor(new OP_Handler(plugin));
        }
    }

    public void syncWithServerOps() {
    Bukkit.getOperators().forEach(op -> {
        if (!plugin.getWhitelistManager().isAllowed(op.getUniqueId(), op.getName())) {
            op.setOp(false);
        } else {
            // Tự động thêm lại OP nếu có trong whitelist
            if (!op.isOp()) {
                op.setOp(true);
            }
        }
        });
    }
}