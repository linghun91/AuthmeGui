package cn.i7mc.authmeGui.config;

import cn.i7mc.authmeGui.AuthmeGui;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 菜单配置解析器
 * 负责解析menu目录下的YAML配置文件
 */
public abstract class MenuConfigParser {
    
    protected final AuthmeGui plugin;
    protected final Map<String, MenuConfig> menuConfigs;
    
    public MenuConfigParser(AuthmeGui plugin) {
        this.plugin = plugin;
        this.menuConfigs = new HashMap<>();
    }
    
    /**
     * 初始化解析器
     */
    public abstract void initialize();
    
    /**
     * 加载所有菜单配置
     */
    public void loadAllMenuConfigs() {
        File menuDir = new File(plugin.getDataFolder(), "menu");
        
        // 确保menu目录存在
        if (!menuDir.exists()) {
            menuDir.mkdirs();
            // 复制默认配置文件
            plugin.saveResource("menu/login.yml", false);
            plugin.saveResource("menu/register.yml", false);
        }
        
        // 加载所有yml文件
        File[] files = menuDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                loadMenuConfig(file);
            }
        }
    }
    
    /**
     * 加载单个菜单配置文件
     * @param file 配置文件
     */
    public void loadMenuConfig(File file) {
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            String fileName = file.getName().replace(".yml", "");
            
            MenuConfig menuConfig = parseMenuConfig(config);
            menuConfigs.put(fileName, menuConfig);
            
        } catch (Exception e) {
        }
    }
    
    /**
     * 解析菜单配置
     * @param config 配置文件对象
     * @return 菜单配置对象
     */
    protected MenuConfig parseMenuConfig(FileConfiguration config) {
        String title = config.getString("title", "默认标题");
        List<String> commands = config.getStringList("commands");
        List<String> openActions = config.getStringList("openActions");
        List<String> closeActions = config.getStringList("closeActions");
        
        Map<String, MenuConfig.ItemConfig> items = parseItems(config.getConfigurationSection("items"));
        Map<String, MenuConfig.ItemConfig> playerItems = parseItems(config.getConfigurationSection("playerItems"));
        
        return new MenuConfig(title, commands, openActions, closeActions, items, playerItems);
    }
    
    /**
     * 解析物品配置
     * @param section 配置节
     * @return 物品配置映射
     */
    protected Map<String, MenuConfig.ItemConfig> parseItems(ConfigurationSection section) {
        Map<String, MenuConfig.ItemConfig> items = new HashMap<>();
        
        if (section == null) {
            return items;
        }
        
        for (String key : section.getKeys(false)) {
            ConfigurationSection itemSection = section.getConfigurationSection(key);
            if (itemSection != null) {
                MenuConfig.ItemConfig itemConfig = parseItemConfig(key, itemSection);
                items.put(key, itemConfig);
            }
        }
        
        return items;
    }
    
    /**
     * 解析单个物品配置
     * @param key 物品键名
     * @param section 物品配置节
     * @return 物品配置对象
     */
    protected MenuConfig.ItemConfig parseItemConfig(String key, ConfigurationSection section) {
        String id = section.getString("id", key);
        String type = section.getString("type", "STONE");
        String name = section.getString("name", "");
        List<String> lore = section.getStringList("lore");
        int amount = section.getInt("amount", 1);
        Integer customModelData = section.contains("customModelData") ? 
            section.getInt("customModelData") : null;
        
        // 处理slot和slots
        Object slot = null;
        List<String> slots = null;
        
        if (section.contains("slot")) {
            Object slotValue = section.get("slot");
            if (slotValue instanceof Integer) {
                slot = slotValue;
            } else if (slotValue instanceof String) {
                slot = slotValue;
            }
        }
        
        if (section.contains("slots")) {
            slots = section.getStringList("slots");
        }
        
        List<String> clickAction = section.getStringList("clickAction");
        
        return new MenuConfig.ItemConfig(id, type, name, lore, amount, 
            customModelData, slot, slots, clickAction);
    }
    
    /**
     * 获取菜单配置
     * @param menuName 菜单名称
     * @return 菜单配置对象
     */
    public MenuConfig getMenuConfig(String menuName) {
        return menuConfigs.get(menuName);
    }
    
    /**
     * 获取登录菜单配置
     * @return 登录菜单配置
     */
    public MenuConfig getLoginMenuConfig() {
        return getMenuConfig("login");
    }
    
    /**
     * 获取注册菜单配置
     * @return 注册菜单配置
     */
    public MenuConfig getRegisterMenuConfig() {
        return getMenuConfig("register");
    }
    
    /**
     * 重载所有菜单配置
     */
    public void reloadMenuConfigs() {
        menuConfigs.clear();
        loadAllMenuConfigs();
    }
    
    /**
     * 获取所有菜单配置
     * @return 菜单配置映射
     */
    public Map<String, MenuConfig> getAllMenuConfigs() {
        return new HashMap<>(menuConfigs);
    }
}
