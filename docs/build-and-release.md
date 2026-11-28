# 构建与发布流程

## 1. 环境要求

| 项 | 版本/要求 | 出处 |
| --- | --- | --- |
| JDK | 17 或更高；本地推荐 JDK 21，Java 和 Kotlin 字节码目标均为 17 | `app/build.gradle` |
| Gradle | 9.9.2（使用带分发包 SHA-256 的 wrapper，`./gradlew`） | `gradle/wrapper/gradle-wrapper.properties` |
| Android Gradle Plugin | 9.5.2 | 根 `build.gradle` |
| Kotlin / Compose 编译插件 | 2.4.21，使用 AGP 内置 Kotlin 集成 | 根 `build.gradle` |
| KSP（Room 编译器） | 2.3.14 | 根 `build.gradle` |
| Android SDK | Android 17 SDK Platform（`android-37.0`）、Build Tools 36.0.0（AGP 默认）；`compileSdk 37`、`targetSdk 37`、`minSdk 21` | `app/build.gradle`、AGP 9.5 版本说明 |
| 内存 | Gradle/Kotlin daemon 各 4G（`org.gradle.jvmargs`），`org.gradle.workers.max=2` | `gradle.properties` |
| 仓库 | Google Maven、Maven Central | 根 `build.gradle` |

## 2. 本地配置

首次构建前在本地准备以下三个文件。三者均由 `.gitignore` 忽略。

| 文件 | 作用 |
| --- | --- |
| `local.properties` | `sdk.dir=<Android SDK 路径>` |
| `app/google-services.json` | Firebase Android 配置，客户端包名为 `info.free.scp` |
| `app/src/main/java/info/free/scp/PrivateConstants.kt` | Feed 接口和 Firebase Realtime Database 地址 |

`PrivateConstants.kt` 示例

```kotlin
package info.free.scp

object PrivateConstants {
    const val FEED_API_URL = "https://example.com/"            // 可选的 Feed 接口基地址（占位值会触发默认回退）
    const val FIREBASE_URL = "https://example.firebaseio.com"  // Firebase Realtime Database 地址
}
```

可以使用占位值完成编译。Feed 接口依次使用 Firebase `config/api_url` 的有效缓存、`PrivateConstants.FEED_API_URL` 和公开 Bridge `https://api.zhufree.fun/`。自定义地址使用 HTTPS，Firebase 功能需要可用的 Firebase 配置。

## 3. 构建变体

两个 flavor 维度 × 三个 build type：

- `channel`：`Default`、`firim`、`GooglePlay`、`Github`
- `version`：`Test`（版本名追加 `-test`）、`Production`
- build type：`debug`、`release`、`releaseStaging`。后两者开启 R8 和资源压缩，使用 `proguard-android-optimize.txt` 与 `app/proguard-rules.pro`，后者通过 `-dontoptimize` 禁用代码优化。

变体名 = 渠道 + 版本 + 构建类型，例如 `DefaultProductionDebug`、`GithubProductionRelease`、`DefaultProductionReleaseStaging`。

`GooglePlayProduction` 隐藏「我的」页面的捐赠入口，并在阅读页允许 Wikidot 登录弹窗。各渠道共用源码和资源。

## 4. 常用命令

在仓库根目录执行（macOS/Linux 用 `./gradlew`，Windows 用 `.\gradlew.bat`）：

```bash
./gradlew :app:assembleDefaultProductionDebug :app:testDefaultProductionDebugUnitTest
./gradlew :app:assembleDebug                            # 全部渠道和版本 flavor
./gradlew :app:assembleDefaultProductionRelease         # 未签名 release
./gradlew :app:assembleDefaultProductionReleaseStaging  # R8 与资源压缩预检
```

日常 debug APK 位于 `app/build/outputs/apk/DefaultProduction/debug/app-Default-Production-debug.apk`；JVM 报告位于 `app/build/reports/tests/testDefaultProductionDebugUnitTest/`。运行单个测试类可追加 `--tests 'info.free.scp.data.catalog.*'`。

首次联网构建后，使用同一个 `GRADLE_USER_HOME` 可对已缓存依赖的任务追加 `--offline`。

新增 JVM 单元测试放在 `app/src/testDefaultProductionDebug/java`。大小写不敏感的文件系统会将 `src/test` 与 `Test` flavor 的 `src/Test` 视为同一路径。

## 5. 版本号

`app/build.gradle` 的 `defaultConfig` 中手工维护：

```
versionCode = 31
versionName = '0.3.5'
```

当 Firebase `config/app_version` 大于 `BuildConfig.VERSION_CODE` 时，`UpdateManager.getConfig()` 显示应用内更新提示，下载地址为 `config/update_link`。

## 6. 发布流程

`assembleDefaultProductionRelease` 生成未签名 APK。Android Studio 的「Generate Signed Bundle / APK」可对发布包签名。签名产物保存在本地，`app/*Production/release/` 已加入忽略规则。

1. 在 `app/build.gradle` 提升 `versionCode`/`versionName`。
2. 若 APK 需要新版内置目录，同时替换 `assets/scp_category_v2.db` 和从该库 `spider_meta` 生成的 `assets/scp_category_v2.meta.json`。已有安装在冷启动时按导出时间升级。
3. 运行 `testDefaultProductionDebugUnitTest` 与 `assembleDefaultProductionReleaseStaging`，确认 R8 与资源压缩没有问题。
4. 为 `Default`、`firim`、`GooglePlay` 和 `Github` 各渠道的 `ProductionRelease` 变体生成签名产物。
5. 在 Firebase Realtime Database 中设置 `config/app_version`、`config/update_desc` 和 `config/update_link`，供应用内更新提示读取。

## 7. 构建配置

- `ndk.abiFilters` 限定为 `armeabi-v7a`、`arm64-v8a`、`x86_64`。
- Manifest 默认禁止明文流量。Android 7.0+ 的网络安全配置仅允许 `scp-wiki-cn.wikidot.com` 使用 HTTP。
- `gradle.properties` 禁用 R8 full mode 和优化版资源压缩，使用 final R ID。

## 数据 Release

- App 优先从 R2 获取清单（`https://scp-data.zhufree.fun/latest/manifest.json`）和资产（`https://scp-data.zhufree.fun/<release_tag>/<name>`）。连接、响应或校验失败时回退到 [GitHub Release](https://github.com/zhufree/SCP-Spider/releases/latest/download/manifest.json)。GitHub 的资源重定向限同仓库带标签的清单及 `release-assets.githubusercontent.com`、`objects.githubusercontent.com` 两个资源域名。全程使用 HTTPS。
- 每次发布先上传两边的同一套 `db-YYYYMMDD-N` 资产，再同步更新两边的固定清单并清除 R2 旧清单缓存。App 直接采用有效的 R2 清单，能否发现新版本取决于两个入口同步发布。资产 `release_tag` 须等于清单的 `tag`，复用正文块时保留其原始 `part_export_id`。
- 清单上限 64 KiB，目录和正文块的压缩包与原始库分别限 50 MiB、200 MiB。压缩包和原始库均校验大小与 SHA-256。安装正文块需原始大小的三倍加 50 MiB 可用空间。
- `parts_version` 描述分块规则，独立于 `manifest_version` 和 `contract_version`。版本 2 使用 `scp-en`（英文 SCP）、`tales-cn`（中文故事）、`scp-cn`（中文 SCP）、`creative`（英文故事与其他创作）和 `library`（资料、竞赛与国际分部）。Android 按清单提供的顺序逐块安装，跳过本地 `parts_version` 和 `content_digest` 均匹配的块。重试只补仍需安装的块，取消时完成已经开始的合并事务。

## 主题配色、启动页与图标

### 配色表

配色定义在 `res/values/colors.xml`。

| 角色 | 日间 | 夜间 | 用途 |
| --- | --- | --- | --- |
| primary / onPrimary | `#1B5E3A` / `#FFFFFF` | `#81C995` / `#0A2A16` | 按钮、选中标签、开关 |
| primaryContainer / on | `#C6E9D1` / `#06301B` | `#1F5133` / `#C6EFD2` | “我的”页入口图标底、待读页 FAB |
| secondary / onSecondary | `#2E7D32` / `#FFFFFF` | `#A5D6A7` / `#10301A` | 次级强调（`ThemeUtil.accentColor` 日间值） |
| secondaryContainer / on | `#CFEBD2` / `#0F3A14` | `#2C4A33` / `#D2EDD6` | 分类左侧选中胶囊、底部导航选中指示、次级按钮 |
| tertiary / onTertiary | `#43A047` / `#0B2A10` | `#66BB6A` / `#0B2A10` | 装饰性点缀（当前界面基本未用） |
| background | `#F3F6F4` | `#0E1512` | 页面底色（`containerBg`） |
| surface | `#FFFFFF` | `#17211B` | 卡片、搜索栏底（`itemBoxBg`） |
| surfaceVariant / onSurfaceVariant | `#DDE5DF` / `#414942` | `#2B3630` / `#BFC9C2` | 直达键盘的中性键、次要文字 |
| onSurface | `#1A1C1A` | `#DDE5DF` | 正文 |
| outline / outlineVariant | `#717971` / `#C1C9C2` | `#8A938C` / `#3B4540` | 输入框与搜索框边框、分割线 |
| error / onError | `#BA1A1A` / `#FFFFFF` | `#FFB4AB` / `#690005` | 错误、危险语义（保持红色） |
| errorContainer / on | `#FFDAD6` / `#410002` | `#93000A` / `#FFDAD6` | 错误提示底 |
| toolbar / onToolbar | `#1B5E3A` / `#FFFFFF` | `#14402A` / `#FFFFFF` | 标题栏与状态栏底色、标题栏文字和图标 |
| darkText / mediumText / lightText | `#3A433D` / `#5C645E` / `#6A746D` | `#C9D3CC` / `#A3ADA6` / `#8A938C` | 列表标题 / 次要文字 / 弱提示（`ThemeUtil` 同名字段） |
| link / todo | `#1F6FB5` / `#B35F00` | `#8AB4F8` / `#FFB74D` | 链接、待办（`linkBlue`、`todoYellow`） |
| surfaceContainerLowest…Highest | `#FFFFFF`…`#E2E8E3` | `#09100D`…`#2D3631` | M3 容器层级（底部导航栏用 surfaceContainer） |

### 对比度（WCAG，由 `colors.xml` 直接计算）

| 前景 / 背景 | 日间 | 夜间 | 要求 |
| --- | --- | --- | --- |
| 标题栏文字 / 标题栏 | 7.75 | 11.69 | ≥4.5 |
| onPrimary / primary（按钮文字） | 7.75 | 7.91 | ≥4.5 |
| onPrimaryContainer / primaryContainer | 11.05 | 7.31 | ≥4.5 |
| onSecondaryContainer / secondaryContainer（选中态） | 10.07 | 7.88 | ≥4.5 |
| onSurface / surface、background（正文） | 17.14 / 15.76 | 12.87 / 14.41 | ≥4.5 |
| darkText / surface | 10.24 | 10.77 | ≥4.5 |
| mediumText / surface | 6.11 | 7.15 | ≥4.5 |
| lightText / surface（弱提示） | 4.85 | 5.22 | ≥4.5 |
| primary / background（Tab、分类标题） | 7.12 | 9.45 | ≥4.5 |
| link / surface | 5.25 | 7.85 | ≥4.5 |
| todo / surface | 4.61 | 9.55 | ≥4.5 |
| error / surface | 6.46 | 9.74 | ≥4.5 |
| onError / error | 6.46 | 7.72 | ≥4.5 |
| outline / background（搜索框边框） | 4.13 | 5.85 | ≥3（非文本） |
| outline / surface | 4.49 | 5.22 | ≥3（非文本） |
| tertiary / surface（装饰） | 3.30 | 6.99 | ≥3（非文本） |

### 启动页（SplashScreen）

`Theme.App.Splash` 在 Android 12+ 使用 `values-v31/themes_splash.xml` 的系统启动页属性，背景为 `@color/splash_background`，标志为 `@drawable/ic_launcher_foreground`。API 21–30 使用 `values/themes_splash.xml` 中的 `windowBackground = @drawable/splash_window`。

### 自适应图标与 Android 13 主题图标

- API 21–25 通过 `res/mipmap/ic_launcher(_round).xml` 使用 `res/drawable/ic_launcher.jpg`。
- `res/mipmap-anydpi-v26/ic_launcher.xml` 与 `ic_launcher_round.xml`：`background` = `drawable/ic_launcher_background.xml`（矢量径向渐变，中心 `#14482E` → 边缘 `#0A0F0C`），`foreground` 与 `monochrome` = `drawable-nodpi/ic_launcher_foreground.png`。
- 前景由 `tools/make_adaptive_icon.py` 生成：用 `min(R,B)` 从原图抠出白色标志，6 倍上采样、模糊、提高对比度后缩放到 64dp 宽，居中放入 432×432 透明画布（在 66dp 安全区内）。需要 Pillow 和 numpy，脚本重复运行输出逐字节一致。
- Manifest：`android:icon="@mipmap/ic_launcher"`、`android:roundIcon="@mipmap/ic_launcher_round"`。
