package cn.i7mc.authmeGui.manager.impl;

import cn.i7mc.authmeGui.AuthmeGui;
import cn.i7mc.authmeGui.manager.ConfigManager;

/**
 * 配置文件管理器实现类
 */
public class ConfigManagerImpl extends ConfigManager {
    
    public ConfigManagerImpl(AuthmeGui plugin) {
        super(plugin);
    }
    
    @Override
    public void initialize() {
        
        // 确保数据文件夹存在
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        
        // 加载所有配置文件
        loadConfig("config.yml");
        loadConfig("message.yml");
    }
    
    @Override
    public void reloadConfigs() {

        try {
            // 清理现有配置
            configs.clear();
            configFiles.clear();

            // 重载所有配置文件
            loadConfig("config.yml");
            loadConfig("message.yml");
        } catch (Exception e) {
        }
    }
}
