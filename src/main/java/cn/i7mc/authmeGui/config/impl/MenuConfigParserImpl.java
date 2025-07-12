package cn.i7mc.authmeGui.config.impl;

import cn.i7mc.authmeGui.AuthmeGui;
import cn.i7mc.authmeGui.config.MenuConfigParser;

/**
 * 菜单配置解析器实现类
 */
public class MenuConfigParserImpl extends MenuConfigParser {
    
    public MenuConfigParserImpl(AuthmeGui plugin) {
        super(plugin);
    }
    
    @Override
    public void initialize() {
        
        // 加载所有菜单配置
        loadAllMenuConfigs();
    }
}
