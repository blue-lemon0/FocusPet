# FocusPet

Kotlin Multiplatform Compose Desktop 桌面宠物应用，可在 Windows 上原生运行。

## 环境要求

- **JDK 17+**（推荐使用 Microsoft OpenJDK 17）
- Gradle 9.1+（由 wrapper 自动管理）

## 在 Windows 上运行

```powershell
# 确保 JAVA_HOME 指向 JDK 17+

# 构建并运行桌面应用
.\gradlew.bat :desktopApp:run
```

首次运行会自动下载 Gradle 和依赖项。之后可直接使用：

```powershell
.\gradlew.bat :desktopApp:run
```

## 项目结构

```
FocusPet/
├── desktopApp/                  # 桌面应用入口
│   └── src/main/kotlin/
│       └── com/lemon/focuspet/
│           └── main.kt          # application 入口点
├── shared/                      # 跨平台共享代码
│   └── src/
│       ├── commonMain/          # 公共代码
│       └── jvmMain/             # JVM/Desktop 平台特定代码
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/
```

## 运行测试

```powershell
.\gradlew.bat :shared:jvmTest
```