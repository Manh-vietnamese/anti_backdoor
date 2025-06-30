package AntiBackDoor.Messenger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class Messager {

    private static final Logger LOGGER = Logger.getLogger(Messager.class.getName());
    private static YamlConfiguration messagesConfig;
    private static File messagesFile;

    // Khởi tạo Messager với plugin
    public static void init(Plugin plugin) {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        reload(plugin);
    }

    // Reload cấu hình tin nhắn
    public static void reload(Plugin plugin) {
        // Tạo file nếu chưa tồn tại
        if (!messagesFile.exists()) {
            try {
                messagesFile.getParentFile().mkdirs();
                if (messagesFile.createNewFile()) {
                    LOGGER.info("Đã tạo file messages.yml mới.");
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Lỗi khi tạo file messages.yml: " + e.getMessage(), e);
            }
        }

        // Kiểm tra và sao chép file mặc định nếu file trống
        if (messagesFile.length() == 0) {
            copyDefaultMessages(plugin);
        }

        // Load cấu hình
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        
        // if (messagesConfig.getKeys(false).isEmpty()) {
        //     LOGGER.warning("messages.yml vẫn trống sau khi sao chép! Các key mặc định không tồn tại.");
        // } else {
        //     LOGGER.info("Đã tải messages.yml. Các key có sẵn: " + messagesConfig.getKeys(true));
        // }
    }

    // Sao chép file mặc định từ resources
    private static void copyDefaultMessages(Plugin plugin) {
        try (InputStream in = plugin.getResource("messages.yml")) {
            if (in == null) {
                LOGGER.severe("Không tìm thấy file messages.yml trong resources!");
                return;
            }
            
            Files.copy(in, messagesFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("Đã sao chép messages.yml mặc định từ resources");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Lỗi khi sao chép messages.yml từ resources: " + e.getMessage(), e);
        }
    }

    // Gửi tin nhắn trực tiếp đến người gửi
    public static void send(CommandSender sender, String key) {
        sender.sendMessage(get(key));
    }

    // Gửi tin nhắn với placeholder
    public static void send(CommandSender sender, String key, Map<String, String> placeholders) {
        sender.sendMessage(get(key, placeholders));
    }

    // Lấy tin nhắn dạng string
    public static String get(String key) {
        return get(key, null);
    }

    // Lấy danh sách tin nhắn
    public static List<String> getList(String key) {
        return messagesConfig.getStringList(key);
    }

    // Lấy tin nhắn với placeholder
    public static String get(String key, Map<String, String> placeholders) {
        String msg = messagesConfig.getString(key, "&c[Không tìm thấy key: " + key + "]");

        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                msg = msg.replace("%" + entry.getKey() + "%", entry.getValue());
            }
        }

        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
// ================ SAMPLED MESSAGES ================
// 
// Nếu bạn muốn gửi tin nhắn trong lệnh, bạn đã có sẵn CommandSender từ phương thức onCommand:
//     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
//         Messager.send(sender, "enderchest_fully_upgraded");
//     }
// Nếu bạn muốn gửi đến console, sử dụng:
//     Messager.send(Bukkit.getConsoleSender(), "your_message_key");