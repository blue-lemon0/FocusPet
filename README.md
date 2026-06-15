# FocusPet 🐱

**桌面宠物 + 番茄钟** — 一个在 Windows 上原生运行的桌面应用，用 Kotlin + Compose Multiplatform Desktop 构建。

## 功能

| 功能 | 说明 |
|------|------|
| 🖥️ 悬浮窗口 | 200×200 透明窗口，始终置顶，可拖动，默认在屏幕右下角 |
| 🐱 宠物表情 | 😊开心 → 🧘专注中 → 😠生气(失焦5s) → 🥺撒娇(剩<1min) → 😴休息 |
| ⏱ 番茄钟 | 25分钟专注 / 5分钟休息，自动切换，支持开始/暂停/重置 |
| 🔍 窗口失焦检测 | 切换到其他窗口超过5秒，宠物进入生气状态 |
| 💾 数据持久化 | 累计专注分钟数保存到 `~/.focuspet/data.json` |
| 🎛 系统托盘 | 原生托盘图标 + 右键菜单（显示/隐藏、退出） |

## 环境要求

- **JDK 17+**（推荐 Microsoft OpenJDK 17）
- Gradle 9.1+（由 wrapper 自动管理）

## 快速开始

```powershell
# 设置 JDK 17+ 并运行
$env:JAVA_HOME = "C:\Users\xxx\.jdks\ms-17.0.18"
.\gradlew.bat :desktopApp:run
```

首次运行会自动下载 Gradle 和依赖项。

## 依赖说明

- **系统托盘**: [ComposeNativeTray](https://github.com/kdroidFilter/ComposeNativeTray) (`io.github.kdroidfilter:composenativetray:0.9.1`) — 跨平台原生托盘，Compose DSL 式菜单，支持 Windows/Linux/macOS。
- **Compose Multiplatform Desktop**: 使用 JetBrains Compose 插件构建桌面 UI。

## 项目结构

```
FocusPet/
├── desktopApp/src/main/kotlin/com/lemon/focuspet/
│   └── Main.kt                # 应用入口 + 窗口管理 + 系统托盘
└── shared/src/
    ├── commonMain/kotlin/com/lemon/focuspet/
    │   ├── model/PetState.kt          # 宠物状态枚举
    │   ├── viewmodel/PomodoroViewModel.kt  # 计时器 + 状态机
    │   ├── ui/PetScreen.kt            # 宠物窗口界面
    │   └── util/
    │       ├── DataStore.kt          # expect - 数据持久化
    │       └── DesktopEnv.kt         # 桌面环境抽象接口
    └── jvmMain/kotlin/com/lemon/focuspet/util/
        ├── DataStore.kt              # actual - JSON 文件存储
        └── DesktopEnvJvm.kt          # AWT 窗口操作实现
```