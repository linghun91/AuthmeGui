package cn.i7mc.authmeGui.manager.impl;

import cn.i7mc.authmeGui.AuthmeGui;
import cn.i7mc.authmeGui.manager.ConfigManager;
import cn.i7mc.authmeGui.manager.LoginPluginManager;
import cn.i7mc.authmeGui.manager.MessageManager;
import fr.xephi.authme.api.v3.AuthMeApi;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * AuthMe登录插件适配器
 */
public class AuthMeLoginAdapter extends LoginPluginManager {
    
    private AuthMeApi authMeApi;
    
    public AuthMeLoginAdapter(AuthmeGui plugin, MessageManager messageManager, ConfigManager configManager) {
        super(plugin, messageManager, configManager);
    }
    
    @Override
    public void initialize() {
        if (hookAuthMe()) {
        } else {
        }
    }
    
    @Override
    public String getPluginType() {
        return "authme";
    }
    
    /**
     * 检查并连接AuthMe插件
     * @return 是否成功连接
     */
    private boolean hookAuthMe() {
        try {
            Plugin authMePlugin = plugin.getServer().getPluginManager().getPlugin("AuthMe");
            if (authMePlugin == null || !authMePlugin.isEnabled()) {
                return false;
            }
            
            authMeApi = AuthMeApi.getInstance();
            if (authMeApi == null) {
                return false;
            }
            
            pluginEnabled = true;
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public boolean isPlayerRegistered(Player player) {
        if (!isPluginEnabled()) {
            return false;
        }
        
        try {
            return authMeApi.isRegistered(player.getName());
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public boolean isPlayerLoggedIn(Player player) {
        if (!isPluginEnabled()) {
            return false;
        }
        
        try {
            return authMeApi.isAuthenticated(player);
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public boolean forceLogin(Player player) {
        if (!isPluginEnabled()) {
            return false;
        }
        
        try {
            authMeApi.forceLogin(player);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public boolean registerPlayer(Player player, String password) {
        if (!isPluginEnabled()) {
            return false;
        }

        try {
            authMeApi.registerPlayer(player.getName(), password);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public boolean checkPassword(Player player, String password) {
        if (!isPluginEnabled()) {
            return false;
        }
        
        try {
            return authMeApi.checkPassword(player.getName(), password);
        } catch (Exception e) {
            return false;
        }
    }
}