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
        private final List<String> ips;
        public AllowedOP(UUID uuid, String name, List<String> ips) {
            this.uuid = uuid;
            this.name = name;
            this.ips = new ArrayList<>(ips);    
        }

        // Thêm equals và hashCode
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AllowedOP allowedOP = (AllowedOP) o;
            return uuid.equals(allowedOP.uuid) && name.equals(allowedOP.name);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(uuid, name);
        }
        
        public UUID getUuid() { return uuid; }
        public String getName() { return name; }
        public List<String> getIps() { return ips; }
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
                
                // Kiểm tra và ép kiểu an toàn
                List<String> ips = new ArrayList<>();
                Object rawIps = entry.get("ips");
                
                if (rawIps instanceof List<?>) {
                    for (Object item : (List<?>) rawIps) {
                        if (item instanceof String) {
                            ips.add((String) item);
                        } else {
                            plugin.getLogger().warning("Invalid IP type in whitelist: " + item);
                        }
                    }
                } else if (rawIps != null) {
                    plugin.getLogger().warning("Invalid 'ips' format in whitelist");
                }
                
                allowedOPs.add(new AllowedOP(uuid, name, ips));
            });
            
            plugin.getLogger().info("Loaded " + allowedOPs.size() + " allowed OPs");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load whitelist: " + e.getMessage());
        }
    }

    public void saveWhitelist() {
        try {
            YamlConfiguration config = new YamlConfiguration();
            List<Map<String, Object>> entries = allowedOPs.stream().map(op -> {
                Map<String, Object> entry = new HashMap<>();
                entry.put("uuid", op.getUuid().toString());
                entry.put("name", op.getName());
                entry.put("ips", op.getIps());
                return entry;
            }).collect(Collectors.toList());
            
            config.set("allowed_ops", entries);
            config.save(whitelistFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Lỗi khi lưu whitelist: " + e.getMessage());
        }
    }

    // Thêm OP mới
    public boolean addOP(UUID uuid, String name) {
        // Thêm OP mới với danh sách IP rỗng
        if (allowedOPs.add(new AllowedOP(uuid, name, new ArrayList<>()))) {
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
            .map(op -> op.getUuid() + ":" + op.getName() + ":" + String.join(",", op.getIps()))
            .collect(Collectors.toList());
    }

    // Thêm IP vào danh sách của OP
    public void addIP(UUID uuid, String ip) {allowedOPs.stream()
        .filter(op -> op.getUuid().equals(uuid))
        .findFirst()
        .ifPresent(op -> {
            if (!op.getIps().contains(ip)) {
                op.getIps().add(ip); // Thay đổi trực tiếp danh sách IP
                saveWhitelist(); // Lưu ngay sau khi thêm IP
            }
        });
    }

    // Kiểm tra IP hợp lệ
    public boolean isValidIP(UUID uuid, String ip) {
        return allowedOPs.stream()
            .filter(op -> op.getUuid().equals(uuid))
            .anyMatch(op -> op.getIps().contains(ip));
    }

}