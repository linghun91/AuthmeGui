package cn.i7mc.authmeGui.util;

import cn.i7mc.authmeGui.AuthmeGui;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

/**
 * 铁砧输入工具类
 * 用于管理玩家在铁砧GUI中输入的文本
 */
public final class AnvilInputUtil {

    /**
     * 存储玩家输入文本的HashMap
     * Key: 玩家名称, Value: 输入的文本
     */
    private static final Map<String, String> anvilInputMap = new HashMap<>();

    /**
     * 存储玩家真实密码的HashMap（用于安全显示）
     * Key: 玩家名称, Value: 真实密码
     */
    private static final Map<String, String> realPasswordMap = new HashMap<>();
    
    /**
     * 获取铁砧输入HashMap
     * @return 铁砧输入HashMap
     */
    public static Map<String, String> getAnvilInputMap() {
        return anvilInputMap;
    }
    
    /**
     * 设置玩家的输入文本（安全密码显示模式）
     * @param playerName 玩家名称
     * @param input 输入文本
     */
    public static void setPlayerInput(String playerName, String input) {
        if (input == null || input.trim().isEmpty()) {
            anvilInputMap.remove(playerName);
            realPasswordMap.remove(playerName);
        } else {
            String cleanInput = input.replace(" ", "");

            // 检查是否启用明文显示
            boolean showPlaintext = isPlaintextPasswordEnabled();

            // 获取当前真实密码
            String currentRealPassword = realPasswordMap.getOrDefault(playerName, "");

            // 检查是否是删除操作（输入长度小于当前真实密码长度）
            if (cleanInput.length() < currentRealPassword.length()) {
                // 删除操作：截取真实密码
                String newRealPassword = currentRealPassword.substring(0, cleanInput.length());

                // 如果删除到空，完全清空
                if (newRealPassword.isEmpty()) {
                    realPasswordMap.remove(playerName);
                    anvilInputMap.remove(playerName);
                } else {
                    realPasswordMap.put(playerName, newRealPassword);
                    // 根据配置决定显示方式
                    String displayPassword = showPlaintext ? newRealPassword : "*".repeat(newRealPassword.length());
                    anvilInputMap.put(playerName, displayPassword);
                }
            } else if (cleanInput.length() > currentRealPassword.length()) {
                // 添加操作：获取新增的字符
                String newChars = cleanInput.substring(currentRealPassword.length());
                String newRealPassword = currentRealPassword + newChars;
                realPasswordMap.put(playerName, newRealPassword);

                // 根据配置决定显示方式
                String displayPassword = showPlaintext ? newRealPassword : "*".repeat(newRealPassword.length());
                anvilInputMap.put(playerName, displayPassword);
            } else {
                // 长度相同，可能是替换操作
                // 如果启用明文显示，直接处理
                if (showPlaintext) {
                    // 明文模式：直接保存和显示
                    realPasswordMap.put(playerName, cleanInput);
                    anvilInputMap.put(playerName, cleanInput);
                } else {
                    // 掩码模式：检查是否全是*号（已经是掩码状态）
                    if (cleanInput.matches("\\*+")) {
                        // 如果是单个*且当前真实密码为空，说明这是删除操作的残留
                        if (cleanInput.equals("*") && currentRealPassword.isEmpty()) {
                            // 完全清空
                            anvilInputMap.remove(playerName);
                            realPasswordMap.remove(playerName);
                        } else {
                            // 保持当前状态
                            anvilInputMap.put(playerName, cleanInput);
                        }
                    } else {
                        // 新的密码输入
                        realPasswordMap.put(playerName, cleanInput);
                        String maskedPassword = "*".repeat(cleanInput.length());
                        anvilInputMap.put(playerName, maskedPassword);
                    }
                }
            }
        }
    }
    
    /**
     * 获取玩家的显示文本（掩码后的密码）
     * @param playerName 玩家名称
     * @return 显示文本，如果没有则返回null
     */
    public static String getPlayerInput(String playerName) {
        return anvilInputMap.get(playerName);
    }

    /**
     * 获取玩家的真实密码
     * @param playerName 玩家名称
     * @return 真实密码，如果没有则返回null
     */
    public static String getRealPassword(String playerName) {
        return realPasswordMap.get(playerName);
    }

    /**
     * 移除玩家的输入文本
     * @param playerName 玩家名称
     */
    public static void removePlayerInput(String playerName) {
        anvilInputMap.remove(playerName);
        realPasswordMap.remove(playerName);
    }

    /**
     * 清空所有输入文本
     */
    public static void clearAll() {
        anvilInputMap.clear();
        realPasswordMap.clear();
    }

    /**
     * 检查玩家是否有输入
     * @param playerName 玩家名称
     * @return 是否有输入
     */
    public static boolean hasInput(String playerName) {
        return realPasswordMap.containsKey(playerName) &&
               realPasswordMap.get(playerName) != null &&
               !realPasswordMap.get(playerName).trim().isEmpty();
    }

    /**
     * 获取玩家密码的长度
     * @param playerName 玩家名称
     * @return 密码长度
     */
    public static int getPasswordLength(String playerName) {
        String realPassword = realPasswordMap.get(playerName);
        return realPassword != null ? realPassword.length() : 0;
    }

    /**
     * 强制清空玩家的所有输入（用于处理特殊情况）
     * @param playerName 玩家名称
     */
    public static void forceRemovePlayerInput(String playerName) {
        anvilInputMap.remove(playerName);
        realPasswordMap.remove(playerName);
    }

    /**
     * 检查是否启用明文显示密码
     * @return 是否启用明文显示
     */
    private static boolean isPlaintextPasswordEnabled() {
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
