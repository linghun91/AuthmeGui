package cn.i7mc.authmeGui.listener;

import cn.i7mc.authmeGui.AuthmeGui;
import cn.i7mc.authmeGui.gui.AnvilGUI;
import cn.i7mc.authmeGui.manager.GUIManager;
import cn.i7mc.authmeGui.manager.MessageManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * 背包事件监听器
 * 处理GUI相关的背包交互事件
 */
public class InventoryEventListener implements Listener {
    
    private final AuthmeGui plugin;
    private final MessageManager messageManager;
    private final GUIManager guiManager;
    
    public InventoryEventListener(AuthmeGui plugin, MessageManager messageManager, GUIManager guiManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
        this.guiManager = guiManager;
    }

    /**
     * 处理背包打开事件
     * 强制允许我们的GUI打开，绕过AuthMe的限制
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();

        // 检查是否是我们的GUI
        if (!(event.getInventory().getHolder() instanceof AnvilGUI)) {
            return;
        }

        AnvilGUI anvilGUI = (AnvilGUI) event.getInventory().getHolder();

        // 确认是当前玩家的GUI
        if (!anvilGUI.getPlayer().equals(player)) {
            return;
        }

        // 检查事件是否被取消
        if (event.isCancelled()) {
            // 强制取消事件的取消状态，允许我们的GUI打开
            event.setCancelled(false);
        }
    }

    /**
     * 处理背包点击事件
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        
        // 检查是否是我们的GUI
        if (!(event.getInventory().getHolder() instanceof AnvilGUI)) {
            return;
        }
        
        AnvilGUI anvilGUI = (AnvilGUI) event.getInventory().getHolder();
        
        // 确认是当前玩家的GUI
        if (!anvilGUI.getPlayer().equals(player)) {
            event.setCancelled(true);
            return;
        }
        
        // 处理点击
        boolean cancelled = anvilGUI.handleClick(event.getSlot(), event.getCurrentItem());
        event.setCancelled(cancelled);
    }
    
    /**
     * 处理背包关闭事件
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();

        // 检查是否是我们的GUI
        if (!(event.getInventory().getHolder() instanceof AnvilGUI)) {
            return;
        }

        AnvilGUI anvilGUI = (AnvilGUI) event.getInventory().getHolder();

        // 确认是当前玩家的GUI
        if (!anvilGUI.getPlayer().equals(player)) {
            return;
        }

        // 处理GUI关闭事件（包括可能的重新打开）
        anvilGUI.handleClose();

        // 从活跃GUI列表中移除
        guiManager.closeGUI(player);
    }
    
    /**
     * 处理铁砧准备事件（用于捕获输入）
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        if (event.getInventory().getType() != InventoryType.ANVIL) {
            return;
        }
        
        AnvilInventory anvilInventory = (AnvilInventory) event.getInventory();
        
        // 检查是否是我们的GUI
        if (!(anvilInventory.getHolder() instanceof AnvilGUI)) {
            return;
        }
        
        AnvilGUI anvilGUI = (AnvilGUI) anvilInventory.getHolder();
        Player player = anvilGUI.getPlayer();
        
        // 获取输入的文本
        String inputText = anvilInventory.getRenameText();
        if (inputText != null && !inputText.isEmpty()) {
            // 处理输入
            anvilGUI.handleInput(inputText);
        }
        
        // 设置结果物品
        ItemStack result = event.getResult();
        if (result != null) {
            ItemMeta meta = result.getItemMeta();
            if (meta != null && inputText != null) {
                meta.setDisplayName(inputText);
                result.setItemMeta(meta);
                event.setResult(result);
            }
        }
    }
}
