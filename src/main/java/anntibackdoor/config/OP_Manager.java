package anntibackdoor.config;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class OP_Manager {
    private final JavaPlugin plugin;
    private final Set<AllowedOP> allowedOPs = new HashSet<>();
    private final File whitelistFile;

    // Lớp nội bộ lưu trữ thông tin OP
    private static class AllowedOP {
        private final UUID uuid;
        private final String name;

        public AllowedOP(UUID uuid, String name) {
            this.uuid = uuid;
            this.name = name;
        }

        public UUID getUuid() { return uuid; }
        public String getName() { return name; }
    }

    public OP_Manager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.whitelistFile = new File(plugin.getDataFolder(), "whitelist_op.yml");
        initializeFile();
    }

    private void initializeFile() {
        if (!whitelistFile.exists()) {
            plugin.saveResource("whitelist_op.yml", false);
            plugin.getLogger().info("Created new whitelist_op.yml file");
        }
    }

    public void loadWhitelist() {
        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(whitelistFile);
            allowedOPs.clear();
            
            config.getMapList("allowed_ops").forEach(entry -> {
                UUID uuid = UUID.fromString((String) entry.get("uuid"));
                String name = (String) entry.get("name");
                allowedOPs.add(new AllowedOP(uuid, name));
            });
            
            plugin.getLogger().info("Loaded " + allowedOPs.size() + " allowed OPs");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load whitelist: " + e.getMessage());
        }
    }

    public void saveWhitelist() {
        try {
            YamlConfiguration config = new YamlConfiguration();
            List<Map<String, String>> entries = allowedOPs.stream()
                .map(op -> {
                    Map<String, String> entry = new HashMap<>();
                    entry.put("uuid", op.getUuid().toString());
                    entry.put("name", op.getName());
                    return entry;
                })
                .collect(Collectors.toList());
            
            config.set("allowed_ops", entries);
            config.save(whitelistFile);
            plugin.getLogger().info("Saved whitelist changes");
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save whitelist: " + e.getMessage());
        }
    }

    // Thêm OP mới
    public boolean addOP(UUID uuid, String name) {
        if (allowedOPs.add(new AllowedOP(uuid, name))) {
            saveWhitelist();
            return true;
        }
        return false;
    }

    // Xóa OP theo UUID
    public boolean removeOP(UUID uuid) {
        boolean removed = allowedOPs.removeIf(op -> op.getUuid().equals(uuid));
        if (removed) saveWhitelist();
        return removed;
    }

    // Kiểm tra quyền OP
    public boolean isAllowed(UUID uuid, String name) {
        return allowedOPs.stream().anyMatch(op ->
            op.getUuid().equals(uuid) ||
            op.getName().equalsIgnoreCase(name)
        );
    }

    // Đồng bộ với ops.json
    public void syncWithServerOps() {
        Bukkit.getOperators().forEach(op -> {
            if (!isAllowed(op.getUniqueId(), op.getName())) {
                op.setOp(false);
                plugin.getLogger().info("Removed illegal OP: " + op.getName());
            }
        });
    }

    // Lấy danh sách OP dạng UUID:Name
    public List<String> getAllowedOPs() {
        return allowedOPs.stream()
            .map(op -> op.getUuid() + ":" + op.getName())
            .collect(Collectors.toList());
    }
}