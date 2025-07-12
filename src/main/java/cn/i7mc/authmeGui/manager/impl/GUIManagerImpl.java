package cn.i7mc.authmeGui.manager.impl;

import cn.i7mc.authmeGui.AuthmeGui;
import cn.i7mc.authmeGui.config.MenuConfigParser;
import cn.i7mc.authmeGui.manager.AuthMeManager;
import cn.i7mc.authmeGui.manager.GUIManager;
import cn.i7mc.authmeGui.manager.MessageManager;

/**
 * GUI管理器实现类
 */
public class GUIManagerImpl extends GUIManager {
    
    public GUIManagerImpl(AuthmeGui plugin, MessageManager messageManager, 
                         AuthMeManager authMeManager, MenuConfigParser menuConfigParser) {
        super(plugin, messageManager, authMeManager, menuConfigParser);
    }
    
    @Override
    public void initialize() {
        
        // 验证依赖组件
        if (!isGUIEnabled()) {
            return;
        }
        
        if (!authMeManager.isAuthMeEnabled()) {
        }
    }
}
