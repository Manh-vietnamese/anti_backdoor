package anntibackdoor;

import anntibackdoor.Messenger.Messager;
import anntibackdoor.commands.OP_Whitelist;
import anntibackdoor.config.OP_Manager;
import anntibackdoor.listeners.OP_BanCheck;
import anntibackdoor.listeners.OP_Ban;
import anntibackdoor.listeners.OP_PlayerJoin;
import anntibackdoor.managers.OP_BanManager;
import anntibackdoor.managers.Main_Manager;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main_plugin extends JavaPlugin {
    private OP_Manager whitelistManager;
    private OP_BanManager banManager;
    private Main_Manager mainManager;
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
        mainManager.registerMainCommand();
        mainManager.overrideOpCommand();

        // Lập lịch quét OP và ban hết hạn
        Bukkit.getScheduler().scheduleSyncRepeatingTask(
            this, mainManager::scanAndEnforceOpPolicy, 100L, 100L
        );
        Bukkit.getScheduler().scheduleSyncRepeatingTask(
            this, () -> banManager.checkExpiredBans(), 20L * 60, 20L * 60
        );

        // Đăng ký listeners
        getServer().getPluginManager().registerEvents(new OP_Ban(this), this);
        getServer().getPluginManager().registerEvents(new OP_BanCheck(), this);
        getServer().getPluginManager().registerEvents(new OP_PlayerJoin(this), this);
        this.getCommand("wop").setExecutor(new OP_Whitelist(this));
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this); // Hủy tất cả tasks
    }

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
    public Messager getMessenger() { return Messenger; }
    public Main_Manager getMainManager() { return mainManager; }

    public void executeSafetyProtocol(Player player) {
        mainManager.executeSafetyProtocol(player);
    }

    // Reload logic
    public void fullReload() {
        reloadConfig();
        saveDefaultConfig();
        whitelistManager.loadWhitelist();
        Messenger.reload();
        mainManager.reloadListeners();
        Bukkit.getScheduler().cancelTasks(this);
        Bukkit.getScheduler().scheduleSyncRepeatingTask(
            this, mainManager::scanAndEnforceOpPolicy, 100L, 100L
        );
    }

    private void saveDefaultMessages() {
        File msgFile = new File(getDataFolder(), "messages.yml");
        if (!msgFile.exists()) {
            saveResource("messages.yml", false); // Sao chép từ resources
        }
    }
}