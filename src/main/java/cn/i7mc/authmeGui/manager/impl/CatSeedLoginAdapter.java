package cn.i7mc.authmeGui.manager.impl;

import cc.baka9.catseedlogin.bukkit.CatSeedLogin;
import cc.baka9.catseedlogin.bukkit.CatSeedLoginAPI;
import cc.baka9.catseedlogin.bukkit.database.Cache;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayer;
import cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper;
import cn.i7mc.authmeGui.AuthmeGui;
import cn.i7mc.authmeGui.manager.ConfigManager;
import cn.i7mc.authmeGui.manager.LoginPluginManager;
import cn.i7mc.authmeGui.manager.MessageManager;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * CatSeedLogin登录插件适配器
 */
public class CatSeedLoginAdapter extends LoginPluginManager {
    
    private CatSeedLogin catSeedLogin;
    
    public CatSeedLoginAdapter(AuthmeGui plugin, MessageManager messageManager, ConfigManager configManager) {
        super(plugin, messageManager, configManager);
    }
    
    @Override
    public void initialize() {
        if (hookCatSeedLogin()) {
        } else {
        }
    }
    
    @Override
    public String getPluginType() {
        return "catseedlogin";
    }
    
    /**
     * 检查并连接CatSeedLogin插件
     * @return 是否成功连接
     */
    private boolean hookCatSeedLogin() {
        try {
            Plugin catSeedPlugin = plugin.getServer().getPluginManager().getPlugin("CatSeedLogin");
            if (catSeedPlugin == null || !catSeedPlugin.isEnabled()) {
                return false;
            }
            
            catSeedLogin = (CatSeedLogin) catSeedPlugin;
            if (catSeedLogin == null) {
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
            return CatSeedLoginAPI.isRegister(player.getName());
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
            return CatSeedLoginAPI.isLogin(player.getName());
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
            LoginPlayer loginPlayer = Cache.getIgnoreCase(player.getName());
            if (loginPlayer != null) {
                LoginPlayerHelper.add(loginPlayer);
                LoginPlayerHelper.recordCurrentIP(player, loginPlayer);
                return true;
            }
            return false;
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
            String playerName = player.getName();
            String hashedPassword = cc.baka9.catseedlogin.util.Crypt.encrypt(playerName, password);
            
            LoginPlayer loginPlayer = new LoginPlayer(playerName, hashedPassword);
            loginPlayer.setEmail("");
            loginPlayer.setIps(player.getAddress().getAddress().getHostAddress());
            loginPlayer.setLastAction(System.currentTimeMillis());
            
            CatSeedLogin.sql.add(loginPlayer);
            
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
            LoginPlayer loginPlayer = Cache.getIgnoreCase(player.getName());
            if (loginPlayer == null) {
                return false;
            }
            
            String hashedPassword = cc.baka9.catseedlogin.util.Crypt.encrypt(player.getName(), password);
            return hashedPassword.equals(loginPlayer.getPassword());
        } catch (Exception e) {
            return false;
        }
    }
}