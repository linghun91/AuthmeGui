package cn.i7mc.authmeGui.manager;

import cn.i7mc.authmeGui.AuthmeGui;
import cn.i7mc.authmeGui.config.MenuConfig;
import cn.i7mc.authmeGui.config.MenuConfigParser;
import cn.i7mc.authmeGui.gui.AnvilGUI;
import cn.i7mc.authmeGui.gui.LoginGUI;
import cn.i7mc.authmeGui.gui.RegisterGUI;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * GUI管理器抽象类
 * 统一管理所有GUI的创建、显示和销毁
 */
public abstract class GUIManager {
    
    protected final AuthmeGui plugin;
    protected final MessageManager messageManager;
    protected final AuthMeManager authMeManager;
    protected final MenuConfigParser menuConfigParser;
    protected final Map<UUID, AnvilGUI> activeGUIs;
    
    public GUIManager(AuthmeGui plugin, MessageManager messageManager, 
                     AuthMeManager authMeManager, MenuConfigParser menuConfigParser) {
        this.plugin = plugin;
        this.messageManager = messageManager;
        this.authMeManager = authMeManager;
        this.menuConfigParser = menuConfigParser;
        this.activeGUIs = new HashMap<>();
    }
    
    /**
     * 初始化GUI管理器
     */
    public abstract void initialize();
    
    /**
     * 为玩家打开登录GUI
     * @param player 玩家
     */
    public void openLoginGUI(Player player) {
        // 检查玩家是否已经有活跃的GUI
        closeGUI(player);
        
        MenuConfig loginConfig = menuConfigParser.getLoginMenuConfig();
        if (loginConfig == null) {
            messageManager.sendMessage(player, "error.config-load-failed", null);
            return;
        }
        
        try {
            LoginGUI loginGUI = new LoginGUI(plugin, messageManager, authMeManager, player, loginConfig);
            activeGUIs.put(player.getUniqueId(), loginGUI);
            
            // 延迟打开GUI
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                loginGUI.openGUI();
            }, getOpenDelay());

        } catch (Exception e) {
            messageManager.sendMessage(player, "error.gui-creation-failed", null);
        }
    }
    
    /**
     * 为玩家打开注册GUI
     * @param player 玩家
     */
    public void openRegisterGUI(Player player) {
        // 检查玩家是否已经有活跃的GUI
        closeGUI(player);

        MenuConfig registerConfig = menuConfigParser.getRegisterMenuConfig();
        if (registerConfig == null) {
            messageManager.sendMessage(player, "error.config-load-failed", null);
            return;
        }

        try {
            RegisterGUI registerGUI = new RegisterGUI(plugin, messageManager, authMeManager, player, registerConfig);
            activeGUIs.put(player.getUniqueId(), registerGUI);

            // 延迟打开GUI
            long delay = getOpenDelay();

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                registerGUI.openGUI();
            }, delay);

        } catch (Exception e) {
            messageManager.sendMessage(player, "error.gui-creation-failed", null);
        }
    }
    
    /**
     * 为玩家打开适当的GUI（根据注册状态）
     * @param player 玩家
     */
    public void openAppropriateGUI(Player player) {
        // 检查玩家是否已经登录
        if (authMeManager.isPlayerLoggedIn(player)) {
            messageManager.sendMessage(player, "gui.already-logged-in", null);
            return;
        }
        
        // 根据注册状态打开相应的GUI
        if (authMeManager.isPlayerRegistered(player)) {
            openLoginGUI(player);
        } else {
            openRegisterGUI(player);
        }
    }
    
    /**
     * 关闭玩家的GUI
     * @param player 玩家
     */
    public void closeGUI(Player player) {
        AnvilGUI gui = activeGUIs.remove(player.getUniqueId());
        if (gui != null) {
            gui.closeGUI();
        }
    }
    
    /**
     * 获取玩家的活跃GUI
     * @param player 玩家
     * @return GUI对象，如果没有则返回null
     */
    public AnvilGUI getActiveGUI(Player player) {
        return activeGUIs.get(player.getUniqueId());
    }
    
    /**
     * 检查玩家是否有活跃的GUI
     * @param player 玩家
     * @return 是否有活跃的GUI
     */
    public boolean hasActiveGUI(Player player) {
        return activeGUIs.containsKey(player.getUniqueId());
    }
    
    /**
     * 清理所有活跃的GUI
     */
    public void clearAllGUIs() {
        for (AnvilGUI gui : activeGUIs.values()) {
            gui.closeGUI();
        }
        activeGUIs.clear();
    }
    
    /**
     * 获取GUI打开延迟
     * @return 延迟tick数
     */
    protected long getOpenDelay() {
        return plugin.getConfig().getLong("gui.open-delay", 20L);
    }
    
    /**
     * 检查是否启用GUI系统
     * @return 是否启用
     */
    public boolean isGUIEnabled() {
        return plugin.getConfig().getBoolean("gui.enabled", true);
    }
    
    /**
     * 检查是否自动打开GUI
     * @return 是否自动打开
     */
    public boolean isAutoOpenEnabled() {
        return plugin.getConfig().getBoolean("gui.auto-open", true);
    }
    
    /**
     * 获取活跃GUI数量
     * @return 活跃GUI数量
     */
    public int getActiveGUICount() {
        return activeGUIs.size();
    }

    /**
     * 重载GUI管理器
     */
    public void reload() {
        // 清理所有活跃的GUI
        clearAllGUIs();

        // 重载菜单配置
        menuConfigParser.reloadMenuConfigs();
    }
}
