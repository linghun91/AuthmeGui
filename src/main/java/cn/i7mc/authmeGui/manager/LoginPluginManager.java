package cn.i7mc.authmeGui.manager;

import cn.i7mc.authmeGui.AuthmeGui;
import org.bukkit.entity.Player;

/**
 * 登录插件管理器抽象类
 * 统一处理所有登录插件的交互接口
 */
public abstract class LoginPluginManager {
    
    protected final AuthmeGui plugin;
    protected final MessageManager messageManager;
    protected final ConfigManager configManager;
    protected boolean pluginEnabled;
    
    public LoginPluginManager(AuthmeGui plugin, MessageManager messageManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
        this.configManager = configManager;
        this.pluginEnabled = false;
    }
    
    /**
     * 初始化登录插件管理器
     */
    public abstract void initialize();
    
    /**
     * 获取插件类型名称
     * @return 插件类型
     */
    public abstract String getPluginType();
    
    /**
     * 检查登录插件是否可用
     * @return 是否可用
     */
    public boolean isPluginEnabled() {
        return pluginEnabled;
    }
    
    /**
     * 检查玩家是否已注册
     * @param player 玩家
     * @return 是否已注册
     */
    public abstract boolean isPlayerRegistered(Player player);
    
    /**
     * 检查玩家是否已登录
     * @param player 玩家
     * @return 是否已登录
     */
    public abstract boolean isPlayerLoggedIn(Player player);
    
    /**
     * 强制玩家登录
     * @param player 玩家
     * @return 是否成功
     */
    public abstract boolean forceLogin(Player player);
    
    /**
     * 注册玩家
     * @param player 玩家
     * @param password 密码
     * @return 是否成功
     */
    public abstract boolean registerPlayer(Player player, String password);
    
    /**
     * 检查密码是否正确
     * @param player 玩家
     * @param password 密码
     * @return 是否正确
     */
    public abstract boolean checkPassword(Player player, String password);
    
    /**
     * 验证密码格式
     * @param password 密码
     * @return 是否有效
     */
    public boolean isValidPassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            return false;
        }

        int minLength = configManager.getMainConfig().getInt("security.min-password-length", 6);
        int maxLength = configManager.getMainConfig().getInt("security.max-password-length", 20);

        if (password.length() < minLength || password.length() > maxLength) {
            return false;
        }

        boolean complexityCheck = configManager.getMainConfig().getBoolean("security.password-complexity", false);
        if (complexityCheck) {
            return isPasswordComplex(password);
        }

        return true;
    }
    
    /**
     * 验证密码格式并发送相应的错误消息
     * @param player 玩家
     * @param password 密码
     * @return 是否有效
     */
    public boolean isValidPasswordWithMessage(Player player, String password) {
        if (password == null || password.trim().isEmpty()) {
            messageManager.sendMessage(player, "gui.password-placeholder", null);
            return false;
        }

        int minLength = configManager.getMainConfig().getInt("security.min-password-length", 6);
        int maxLength = configManager.getMainConfig().getInt("security.max-password-length", 20);

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

        boolean complexityCheck = configManager.getMainConfig().getBoolean("security.password-complexity", false);
        if (complexityCheck) {
            if (!isPasswordComplex(password)) {
                messageManager.sendMessage(player, "gui.password-complexity-failed", null);
                return false;
            }
        }

        return true;
    }
    
    /**
     * 处理登录成功
     * @param player 玩家
     */
    public void handleLoginSuccess(Player player) {
        messageManager.sendMessage(player, "gui.login-success", 
            messageManager.createPlaceholders("player", player.getName()));
        
        executeSuccessActions(player, "login-actions.login-success-actions");
    }
    
    /**
     * 处理注册成功
     * @param player 玩家
     */
    public void handleRegisterSuccess(Player player) {
        messageManager.sendMessage(player, "gui.register-success", 
            messageManager.createPlaceholders("player", player.getName()));
        
        executeSuccessActions(player, "login-actions.register-success-actions");
    }
    
    /**
     * 处理操作失败
     * @param player 玩家
     * @param reason 失败原因
     */
    public void handleFailure(Player player, String reason) {
        messageManager.sendMessage(player, "gui.login-failed", null);
        
        executeSuccessActions(player, "login-actions.failure-actions");
    }
    
    /**
     * 检查密码复杂度
     * @param password 密码
     * @return 是否符合复杂度要求
     */
    protected boolean isPasswordComplex(String password) {
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        
        return hasLetter && hasDigit;
    }
    
    /**
     * 执行成功后的动作
     * @param player 玩家
     * @param configPath 配置路径
     */
    protected void executeSuccessActions(Player player, String configPath) {
        java.util.List<String> actions = configManager.getMainConfig().getStringList(configPath);
        
        for (String action : actions) {
            executeAction(player, action);
        }
    }
    
    /**
     * 执行单个动作
     * @param player 玩家
     * @param action 动作字符串
     */
    protected void executeAction(Player player, String action) {
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