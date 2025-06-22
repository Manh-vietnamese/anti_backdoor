package AntiBackDoor.Managers;

import java.util.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.command.PluginCommand;

import AntiBackDoor.Main_plugin;
import AntiBackDoor.Commands.OP_Handler;
import AntiBackDoor.Messenger.Messager;
import AntiBackDoor.listeners.OP_PlayerJoin;

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

    public Main_plugin getPlugin() {return plugin;}
    public Messager getMessenger() {return messenger;}
    public OP_BanManager getBanManager() {return banManager;}
    public OP_Manager getWhitelistManager() {return whitelistManager;}

    public void reloadListeners() {
        HandlerList.unregisterAll(plugin);
        plugin.getServer().getPluginManager().registerEvents(new OP_PlayerJoin(plugin), plugin);
    }

    public void overrideOpCommand() {
        PluginCommand opCommand = plugin.getServer().getPluginCommand("op");
        if (opCommand != null) {
            opCommand.setExecutor(new OP_Handler(plugin));
        }
    }

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

    public void syncWithServerOps() {
        Bukkit.getOperators().forEach(op -> {
            if (!plugin.getWhitelistManager().isAllowed(op.getUniqueId(), op.getName())) {
                op.setOp(false);
            } else {
                // Tự động thêm lại OP nếu có trong whitelist
                if (!op.isOp()) {op.setOp(true);}
            }
        });
    }

    public void scanCreativeInventories() {
        if (!plugin.getConfig().getBoolean("Anti_Give_Item.enabled", true)) return;
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Kiểm tra quyền bypass
            if (player.hasPermission("antibackdoor.bypass.giveitem")) {
                // Xóa inventory đã lưu nếu có
                if (plugin.getAntiGiveItemManager().hasSavedInventory(player)) {
                    plugin.getAntiGiveItemManager().removeSavedInventory(player);
                }
                continue;
            }
            
            if (player.getGameMode() == GameMode.CREATIVE) {
                // Nếu chưa có file lưu trữ, tạo mới
                if (!plugin.getAntiGiveItemManager().hasSavedInventory(player)) {
                    plugin.getAntiGiveItemManager().saveCreativeInventory(player);
                    
                    // Dọn dẹp các item xung quanh ngay khi vào Creative
                    cleanupNearbyItems(player);
                }
                // Nếu đã có file, kiểm tra thay đổi
                else {
                    if (plugin.getAntiGiveItemManager().isInventoryChanged(player)) {
                        plugin.getAntiGiveItemManager().restoreCreativeInventory(player);
                        player.sendMessage(plugin.getMessenger().get("anti_give_item.warning"));
                        
                        // Thêm cảnh báo
                        plugin.getWarningManager().addWarning(player);
                        
                        // Dọn dẹp các item xung quanh sau khi phục hồi inventory
                        cleanupNearbyItems(player);
                    }
                }
            }
            // Xóa file nếu đổi sang chế độ khác
            else if (plugin.getAntiGiveItemManager().hasSavedInventory(player)) {
                plugin.getAntiGiveItemManager().removeSavedInventory(player);
            }
        }
    }

    // Dọn dẹp các item gần người chơi
    public void cleanupNearbyItems(Player player) {
        if (!plugin.getConfig().getBoolean("Anti_Give_Item.cleanup_items", true)) return;
        
        int radius = plugin.getConfig().getInt("Anti_Give_Item.cleanup_radius", 5);
        int cleanupDelay = plugin.getConfig().getInt("Anti_Give_Item.cleanup_delay", 5); // 0.25 giây
        
        // Sử dụng task trễ để đảm bảo item đã xuất hiện
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            player.getNearbyEntities(radius, radius, radius).forEach(entity -> {
                if (entity instanceof org.bukkit.entity.Item) {
                    org.bukkit.entity.Item item = (org.bukkit.entity.Item) entity;
                    
                    // Kiểm tra thời gian tồn tại của item
                    if (item.getTicksLived() < 100) { // Chỉ xóa item mới
                        item.remove();
                    }
                }
            });
        }, cleanupDelay);
    }
}
