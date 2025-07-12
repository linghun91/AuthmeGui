package cn.i7mc.authmeGui.util;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * GUI打开辅助工具类
 * 管理GUI打开过程中的特殊状态
 */
public class GuiOpenHelper {

    /**
     * 正在打开GUI的玩家白名单
     * 在此列表中的玩家不会被其他逻辑干扰GUI打开过程
     */
    private static final Set<UUID> openingGuiPlayers = new HashSet<>();

    /**
     * 已成功认证的玩家白名单
     * 在此列表中的玩家不会重新打开GUI
     */
    private static final Set<UUID> authenticatedPlayers = new HashSet<>();
    
    /**
     * 将玩家添加到GUI打开白名单
     * @param player 玩家
     */
    public static void addToOpeningList(Player player) {
        if (player != null) {
            openingGuiPlayers.add(player.getUniqueId());
        }
    }
    
    /**
     * 从GUI打开白名单中移除玩家
     * @param player 玩家
     */
    public static void removeFromOpeningList(Player player) {
        if (player != null) {
            openingGuiPlayers.remove(player.getUniqueId());
        }
    }
    
    /**
     * 检查玩家是否在GUI打开白名单中
     * @param player 玩家
     * @return 是否在白名单中
     */
    public static boolean isInOpeningList(Player player) {
        return player != null && openingGuiPlayers.contains(player.getUniqueId());
    }
    
    /**
     * 将玩家添加到认证成功白名单
     * @param player 玩家
     */
    public static void addToAuthenticatedList(Player player) {
        if (player != null) {
            authenticatedPlayers.add(player.getUniqueId());
        }
    }

    /**
     * 从认证成功白名单中移除玩家
     * @param player 玩家
     */
    public static void removeFromAuthenticatedList(Player player) {
        if (player != null) {
            authenticatedPlayers.remove(player.getUniqueId());
        }
    }

    /**
     * 检查玩家是否在认证成功白名单中
     * @param player 玩家
     * @return 是否在白名单中
     */
    public static boolean isInAuthenticatedList(Player player) {
        return player != null && authenticatedPlayers.contains(player.getUniqueId());
    }

    /**
     * 清理所有白名单数据
     */
    public static void clearAll() {
        openingGuiPlayers.clear();
        authenticatedPlayers.clear();
    }

    /**
     * 获取当前白名单大小（用于调试）
     * @return 白名单大小
     */
    public static int getOpeningListSize() {
        return openingGuiPlayers.size();
    }

    /**
     * 获取当前认证白名单大小（用于调试）
     * @return 认证白名单大小
     */
    public static int getAuthenticatedListSize() {
        return authenticatedPlayers.size();
    }
}
