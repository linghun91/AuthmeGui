package cn.i7mc.authmeGui.manager;

import cn.i7mc.authmeGui.AuthmeGui;
import fr.xephi.authme.api.v3.AuthMeApi;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * AuthMe API管理器抽象类
 * 统一处理与AuthMe插件的所有交互
 */
public abstract class AuthMeManager {
    
    protected final AuthmeGui plugin;
    protected final MessageManager messageManager;
    protected AuthMeApi authMeApi;
    protected boolean authMeEnabled;
    
    public AuthMeManager(AuthmeGui plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
        this.authMeEnabled = false;
    }
    
    /**
     * 初始化AuthMe管理器
     */
    public abstract void initialize();
    
    /**
     * 检查并连接AuthMe插件
     * @return 是否成功连接
     */
    protected boolean hookAuthMe() {
        try {
            Plugin authMePlugin = plugin.getServer().getPluginManager().getPlugin("AuthMe");
            if (authMePlugin == null || !authMePlugin.isEnabled()) {
                return false;
            }
            
            authMeApi = AuthMeApi.getInstance();
            if (authMeApi == null) {
                return false;
            }
            
            authMeEnabled = true;

            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查AuthMe是否可用
     * @return 是否可用
     */
    public boolean isAuthMeEnabled() {
        return authMeEnabled && authMeApi != null;
    }
    
    /**
     * 检查玩家是否已注册
     * @param player 玩家
     * @return 是否已注册
     */
    public boolean isPlayerRegistered(Player player) {
        if (!isAuthMeEnabled()) {
            return false;
        }
        
        try {
            boolean registered = authMeApi.isRegistered(player.getName());
            return registered;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 检查玩家是否已登录
     * @param player 玩家
     * @return 是否已登录
     */
    public boolean isPlayerLoggedIn(Player player) {
        if (!isAuthMeEnabled()) {
            return false;
        }
        
        try {
            boolean loggedIn = authMeApi.isAuthenticated(player);
            return loggedIn;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 强制玩家登录
     * @param player 玩家
     * @return 是否成功
     */
    public boolean forceLogin(Player player) {
        if (!isAuthMeEnabled()) {
            return false;
        }
        
        try {
            authMeApi.forceLogin(player);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 注册玩家
     * @param player 玩家
     * @param password 密码
     * @return 是否成功
     */
    public boolean registerPlayer(Player player, String password) {
        if (!isAuthMeEnabled()) {
            return false;
        }

        try {
            authMeApi.registerPlayer(player.getName(), password);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 强制玩家注册（保留兼容性）
     * @param player 玩家
     * @param password 密码
     * @return 是否成功
     */
    public boolean forceRegister(Player player, String password) {
        return registerPlayer(player, password);
    }
    
    /**
     * 检查密码是否正确
     * @param player 玩家
     * @param password 密码
     * @return 是否正确
     */
    public boolean checkPassword(Player player, String password) {
        if (!isAuthMeEnabled()) {
            return false;
        }
        
        try {
            boolean correct = authMeApi.checkPassword(player.getName(), password);
            return correct;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 验证密码格式
     * @param password 密码
     * @return 是否有效
     */
    public abstract boolean isValidPassword(String password);

    /**
     * 验证密码格式并发送相应的错误消息
     * @param player 玩家
     * @param password 密码
     * @return 是否有效
     */
    public abstract boolean isValidPasswordWithMessage(Player player, String password);
    
    /**
     * 处理登录成功
     * @param player 玩家
     */
    public abstract void handleLoginSuccess(Player player);
    
    /**
     * 处理注册成功
     * @param player 玩家
     */
    public abstract void handleRegisterSuccess(Player player);
    
    /**
     * 处理操作失败
     * @param player 玩家
     * @param reason 失败原因
     */
    public abstract void handleFailure(Player player, String reason);
}
