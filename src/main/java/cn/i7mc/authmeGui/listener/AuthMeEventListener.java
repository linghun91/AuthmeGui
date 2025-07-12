package cn.i7mc.authmeGui.listener;

import cn.i7mc.authmeGui.AuthmeGui;
import cn.i7mc.authmeGui.manager.AuthMeManager;
import cn.i7mc.authmeGui.manager.GUIManager;
import cn.i7mc.authmeGui.manager.MessageManager;
import cn.i7mc.authmeGui.util.AnvilInputUtil;
import cn.i7mc.authmeGui.util.GuiOpenHelper;
import fr.xephi.authme.events.LoginEvent;
import fr.xephi.authme.events.LogoutEvent;
import fr.xephi.authme.events.RegisterEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * AuthMe事件监听器
 * 监听AuthMe的登录/注册相关事件，在适当时机弹出GUI
 *
 * 重要说明：
 * - AuthMeAsyncPreLoginEvent 只在玩家主动输入 /login 命令时触发
 * - AuthMeAsyncPreRegisterEvent 只在玩家主动输入 /register 命令时触发
 * - 我们需要在玩家加入服务器时主动检查AuthMe状态来决定是否弹出GUI
 */
public class AuthMeEventListener implements Listener {

    private final AuthmeGui plugin;
    private final MessageManager messageManager;
    private final GUIManager guiManager;
    private final AuthMeManager authMeManager;

    public AuthMeEventListener(AuthmeGui plugin, MessageManager messageManager,
                              GUIManager guiManager, AuthMeManager authMeManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
        this.guiManager = guiManager;
        this.authMeManager = authMeManager;
    }
    
    /**
     * 处理玩家加入事件
     * 当玩家加入服务器时，检查AuthMe状态并打开相应的GUI
     *
     * 这是核心逻辑：当玩家加入服务器后，AuthMe会自动检查玩家状态
     * 如果玩家需要登录/注册，AuthMe会限制玩家的行动
     * 此时我们检测到这种状态，就弹出相应的GUI
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 检查是否启用GUI系统
        if (!guiManager.isGUIEnabled()) {
            return;
        }

        // 检查是否启用自动打开GUI
        if (!guiManager.isAutoOpenEnabled()) {
            return;
        }

        // 检查AuthMe是否可用
        if (!authMeManager.isAuthMeEnabled()) {
            messageManager.sendMessage(player, "authme.not-found", null);
            return;
        }

        // 延迟检查玩家状态并打开GUI
        // 使用较长的延迟确保AuthMe完全处理了玩家加入
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            checkAndOpenGUI(player);
        }, getOpenDelay());

        // 额外的定期检查，确保不会遗漏
        // 增加检查频率和持续时间，确保GUI能够正确弹出
        plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
            private int attempts = 0;
            private final int maxAttempts = 10; // 最多尝试10次

            @Override
            public void run() {
                attempts++;

                if (!player.isOnline() || attempts > maxAttempts) {
                    // 玩家离线或达到最大尝试次数，停止检查
                    return;
                }

                // 如果玩家已登录或已有活跃GUI，停止检查
                if (authMeManager.isPlayerLoggedIn(player) || guiManager.hasActiveGUI(player)) {
                    return;
                }

                // 尝试打开GUI
                checkAndOpenGUI(player);
            }
        }, getOpenDelay() + 20L, 20L); // 2秒后开始，每1秒检查一次
    }
    
    /**
     * 处理登录成功事件
     * 当玩家成功登录时关闭GUI
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onLogin(LoginEvent event) {
        Player player = event.getPlayer();

        // 将玩家添加到认证成功白名单，防止GUI重新打开
        GuiOpenHelper.addToAuthenticatedList(player);

        // 清理玩家的输入记录
        AnvilInputUtil.removePlayerInput(player.getName());

        // 关闭玩家的GUI
        guiManager.closeGUI(player);

        // 发送成功消息
        messageManager.sendMessage(player, "gui.login-success",
            messageManager.createPlaceholders("player", player.getName()));
    }
    
    /**
     * 处理注册成功事件
     * 当玩家成功注册时关闭GUI
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onRegister(RegisterEvent event) {
        Player player = event.getPlayer();

        // 将玩家添加到认证成功白名单，防止GUI重新打开
        GuiOpenHelper.addToAuthenticatedList(player);

        // 清理玩家的输入记录
        AnvilInputUtil.removePlayerInput(player.getName());

        // 关闭玩家的GUI
        guiManager.closeGUI(player);

        // 发送成功消息
        messageManager.sendMessage(player, "gui.register-success",
            messageManager.createPlaceholders("player", player.getName()));
    }
    
    /**
     * 处理登出事件
     * 当玩家登出时可能需要重新打开GUI
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onLogout(LogoutEvent event) {
        Player player = event.getPlayer();

        // 关闭玩家的GUI
        guiManager.closeGUI(player);
        
        // 如果启用自动打开，延迟打开适当的GUI
        if (guiManager.isGUIEnabled() && guiManager.isAutoOpenEnabled()) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    guiManager.openAppropriateGUI(player);
                }
            }, getOpenDelay());
        }
    }
    
    /**
     * 检查玩家状态并打开相应的GUI
     * @param player 玩家
     */
    private void checkAndOpenGUI(Player player) {
        if (!player.isOnline()) {
            return;
        }

        // 检查AuthMe是否可用
        if (!authMeManager.isAuthMeEnabled()) {
            return;
        }

        // 检查玩家是否已经登录
        boolean isLoggedIn = authMeManager.isPlayerLoggedIn(player);

        if (isLoggedIn) {
            // 玩家已登录，不需要打开GUI
            return;
        }

        // 检查是否已经有活跃的GUI
        boolean hasActiveGUI = guiManager.hasActiveGUI(player);

        if (hasActiveGUI) {
            return;
        }

        // 此时玩家未登录，需要身份验证
        // 根据注册状态打开相应的GUI
        boolean isRegistered = authMeManager.isPlayerRegistered(player);

        if (isRegistered) {
            // 玩家已注册但未登录，需要登录
            guiManager.openLoginGUI(player);
            messageManager.sendMessage(player, "gui.login-required",
                messageManager.createPlaceholders("player", player.getName()));
        } else {
            // 玩家未注册，需要注册
            guiManager.openRegisterGUI(player);
            messageManager.sendMessage(player, "gui.register-required",
                messageManager.createPlaceholders("player", player.getName()));
        }
    }

    /**
     * 获取GUI打开延迟
     * @return 延迟tick数
     */
    private long getOpenDelay() {
        return plugin.getConfig().getLong("gui.open-delay", 20L);
    }
}
