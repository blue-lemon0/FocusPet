# FocusPet

**帧动画桌宠 + 番茄钟** — Kotlin + Compose Multiplatform Desktop 构建，Windows 原生运行。

## 功能

| 功能 | 说明 |
|------|------|
| 🖥️ 悬浮窗口 | 透明圆角窗口，始终置顶（`Ctrl+Shift+F` 切换），可拖动 |
| 🎞️ 帧动画桌宠 | 逐帧 PNG 动画的卡通少女头像，7 种表情：NEUTRAL / HAPPY / FOCUSING / ANGRY / PLEADING / WINK / RESTING |
| 🔄 类型队列交替 | 随机表情 → NEUTRAL 桥接 → 随机 → NEUTRAL → ...，外部状态切换也会先经过 NEUTRAL |
| 💬 随机气泡 | 每 30~55s 弹出随机问候语，停留 4s |
| ⏱ 番茄钟 | 25分钟专注 / 5分钟休息，自动切换表情，支持开始/暂停/重置 |
| 🎯 计时联动 | 专注中 → FOCUSING，剩余 <1min → PLEADING，休息 → RESTING，空闲时随机切换表情 |
| 💾 数据持久化 | 累计专注分钟数保存到 `~/.focuspet/data.json` |
| 🎛 系统托盘 | 原生托盘图标 + 右键菜单（显示/隐藏、退出） |

## 环境要求

- **JDK 21+**（推荐 Amazon Corretto 21）
- Gradle（由 wrapper 自动管理）

## 快速开始

```powershell
$env:JAVA_HOME = "C:\Users\xxx\.jdks\corretto-21.0.6"
.\gradlew.bat :desktopApp:run
```

## 依赖

- **系统托盘**: [ComposeNativeTray](https://github.com/kdroidFilter/ComposeNativeTray) (`io.github.kdroidfilter:composenativetray:0.9.1`)
- **Compose Multiplatform Desktop**: JetBrains Compose 插件

## 打包

```powershell
.\gradlew.bat :desktopApp:packagePortable
# → desktopApp/build/compose/dist/FocusPet/FocusPet.exe
```

产物为便携 EXE（内嵌裁剪 JRE，双击运行，无需预装 Java）。

## 项目结构

```
FocusPet/
├── desktopApp/src/main/kotlin/com/lemon/focuspet/
│   └── main.kt                      # 入口 + 窗口 + 系统托盘 + 置顶切换
└── shared/src/
    ├── commonMain/kotlin/com/lemon/focuspet/
    │   ├── model/PetState.kt        # 7 种表情枚举
    │   ├── viewmodel/PomodoroViewModel.kt  # 番茄钟状态机 + petState
    │   ├── ui/PetScreen.kt          # 主界面：组装 PetAvatar + PetBubble + 计时器/按钮
    │   ├── ui/PetAvatar.kt          # 帧动画头像组件（精灵加载、队列管理、Canvas 渲染）
    │   ├── ui/PetBubble.kt          # 独立气泡 UI 组件
    │   ├── ui/FrameQueue.kt         # 类型队列（长度 2，NEUTRAL 交替桥接）
    │   ├── ui/SpriteSheet.kt        # 帧数据模型 + expect SpriteLoader
    │   └── util/
    │       ├── DataStore.kt         # expect 持久化接口
    │       └── DesktopEnv.kt        # 桌面环境抽象
    ├── jvmMain/kotlin/com/lemon/focuspet/
    │   ├── ui/SpriteLoader.kt       # actual 精灵加载：getResource → File/Jar 遍历
    │   └── util/
    │       ├── DataStore.kt         # actual JSON 文件存储
    │       └── DesktopEnvJvm.kt     # AWT 窗口操作
    └── jvmMain/resources/sprites/   # 帧 PNG 资源，按表情分目录
        ├── happy/
        ├── focusing/
        ├── angry/
        ├── pleading/
        ├── resting/
        ├── neutral/
        └── wink/
```

## 架构要点

- **帧动画**: SpriteLoader 通过 classpath getResource 找到首帧 → 根据 `file://`/`jar://` 协议遍历目录，等间隔采样最多 24 帧，`Canvas` 绘制
- **类型队列**: FrameQueue 维护 `[currentState, nextState]`，每 300ms `advance()` 前进一帧，播完自动切换并补位 NEUTRAL；`scheduleTransition(target)` 用于外部状态干预
- **组件隔离**: PetAvatar 只负责脸渲染 + overlay 定位槽，PetBubble 负责气泡样式，PetScreen 编排布局和颜色动画
- **颜色动画**: PetScreen 从 `viewModel.petState` 驱动 `animateColorAsState`，Surface 背景色和 accent 色平滑过渡
