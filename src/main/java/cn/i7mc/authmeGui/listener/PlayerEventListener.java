package cn.i7mc.authmeGui.listener;

import cn.i7mc.authmeGui.AuthmeGui;
import cn.i7mc.authmeGui.manager.AuthMeManager;
import cn.i7mc.authmeGui.manager.GUIManager;
import cn.i7mc.authmeGui.manager.MessageManager;
import cn.i7mc.authmeGui.util.GuiOpenHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * 玩家事件监听器
 * 处理玩家进入和离开服务器的事件
 */
public class PlayerEventListener implements Listener {
    
    private final AuthmeGui plugin;
    private final MessageManager messageManager;
    private final AuthMeManager authMeManager;
    private final GUIManager guiManager;
    
    public PlayerEventListener(AuthmeGui plugin, MessageManager messageManager, 
                              AuthMeManager authMeManager, GUIManager guiManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
        this.authMeManager = authMeManager;
        this.guiManager = guiManager;
    }
    
    /**
     * 处理玩家加入事件
     * 注意：GUI的打开现在由AuthMe事件触发，而不是PlayerJoinEvent
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 检查AuthMe是否可用
        if (!authMeManager.isAuthMeEnabled()) {
            messageManager.sendMessage(player, "authme.not-found", null);
            return;
        }

        // 这里不再自动打开GUI，GUI的打开由AuthMe事件触发
        // 只是记录玩家加入的调试信息
    }
    
    /**
     * 处理玩家离开事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // 清理玩家的GUI
        guiManager.closeGUI(player);

        // 清理玩家的白名单状态
        GuiOpenHelper.removeFromOpeningList(player);
        GuiOpenHelper.removeFromAuthenticatedList(player);
    }
    

}
