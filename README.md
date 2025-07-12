# AuthmeGui - 铁砧GUI登录系统

一个基于AuthMe的Minecraft服务器登录注册插件，提供美观易用的铁砧GUI界面，让玩家通过图形界面完成登录和注册操作。

## 📁 项目结构

### src目录结构树
```
src/
└── main/
    ├── java/
    │   └── cn/
    │       └── i7mc/
    │           └── authmeGui/
    │               ├── AuthmeGui.java                     # 主插件类
    │               ├── command/
    │               │   └── AuthGuiCommand.java            # 命令处理器
    │               ├── config/
    │               │   ├── MenuConfig.java                # 菜单配置数据类
    │               │   ├── MenuConfigParser.java          # 菜单配置解析器抽象类
    │               │   └── impl/
    │               │       └── MenuConfigParserImpl.java  # 菜单配置解析器实现
    │               ├── gui/
    │               │   ├── AnvilGUI.java                  # 铁砧GUI抽象基类
    │               │   ├── LoginGUI.java                  # 登录界面实现
    │               │   └── RegisterGUI.java               # 注册界面实现
    │               ├── listener/
    │               │   ├── AnvilInputListener.java        # 铁砧输入监听器(PacketEvents)
    │               │   ├── AuthMeEventListener.java       # AuthMe事件监听器
    │               │   ├── InventoryEventListener.java    # 背包事件监听器
    │               │   └── PlayerEventListener.java       # 玩家事件监听器
    │               ├── manager/
    │               │   ├── AuthMeManager.java             # AuthMe管理器抽象类
    │               │   ├── ConfigManager.java             # 配置管理器抽象类
    │               │   ├── GUIManager.java                # GUI管理器抽象类
    │               │   ├── MessageManager.java            # 消息管理器抽象类
    │               │   └── impl/
    │               │       ├── AuthMeManagerImpl.java     # AuthMe管理器实现
    │               │       ├── ConfigManagerImpl.java     # 配置管理器实现
    │               │       ├── GUIManagerImpl.java        # GUI管理器实现
    │               │       └── MessageManagerImpl.java    # 消息管理器实现
    │               └── util/
    │                   ├── AnvilInputUtil.java            # 铁砧输入处理工具
    │                   └── GuiOpenHelper.java             # GUI打开辅助工具
    └── resources/
        ├── config.yml          # 主配置文件
        ├── message.yml         # 消息配置文件
        ├── plugin.yml          # 插件元数据
        └── menu/
            ├── login.yml       # 登录界面配置
            └── register.yml    # 注册界面配置
```

### 架构设计特点

#### 🏗️ 抽象类模块化架构
- **Manager层**：统一管理不同功能模块（AuthMe、配置、GUI、消息）
- **GUI层**：AnvilGUI抽象基类统一处理铁砧界面交互逻辑
- **Listener层**：分离不同类型的事件监听（玩家、背包、AuthMe、PacketEvents）
- **Util层**：提供通用工具类支持

#### 🔄 统一方法原则
- 所有消息发送通过MessageManager统一处理
- 所有配置读取通过ConfigManager统一管理

#### 🛡️ 安全设计
- 密码输入支持掩码显示(*号)，防止密码泄露
- 白名单机制防止GUI重复打开和事件冲突
- 完善的异常处理，确保插件稳定运行

## 🌟 主要特性

### 🎮 用户友好的界面
- **铁砧GUI界面**：使用Minecraft原生铁砧界面，操作直观简单
- **自动弹出**：玩家进入服务器时根据AuthMe状态自动显示登录/注册界面
- **密码安全**：支持密码掩码显示（*号）和明文显示两种模式
- **实时反馈**：操作成功/失败时提供清晰的消息提示
- **PacketEvents支持**：使用PacketEvents实现精确的铁砧输入监听

### 🔐 安全可靠
- **AuthMe集成**：完全兼容AuthMe插件，使用其安全的密码验证系统
- **密码验证**：支持密码长度限制和复杂度检查
- **会话管理**：自动处理玩家登录状态，防止重复操作
- **输入安全**：密码输入过程中实时掩码处理，防止密码泄露

### ⚙️ 高度可定制
- **界面自定义**：通过menu配置文件自定义GUI外观、按钮材质和描述
- **消息自定义**：所有提示消息都可以在message.yml中修改，支持颜色代码和占位符
- **行为配置**：可调整自动弹出、延迟时间、密码显示模式等行为设置
- **动作配置**：支持自定义登录/注册成功后的执行动作

### 🚀 技术特性
- **模块化架构**：采用抽象类设计，易于扩展和维护
- **事件驱动**：基于AuthMe事件触发GUI显示，而非简单的玩家加入事件
- **异常处理**：完善的错误处理机制，确保插件稳定性
- **性能优化**：GUI缓存机制，减少重复创建开销

## 📋 安装要求

### 必需插件
- **Minecraft服务端**：Paper/Spigot/Bukkit 1.20+ 或更高版本
- **AuthMe插件**：必须先安装AuthMe插件作为依赖
- **PacketEvents插件**：必须安装PacketEvents 2.9.1或更高版本

### 兼容性
- 支持Minecraft 1.20+（api-version: 1.20）
- 兼容Paper、Spigot、Bukkit等主流服务端
- 与其他插件兼容性良好

## 🚀 安装步骤

1. **安装依赖插件**
   - 下载并安装AuthMe插件到服务器
   - 下载并安装PacketEvents插件到服务器
   - 确保两个依赖插件都正常运行

2. **安装AuthmeGui插件**
   - 将AuthmeGui.jar文件放入服务器的plugins文件夹
   - 重启服务器或使用插件管理器加载

3. **配置插件**
   - 首次启动后会自动生成配置文件
   - 根据需要修改配置文件（可选）

## 🎯 使用方法

### 玩家操作

#### 登录流程
1. 玩家进入服务器
2. 系统自动弹出登录界面
3. 在铁砧界面中输入密码
4. 点击绿色确认按钮完成登录

#### 注册流程
1. 新玩家进入服务器
2. 系统自动弹出注册界面
3. 在铁砧界面中设置密码
4. 点击绿色确认按钮完成注册

#### 界面操作说明
- **红色按钮**：取消操作（会被踢出服务器）
- **黄色按钮**：重置输入（清空已输入的密码）
- **绿色按钮**：确认操作（提交密码）

### 管理员操作

#### 基本命令
```
/authgui reload              # 重载插件配置
/authgui open <玩家> [类型]   # 为指定玩家打开GUI (类型: login/register/auto)
/authgui close <玩家>        # 关闭指定玩家的GUI
/authgui info               # 显示插件信息和状态
```

#### 命令参数说明
- `<玩家>`：目标玩家名称，支持Tab补全
- `[类型]`：可选参数
  - `login`：强制打开登录界面
  - `register`：强制打开注册界面  
  - `auto`：根据玩家注册状态自动选择（默认）

#### 权限节点
- `authgui.admin` - 管理员权限（默认：OP）

## ⚙️ 配置说明

### 主配置文件 (config.yml)

#### GUI设置
```yaml
gui:
  enabled: true          # 是否启用GUI系统
  auto-open: true        # 玩家进入时自动打开GUI
  open-delay: 5          # GUI打开延迟(tick)
  input-timeout: 50      # 密码输入超时时间(秒)
```

#### AuthMe集成设置  
```yaml
authme:
  enabled: true          # 是否启用AuthMe集成
  # 登录成功后的操作
  login-success-actions:
    - "[message]&a登录成功！欢迎回来！"
  # 注册成功后的操作  
  register-success-actions:
    - "[message]&a注册成功！欢迎加入服务器！"
  # 操作失败后的操作
  failure-actions:
    - "[message]&c操作失败，请重试！"
```

#### 安全设置
```yaml
security:
  min-password-length: 6           # 最小密码长度
  max-password-length: 20          # 最大密码长度
  password-complexity: false       # 密码复杂度检查
  show-password-plaintext: false   # 是否明文显示密码(true=明文,false=掩码)
```

#### 性能设置
```yaml
performance:
  gui-cache-size: 100        # GUI缓存大小
  reload-interval: 5         # 配置文件重载间隔(秒)
```

### 消息配置文件 (message.yml)

所有玩家看到的提示消息都可以在此文件中自定义：
- 支持Minecraft颜色代码（&a、&c等）
- 支持占位符（{player}、{password}等）
- 可自定义登录/注册成功失败消息

### GUI界面配置

#### 登录界面 (menu/login.yml)
```yaml
title: "&b登录系统"           # 界面标题
commands: []                # 打开时执行的命令
openActions: []             # 打开时执行的动作
closeActions: []            # 关闭时执行的动作

items:
  格子一:                   # 取消按钮
    type: RED_STAINED_GLASS_PANE
    name: ""
    lore:
      - "&c&l取消登录"
    slot: 0
    clickAction:
      - "[kick]|&c取消登录"
      
  格子二:                   # 重置按钮
    type: YELLOW_STAINED_GLASS_PANE  
    name: ""
    lore:
      - "&e&l重置填写"
    slot: 1
    
  结果:                     # 确认按钮
    type: LIME_STAINED_GLASS_PANE
    name: ""
    lore:
      - "&a确认登录"
    slot: 2
```

#### 注册界面 (menu/register.yml)
- 配置格式与登录界面相同
- 可自定义注册界面的按钮外观、材质、名称、描述
- 支持特殊物品类型：
  - `craftEngine-<物品ID>`：CraftEngine物品
  - `mythicMobs-<物品ID>`：MythicMobs物品  
  - `head-<玩家ID>`：指定玩家头颅
  - `random_bed`：随机颜色的床

#### 动作类型说明
- `[kick]|消息`：踢出玩家并显示消息
- `[message]|消息`：发送消息给玩家
- `[command]|命令`：执行控制台命令（支持{player}占位符）

## 🔧 常见问题

### Q: 玩家进入服务器后没有弹出GUI界面？
A: 请检查：
- AuthMe插件是否正常运行
- PacketEvents插件是否正常运行且版本兼容
- config.yml中gui.enabled和gui.auto-open是否为true
- 玩家是否已经登录（GUI只对未认证玩家显示）
- 检查控制台是否有错误日志

### Q: 密码输入显示为*号，如何看到真实密码？
A: 修改config.yml中security.show-password-plaintext为true（不推荐，仅用于调试）

### Q: 如何修改界面上的文字和按钮？
A: 
- 修改message.yml调整提示消息
- 修改menu/login.yml和menu/register.yml调整按钮外观和描述
- 支持Minecraft颜色代码(&a、&c等)

### Q: 如何调整密码安全设置？
A: 修改config.yml中security部分的配置项：
- min-password-length：最小密码长度
- max-password-length：最大密码长度  
- password-complexity：是否启用复杂度检查（字母+数字）

### Q: 提示缺少PacketEvents依赖怎么办？
A: 请确保已安装PacketEvents插件：
- 下载PacketEvents 2.9.1或更高版本
- 将其放入plugins文件夹并重启服务器

### Q: GUI突然关闭或重复打开怎么办？
A: 这可能是事件冲突导致的：
- 检查是否有其他插件也在处理背包事件
- 适当增加config.yml中gui.open-delay的值
- 查看控制台日志确认具体错误

### Q: 玩家输入密码后没有反应？
A: 请检查：
- 密码是否符合长度和复杂度要求
- AuthMe插件是否正常工作
- 控制台是否有AuthMe相关错误
- 尝试使用/authgui info查看插件状态

## 📞 支持与反馈

- **作者**：Saga
- **网站**：https://github.com/linghun91/AuthmeGui
- **版本**：1.0-SNAPSHOT
- **API版本**：1.20


如果您在使用过程中遇到问题或有改进建议，欢迎通过GitHub提交Issue。

## 🛠️ 开发与构建

### 构建指令
```bash
gradle clean build
```

### 开发环境要求
- JDK 17+
- Gradle 8.13+
- Paper API 1.20+

## 📄 许可证

本插件遵循开源许可证，详情请查看项目仓库。

---

*感谢使用AuthmeGui插件！希望它能为您的服务器带来更好的用户体验。*
