package AntiBackDoor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import AntiBackDoor.Messenger.Messager;
import AntiBackDoor.commands.OP_Whitelist;
import AntiBackDoor.config.OP_Manager;

import AntiBackDoor.listeners.OP_Ban;
import AntiBackDoor.listeners.OP_BanCheck;
import AntiBackDoor.listeners.OP_PlayerJoin;
import AntiBackDoor.listeners.CREATE_CommandBlocker;
import AntiBackDoor.listeners.CREATE_InteractListener;
import AntiBackDoor.listeners.CREATE_MovementRestrictor;

import AntiBackDoor.managers.Main_Manager;
import AntiBackDoor.managers.OP_BanManager;
import AntiBackDoor.managers.CREATE_WarningManager;

public class Main_plugin extends JavaPlugin {
    private CREATE_WarningManager create_warningManager;
    private CREATE_CommandBlocker commandBlocker;
    private CREATE_InteractListener interactListener;
    private CREATE_MovementRestrictor movementRestrictor;
    private OP_BanManager banManager;
    private Main_Manager mainManager;
    private OP_Manager whitelistManager;
    private Messager Messenger;

    @Override
    public void onEnable() {
        // 1. Tạo config.yml và messages.yml
        saveDefaultConfig();
        saveDefaultMessages();

        // 2. Khởi tạo các thành phần
        whitelistManager = new OP_Manager(this);
        whitelistManager.loadWhitelist();
        banManager = new OP_BanManager(this);
        Messenger = new Messager(getDataFolder());

        // 3. Khởi tạo mainManager sau khi các dependency đã sẵn sàng
        mainManager = new Main_Manager(this);

        // 4. Gọi các phương thức từ Main_Manager
        mainManager.scanAndEnforceOpPolicy();
        mainManager.overrideOpCommand();

        // Lưu trữ các listener để hủy đăng ký sau này
        this.commandBlocker = new CREATE_CommandBlocker(this);
        this.interactListener = new CREATE_InteractListener(this);
        this.movementRestrictor = new CREATE_MovementRestrictor(this);
        this.create_warningManager = new CREATE_WarningManager(this);

        // Đăng ký listener
        Bukkit.getPluginManager().registerEvents(commandBlocker, this);
        Bukkit.getPluginManager().registerEvents(interactListener, this);
        Bukkit.getPluginManager().registerEvents(movementRestrictor, this);

        // Lập lịch quét OP và ban hết hạn
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, mainManager::scanAndEnforceOpPolicy, 100L, 100L);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> banManager.checkExpiredBans(), 20L * 60, 20L * 60);

        // Đăng ký listeners
        getServer().getPluginManager().registerEvents(new OP_Ban(this), this);
        getServer().getPluginManager().registerEvents(new OP_BanCheck(), this);
        getServer().getPluginManager().registerEvents(new OP_PlayerJoin(this), this);
        this.getCommand("wop").setExecutor(new OP_Whitelist(this));
    }

    // Hủy tất cả tasks
    @Override
    public void onDisable() {Bukkit.getScheduler().cancelTasks(this);}

    @Override
    public void saveDefaultConfig() {
        super.saveDefaultConfig();
        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveResource("config.yml", false);
        }
    }

    // Getter methods
    public OP_Manager getWhitelistManager() { return whitelistManager; }
    public OP_BanManager getBanManager() { return banManager; }
    public CREATE_WarningManager getWarningManager() { return create_warningManager;}
    public Main_Manager getMainManager() { return mainManager; }
    public Messager getMessenger() { return Messenger; }

    public void executeSafetyProtocol(Player player) {mainManager.executeSafetyProtocol(player);}

    // Reload logic
    public void fullReload() {
        reloadConfig();
        saveDefaultConfig();
        whitelistManager.loadWhitelist();
        banManager.loadBans();
        Messenger.reload();

        // Hủy đăng ký listener cũ
        HandlerList.unregisterAll(commandBlocker);
        HandlerList.unregisterAll(interactListener);
        HandlerList.unregisterAll(movementRestrictor);

        // Đăng ký listener mới với cấu hình đã cập nhật
        this.commandBlocker = new CREATE_CommandBlocker(this);
        this.interactListener = new CREATE_InteractListener(this);
        this.movementRestrictor = new CREATE_MovementRestrictor(this);

        Bukkit.getPluginManager().registerEvents(commandBlocker, this);
        Bukkit.getPluginManager().registerEvents(interactListener, this);
        Bukkit.getPluginManager().registerEvents(movementRestrictor, this);

        // Hủy và lập lại lịch tasks
        Bukkit.getScheduler().cancelTasks(this);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, mainManager::scanAndEnforceOpPolicy, 100L, 100L);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> banManager.checkExpiredBans(), 20L * 60, 20L * 60);
    }

    public void logIP(Player player) {
        String time = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date());
        String logEntry = String.format(
            "[%s][%s][%s][%s]: đã đăng nhập.",
            time,
            player.getName(),
            player.getUniqueId(),
            player.getAddress().getAddress().getHostAddress()
        );

        File logFile = new File(getDataFolder(), "ips.log");
        try (FileWriter fw = new FileWriter(logFile, true)) {
            fw.write(logEntry + "\n");
        } catch (IOException e) {
            getLogger().severe("Không thể ghi IP log: " + e.getMessage());
        }
    }

    private void saveDefaultMessages() {
        File msgFile = new File(getDataFolder(), "messages.yml");
        if (!msgFile.exists()) {
            saveResource("messages.yml", false); // Sao chép từ resources
        }
    }
}