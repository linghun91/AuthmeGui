package cn.i7mc.authmeGui.command;

import cn.i7mc.authmeGui.AuthmeGui;
import cn.i7mc.authmeGui.manager.ConfigManager;
import cn.i7mc.authmeGui.manager.GUIManager;
import cn.i7mc.authmeGui.manager.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * AuthGui主命令处理器
 * 处理插件的所有命令
 */
public class AuthGuiCommand implements CommandExecutor, TabCompleter {
    
    private final AuthmeGui plugin;
    private final MessageManager messageManager;
    private final ConfigManager configManager;
    private final GUIManager guiManager;
    
    public AuthGuiCommand(AuthmeGui plugin, MessageManager messageManager, 
                         ConfigManager configManager, GUIManager guiManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
        this.configManager = configManager;
        this.guiManager = guiManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 检查权限
        if (!sender.hasPermission("authgui.admin")) {
            messageManager.sendMessage(sender, "system.no-permission", null);
            return true;
        }
        
        // 如果没有参数，显示帮助
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload":
                handleReload(sender);
                break;
                
            case "open":
                handleOpen(sender, args);
                break;
                
            case "close":
                handleClose(sender, args);
                break;
                
            case "info":
                handleInfo(sender);
                break;
                
            default:
                sendHelp(sender);
                break;
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (!sender.hasPermission("authgui.admin")) {
            return completions;
        }
        
        if (args.length == 1) {
            // 第一个参数的补全
            List<String> subCommands = Arrays.asList("reload", "open", "close", "info");
            String input = args[0].toLowerCase();
            
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(input)) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            // 第二个参数的补全
            String subCommand = args[0].toLowerCase();
            
            if ("open".equals(subCommand) || "close".equals(subCommand)) {
                // 补全在线玩家名称
                String input = args[1].toLowerCase();
                for (Player player : plugin.getServer().getOnlinePlayers()) {
                    if (player.getName().toLowerCase().startsWith(input)) {
                        completions.add(player.getName());
                    }
                }
            }
        } else if (args.length == 3) {
            // 第三个参数的补全
            String subCommand = args[0].toLowerCase();
            
            if ("open".equals(subCommand)) {
                // 补全GUI类型
                List<String> guiTypes = Arrays.asList("login", "register", "auto");
                String input = args[2].toLowerCase();
                
                for (String guiType : guiTypes) {
                    if (guiType.startsWith(input)) {
                        completions.add(guiType);
                    }
                }
            }
        }
        
        return completions;
    }
    
    /**
     * 处理重载命令
     */
    private void handleReload(CommandSender sender) {
        try {
            // 重载配置文件
            configManager.reloadConfigs();
            
            // 重载GUI管理器
            guiManager.reload();
            
            messageManager.sendMessage(sender, "command.reload-success", null);
            
        } catch (Exception e) {
            messageManager.sendMessage(sender, "command.reload-failed", null);
        }
    }
    
    /**
     * 处理打开GUI命令
     */
    private void handleOpen(CommandSender sender, String[] args) {
        if (args.length < 2) {
            messageManager.sendMessage(sender, "command.usage", null);
            return;
        }
        
        String playerName = args[1];
        Player target = plugin.getServer().getPlayer(playerName);
        
        if (target == null || !target.isOnline()) {
            messageManager.sendMessage(sender, "error.player-not-found", 
                messageManager.createPlaceholders("player", playerName));
            return;
        }
        
        String guiType = args.length > 2 ? args[2].toLowerCase() : "auto";
        
        switch (guiType) {
            case "login":
                guiManager.openLoginGUI(target);
                break;
            case "register":
                guiManager.openRegisterGUI(target);
                break;
            case "auto":
            default:
                guiManager.openAppropriateGUI(target);
                break;
        }
        
        messageManager.sendMessage(sender, "command.gui-opened", 
            messageManager.createPlaceholders("player", target.getName(), "type", guiType));
    }
    
    /**
     * 处理关闭GUI命令
     */
    private void handleClose(CommandSender sender, String[] args) {
        if (args.length < 2) {
            messageManager.sendMessage(sender, "command.usage", null);
            return;
        }
        
        String playerName = args[1];
        Player target = plugin.getServer().getPlayer(playerName);
        
        if (target == null || !target.isOnline()) {
            messageManager.sendMessage(sender, "error.player-not-found", 
                messageManager.createPlaceholders("player", playerName));
            return;
        }
        
        guiManager.closeGUI(target);
        messageManager.sendMessage(sender, "command.gui-closed", 
            messageManager.createPlaceholders("player", target.getName()));
    }
    
    /**
     * 处理信息命令
     */
    private void handleInfo(CommandSender sender) {
        sender.sendMessage("§6=== AuthmeGui 插件信息 ===");
        sender.sendMessage("§e版本: §f" + plugin.getDescription().getVersion());
        sender.sendMessage("§e作者: §f" + plugin.getDescription().getAuthors());
        sender.sendMessage("§e状态: §a运行中");
        sender.sendMessage("§eGUI系统: §f" + (guiManager.isGUIEnabled() ? "§a启用" : "§c禁用"));
        sender.sendMessage("§e自动打开: §f" + (guiManager.isAutoOpenEnabled() ? "§a启用" : "§c禁用"));
        sender.sendMessage("§e活跃GUI数量: §f" + guiManager.getActiveGUICount());
    }
    
    /**
     * 发送帮助信息
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== AuthmeGui 命令帮助 ===");
        sender.sendMessage("§e/authgui reload §7- 重载配置文件");
        sender.sendMessage("§e/authgui open <玩家> [类型] §7- 为玩家打开GUI");
        sender.sendMessage("§e/authgui close <玩家> §7- 关闭玩家的GUI");
        sender.sendMessage("§e/authgui info §7- 显示插件信息");
        sender.sendMessage("§7GUI类型: login, register, auto");
    }
}
