package cn.i7mc.authmeGui.listener;

import cn.i7mc.authmeGui.AuthmeGui;
import cn.i7mc.authmeGui.gui.AnvilGUI;
import cn.i7mc.authmeGui.manager.MessageManager;
import cn.i7mc.authmeGui.util.AnvilInputUtil;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientNameItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;

/**
 * 铁砧输入监听器
 * 使用PacketEvents监听玩家在铁砧界面中的输入
 */
public class AnvilInputListener implements PacketListener {
    
    private final MessageManager messageManager;
    
    public AnvilInputListener(MessageManager messageManager) {
        this.messageManager = messageManager;
    }
    
    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        Player player = (Player) event.getPlayer();
        
        // 只处理物品重命名数据包
        if (event.getPacketType() == PacketType.Play.Client.NAME_ITEM) {
            handleNameItem(player, event);
        }
    }
    
    /**
     * 处理物品重命名数据包
     * @param player 玩家
     * @param event 数据包事件
     */
    private void handleNameItem(Player player, PacketReceiveEvent event) {
        try {
            // 检查玩家是否打开了我们的铁砧GUI
            Inventory inventory = player.getOpenInventory().getTopInventory();
            InventoryHolder inventoryHolder = inventory.getHolder();
            
            if (!(inventoryHolder instanceof AnvilGUI)) {
                return;
            }
            
            AnvilGUI anvilGUI = (AnvilGUI) inventoryHolder;
            
            // 确认是当前玩家的GUI
            if (!anvilGUI.getPlayer().equals(player)) {
                return;
            }
            
            // 获取输入的文本
            WrapperPlayClientNameItem packet = new WrapperPlayClientNameItem(event);
            String inputText = packet.getItemName();

            if (inputText != null) {
                // 移除空格并存储输入
                String cleanInput = inputText.replace(" ", "");

                // 过滤掉提示文本，只处理真实的用户输入
                if (!isPlaceholderText(cleanInput)) {
                    // 检查是否启用明文显示
                    boolean showPlaintext = isPlaintextPasswordEnabled();

                    // 特殊处理：如果是单个*字符且没有真实密码，强制清空（仅在掩码模式下）
                    if (!showPlaintext && cleanInput.equals("*") && !AnvilInputUtil.hasInput(player.getName())) {
                        AnvilInputUtil.forceRemovePlayerInput(player.getName());
                        updateGUIDisplay(anvilGUI, "");
                    } else {
                        // 处理密码输入
                        AnvilInputUtil.setPlayerInput(player.getName(), cleanInput);

                        // 获取处理后的密码用于显示
                        String displayPassword = AnvilInputUtil.getPlayerInput(player.getName());

                        // 更新GUI显示
                        updateGUIDisplay(anvilGUI, displayPassword);

                        // 延迟刷新GUI以确保显示正确
                        if (!showPlaintext) {
                            // 掩码模式需要延迟刷新
                            refreshGUIWithMask(anvilGUI, player, displayPassword);
                        }
                    }
                } else {
                    // 如果是提示文本，清空输入记录
                    AnvilInputUtil.forceRemovePlayerInput(player.getName());

                    // 更新GUI显示
                    updateGUIDisplay(anvilGUI, "");
                }
            }
        } catch (Exception e) {
            // 忽略输入处理错误
        }
    }
    
    /**
     * 更新GUI显示
     * @param anvilGUI 铁砧GUI
     * @param input 输入文本
     */
    private void updateGUIDisplay(AnvilGUI anvilGUI, String input) {
        try {
            // 只更新结果槽位的物品显示，不触发handleInput避免循环刷新
            updateResultSlot(anvilGUI, input);

            // 不再自动调用handleInput，避免GUI循环刷新和提前验证
            // anvilGUI.handleInput(input);
        } catch (Exception e) {
            // 忽略GUI更新错误
        }
    }

    /**
     * 检查是否为提示文本
     * @param input 输入文本
     * @return 是否为提示文本
     */
    private boolean isPlaceholderText(String input) {
        if (input == null || input.trim().isEmpty()) {
            return true;
        }

        // 检查常见的提示文本
        String cleanInput = input.toLowerCase().replace(".", "").replace("…", "");

        // 检查是否是提示文本
        boolean isPlaceholder = cleanInput.contains("请输入密码") ||
               cleanInput.contains("输入密码") ||
               cleanInput.contains("password") ||
               cleanInput.contains("请输入") ||
               cleanInput.equals("请输入密码");

        // 如果不是提示文本，但是是单个*号，也应该被处理为删除操作
        if (!isPlaceholder && cleanInput.equals("*")) {
            // 这是删除到最后一个字符的情况，应该完全清空
            return true;
        }

        return isPlaceholder;
    }

    /**
     * 刷新GUI以显示掩码密码
     * @param anvilGUI 铁砧GUI
     * @param player 玩家
     * @param maskedPassword 掩码密码
     */
    private void refreshGUIWithMask(AnvilGUI anvilGUI, Player player, String maskedPassword) {
        try {
            // 延迟更新输入槽位以显示掩码
            org.bukkit.Bukkit.getScheduler().runTaskLater(
                org.bukkit.Bukkit.getPluginManager().getPlugin("AuthmeGui"),
                () -> {
                    try {
                        if (player.isOnline() && anvilGUI.getInventory().equals(player.getOpenInventory().getTopInventory())) {
                            updateInputSlotWithMask(anvilGUI, maskedPassword);
                        }
                    } catch (Exception e) {
                        // 忽略刷新错误
                    }
                }, 1L);
        } catch (Exception e) {
            // 忽略刷新错误
        }
    }

    /**
     * 更新输入槽位显示掩码密码
     * @param anvilGUI 铁砧GUI
     * @param maskedPassword 掩码密码
     */
    private void updateInputSlotWithMask(AnvilGUI anvilGUI, String maskedPassword) {
        try {
            Inventory inventory = anvilGUI.getInventory();
            Player player = anvilGUI.getPlayer();

            // 创建显示掩码密码的物品
            org.bukkit.inventory.ItemStack inputItem = new org.bukkit.inventory.ItemStack(org.bukkit.Material.PAPER);
            org.bukkit.inventory.meta.ItemMeta meta = inputItem.getItemMeta();
            if (meta != null) {
                // 显示掩码密码或提示文本
                String displayText = (maskedPassword != null && !maskedPassword.trim().isEmpty()) ?
                    maskedPassword : messageManager.getMessage("gui.password-placeholder", null);
                meta.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&', displayText));
                inputItem.setItemMeta(meta);
            }

            // 设置到输入槽位（slot 0）
            inventory.setItem(0, inputItem);

            // 更新玩家的背包显示
            player.updateInventory();
        } catch (Exception e) {
            // 忽略输入槽更新错误
        }
    }

    /**
     * 更新结果槽位的物品
     * @param anvilGUI 铁砧GUI
     * @param input 输入文本
     */
    private void updateResultSlot(AnvilGUI anvilGUI, String input) {
        try {
            Inventory inventory = anvilGUI.getInventory();
            Player player = anvilGUI.getPlayer();

            // 创建结果物品，显示输入的内容
            org.bukkit.inventory.ItemStack resultItem = new org.bukkit.inventory.ItemStack(org.bukkit.Material.LIME_STAINED_GLASS_PANE);
            org.bukkit.inventory.meta.ItemMeta meta = resultItem.getItemMeta();
            if (meta != null) {
                // 如果有输入，显示确认文本；否则显示提示
                String displayText = (input != null && !input.trim().isEmpty()) ?
                    "确认密码" : messageManager.getMessage("gui.password-placeholder", null);
                meta.setDisplayName(org.bukkit.ChatColor.translateAlternateColorCodes('&', displayText));
                resultItem.setItemMeta(meta);
            }

            // 设置到结果槽位（slot 2）
            inventory.setItem(2, resultItem);

            // 更新玩家的背包显示
            player.updateInventory();
        } catch (Exception e) {
            // 忽略结果槽更新错误
        }
    }

    /**
     * 检查是否启用明文显示密码
     * @return 是否启用明文显示
     */
    private boolean isPlaintextPasswordEnabled() {
        try {
            Plugin plugin = Bukkit.getPluginManager().getPlugin("AuthmeGui");
            if (plugin instanceof AuthmeGui) {
                AuthmeGui authmeGui = (AuthmeGui) plugin;
                return authmeGui.getConfigManager().getMainConfig()
                    .getBoolean("security.show-password-plaintext", false);
            }
        } catch (Exception e) {
            // 如果获取配置失败，默认使用安全模式（隐藏密码）
        }
        return false;
    }
}
