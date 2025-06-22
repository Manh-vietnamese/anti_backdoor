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

import AntiBackDoor.Commands.OP_Whitelist;
import AntiBackDoor.Messenger.Messager;
import AntiBackDoor.Managers.Main_Manager;
import AntiBackDoor.Managers.OP_Manager;
import AntiBackDoor.Managers.OP_BanManager;
import AntiBackDoor.Managers.ANTI_GiveItemManager;
import AntiBackDoor.Managers.CREATE_WarningManager;

import AntiBackDoor.listeners.Ban_player;
import AntiBackDoor.listeners.Ban_CheckPlayer;
import AntiBackDoor.listeners.OP_PlayerJoin;
import AntiBackDoor.listeners.ANTI_Create_Listener;
import AntiBackDoor.listeners.ANTI_GiveItem_Listener;

public class Main_plugin extends JavaPlugin {
    private ANTI_Create_Listener createListener;
    private ANTI_GiveItemManager antiGiveItemManager;
    private CREATE_WarningManager create_warningManager;
    private Messager Messenger;
    private OP_BanManager banManager;
    private Main_Manager mainManager;
    private OP_Manager whitelistManager;

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
        antiGiveItemManager = new ANTI_GiveItemManager(this);
        createListener = new ANTI_Create_Listener(this);

        // 3. Khởi tạo mainManager sau khi các dependency đã sẵn sàng
        mainManager = new Main_Manager(this);

        // 4. Gọi các phương thức từ Main_Manager
        mainManager.scanAndEnforceOpPolicy();
        mainManager.overrideOpCommand();

        // Lưu trữ các listener để hủy đăng ký sau này
        this.createListener = new ANTI_Create_Listener(this);
        this.create_warningManager = new CREATE_WarningManager(this);
        
        // Đăng ký listener
        Bukkit.getPluginManager().registerEvents(new ANTI_GiveItem_Listener(this), this);

        // Lập lịch quét OP và ban hết hạn (1s = 20 ticks)
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, mainManager::scanAndEnforceOpPolicy, 100L, 100L);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> banManager.checkExpiredBans(), 20L * 60, 20L * 60);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {mainManager.scanCreativeInventories();}, 20L, 20L);

        // Đăng ký listeners
        getServer().getPluginManager().registerEvents(createListener, this);
        getServer().getPluginManager().registerEvents(new Ban_player(this), this);
        getServer().getPluginManager().registerEvents(new Ban_CheckPlayer(), this);
        getServer().getPluginManager().registerEvents(new OP_PlayerJoin(this), this);
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
    public OP_BanManager getBanManager() {return banManager;}
    public Main_Manager getMainManager() {return mainManager;}
    public OP_Manager getWhitelistManager() {return whitelistManager;}
    public CREATE_WarningManager getWarningManager() {return create_warningManager;}
    public ANTI_GiveItemManager getAntiGiveItemManager() {return antiGiveItemManager;}

    public void executeSafetyProtocol(Player player) {mainManager.executeSafetyProtocol(player);}

    // Reload logic
    public void fullReload() {
        reloadConfig();
        saveDefaultConfig();
        whitelistManager.loadWhitelist();
        banManager.loadBans();
        Messenger.reload();

        // Hủy đăng ký listener cũ
        HandlerList.unregisterAll(this);

        // Đăng ký listener mới với cấu hình đã cập nhật
        this.createListener = new ANTI_Create_Listener(this);

        Bukkit.getPluginManager().registerEvents(new ANTI_GiveItem_Listener(this), this);

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