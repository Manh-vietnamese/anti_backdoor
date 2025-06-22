package AntiBackDoor.Managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap; 
import java.util.Map;
import java.util.UUID;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import AntiBackDoor.Main_plugin;

public class ANTI_GiveItemManager {
    private final Main_plugin plugin;
    private final Map<UUID, ANTI_CreativeInventoryData> savedInventories = new HashMap<>();
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ANTI_GiveItemManager(Main_plugin plugin) {
        this.plugin = plugin;
    }

    public void saveCreativeInventory(Player player) {
        UUID uuid = player.getUniqueId();
        ANTI_CreativeInventoryData data = new ANTI_CreativeInventoryData(player);
        savedInventories.put(uuid, data);
        
        // Tạo thư mục nếu chưa tồn tại
        File folder = new File(plugin.getDataFolder(), "creative_inventories");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        
        File file = new File(folder, uuid + ".json");
        
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(data, writer);
            plugin.getLogger().info("Đã lưu inventory cho " + player.getName() + " vào file: " + file.getName());
        } catch (IOException e) {
            plugin.getLogger().severe("Lỗi khi lưu inventory: " + e.getMessage());
            e.printStackTrace(); // Thêm stack trace để debug
        }
    }

    public void restoreCreativeInventory(Player player) {
        ANTI_CreativeInventoryData data = savedInventories.get(player.getUniqueId());
        if (data != null) {
            data.applyTo(player);
        }
    }

    public boolean hasSavedInventory(Player player) {
        return savedInventories.containsKey(player.getUniqueId());
    }

    public void removeSavedInventory(Player player) {
        savedInventories.remove(player.getUniqueId());
        File file = new File(plugin.getDataFolder(), "creative_inventories/" + player.getUniqueId() + ".json");
        if (file.exists()) {
            file.delete();
        }
    }

    public boolean isInventoryChanged(Player player) {
        ANTI_CreativeInventoryData saved = savedInventories.get(player.getUniqueId());
        if (saved == null) return false;
        
        PlayerInventory currentInv = player.getInventory();
        
        // Kiểm tra nhanh các slot quan trọng trước
        if (checkChangedArmor(currentInv, saved)) return true;
        if (checkChangedHotbar(currentInv, saved)) return true;
        
        // Kiểm tra toàn bộ inventory
        Map<Integer, String> savedItems = saved.getItems();
        for (int slot = 0; slot < currentInv.getSize(); slot++) {
            ItemStack currentItem = currentInv.getItem(slot);
            String savedBase64 = savedItems.get(slot);
            
            // Xử lý trường hợp null
            if (currentItem == null && savedBase64 == null) continue;
            if (currentItem == null || savedBase64 == null) return true;
            
            // So sánh bằng base64
            try {
                String currentBase64 = itemStackToBase64(currentItem);
                if (!currentBase64.equals(savedBase64)) {
                    return true;
                }
            } catch (IOException e) {
                return true;
            }
        }
        
        return false;
    }

    // Kiểm tra nhanh giáp
    private boolean checkChangedArmor(PlayerInventory inv, ANTI_CreativeInventoryData saved) {
        try {
            Map<String, String> savedArmor = saved.getArmor();
            if (!compareSingleArmor(inv.getHelmet(), savedArmor.get("HELMET"))) return true;
            if (!compareSingleArmor(inv.getChestplate(), savedArmor.get("CHESTPLATE"))) return true;
            if (!compareSingleArmor(inv.getLeggings(), savedArmor.get("LEGGINGS"))) return true;
            if (!compareSingleArmor(inv.getBoots(), savedArmor.get("BOOTS"))) return true;
        } catch (IOException e) {
            return true;
        }
        return false;
    }

    // Kiểm tra nhanh hotbar (slot 0-8)
    private boolean checkChangedHotbar(PlayerInventory inv, ANTI_CreativeInventoryData saved) {
        Map<Integer, String> savedItems = saved.getItems();
        for (int slot = 0; slot < 9; slot++) {
            ItemStack currentItem = inv.getItem(slot);
            String savedBase64 = savedItems.get(slot);
            
            if (currentItem == null && savedBase64 == null) continue;
            if (currentItem == null || savedBase64 == null) return true;
            
            try {
                String currentBase64 = itemStackToBase64(currentItem);
                if (!currentBase64.equals(savedBase64)) {
                    return true;
                }
            } catch (IOException e) {
                return true;
            }
        }
        return false;
    }

    private boolean compareSingleArmor(ItemStack current, String savedBase64) throws IOException {
        if (current == null && savedBase64 == null) return true;
        if (current == null || savedBase64 == null) return false;
        return itemStackToBase64(current).equals(savedBase64);
    }
    
    // Chuyển ItemStack thành Base64 (public và static để có thể gọi từ ANTI_CreativeInventoryData)
    public static String itemStackToBase64(ItemStack item) throws IOException {
        if (item == null) {
            return null;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {
            dataOutput.writeObject(item);
        }
        return Base64Coder.encodeLines(outputStream.toByteArray());
    }

    // Chuyển Base64 thành ItemStack (public và static để có thể gọi từ ANTI_CreativeInventoryData)
    public static ItemStack itemStackFromBase64(String data) throws IOException {
        if (data == null || data.isEmpty()) {
            return null;
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
        try (BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
            return (ItemStack) dataInput.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Lỗi chuyển đổi Base64", e);
        }
    }
}