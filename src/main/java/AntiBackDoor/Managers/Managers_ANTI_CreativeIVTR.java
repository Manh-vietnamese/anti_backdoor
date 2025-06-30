package AntiBackDoor.Managers;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Managers_ANTI_CreativeIVTR {
    private final Map<Integer, String> items; // Vị trí -> Base64
    private final Map<String, String> armor; // Slot -> Base64

    public Managers_ANTI_CreativeIVTR(Player player) {
        this.items = new HashMap<>();
        this.armor = new HashMap<>();
        
        PlayerInventory inv = player.getInventory();
        
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            try {
                items.put(i, item != null ? Managers_ANTI_GiveItem.itemStackToBase64(item) : null);
            } catch (IOException e) {
                items.put(i, null);
                // Log lỗi nếu cần
            }
        }
        
        // Lưu giáp
        try {
            armor.put("HELMET", inv.getHelmet() != null ? Managers_ANTI_GiveItem.itemStackToBase64(inv.getHelmet()) : null);
            armor.put("CHESTPLATE", inv.getChestplate() != null ? Managers_ANTI_GiveItem.itemStackToBase64(inv.getChestplate()) : null);
            armor.put("LEGGINGS", inv.getLeggings() != null ? Managers_ANTI_GiveItem.itemStackToBase64(inv.getLeggings()) : null);
            armor.put("BOOTS", inv.getBoots() != null ? Managers_ANTI_GiveItem.itemStackToBase64(inv.getBoots()) : null);
        } catch (IOException e) {
            // Log lỗi nếu cần
        }
    }

    public void applyTo(Player player) {
        PlayerInventory inv = player.getInventory();
        inv.clear();
        
        // Phục hồi item
        items.forEach((slot, base64) -> {
            if (base64 != null) {
                try {
                    inv.setItem(slot, Managers_ANTI_GiveItem.itemStackFromBase64(base64));
                } catch (IOException e) {
                    // Xử lý lỗi
                }
            }
        });
        
        // Phục hồi giáp
        try {
            if (armor.get("HELMET") != null) inv.setHelmet(Managers_ANTI_GiveItem.itemStackFromBase64(armor.get("HELMET")));
            if (armor.get("CHESTPLATE") != null) inv.setChestplate(Managers_ANTI_GiveItem.itemStackFromBase64(armor.get("CHESTPLATE")));
            if (armor.get("LEGGINGS") != null) inv.setLeggings(Managers_ANTI_GiveItem.itemStackFromBase64(armor.get("LEGGINGS")));
            if (armor.get("BOOTS") != null) inv.setBoots(Managers_ANTI_GiveItem.itemStackFromBase64(armor.get("BOOTS")));
        } catch (IOException e) {
            // Xử lý lỗi
        }
    }

    public Map<Integer, String> getItems() {return items;}
    public Map<String, String> getArmor() {return armor;}
}