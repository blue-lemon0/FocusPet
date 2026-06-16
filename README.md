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

## 打包发布

### 打包流程（三步）

```powershell
# 1. 编译 + 打 uber JAR（将所有依赖合并为一个 fat JAR）
.\gradlew.bat :desktopApp:packageUberJarForCurrentOS
#    → 输出: desktopApp/build/compose/jars/FocusPet-windows-x64-1.0.0.jar

# 2. 裁剪 JRE（只保留应用需要的模块）
.\gradlew.bat :desktopApp:createRuntimeImage
#    → 输出: desktopApp/build/compose/tmp/main/runtime/（~73 MB）

# 3. 用 jpackage 打包为便携 EXE（不需要 WiX）
$jpackage = "$env:USERPROFILE\.jdks\openjdk-24.0.1\bin\jpackage.exe"
& $jpackage --type app-image `
    --input desktopApp/build/compose/jars `
    --main-jar FocusPet-windows-x64-1.0.0.jar `
    --main-class com.lemon.focuspet.MainKt `
    --name FocusPet `
    --dest desktopApp/build/compose/dist `
    --runtime-image desktopApp/build/compose/tmp/main/runtime `
    --java-options "-Dfile.encoding=GBK" `
    --java-options "--enable-native-access=ALL-UNNAMED"
#    → 输出: desktopApp/build/compose/dist/FocusPet/
```

### 打包产物结构

```
desktopApp/build/compose/dist/FocusPet/
├── FocusPet.exe                         ← Windows 可执行文件（双击运行）
├── FocusPet.cfg                         ← 启动配置
├── .jpackage.xml                        ← jpackage 元信息
└── app/
    └── FocusPet-windows-x64-1.0.0.jar   ← fat JAR（主程序 + 全部依赖）
└── runtime/                             ← 裁剪后的 JRE（内嵌，无需用户安装 JDK）
    ├── bin/java.exe                     ← Java 运行时入口
    ├── lib/modules                      ← JDK 模块（~51 MB）
    └── ...
```

> **注意**: jpackage 需要 JDK 14+，这里使用 `openjdk-24.0.1`。`--type app-image` 输出便携版（不安装），如果要生成安装包则改用 `--type exe` 或 `--type msi`（需要安装 WiX Toolset 3.11+）。

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