package AntiBackDoor;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import AntiBackDoor.Commands.OP_Whitelist;

import AntiBackDoor.listeners.Listener_ANTI_Create;
import AntiBackDoor.listeners.Listener_ANTI_GiveItem;
import AntiBackDoor.listeners.Listener_Ban_CheckPlayer;
import AntiBackDoor.listeners.Listener_Banplayer;
import AntiBackDoor.listeners.Listener_Command_Log;
import AntiBackDoor.listeners.Listener_PlayerJoin;

import AntiBackDoor.Managers.Managers_ANTI_GiveItem;
import AntiBackDoor.Managers.Managers_CREATE_Warning;
import AntiBackDoor.Managers.Managers_Main;
import AntiBackDoor.Managers.Managers_OP_Ban;
import AntiBackDoor.Managers.Managers_OP;

import AntiBackDoor.Messenger.Messager;
import AntiBackDoor.Utils.CommandLogger;
import AntiBackDoor.Utils.LogManager;

public class Main_plugin extends JavaPlugin {
    private Listener_ANTI_Create createListener;
    private Managers_ANTI_GiveItem antiGiveItemManager;
    private Managers_CREATE_Warning Managers_CREATE_Warning;
    private Managers_Main mainManager;
    private Managers_OP_Ban banManager;
    private Managers_OP whitelistManager;
    private Messager Messenger;
    private CommandLogger commandLogger; 
    private LogManager logManager;
    
    @Override
    public void onEnable() {
        // 1. Tạo config.yml và messages.yml
        saveDefaultConfig();
        saveDefaultMessages();

        // 2. Khởi tạo các thành phần
        Messager.init(this);
        createListener = new Listener_ANTI_Create(this);
        antiGiveItemManager = new Managers_ANTI_GiveItem(this);
        banManager = new Managers_OP_Ban(this);
        whitelistManager = new Managers_OP(this);
        whitelistManager.loadWhitelist();
        commandLogger = new CommandLogger(getDataFolder(), "commands.log");
        logManager = new LogManager(getDataFolder(), "ips.log");

        // 3. Khởi tạo mainManager sau khi các dependency đã sẵn sàng
        mainManager = new Managers_Main(this);

        // 4. Gọi các phương thức từ Managers_Main
        mainManager.scanAndEnforceOpPolicy();
        mainManager.overrideOpCommand();

        // Lưu trữ các listener để hủy đăng ký sau này
        this.createListener = new Listener_ANTI_Create(this);
        this.Managers_CREATE_Warning = new Managers_CREATE_Warning(this);
        
        // Đăng ký listener
        Bukkit.getPluginManager().registerEvents(new Listener_ANTI_GiveItem(this), this);

        // Lập lịch quét OP và ban hết hạn (1s = 20 ticks)
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, mainManager::scanAndEnforceOpPolicy, 100L, 100L);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> banManager.checkExpiredBans(), 20L * 60, 20L * 60);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {mainManager.scanCreativeInventories(Bukkit.getConsoleSender());}, 20L, 20L);

        // Đăng ký listeners
        getServer().getPluginManager().registerEvents(createListener, this);
        getServer().getPluginManager().registerEvents(new Listener_Ban_CheckPlayer(), this);
        getServer().getPluginManager().registerEvents(new Listener_Banplayer(this), this);
        getServer().getPluginManager().registerEvents(new Listener_Command_Log(this), this);
        getServer().getPluginManager().registerEvents(new Listener_PlayerJoin(this), this);
        this.getCommand("wop").setExecutor(new OP_Whitelist(this));
    }

    // Hủy tất cả tasks
    @Override
    public void onDisable() {
        // Unregister tất cả listener của plugin
        HandlerList.unregisterAll(this);
    }

    @Override
    public void saveDefaultConfig() {
        super.saveDefaultConfig();
        if (!new File(getDataFolder(), "config.yml").exists()) {
            saveResource("config.yml", false);
        }
    }

    // Getter methods
    public Messager getMessenger() {return Messenger;}
    public Managers_OP_Ban getBanManager() {return banManager;}
    public Managers_Main getMainManager() {return mainManager;}
    public Managers_OP getWhitelistManager() {return whitelistManager;}
    public Managers_CREATE_Warning getWarningManager() {return Managers_CREATE_Warning;}
    public Managers_ANTI_GiveItem getAntiGiveItemManager() {return antiGiveItemManager;}
    public CommandLogger getCommandLogger() {return commandLogger;}

    public void logIP(Player player) {logManager.logIP(player);}
    public void executeSafetyProtocol(Player player) {mainManager.executeSafetyProtocol(player);}

    // Reload logic
    public void fullReload() {
        reloadConfig();
        saveDefaultConfig();
        whitelistManager.loadWhitelist();
        banManager.loadBans();
        Messager.reload(this);

        // Hủy đăng ký listener cũ
        HandlerList.unregisterAll(this);

        // Đăng ký listener mới với cấu hình đã cập nhật
        this.createListener = new Listener_ANTI_Create(this);

        Bukkit.getPluginManager().registerEvents(new Listener_ANTI_GiveItem(this), this);

        // Hủy và lập lại lịch tasks
        Bukkit.getScheduler().cancelTasks(this);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, mainManager::scanAndEnforceOpPolicy, 100L, 100L);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> banManager.checkExpiredBans(), 20L * 60, 20L * 60);
    }

    private void saveDefaultMessages() {
        File msgFile = new File(getDataFolder(), "messages.yml");
        if (!msgFile.exists()) {
            saveResource("messages.yml", false); // Sao chép từ resources
        }
    }
}