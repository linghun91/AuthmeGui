package cn.i7mc.authmeGui.manager.impl;

import cn.i7mc.authmeGui.AuthmeGui;
import cn.i7mc.authmeGui.manager.AuthMeManager;
import cn.i7mc.authmeGui.manager.ConfigManager;
import cn.i7mc.authmeGui.manager.MessageManager;
import org.bukkit.entity.Player;

/**
 * AuthMe管理器实现类
 */
public class AuthMeManagerImpl extends AuthMeManager {
    
    private final ConfigManager configManager;
    
    public AuthMeManagerImpl(AuthmeGui plugin, MessageManager messageManager, ConfigManager configManager) {
        super(plugin, messageManager);
        this.configManager = configManager;
    }
    
    @Override
    public void initialize() {
        
        // 尝试连接AuthMe插件
        if (hookAuthMe()) {
        } else {
        }
    }
    
    @Override
    public boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }

        // 获取密码长度限制
        int minLength = configManager.getMainConfig().getInt("security.min-password-length", 6);
        int maxLength = configManager.getMainConfig().getInt("security.max-password-length", 20);

        // 检查密码长度
        if (password.length() < minLength) {
            return false;
        }

        if (password.length() > maxLength) {
            return false;
        }

        // 检查密码复杂度（如果启用）
        boolean complexityCheck = configManager.getMainConfig().getBoolean("security.password-complexity", false);
        if (complexityCheck) {
            if (!isPasswordComplex(password)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 验证密码并发送相应的错误消息
     * @param player 玩家
     * @param password 密码
     * @return 是否有效
     */
    public boolean isValidPasswordWithMessage(Player player, String password) {
        if (password == null || password.trim().isEmpty()) {
            messageManager.sendMessage(player, "gui.password-placeholder", null);
            return false;
        }

        // 获取密码长度限制
        int minLength = configManager.getMainConfig().getInt("security.min-password-length", 6);
        int maxLength = configManager.getMainConfig().getInt("security.max-password-length", 20);

        // 检查密码长度
        if (password.length() < minLength) {
            messageManager.sendMessage(player, "gui.password-too-short",
                messageManager.createPlaceholders("min", String.valueOf(minLength)));
            return false;
        }

        if (password.length() > maxLength) {
            messageManager.sendMessage(player, "gui.password-too-long",
                messageManager.createPlaceholders("max", String.valueOf(maxLength)));
            return false;
        }

        // 检查密码复杂度（如果启用）
        boolean complexityCheck = configManager.getMainConfig().getBoolean("security.password-complexity", false);
        if (complexityCheck) {
            if (!isPasswordComplex(password)) {
                messageManager.sendMessage(player, "gui.password-complexity-failed", null);
                return false;
            }
        }

        return true;
    }
    
    @Override
    public void handleLoginSuccess(Player player) {
        // 发送成功消息
        messageManager.sendMessage(player, "gui.login-success", 
            messageManager.createPlaceholders("player", player.getName()));
        
        // 执行登录成功后的动作
        executeSuccessActions(player, "authme.login-success-actions");
    }
    
    @Override
    public void handleRegisterSuccess(Player player) {
        // 发送成功消息
        messageManager.sendMessage(player, "gui.register-success", 
            messageManager.createPlaceholders("player", player.getName()));
        
        // 执行注册成功后的动作
        executeSuccessActions(player, "authme.register-success-actions");
    }
    
    @Override
    public void handleFailure(Player player, String reason) {
        // 发送失败消息
        messageManager.sendMessage(player, "gui.login-failed", null);
        
        // 执行失败后的动作
        executeSuccessActions(player, "authme.failure-actions");
    }
    
    /**
     * 检查密码复杂度
     * @param password 密码
     * @return 是否符合复杂度要求
     */
    private boolean isPasswordComplex(String password) {
        // 简单的复杂度检查：至少包含字母和数字
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        
        return hasLetter && hasDigit;
    }
    
    /**
     * 执行成功后的动作
     * @param player 玩家
     * @param configPath 配置路径
     */
    private void executeSuccessActions(Player player, String configPath) {
        var actions = configManager.getMainConfig().getStringList(configPath);
        
        for (String action : actions) {
            executeAction(player, action);
        }
    }
    
    /**
     * 执行单个动作
     * @param player 玩家
     * @param action 动作字符串
     */
    private void executeAction(Player player, String action) {
        if (action == null || action.isEmpty()) {
            return;
        }
        
        try {
            if (action.startsWith("[message]")) {
                String message = action.substring(9);
                player.sendMessage(messageManager.formatMessage(message, 
                    messageManager.createPlaceholders("player", player.getName())));
            } else if (action.startsWith("[command]")) {
                String command = action.substring(9);
                plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), 
                    command.replace("{player}", player.getName()));
            }
        } catch (Exception e) {
        }
    }
}
