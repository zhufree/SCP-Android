# 构建与发布流程

> 依据 `build.gradle`、`app/build.gradle`、`gradle.properties`、`.gitignore`、`proguard-rules.pro` 与 `refactor-handoff.md` 的验证记录整理，核对日期 2026-11-23。标注“待确认”的内容在仓库中没有依据，本次没有重新执行构建。

## 1. 环境要求

| 项 | 版本/要求 | 出处 |
| --- | --- | --- |
| JDK | 17 或更高（源码/Kotlin 目标均为 17；交接文档记录本机用 JDK 21 构建通过） | `app/build.gradle`、`refactor-handoff.md` |
| Gradle | 8.13（使用 wrapper，`./gradlew`） | `gradle/wrapper/gradle-wrapper.properties` |
| Android Gradle Plugin | 8.13.2 | 根 `build.gradle` |
| Kotlin / Compose 编译插件 | 2.2.0 | 根 `build.gradle` |
| KSP（Room 编译器） | 2.2.0-2.0.2 | 根 `build.gradle` |
| Android SDK | `compileSdk 34`、`targetSdk 34`、`minSdk 21` | `app/build.gradle` |
| 内存 | Gradle/Kotlin daemon 各 4G（`org.gradle.jvmargs`），`org.gradle.workers.max=2` | `gradle.properties` |
| 仓库 | Google、阿里云镜像、Maven Central；`app` 另有 JitPack | 两个 `build.gradle` |

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
    const val FEED_API_URL = "https://example.com/"            // 后端接口基地址（Retrofit baseUrl，需以 / 结尾）
    const val FIREBASE_URL = "https://example.firebaseio.com"  // Firebase Realtime Database 地址
}
```

仅为通过编译，可使用占位值（交接文档记录开发机上就是占位文件）；此时联网功能和 Firebase 远程配置不会返回真实数据。要在真机上联调，需要换成真实的后端与 Firebase 配置。真实值的获取方式属于维护者私有信息，本仓库没有记录（待确认）。不要提交这些文件。

## 3. 构建变体

两个 flavor 维度 × 三个 build type：

- `channel`：`Default`、`firim`、`GooglePlay`、`Github`
- `version`：`Test`（版本名追加 `-test`）、`Production`
- build type：`debug`、`release`、`releaseStaging`（后两者都开启 `minifyEnabled` 与 `shrinkResources`，使用 `proguard-android.txt` + `app/proguard-rules.pro`）

变体名 = 渠道 + 版本 + 构建类型，例如 `DefaultProductionDebug`、`GithubProductionRelease`、`DefaultProductionReleaseStaging`。

渠道在代码里的差异只有两处用 `BuildConfig.FLAVOR == "GooglePlayProduction"` 判断：“我的”页面不显示捐赠入口，详情页 WebView 不拦截 Wikidot 登录弹窗页面。除此之外各渠道没有各自的源码目录或资源。

## 4. 常用命令

在仓库根目录执行（macOS/Linux 用 `./gradlew`，Windows 用 `.\gradlew.bat`）：

```bash
./gradlew assembleDefaultProductionDebug            # 日常开发 debug 包
./gradlew testDefaultProductionDebugUnitTest        # JVM 单元测试
./gradlew assembleDefaultProductionReleaseStaging   # 带 R8/资源压缩的发布预检
./gradlew assembleDebug                             # 全部渠道的 debug 包
```

这是交接文档记录的验证矩阵，2026-11-18 在 `compose-refactor` 上均通过。运行单个测试类可追加 `--tests 'info.free.scp.data.catalog.*'`。

单元测试放在 `app/src/testDefaultProductionDebug/java`，而不是常规的 `app/src/test`：项目有名为 `Test` 的 product flavor，在大小写不敏感的文件系统上 `src/test` 会与 `src/Test` 冲突（来自交接文档）。新增 JVM 测试请放在该目录。

## 5. 版本号

`app/build.gradle` 的 `defaultConfig` 中手工维护：

```
versionCode 30
versionName "0.3.2"
```

应用内更新提示用 `BuildConfig.VERSION_CODE` 与 Firebase `config/app_version` 比较，后者更大时弹出“发现新版本”对话框并跳转 `config/update_link`（`UpdateManager.getConfig()`）。发版时需要同步更新 Firebase 里的这些字段，由维护者在 Firebase 控制台操作，仓库内无脚本。

## 6. 发布产物

仓库中已提交了几个发布输出目录：`app/DefaultProduction/release/`、`app/GithubProduction/release/`、`app/GooglePlayProduction/release/`、`app/firimProduction/release/`。每个目录含 APK、`output-metadata.json`（记录 `versionCode 30` / `versionName 0.3.2`）和 baseline profile（`.dm`）。其中只有 `DefaultProduction` 里的 APK 名带版本号（`app-0.3.2-release.apk`）。这些看起来是 Android Studio 的“生成已签名 APK”向导选择 `app/<渠道>Production` 作为输出目录的结果（推断，待确认）。

`app/build.gradle` 中**没有** `signingConfigs`，因此 Gradle 命令行产出的 release 包是否已签名、使用哪把 keystore、如何保管，仓库里没有记录，**待确认**。

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

## 9. 构建中已知的现象

- 全渠道 debug 构建时 `libandroidx.graphics.path.so` 无法 strip，按原样打包，不影响构建成功（交接文档）。
- `ndk.abiFilters` 限定为 `armeabi-v7a`、`arm64-v8a`、`x86_64`。
- Manifest 设置了 `usesCleartextTraffic="true"`，`SCP_SITE_URL` 是 `http://` 地址。

## 数据 Release

- 固定清单地址为 `https://github.com/zhufree/SCP-Spider/releases/latest/download/manifest.json`。目录位于最新的普通 `db-*` Release，正文块位于 `detail-parts`。相同内容可复用旧资产，Android 用 `part_export_id` 核对该块自身的导出 ID。
- 清单上限 64 KiB，目录压缩包和原始库各限 50 MiB，正文块各限 200 MiB。安装正文块至少需要原始大小的三倍加 50 MiB 可用空间。下载地址由已校验的 `release_tag` 和 `name` 生成。
- 重定向资源域名为 `release-assets.githubusercontent.com` 和 `objects.githubusercontent.com`，实际域名待首次公开 Release 核对。固定清单地址允许先跳转到同仓库带标签的 `manifest.json`。全部新请求使用 HTTPS。
- JVM 测试的真实 SQLite fixture 使用 SQLite JDBC。设备需复验冷启动暂存安装与恢复、Room 对附加表的校验、正文 ATTACH 与 WAL、游标回退与检查点，以及计费网络确认和 Wi-Fi 门控。
- C2 为各仓库合入 `master` 前确认。C6 为 Android 新版本发布前确认。
