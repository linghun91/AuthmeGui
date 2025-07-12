package cn.i7mc.authmeGui.manager;

import cn.i7mc.authmeGui.AuthmeGui;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息管理器抽象类
 * 统一处理所有消息的发送、格式化和国际化
 */
public abstract class MessageManager {
    
    protected final AuthmeGui plugin;
    protected final ConfigManager configManager;
    
    public MessageManager(AuthmeGui plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }
    
    /**
     * 初始化消息管理器
     */
    public abstract void initialize();
    
    /**
     * 发送普通消息给玩家
     * @param player 目标玩家
     * @param messageKey 消息键
     * @param placeholders 占位符替换
     */
    public void sendMessage(Player player, String messageKey, Map<String, String> placeholders) {
        String message = getMessage(messageKey, placeholders);
        if (message != null && !message.isEmpty()) {
            player.sendMessage(message);
        }
    }
    
    /**
     * 发送普通消息给命令发送者
     * @param sender 命令发送者
     * @param messageKey 消息键
     * @param placeholders 占位符替换
     */
    public void sendMessage(CommandSender sender, String messageKey, Map<String, String> placeholders) {
        String message = getMessage(messageKey, placeholders);
        if (message != null && !message.isEmpty()) {
            sender.sendMessage(message);
        }
    }
    

    
    /**
     * 获取格式化后的消息
     * @param messageKey 消息键
     * @param placeholders 占位符替换
     * @return 格式化后的消息
     */
    public String getMessage(String messageKey, Map<String, String> placeholders) {
        FileConfiguration messageConfig = configManager.getMessageConfig();
        if (messageConfig == null) {
            return "消息配置未加载";
        }
        
        String message = messageConfig.getString(messageKey);
        if (message == null) {
            return "未找到消息: " + messageKey;
        }
        
        return formatMessage(message, placeholders);
    }
    

    
    /**
     * 格式化消息（处理颜色代码和占位符）
     * @param message 原始消息
     * @param placeholders 占位符替换
     * @return 格式化后的消息
     */
    public String formatMessage(String message, Map<String, String> placeholders) {
        if (message == null) {
            return null;
        }
        
        // 处理占位符
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        
        // 处理颜色代码
        return ChatColor.translateAlternateColorCodes('&', message);
    }
    
    /**
     * 创建占位符映射的便捷方法
     * @param key 键
     * @param value 值
     * @return 占位符映射
     */
    public Map<String, String> createPlaceholders(String key, String value) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(key, value);
        return placeholders;
    }
    
    /**
     * 创建多个占位符映射的便捷方法
     * @param keyValuePairs 键值对（偶数个参数，奇数位置为键，偶数位置为值）
     * @return 占位符映射
     */
    public Map<String, String> createPlaceholders(String... keyValuePairs) {
        Map<String, String> placeholders = new HashMap<>();
        for (int i = 0; i < keyValuePairs.length - 1; i += 2) {
            placeholders.put(keyValuePairs[i], keyValuePairs[i + 1]);
        }
        return placeholders;
    }
}
