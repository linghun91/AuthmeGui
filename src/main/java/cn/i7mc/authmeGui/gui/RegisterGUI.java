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
 * 注册GUI实现类
 * 处理玩家注册相关的GUI交互
 */
public class RegisterGUI extends AnvilGUI {
    
    private final AuthMeManager authMeManager;
    
    public RegisterGUI(AuthmeGui plugin, MessageManager messageManager, AuthMeManager authMeManager,
                      Player player, MenuConfig menuConfig) {
        super(plugin, messageManager, player, menuConfig, "register");
        this.authMeManager = authMeManager;
    }
    
    @Override
    public boolean handleClick(int slot, ItemStack clickedItem) {

        // 根据槽位处理不同的点击
        switch (slot) {
            case 0: // 输入槽位 - 不处理点击
                return true;

            case 1: // 重置填写按钮
                handleResetInput();
                return true;

            case 2: // 确认注册按钮
                handleConfirmRegister();
                return true;

            default:
                return true; // 取消所有其他点击
        }
    }
    
    @Override
    public void handleInput(String input) {
        // 只记录输入，不自动处理注册
        // 注册只在点击确认按钮时进行
        // 这样避免了GUI循环刷新的问题
    }
    
    /**
     * 处理取消注册
     */
    private void handleCancelRegister() {
        // 执行取消注册的点击动作
        MenuConfig.ItemConfig cancelItem = getCancelItem();
        if (cancelItem != null && cancelItem.getClickAction() != null) {
            executeActions(cancelItem.getClickAction());
        }
        
        // 发送取消消息
        if (messageManager != null) {
            messageManager.sendMessage(player, "gui.register-cancelled", null);
        }
        
        closeGUI();
    }
    
    /**
     * 处理重置输入
     */
    private void handleResetInput() {
        // 清空玩家的输入记录
        AnvilInputUtil.removePlayerInput(player.getName());

        // 重新初始化GUI内容，清空输入框
        initializeGUI();

        // 发送提示消息
        if (messageManager != null) {
            messageManager.sendMessage(player, "gui.input-reset", null);
        }
    }
    
    /**
     * 处理确认注册
     */
    private void handleConfirmRegister() {
        // 从AnvilInputUtil获取玩家输入的真实密码（不是掩码）
        String password = AnvilInputUtil.getRealPassword(player.getName());
        if (password != null && !password.trim().isEmpty()) {
            // 直接尝试注册，不再调用handleInput
            attemptRegister(password.trim());
        } else {
            // 发送提示消息要求输入密码
            if (messageManager != null) {
                messageManager.sendMessage(player, "gui.password-placeholder", null);
            }
        }
    }
    
    /**
     * 尝试注册
     * @param password 密码
     */
    private void attemptRegister(String password) {
        // 检查玩家是否已注册
        if (authMeManager.isPlayerRegistered(player)) {
            messageManager.sendMessage(player, "gui.already-registered", null);
            closeGUI();
            return;
        }

        // 验证密码格式
        if (!authMeManager.isValidPasswordWithMessage(player, password)) {
            return;
        }

        // 关闭GUI（在注册前关闭，避免事件冲突）
        closeGUI();

        // 注册玩家
        if (authMeManager.registerPlayer(player, password)) {
            // 立即将玩家添加到认证成功白名单，防止GUI重新打开
            GuiOpenHelper.addToAuthenticatedList(player);

            // 注册成功，自动登录玩家
            boolean loginSuccess = authMeManager.forceLogin(player);

            // 处理注册成功
            authMeManager.handleRegisterSuccess(player);

            if (loginSuccess) {
                plugin.getLogger().info("玩家 " + player.getName() + " 注册并自动登录成功");
            } else {
                plugin.getLogger().warning("玩家 " + player.getName() + " 注册成功但自动登录失败");
            }
        } else {
            // 注册失败
            authMeManager.handleFailure(player, "注册失败");
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
