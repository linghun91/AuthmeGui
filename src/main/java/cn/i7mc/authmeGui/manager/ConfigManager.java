package cn.i7mc.authmeGui.manager;

import cn.i7mc.authmeGui.AuthmeGui;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 配置文件管理器抽象类
 * 统一处理所有配置文件的加载、保存和重载
 */
public abstract class ConfigManager {
    
    protected final AuthmeGui plugin;
    protected final Map<String, FileConfiguration> configs;
    protected final Map<String, File> configFiles;
    
    public ConfigManager(AuthmeGui plugin) {
        this.plugin = plugin;
        this.configs = new HashMap<>();
        this.configFiles = new HashMap<>();
    }
    
    /**
     * 初始化配置管理器
     */
    public abstract void initialize();
    
    /**
     * 重载所有配置文件
     */
    public abstract void reloadConfigs();
    
    /**
     * 加载指定配置文件
     * @param fileName 配置文件名
     * @return 是否加载成功
     */
    protected boolean loadConfig(String fileName) {
        try {
            File configFile = new File(plugin.getDataFolder(), fileName);
            
            // 如果文件不存在，从资源中复制
            if (!configFile.exists()) {
                plugin.saveResource(fileName, false);
            }
            
            FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            configs.put(fileName, config);
            configFiles.put(fileName, configFile);
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 保存指定配置文件
     * @param fileName 配置文件名
     * @return 是否保存成功
     */
    protected boolean saveConfig(String fileName) {
        try {
            FileConfiguration config = configs.get(fileName);
            File configFile = configFiles.get(fileName);
            
            if (config != null && configFile != null) {
                config.save(configFile);
                return true;
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }
    
    /**
     * 获取指定配置文件
     * @param fileName 配置文件名
     * @return 配置文件对象
     */
    public FileConfiguration getConfig(String fileName) {
        return configs.get(fileName);
    }
    
    /**
     * 获取主配置文件
     * @return 主配置文件
     */
    public FileConfiguration getMainConfig() {
        return getConfig("config.yml");
    }
    
    /**
     * 获取消息配置文件
     * @return 消息配置文件
     */
    public FileConfiguration getMessageConfig() {
        return getConfig("message.yml");
    }
    

}
