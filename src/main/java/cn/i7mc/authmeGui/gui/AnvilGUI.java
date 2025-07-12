package cn.i7mc.authmeGui.gui;

import cn.i7mc.authmeGui.AuthmeGui;
import cn.i7mc.authmeGui.config.MenuConfig;
import cn.i7mc.authmeGui.manager.MessageManager;
import cn.i7mc.authmeGui.util.GuiOpenHelper;
import fr.xephi.authme.api.v3.AuthMeApi;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 铁砧GUI抽象类
 * 统一处理铁砧界面的创建、显示和交互
 */
public abstract class AnvilGUI implements InventoryHolder {
    
    protected final AuthmeGui plugin;
    protected final MessageManager messageManager;
    protected final Player player;
    protected final MenuConfig menuConfig;
    protected final Inventory inventory;
    protected final String guiType;
    
    public AnvilGUI(AuthmeGui plugin, MessageManager messageManager, Player player, 
                   MenuConfig menuConfig, String guiType) {
        this.plugin = plugin;
        this.messageManager = messageManager;
        this.player = player;
        this.menuConfig = menuConfig;
        this.guiType = guiType;
        
        // 创建铁砧背包
        String title = ChatColor.translateAlternateColorCodes('&', menuConfig.getTitle());
        this.inventory = Bukkit.createInventory(this, InventoryType.ANVIL, title);
        
        // 初始化GUI
        initializeGUI();
    }
    
    /**
     * 初始化GUI内容
     */
    protected void initializeGUI() {
        // 清空背包
        inventory.clear();
        
        // 设置物品
        setupItems();
    }
    
    /**
     * 设置GUI中的物品
     */
    protected void setupItems() {
        Map<String, MenuConfig.ItemConfig> items = menuConfig.getItems();

        for (MenuConfig.ItemConfig itemConfig : items.values()) {
            ItemStack item = createItemFromConfig(itemConfig);

            // 设置物品到指定槽位
            Object slot = itemConfig.getSlot();
            if (slot instanceof Integer) {
                int slotIndex = (Integer) slot;
                if (slotIndex >= 0 && slotIndex < inventory.getSize()) {
                    inventory.setItem(slotIndex, item);
                }
            }
        }

        // 为铁砧GUI设置输入提示物品（slot 0）
        if (inventory.getType() == InventoryType.ANVIL) {
            setupInputPlaceholder();
        }
    }

    /**
     * 设置输入提示物品
     */
    protected void setupInputPlaceholder() {
        // 获取输入提示文本
        String placeholderText = messageManager.getMessage("gui.password-placeholder", null);
        if (placeholderText == null || placeholderText.isEmpty()) {
            placeholderText = "请输入密码";
        }

        // 创建输入提示物品
        ItemStack placeholderItem = new ItemStack(Material.PAPER);
        ItemMeta meta = placeholderItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', placeholderText));
            placeholderItem.setItemMeta(meta);
        }

        // 设置到第一个槽位
        inventory.setItem(0, placeholderItem);
    }
    
    /**
     * 根据配置创建物品
     * @param itemConfig 物品配置
     * @return 创建的物品
     */
    protected ItemStack createItemFromConfig(MenuConfig.ItemConfig itemConfig) {
        Material material = itemConfig.parseMaterial();
        ItemStack item = new ItemStack(material, itemConfig.getAmount());
        
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            // 设置名称
            String name = itemConfig.getName();
            if (name != null && !name.isEmpty()) {
                meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
            }
            
            // 设置Lore
            List<String> lore = itemConfig.getLore();
            if (lore != null && !lore.isEmpty()) {
                List<String> coloredLore = new ArrayList<>();
                for (String line : lore) {
                    coloredLore.add(ChatColor.translateAlternateColorCodes('&', line));
                }
                meta.setLore(coloredLore);
            }
            
            // 设置自定义模型数据
            Integer customModelData = itemConfig.getCustomModelData();
            if (customModelData != null) {
                meta.setCustomModelData(customModelData);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    /**
     * 打开GUI给玩家
     */
    public void openGUI() {
        try {
            // 检查玩家是否在线
            if (!player.isOnline()) {
                return;
            }

            // 检查inventory是否为null
            if (inventory == null) {
                return;
            }

            // 使用强制打开方法，绕过可能的事件取消
            forceOpenGUI();

        } catch (Exception e) {
            if (messageManager != null) {
                messageManager.sendMessage(player, "error.gui-creation-failed", null);
            }
        }
    }

    /**
     * 强制打开GUI，使用多种方法尝试绕过限制
     */
    private void forceOpenGUI() {
        // 方法1：直接尝试打开
        boolean success = attemptDirectOpen();

        if (!success) {
            // 方法2：延迟重试
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    boolean retrySuccess = attemptDirectOpen();
                    if (!retrySuccess) {
                        // 方法3：使用更长的延迟再次尝试
                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                            if (player.isOnline()) {
                                attemptDirectOpen();
                            }
                        }, 40L); // 2秒后再试
                    }
                }
            }, 10L); // 0.5秒后重试
        }
    }

    /**
     * 尝试直接打开GUI
     * @return 是否成功
     */
    private boolean attemptDirectOpen() {
        try {
            // 将玩家添加到白名单，防止其他逻辑干扰
            GuiOpenHelper.addToOpeningList(player);

            // 尝试使用AuthMe API打开GUI
            boolean authMeOpenSuccess = false;
            try {
                AuthMeApi authMeApi = AuthMeApi.getInstance();
                if (authMeApi != null) {
                    // 尝试查找AuthMe的openInventory方法
                    Method[] methods = AuthMeApi.class.getDeclaredMethods();
                    Method openInventoryMethod = null;

                    for (Method method : methods) {
                        if ("openInventory".equals(method.getName()) &&
                            method.getParameterCount() == 2 &&
                            method.getParameterTypes()[0] == Player.class &&
                            method.getParameterTypes()[1] == Inventory.class) {
                            openInventoryMethod = method;
                            break;
                        }
                    }

                    if (openInventoryMethod != null) {
                        openInventoryMethod.setAccessible(true);
                        openInventoryMethod.invoke(authMeApi, player, inventory);
                        authMeOpenSuccess = true;
                    }
                }
            } catch (Exception authMeException) {
                // AuthMe API调用失败，继续使用普通方法
            }

            // 如果AuthMe API失败或不可用，使用普通方法
            if (!authMeOpenSuccess) {
                player.openInventory(inventory);
            }

            // 检查是否真的打开了
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                try {
                    if (player.isOnline()) {
                        if (player.getOpenInventory().getTopInventory().equals(inventory)) {
                            // 成功打开
                            executeActions(menuConfig.getOpenActions());
                        } else {
                            // 打开失败
                        }
                    }
                } finally {
                    // 无论成功还是失败，都从白名单中移除玩家
                    GuiOpenHelper.removeFromOpeningList(player);
                }
            }, 1L); // 1 tick后验证

            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 关闭GUI
     */
    public void closeGUI() {
        try {
            // 从白名单中移除玩家
            GuiOpenHelper.removeFromOpeningList(player);

            // 确保在主线程中关闭GUI
            if (plugin.getServer().isPrimaryThread()) {
                player.closeInventory();

                // 执行关闭动作
                executeActions(menuConfig.getCloseActions());
            } else {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (player.isOnline()) {
                        player.closeInventory();

                        // 执行关闭动作
                        executeActions(menuConfig.getCloseActions());
                    }
                });
            }


        } catch (Exception e) {
        }
    }

    /**
     * 处理GUI关闭事件
     * 如果玩家还没有登录且不在白名单中，重新打开GUI
     */
    public void handleClose() {
        try {
            // 执行关闭动作
            executeActions(menuConfig.getCloseActions());

            // 检查是否需要重新打开GUI
            if (shouldReopenAfterClose()) {
                // 延迟重新打开GUI，避免无限循环
                // 使用更长的延迟来给AuthMe事件处理时间
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        // 再次检查玩家状态，确保真的需要重新打开
                        try {
                            AuthMeApi authMeApi = AuthMeApi.getInstance();
                            if (authMeApi != null && !authMeApi.isAuthenticated(player)) {
                                // 只有在玩家确实需要认证时才重新打开
                                openGUI();
                            }
                        } catch (Exception e) {
                            // 如果检查失败，不重新打开
                        }
                    }
                }, 40L); // 2秒延迟，给AuthMe事件处理更多时间
            }
        } catch (Exception e) {
        }
    }

    /**
     * 检查是否应该在关闭后重新打开GUI
     * @return 是否应该重新打开
     */
    protected boolean shouldReopenAfterClose() {
        // 如果玩家在白名单中（正在进行某些操作），不重新打开
        if (GuiOpenHelper.isInOpeningList(player)) {
            return false;
        }

        // 如果玩家已经认证成功，不重新打开
        if (GuiOpenHelper.isInAuthenticatedList(player)) {
            return false;
        }

        // 检查AuthMe状态
        try {
            AuthMeApi authMeApi = AuthMeApi.getInstance();
            if (authMeApi != null) {
                // 如果玩家已经登录，不需要重新打开
                if (authMeApi.isAuthenticated(player)) {
                    return false;
                }

                // 对于注册GUI，如果玩家已经注册，也不需要重新打开
                if ("register".equals(guiType) && authMeApi.isRegistered(player.getName())) {
                    return false;
                }

                // 如果玩家还没有登录且需要认证，才重新打开GUI
                return true;
            }
        } catch (Exception e) {
            // 如果AuthMe API调用失败，为了安全起见不重新打开
        }

        return false;
    }
    
    /**
     * 执行动作列表
     * @param actions 动作列表
     */
    protected void executeActions(List<String> actions) {
        if (actions == null || actions.isEmpty()) {
            return;
        }
        
        for (String action : actions) {
            executeAction(action);
        }
    }
    
    /**
     * 执行单个动作
     * @param action 动作字符串
     */
    protected void executeAction(String action) {
        if (action == null || action.isEmpty()) {
            return;
        }
        
        try {
            if (action.startsWith("[kick]")) {
                // 踢出玩家
                String reason = action.substring(6);
                if (reason.startsWith("|")) {
                    reason = reason.substring(1);
                }
                player.kickPlayer(ChatColor.translateAlternateColorCodes('&', reason));
            } else if (action.startsWith("[message]")) {
                // 发送消息
                String message = action.substring(9);
                if (message.startsWith("|")) {
                    message = message.substring(1);
                }
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            } else if (action.startsWith("[command]")) {
                // 执行命令
                String command = action.substring(9);
                if (command.startsWith("|")) {
                    command = command.substring(1);
                }
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), 
                    command.replace("{player}", player.getName()));
            }
        } catch (Exception e) {
        }
    }
    
    /**
     * 处理点击事件
     * @param slot 点击的槽位
     * @param clickedItem 点击的物品
     * @return 是否取消事件
     */
    public abstract boolean handleClick(int slot, ItemStack clickedItem);
    
    /**
     * 处理输入事件
     * @param input 输入的文本
     */
    public abstract void handleInput(String input);
    
    /**
     * 获取GUI类型
     * @return GUI类型
     */
    public String getGuiType() {
        return guiType;
    }
    
    /**
     * 获取玩家
     * @return 玩家对象
     */
    public Player getPlayer() {
        return player;
    }
    
    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
