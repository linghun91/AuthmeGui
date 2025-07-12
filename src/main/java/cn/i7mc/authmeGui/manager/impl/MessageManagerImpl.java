package cn.i7mc.authmeGui.manager.impl;

import cn.i7mc.authmeGui.AuthmeGui;
import cn.i7mc.authmeGui.manager.ConfigManager;
import cn.i7mc.authmeGui.manager.MessageManager;

/**
 * 消息管理器实现类
 */
public class MessageManagerImpl extends MessageManager {
    
    public MessageManagerImpl(AuthmeGui plugin, ConfigManager configManager) {
        super(plugin, configManager);
    }
    
    @Override
    public void initialize() {
        
        // 验证消息配置文件是否加载成功
        if (configManager.getMessageConfig() == null) {
        }
        
    }
}
