package cn.i7mc.authmeGui;

import cn.i7mc.authmeGui.command.AuthGuiCommand;
import cn.i7mc.authmeGui.config.MenuConfigParser;
import cn.i7mc.authmeGui.config.impl.MenuConfigParserImpl;
import cn.i7mc.authmeGui.listener.AnvilInputListener;
import cn.i7mc.authmeGui.listener.AuthMeEventListener;
import cn.i7mc.authmeGui.listener.InventoryEventListener;
import cn.i7mc.authmeGui.listener.PlayerEventListener;
import cn.i7mc.authmeGui.util.AnvilInputUtil;
import cn.i7mc.authmeGui.manager.AuthMeManager;
import cn.i7mc.authmeGui.manager.ConfigManager;
import cn.i7mc.authmeGui.manager.GUIManager;
import cn.i7mc.authmeGui.manager.MessageManager;
import cn.i7mc.authmeGui.manager.impl.AuthMeManagerImpl;
import cn.i7mc.authmeGui.manager.impl.ConfigManagerImpl;
import cn.i7mc.authmeGui.manager.impl.GUIManagerImpl;
import cn.i7mc.authmeGui.manager.impl.MessageManagerImpl;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * AuthmeGui主插件类
 * 基于AuthMe的铁砧GUI登录系统
 */
public final class AuthmeGui extends JavaPlugin {

    // 管理器实例
    private ConfigManager configManager;
    private MessageManager messageManager;
    private AuthMeManager authMeManager;
    private MenuConfigParser menuConfigParser;
    private GUIManager guiManager;

    // 监听器实例
    private PlayerEventListener playerEventListener;
    private InventoryEventListener inventoryEventListener;
    private AuthMeEventListener authMeEventListener;
    private AnvilInputListener anvilInputListener;

    // 命令处理器实例
    private AuthGuiCommand authGuiCommand;

    @Override
    public void onEnable() {
        try {
            // 初始化PacketEvents
            initializePacketEvents();

            // 初始化插件
            initializePlugin();

            // 注册事件监听器
            registerListeners();

            // 注册命令
            registerCommands();

            // 插件启动完成

        } catch (Exception e) {
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        try {
            // 清理资源
            cleanup();

            // 终止PacketEvents
            PacketEvents.getAPI().terminate();

        } catch (Exception e) {
        }
    }

    /**
     * 初始化PacketEvents
     */
    private void initializePacketEvents() {

        // 构建PacketEvents实例
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));

        // 加载PacketEvents
        PacketEvents.getAPI().load();
    }

    /**
     * 初始化插件
     */
    private void initializePlugin() {

        // 保存默认配置文件
        saveDefaultConfig();

        // 初始化管理器（按依赖顺序）
        configManager = new ConfigManagerImpl(this);
        configManager.initialize();

        messageManager = new MessageManagerImpl(this, configManager);
        messageManager.initialize();

        authMeManager = new AuthMeManagerImpl(this, messageManager, configManager);
        authMeManager.initialize();

        menuConfigParser = new MenuConfigParserImpl(this);
        menuConfigParser.initialize();

        guiManager = new GUIManagerImpl(this, messageManager, authMeManager, menuConfigParser);
        guiManager.initialize();
    }

    /**
     * 注册事件监听器
     */
    private void registerListeners() {

        playerEventListener = new PlayerEventListener(this, messageManager, authMeManager, guiManager);
        getServer().getPluginManager().registerEvents(playerEventListener, this);

        inventoryEventListener = new InventoryEventListener(this, messageManager, guiManager);
        getServer().getPluginManager().registerEvents(inventoryEventListener, this);

        // 注册AuthMe事件监听器（核心功能）
        authMeEventListener = new AuthMeEventListener(this, messageManager, guiManager, authMeManager);
        getServer().getPluginManager().registerEvents(authMeEventListener, this);

        // 注册PacketEvents监听器（铁砧输入监听）
        anvilInputListener = new AnvilInputListener(messageManager);
        PacketEvents.getAPI().getEventManager().registerListener(anvilInputListener, PacketListenerPriority.NORMAL);
    }

    /**
     * 注册命令
     */
    private void registerCommands() {

        authGuiCommand = new AuthGuiCommand(this, messageManager, configManager, guiManager);
        getCommand("authgui").setExecutor(authGuiCommand);
        getCommand("authgui").setTabCompleter(authGuiCommand);
    }

    /**
     * 清理资源
     */
    private void cleanup() {

        // 关闭所有活跃的GUI
        if (guiManager != null) {
            guiManager.clearAllGUIs();
        }

        // 清空所有铁砧输入记录
        AnvilInputUtil.clearAll();
    }

    /**
     * 重载插件配置
     */
    @Override
    public void reloadConfig() {
        // 先调用Paper API的原生重载方法
        super.reloadConfig();

        // 然后重载自定义配置管理器
        if (configManager != null) {
            configManager.reloadConfigs();
        }

        // 最后重载GUI管理器
        if (guiManager != null) {
            guiManager.reload();
        }
    }

    // Getter方法，供其他类使用

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public AuthMeManager getAuthMeManager() {
        return authMeManager;
    }

    public MenuConfigParser getMenuConfigParser() {
        return menuConfigParser;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }
}
