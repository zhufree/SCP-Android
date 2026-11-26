# 构建与发布流程

> 构建配置核对日期：2026-11-24。发布步骤中没有仓库记录的部分仍标为“待确认”。

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

`local.properties` 需要有 `sdk.dir`（已被 gitignore，由 Android Studio 生成）。

## 2. 首次构建前必须准备的本地文件

以下三个文件被 `.gitignore` 忽略，**全新克隆后不存在，缺失则无法编译**：

| 文件 | 作用 |
| --- | --- |
| `local.properties` | `sdk.dir=<Android SDK 路径>` |
| `app/google-services.json` | Firebase 配置，`com.google.gms.google-services` 插件在构建时需要 |
| `app/src/main/java/info/free/scp/PrivateConstants.kt` | 后端地址与 Firebase 地址 |

`PrivateConstants.kt` 的结构（常量名取自当前引用处）：

```kotlin
package info.free.scp

object PrivateConstants {
    const val FEED_API_URL = "https://example.com/"            // 可选的 Feed 接口基地址（占位值会触发默认回退）
    const val FIREBASE_URL = "https://example.firebaseio.com"  // Firebase Realtime Database 地址
}
```

可以使用占位值完成编译。Feed 接口优先使用 Firebase `config/api_url` 的有效缓存，其次使用 `PrivateConstants.FEED_API_URL`，两者均为空、占位或无效时使用公开 Bridge `https://api.zhufree.fun/`。自定义地址应使用 HTTPS，旧版 Bridge 的 HTTP 地址会自动升级为 HTTPS。Firebase 功能联调需要可用的 Firebase 配置。不要提交这些文件。

## 3. 构建变体

两个 flavor 维度 × 三个 build type：

- `channel`：`Default`、`firim`、`GooglePlay`、`Github`
- `version`：`Test`（版本名追加 `-test`）、`Production`
- build type：`debug`、`release`、`releaseStaging`（后两者都开启 `minifyEnabled` 与 `shrinkResources`，使用 `proguard-android-optimize.txt`，并在 `app/proguard-rules.pro` 中显式禁用代码优化，维持旧配置）

变体名 = 渠道 + 版本 + 构建类型，例如 `DefaultProductionDebug`、`GithubProductionRelease`、`DefaultProductionReleaseStaging`。

渠道在代码里的差异只有两处用 `BuildConfig.FLAVOR == "GooglePlayProduction"` 判断：“我的”页面不显示捐赠入口，详情页 WebView 不拦截 Wikidot 登录弹窗页面。除此之外各渠道没有各自的源码目录或资源。

## 4. 常用命令

在仓库根目录执行（macOS/Linux 用 `./gradlew`，Windows 用 `.\gradlew.bat`）：

```bash
./gradlew :app:assembleDefaultProductionDebug :app:testDefaultProductionDebugUnitTest
./gradlew :app:assembleDebug                            # 全部渠道和版本 flavor
./gradlew :app:assembleDefaultProductionRelease         # 未签名 release
./gradlew :app:assembleDefaultProductionReleaseStaging  # R8 与资源压缩预检
```

日常 debug APK 位于 `app/build/outputs/apk/DefaultProduction/debug/app-Default-Production-debug.apk`；JVM 报告位于 `app/build/reports/tests/testDefaultProductionDebugUnitTest/`。运行单个测试类可追加 `--tests 'info.free.scp.data.catalog.*'`。

首次构建先联网执行所需任务，缓存 wrapper 分发包和各任务需要的依赖。之后使用同一个 `GRADLE_USER_HOME`，可在相同命令末尾加 `--offline`。`--offline` 不会阻止尚未缓存的 wrapper 分发包下载；不同的 Gradle 用户目录也不共享缓存。若本机 Gradle 配置含已失效的代理，确认网络可直连后，可在单次命令中加 `-Dhttp.proxyHost= -Dhttps.proxyHost=`，无需修改全局配置。

2026-11-24 本机以 JDK 21 验证：上述四组任务均可离线通过，生成 8 个 Debug APK 及两个未签名的 `DefaultProduction` Release APK；日常 Debug 编译与 JVM 测试再用 `--offline --rerun-tasks` 实跑，108 项全通过。

单元测试放在 `app/src/testDefaultProductionDebug/java`，而不是常规的 `app/src/test`：项目有名为 `Test` 的 product flavor，在大小写不敏感的文件系统上 `src/test` 会与 `src/Test` 冲突（来自交接文档）。新增 JVM 测试请放在该目录。

### 0.3.5 构建工具链升级记录

| 组件 | 旧版 → 新版 |
| --- | --- |
| Gradle / AGP | 8.13 → 9.9.2；8.13.2 → 9.5.2 |
| Kotlin / Compose 编译插件 / kotlinx.serialization JSON | 2.2.0 → 2.4.21；JSON 1.3.2 → 1.13.0 |
| KSP / Google Services 插件 | 2.2.0-2.0.2 → 2.3.14；4.4.0 → 4.6.0 |
| Android SDK | compile/target 34 → 37，min 仍为 21 |
| Compose | 手动固定 1.7.4 → BOM 2025.11.01（UI、Foundation 和 Material 1.9.5，Material Icons 1.7.8）；Accompanist Pager 0.25.0 → Foundation Pager |
| AndroidX | AppCompat 1.7.0 → 1.7.1；Core 1.13.1 → 1.17.0；Activity 1.9.3 → 1.11.0；Lifecycle 2.8.6 → 2.9.4；Navigation 2.8.3 → 2.9.8；ConstraintLayout 2.1.4 → 2.2.2 |
| Material / Firebase BOM / Coil / jsoup | 1.12.0 → 1.13.0；33.5.0 → 33.16.0；2.1.0 → 2.7.0；1.19.1 → 1.24.1 |
| 网络库 | Retrofit 和 Gson converter 2.9.0 → 3.0.0；OkHttp 由传递依赖 4.9.3 改为直接依赖 5.5.1 |
| 测试依赖 | sqlite-jdbc 3.41.2.2 → 3.54.1.0；AndroidX Test Runner 1.6.2 → 1.7.0；Espresso 3.6.1 → 3.7.0 |

为保留 API 21，Compose、AndroidX Core/Activity/Lifecycle/AppCompat/Navigation、Material 与 Firebase 没有追到要求更高 `minSdk` 的主线。Room 仍用 2.7.2（2.8+ 要求 API 23），schema 继续导出到 `app/schemas/`，`AppInfoDatabase` 保持 v8。Coil 3.x 需要更换坐标并迁移网络加载和缓存行为，本次只升到 2.x 最新版本。Preference 1.2.1、verticalseekbar 1.0.0 和 JUnit 4.13.2 无同坐标更新的稳定版。

## 5. 版本号

`app/build.gradle` 的 `defaultConfig` 中手工维护：

```
versionCode = 31
versionName = '0.3.5'
```

应用内更新提示用 `BuildConfig.VERSION_CODE` 与 Firebase `config/app_version` 比较，后者更大时弹出“发现新版本”对话框并跳转 `config/update_link`（`UpdateManager.getConfig()`）。发版时需要同步更新 Firebase 里的这些字段，由维护者在 Firebase 控制台操作，仓库内无脚本。

## 6. 发布产物

发布输出目录 `app/DefaultProduction/release/`、`app/GithubProduction/release/`、`app/GooglePlayProduction/release/`、`app/firimProduction/release/` 里的旧签名 APK 已从仓库移除，并在 `app/.gitignore` 里忽略这些输出路径。它们来自 Android Studio 的“生成已签名 APK”向导（推断，待确认）；发版时在本地生成，不要提交。当前版本为 `versionCode 31` / `versionName 0.3.5`。

`app/build.gradle` 中**没有**生产签名配置，命令行产出的 release APK 未签名。生产 keystore 的来源与保管方式没有仓库记录，**待确认**。

## 7. 当前可核实的发布流程

以下是仓库里能够确认的步骤；没有依据的环节标注“待确认”。

1. 在 `app/build.gradle` 提升 `versionCode`/`versionName`。
2. 若 APK 需要新版内置目录，同时替换 `assets/scp_category_v2.db` 和从该库 `spider_meta` 生成的 `assets/scp_category_v2.meta.json`。已有安装在冷启动时按导出时间升级。
3. 运行 `testDefaultProductionDebugUnitTest` 与 `assembleDefaultProductionReleaseStaging`，确认 R8 与资源压缩没有问题。
4. 为各渠道生成已签名发布包（签名方式待确认）：`Default`、`firim`、`GooglePlay`、`Github`，版本均为 `Production`。
5. 在 Firebase Realtime Database 更新 `config`（`app_version`、`update_desc`、`update_link`、`notice`）（手工操作，待确认具体流程）。
6. 若只更新数据，使用 SCP-Spider Release。旧版全文库仍可通过“离线与备份”中的下载线路和 SAF 导入。
7. 上架或分发渠道（Google Play、GitHub Release、fir.im、应用商店）的具体操作没有在仓库中记录，待确认。

## 8. 发布前的设备复验清单

来自 `refactor-handoff.md`，截至 2026-11-18 尚未完成（当时没有连接设备）：

- 桌面小部件（RemoteViews）
- SAF 导入数据库、备份与恢复
- FileProvider 截图分享
- WebView 在线/离线切换与 Cookie 登录
- Android 17 上的系统栏、刘海、旋转、键盘与返回操作；API 21 上的冷启动、图片加载和网络请求
- 游戏推荐分页滑动与页面恢复；只有中文 Wiki 域名可走 HTTP 时的正文、图片和内嵌内容

## 9. 构建中已知的现象

- `app/build.gradle` 里 `configurations.configureEach { exclude ... lifecycle-common-java8 }`：`material3:1.4.0` 间接依赖 `lifecycle-common-java8:2.9.4`，该模块自 2.8 起只是空壳（`DefaultLifecycleObserver` 已并入 `lifecycle-common`），排除后不影响运行；这样离线缓存（没有该版本）也能解析。联网构建时可以删掉这段，但保留也无害。
- 全渠道 debug 构建时 `libandroidx.graphics.path.so` 无法 strip，按原样打包，不影响构建成功（交接文档）。
- `ndk.abiFilters` 限定为 `armeabi-v7a`、`arm64-v8a`、`x86_64`。
- Manifest 默认禁止明文流量；Android 7.0+ 的网络安全配置只对白名单中的 `scp-wiki-cn.wikidot.com` 保留 HTTP。其余应用内请求需使用 HTTPS。
- `android.enableR8.fullMode=false` 与 `android.nonFinalResIds=false` 在 AGP 9.5 仍生效，但已弃用。优化版资源压缩不支持 final R ID，因此本次关闭优化版资源压缩，保留原有代码和资源压缩流程；升级到 AGP 10 时需迁移并重新验证。
- 资源压缩复查（2026-11-24，主题改为 Material 3 之后重做）：用 `aapt2 dump resources` 检查 `DefaultProduction` 未签名 Release APK，`DayTheme`/`NightTheme`/`Theme.App.Splash`（含 v31 变体）/`AppTheme.AppBarOverlay`/`ToolbarPopupTheme`、`toolbarBg`/`onToolbar`/`containerBg`/`accentColor` 属性、`md_day_*`/`md_night_*` 颜色、`ic_launcher_foreground`/`ic_launcher_background`/`splash_window`、`mipmap/ic_launcher(_round)` 的 `anydpi-v26` 变体（含 `<monochrome>`）、`preference_widget_material_switch` 都保留（`preference_widget_material_switch` 已在 2026-11-25 设置页 Compose 化时随 Preference 一起删除），Manifest 里 `enableOnBackInvokedCallback=true`。项目没有 `values-night`（日夜样式由 `ThemeUtil` 运行时切换），因此不存在被收缩误删的夜间资源；`ThemeUtil` 通过 `R.attr`/`R.style` 引用，`colorResource(R.color.md_*)` 也是 R 字段引用，R8/资源压缩不会误删。
- 升级到 `targetSdk 37` 后，Android 15+ 强制 edge-to-edge，状态栏/导航栏颜色由 `ActivityInsets.kt` 处理，见 `docs/architecture.md` 的“主题”一节。临时改回 `targetSdk 34` 在真机上的外观与 37 一致，因此无需为此降级。

## 数据 Release

- 固定清单地址为 `https://github.com/zhufree/SCP-Spider/releases/latest/download/manifest.json`。清单、目录库和全部正文块随同一个 `db-YYYYMMDD-N` Release 发布。每个资产的 `release_tag` 必须等于清单顶层的 `tag`。已安装的块按块 ID 匹配，`parts_version` 和 `content_digest` 都相同时跳过下载。重新发布已有块文件时，清单用 `part_export_id` 保留该块自身的导出 ID，供 Android 校验。
- 清单上限 64 KiB，目录压缩包和原始库各限 50 MiB，正文块各限 200 MiB。安装正文块至少需要原始大小的三倍加 50 MiB 可用空间。下载地址由已校验的 `release_tag` 和 `name` 生成。
- `parts_version` 描述分块规则，独立于 `manifest_version` 和 `contract_version`。`parts_version: 2` 使用 `scp-en`（英文 SCP）、`tales-cn`（中文故事）、`scp-cn`（中文 SCP）、`creative`（英文故事与其他创作）和 `library`（资料、竞赛与国际分部）。Android 从清单读取块 ID、顺序和数量，并核对 SQLite 块的 `spider_meta.parts_version`。
- 「数据更新」的「下载全部正文」逐块执行单块下载、校验和事务合并，按块重新检查网络和空间。已是最新的块直接跳过，超限块会提示并跳过。单块失败后继续尝试后续块，重试时按本地安装状态重新计算。取消后保留已提交的块，正在合并的事务会运行至结束。
- 重定向资源域名为 `release-assets.githubusercontent.com` 和 `objects.githubusercontent.com`，实际域名待首次公开 Release 核对。固定清单地址允许先跳转到同仓库带标签的 `manifest.json`。全部新请求使用 HTTPS。
- JVM 测试用 SQLite JDBC 校验 v1 真实导出样本及 v2 合成样本，并演练升级、合并失败后补装与取消。设备需复验冷启动暂存安装与恢复、Room 对附加表的校验、正文 ATTACH 与 WAL、游标回退与检查点，以及计费网络确认和 Wi-Fi 门控。实际 v2 打包结果待单独核验。
- C2 为各仓库合入 `master` 前确认。C6 为 Android 新版本发布前确认。

## 10. 主题配色、启动页与图标

### 配色表（`res/values/colors.xml`）

配色以启动图标（黑底荧光绿花纹 + 白色标志）为依据：主色取深绿 `#1B5E3A`（与白字对比 7.75:1，没有取更深的 `#0F4D2E`，是为了在手机屏上仍能看出是“绿”而不是“黑”）；亮绿 `#4CAF50` 在白底上只有 2.8:1，不能当文字色，所以日间只让深绿和 `#2E7D32` 承担文字/按钮，亮绿系只在夜间（`#81C995`、`#66BB6A`）当主色使用，日间的 `tertiary #43A047` 仅作装饰（非文本，3.3:1）。夜间背景 `#0E1512` 是带一点绿灰的近黑，避免纯黑刺眼。红色只保留给错误语义（`error`）。

| 角色 | 日间 | 夜间 | 用途 |
| --- | --- | --- | --- |
| primary / onPrimary | `#1B5E3A` / `#FFFFFF` | `#81C995` / `#0A2A16` | 按钮、Tab 选中和指示线、Preference 分类标题、开关 |
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

其他使用处：“最高评分”入口卡片的分类色里，`#BA6502` 白字只有 4.24 → 改为 `#B45F00`（4.59）；`#0780B7` 4.39 → `#06709F`（5.49）。捐赠页第三方品牌按钮 `#8A62DC`（白字 4.32，文字 20sp 属于大字，要求 3）保留，`#FED04D` 黄色按钮的文字由白改为 `#1A1C1A`（14.3）。搜索框文字和边框随主题。

### 启动页（SplashScreen）

没有引入 `androidx.core:core-splashscreen`：本机离线 Gradle 缓存里没有这个库，而 Android 12+ 平台本身支持同样的属性。`values-v31/themes_splash.xml` 的 `Theme.App.Splash` 设置 `windowSplashScreenBackground`/`IconBackgroundColor = @color/splash_background`（`#0A0F0C`）和 `windowSplashScreenAnimatedIcon = @drawable/ic_launcher_foreground`；低版本（API 21–30）由 `values/themes_splash.xml` 的 `windowBackground = @drawable/splash_window`（近黑底 + 居中标志）承担。运行时 `BaseActivity.onCreate` 里的 `ThemeUtil.setTheme` 把主题换成日/夜主题，因此日夜仍只有 `ThemeUtil` 一个来源。后续如要用 `postSplashScreenTheme`，需联网拉取 `core-splashscreen` 后再改。

### 自适应图标与 Android 13 主题图标

- 源文件仍是 `res/drawable/ic_launcher.jpg`（实际是 198×198 PNG，保留不删，API 21–25 通过 `res/mipmap/ic_launcher(_round).xml` 位图包装回退到它）。
- `res/mipmap-anydpi-v26/ic_launcher.xml` 与 `ic_launcher_round.xml`：`background` = `drawable/ic_launcher_background.xml`（矢量径向渐变，中心 `#14482E` → 边缘 `#0A0F0C`），`foreground` 与 `monochrome` = `drawable-nodpi/ic_launcher_foreground.png`。
- 前景由 `tools/make_adaptive_icon.py` 生成：用 `min(R,B)` 从原图抠出白色标志，6 倍上采样、模糊、提高对比度后缩放到 64dp 宽，居中放入 432×432 透明画布（在 66dp 安全区内）。需要 Pillow 和 numpy，脚本重复运行输出逐字节一致。
- Manifest：`android:icon="@mipmap/ic_launcher"`、`android:roundIcon="@mipmap/ic_launcher_round"`。
- 验证：`aapt2 dump xmltree` 确认 release APK 里 `<adaptive-icon>` 含 background/foreground/monochrome；真机（Android 15）上系统 Toast 里已渲染出新的自适应图标。桌面图标和主题图标（需在系统设置里开启“主题图标”）没有在桌面上截图确认。
