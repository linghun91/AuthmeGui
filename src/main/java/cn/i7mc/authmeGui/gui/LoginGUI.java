package cn.i7mc.authmeGui.gui;

import cn.i7mc.authmeGui.AuthmeGui;
import cn.i7mc.authmeGui.config.MenuConfig;
import cn.i7mc.authmeGui.manager.AuthMeManager;
import cn.i7mc.authmeGui.manager.MessageManager;
import cn.i7mc.authmeGui.util.AnvilInputUtil;
import cn.i7mc.authmeGui.util.GuiOpenHelper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * 登录GUI实现类
 * 处理玩家登录相关的GUI交互
 */
public class LoginGUI extends AnvilGUI {
    
    private final AuthMeManager authMeManager;
    
    public LoginGUI(AuthmeGui plugin, MessageManager messageManager, AuthMeManager authMeManager,
                   Player player, MenuConfig menuConfig) {
        super(plugin, messageManager, player, menuConfig, "login");
        this.authMeManager = authMeManager;
    }
    
    @Override
    public boolean handleClick(int slot, ItemStack clickedItem) {
        
        // 根据槽位处理不同的点击
        switch (slot) {
            case 0: // 取消登录按钮
                handleCancelLogin();
                return true;
                
            case 1: // 重置填写按钮
                handleResetInput();
                return true;
                
            case 2: // 确认登录按钮
                handleConfirmLogin();
                return true;
                
            default:
                return true; // 取消所有其他点击
        }
    }
    
    @Override
    public void handleInput(String input) {
        // 只记录输入，不自动处理登录
        // 登录只在点击确认按钮时进行
        // 这样避免了提前验证和GUI循环刷新的问题
    }
    
    /**
     * 处理取消登录
     */
    private void handleCancelLogin() {
        // 执行取消登录的点击动作
        MenuConfig.ItemConfig cancelItem = getCancelItem();
        if (cancelItem != null && cancelItem.getClickAction() != null) {
            executeActions(cancelItem.getClickAction());
        }
        
        // 发送取消消息
        if (messageManager != null) {
            messageManager.sendMessage(player, "gui.login-cancelled", null);
        }
        
        closeGUI();
    }
    
    /**
     * 处理重置输入
     */
    private void handleResetInput() {
        // 清空玩家的输入记录
        AnvilInputUtil.removePlayerInput(player.getName());

        // 清空输入框（重新打开GUI）
        closeGUI();

        // 延迟重新打开
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            openGUI();
        }, 1L);

        // 发送提示消息
        if (messageManager != null) {
            messageManager.sendMessage(player, "gui.password-placeholder", null);
        }
    }
    
    /**
     * 处理确认登录
     */
    private void handleConfirmLogin() {
        // 从AnvilInputUtil获取玩家输入的真实密码（不是掩码）
        String password = AnvilInputUtil.getRealPassword(player.getName());
        if (password != null && !password.trim().isEmpty()) {
            // 直接尝试登录，不再调用handleInput
            attemptLogin(password.trim());
        } else {
            messageManager.sendMessage(player, "gui.password-placeholder", null);
        }
    }
    
    /**
     * 尝试登录
     * @param password 密码
     */
    private void attemptLogin(String password) {
        // 检查玩家是否已注册
        if (!authMeManager.isPlayerRegistered(player)) {
            messageManager.sendMessage(player, "gui.login-failed", null);
            return;
        }

        // 验证密码格式
        if (!authMeManager.isValidPasswordWithMessage(player, password)) {
            return;
        }
        
        // 检查密码是否正确
        if (!authMeManager.checkPassword(player, password)) {
            messageManager.sendMessage(player, "gui.login-failed", null);
            return;
        }
        
        // 关闭GUI（在登录前关闭，避免事件冲突）
        closeGUI();

        // 强制登录
        if (authMeManager.forceLogin(player)) {
            // 立即将玩家添加到认证成功白名单，防止GUI重新打开
            GuiOpenHelper.addToAuthenticatedList(player);

            // 登录成功
            authMeManager.handleLoginSuccess(player);
        } else {
            // 登录失败
            authMeManager.handleFailure(player, "登录失败");
        }
    }
    
    /**
     * 获取取消按钮配置
     * @return 取消按钮配置
     */
    private MenuConfig.ItemConfig getCancelItem() {
        for (MenuConfig.ItemConfig item : menuConfig.getItems().values()) {
            Object slot = item.getSlot();
            if (slot instanceof Integer && (Integer) slot == 0) {
                return item;
            }
        }
        return null;
    }
}
