package cn.i7mc.authmeGui.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Map;

/**
 * 菜单配置数据类
 * 用于存储从YAML文件解析的菜单配置信息
 */
public class MenuConfig {
    
    private final String title;
    private final List<String> commands;
    private final List<String> openActions;
    private final List<String> closeActions;
    private final Map<String, ItemConfig> items;
    private final Map<String, ItemConfig> playerItems;
    
    public MenuConfig(String title, List<String> commands, List<String> openActions, 
                     List<String> closeActions, Map<String, ItemConfig> items, 
                     Map<String, ItemConfig> playerItems) {
        this.title = title;
        this.commands = commands;
        this.openActions = openActions;
        this.closeActions = closeActions;
        this.items = items;
        this.playerItems = playerItems;
    }
    
    public String getTitle() {
        return title;
    }
    
    public List<String> getCommands() {
        return commands;
    }
    
    public List<String> getOpenActions() {
        return openActions;
    }
    
    public List<String> getCloseActions() {
        return closeActions;
    }
    
    public Map<String, ItemConfig> getItems() {
        return items;
    }
    
    public Map<String, ItemConfig> getPlayerItems() {
        return playerItems;
    }
    
    /**
     * 物品配置数据类
     */
    public static class ItemConfig {
        private final String id;
        private final String type;
        private final String name;
        private final List<String> lore;
        private final int amount;
        private final Integer customModelData;
        private final Object slot; // 可以是Integer或String
        private final List<String> slots;
        private final List<String> clickAction;
        
        public ItemConfig(String id, String type, String name, List<String> lore, 
                         int amount, Integer customModelData, Object slot, 
                         List<String> slots, List<String> clickAction) {
            this.id = id;
            this.type = type;
            this.name = name;
            this.lore = lore;
            this.amount = amount;
            this.customModelData = customModelData;
            this.slot = slot;
            this.slots = slots;
            this.clickAction = clickAction;
        }
        
        public String getId() {
            return id;
        }
        
        public String getType() {
            return type;
        }
        
        public String getName() {
            return name;
        }
        
        public List<String> getLore() {
            return lore;
        }
        
        public int getAmount() {
            return amount;
        }
        
        public Integer getCustomModelData() {
            return customModelData;
        }
        
        public Object getSlot() {
            return slot;
        }
        
        public List<String> getSlots() {
            return slots;
        }
        
        public List<String> getClickAction() {
            return clickAction;
        }
        
        /**
         * 解析物品类型
         * @return Material对象
         */
        public Material parseMaterial() {
            if (type == null) {
                return Material.STONE;
            }
            
            // 处理特殊类型
            if (type.startsWith("craftEngine-")) {
                // TODO: 处理CraftEngine物品
                return Material.STONE;
            } else if (type.startsWith("mythicMobs-")) {
                // TODO: 处理MythicMobs物品
                return Material.STONE;
            } else if (type.startsWith("head-")) {
                return Material.PLAYER_HEAD;
            } else if (type.equals("random_bed")) {
                // 随机颜色的床
                return Material.RED_BED;
            }
            
            // 尝试解析为普通Material
            try {
                return Material.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                return Material.STONE;
            }
        }
    }
}
