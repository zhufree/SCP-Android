# Kotlin + Compose 重构交接

- 最后核对：2026-11-18
- 工作分支：`compose-refactor`
- 阶段 2 代码锚点：`35e98f6`（`refactor: remove later child fragments`）

本文件是继续重构时的快速入口。完整背景、阶段定义和历史执行记录见
[`kotlin-compose-modernization-plan.md`](kotlin-compose-modernization-plan.md)。

## 当前结论

重构已经完成 UI 迁移主线和阶段 2 的数据/状态/宿主收敛。第 0、1、2A、2B、2C、3、4、5B 阶段完成。阶段 5A 的本地代码与工具链收口已完成。还差连接真机后的平台路径复验。

| 阶段 | 状态 | 当前证据 |
| --- | --- | --- |
| 0 构建与工具链基线 | 完成 | 主 debug 与 release staging 可构建 |
| 1 旧依赖首批清理 | 完成 | Anko、EasyPermissions 等已移除 |
| 2A 安全网与验证矩阵 | 完成 | 12 个测试文件、62 项 JVM 测试；debug/release staging 构建通过 |
| 2B Repository 边界 | 完成 | 阅读、目录、搜索、Tag、草稿、随机、详情、备份、直达、Feed 与语义化设置边界均已收敛 |
| 2C 状态模型与宿主瘦身 | 完成 | 主要宿主已消费稳定 UI State；详情平台外逻辑已拆分；待读页两个子 Fragment 已移除 |
| 3 Compose 应用壳层 | 完成 | `MainActivity` 使用 Compose 承载主导航 |
| 4 页面 Compose 化 | 完成 | 普通页面 XML、RecyclerView/Adapter、菜单资源已清理；仅保留 RemoteViews 小部件布局 |
| 5A 发布收口 | 进行中 | 旧依赖、Java 17、KSP、Firebase 主模块和本地可修 warning 已完成。`adb devices -l` 仍无设备，小部件、SAF、FileProvider、WebView/Cookie 未复验 |
| 5B 文档与交接 | 完成 | 本文件已建立；README 和详细计划已提供入口及阶段完成证据规则 |

## 已落地的关键边界

- `data/reading`：收藏、已读、待读、历史和收藏夹读写；详情页与列表宿主已接入。
- `data/catalog`：分类分页、标题/全文搜索、单篇查询和上下篇导航。
- `data/tag`：本地 Tag 查询、远程 fallback、计数和单篇 Tag 内容。
- `data/draft`：草稿列表、读取、保存和删除。
- `data/random`：随机批次获取、隐藏已读/缺失正文过滤和有限次数补足；桌面小部件已复用。
- `data/detail`：详情链接归一化、截图标题格式化、离线/在线正文、Tag 和评论访问。
- `data/backup`：备份文件计划、数据库导入导出及 SAF 宿主所需的数据操作。
- `data/direct`：直达文档的本地查询与远程 fallback。
- `data/feed`：最近更新数据访问，Repository 已从 `view` 移出。
- `data/settings`：详情阅读模式、字号、Web 凭据、提示状态和积分写入的语义化边界。
- `data/user`：用户面板统计与最近历史。

对应的增量提交如下：

- `bb667ae`：完成 Compose UI 迁移并建立首批数据边界。
- `bf6d8f5`：集中详情链接解析。
- `979812a`：详情阅读状态接入 `ReadingListRepository`。
- `bacce73`：详情目录查询和上下篇导航接入 `ScpCatalogRepository`。
- `fae8841`：随机过滤迁入 `data/random`，删除 `ScpDataHelper`。
- `92acd74`：固化详情截图标题格式。
- `d17efe8`：将 `DetailRepository` 从 `view/detail` 移入 `data/detail`。
- `f22e58b`：隔离详情偏好访问。
- `8f99274`：隔离应用数据备份访问。
- `c299464`：关闭直达、Feed 等剩余数据直连。
- `84a4775`：收敛剩余全局偏好访问。
- `3016991`、`e9a1bd0`、`932c907`：分别建立目录/搜索、阅读列表/草稿和详情 UI State。
- `f446dc5`：拆分详情 HTML、菜单、Cookie 对话框和阅读积分编排。
- `35e98f6`：移除待读页两个纯过渡子 Fragment。

## 当前验证基线

以下命令已在 2026-11-18 的当前工作树上执行并通过。本机使用 `./gradlew`，与文档里的 `gradlew.bat` 是同一组任务：

```powershell
.\gradlew.bat testDefaultProductionDebugUnitTest
.\gradlew.bat assembleDefaultProductionDebug
.\gradlew.bat assembleDefaultProductionReleaseStaging
.\gradlew.bat assembleDebug
```

- 全量 `assembleDebug` 覆盖全部 debug 渠道，`BUILD SUCCESSFUL`，365 个任务。测试报告为 12 个文件、62 项、0 失败。
- 随后又在处理完赋值语法和源码弃用 API 后重跑了测试、主 debug 和 release staging，`BUILD SUCCESSFUL`（58 秒）。`--warning-mode all` 日志里没有 Gradle 弃用赋值或 Kotlin 弃用 API。
- JVM 测试源集位于 `app/src/testDefaultProductionDebug/java`。使用该定制源集是因为项目的 `Test` product flavor 在不区分大小写的文件系统上会与常规 `app/src/test` 冲突。
- release staging 已覆盖 Kotlin/Java 编译、R8 minify、资源压缩和 APK 打包。
- `adb devices -l` 当前没有连接设备。WebView 在线/离线切换、Cookie 编辑/登录、SAF 备份恢复、FileProvider 分享和桌面小部件仍是阶段 5A 唯一未完成的发布复验。

## 阶段 2 完成摘要

- 阶段 2B：UI/View 层对 `PreferenceUtil`、`APP_SP` 的扫描为 0 命中；对旧数据库/网络单例的扫描仅命中 `importDetailDatabase` 方法名，不存在直接依赖。数据访问已进入 Repository，偏好访问已进入语义化 SettingsStore。
- 阶段 2C：`CategoryListUiState`、`RandomListUiState`、`SearchUiState`、`LaterUiState`、`DraftListUiState`、`DetailUiState` 已由 ViewModel 暴露并由宿主消费。
- `DetailActivity` 保留 WebView、Intent、剪贴板、截图等平台交互；HTML 生成、菜单 ID 映射、Cookie 对话框构建和阅读积分编排已经拆出并获得定向测试/编译保护。
- Fragment 决策：删除只承载 Compose 列表的 `RecordListFragment`、`LikeBoxListFragment`；保留 `HomeFragment`、`LaterFragment` 作为 `MainActivity` 固定 tag/生命周期边界，保留 `ScpListFragment` 作为首页多分类页独立 ViewModel 与恢复边界。
- 没有修改 Room schema、数据库文件名、备份格式、偏好 key、深链路或 flavor 配置。

## 阶段 5A 进度

2026-11-18 已删除确认无调用方的 RxJava2 组：

- `io.reactivex.rxjava2:rxjava`
- `io.reactivex.rxjava2:rxandroid`
- `com.squareup.retrofit2:adapter-rxjava2`
- `androidx.compose.runtime:runtime-rxjava2`
- `ApiService` 中未使用的 `io.reactivex.Observable` import。接口本身已全部是 `suspend`，`HttpManager` 没有注册 Rx CallAdapter。

源码扫描 `io.reactivex` 为 0 命中。`ConstraintLayout` 仍被详情页程序化布局使用，因此保留。

2026-11-18 继续删除确认无调用方的 AndroidX 旧依赖：

- `androidx.cardview:cardview`：源码和布局没有 `CardView`。
- `androidx.legacy:legacy-support-v4`：没有 `android.support` 或 `androidx.legacy` 类引用。Manifest 里的 `android.support.FILE_PROVIDER_PATHS` 是 FileProvider 的元数据名，不依赖这个库。Fragment 由 AppCompat 传递引入。
- `androidx.lifecycle:lifecycle-extensions`：没有 `ViewModelProviders` 或 `ProcessLifecycleOwner`。同时去掉重复声明的 `lifecycle-viewmodel-ktx`。
- 补上代码实际使用的 `lifecycle-livedata-ktx` 与 `lifecycle-runtime-ktx` 2.8.6，对应 `LiveData.map`、`distinctUntilChanged` 和 `lifecycleScope`。

同一工作树重新通过 62 项 JVM 测试、debug APK 和 release staging。

2026-11-18 把剩余本地工具链和依赖收口一次做完，没有再按组拆开：

- Java/Kotlin 目标改为 17。Room 注解处理从 kapt 换成 KSP `2.2.0-2.0.2`。`android.enableJetifier=false`。
- Gradle Groovy 赋值改为 `prop = value`。删除 Bintray 仓库、未使用的 `ext.anko_version` 和 `flatDir`。本地 `app/libs/jcc-bate-0.7.3.jar` 仍通过 `fileTree` 引入。
- Firebase 改为 BOM `33.5.0` 的 `firebase-analytics` 与 `firebase-database` 主模块。调用处改用 `com.google.firebase.Firebase` 和 `database.getValue`。
- 删除 Glide 与其编译器、显式 `vectordrawable`、`useSupportLibrary`，以及 `androidx.multidex`。`minSdk 21` 自带 multidex，`multiDexEnabled` 保留，`ScpApplication` 改为继承 `Application`。图片加载继续用 Coil。
- Room 保持 schema 不变。`fallbackToDestructiveMigration(dropAllTables = true)` 维持原先“删除全部表”的回退行为。`getAllTags()` 改为 `List<String>`，去掉 Room 对可空列表元素的警告。`allowMainThreadQueries()` 未动。
- 替换仍会报警的 API：系统字号锁定改为 `createConfigurationContext`，返回图标改为 `Icons.AutoMirrored.Filled.ArrowBack`，屏幕尺寸改为 `displayMetrics`。API 23 以下的网络检测仍用旧 `NetworkInfo`，并用 `@Suppress("DEPRECATION")` 限定在这个兼容分支。

全渠道 debug 构建里，`libandroidx.graphics.path.so` 无法 strip，按原样打进包。这不影响构建成功。

## 下一步：阶段 5A 设备复验

本地代码和工具链收口已经完成。下一步只剩连上设备后的手动复验：

1. 桌面小部件。
2. SAF 导入、备份和恢复。
3. FileProvider 截图分享。
4. WebView 在线/离线与 Cookie 登录。

本机 `local.properties`、`app/google-services.json` 和 `app/src/main/java/info/free/scp/PrivateConstants.kt` 是本地占位，已在 gitignore 中，不要提交。真机联网或 Firebase 前需要换回真实配置。

## 已知约束与不要破坏的行为

- `res/layout/layout_widget.xml` 服务于 RemoteViews，是明确保留项，不属于普通页面 XML 债务。
- 不改变 Room schema、数据库文件名、备份格式、包名、深链路和 flavor 配置，除非单独立项并补迁移验证。
- Repository 外提期间不要同时重写导航；先稳定数据边界，再做宿主瘦身。
- 当前没有 instrumentation 测试。涉及 WebView、Cookie、SAF、FileProvider 和桌面小部件时，需要补人工验证记录或最小设备测试。
- `view/detail` 已无 `AppInfoDatabase`、`ScpDatabase`、`DetailDatabase`、`HttpManager` 和旧 Helper 直连；不要把这些依赖重新引回 UI 层。

## 接手检查清单

```powershell
git switch compose-refactor
git status --short --branch
git log --oneline -10
rg -n "PreferenceUtil|APP_SP" app/src/main/java/info/free/scp/view app/src/main/java/info/free/scp/ui
.\gradlew.bat testDefaultProductionDebugUnitTest
```

阶段 5A 的设备复验开始前应确认：62 项 JVM 测试通过，且已连接设备。每个阶段的完成证据必须同时包含定向扫描、自动测试/构建结果，以及涉及平台行为时的手动验证结果。原文写“远端 `origin/compose-refactor` 已不存在，不要推送”；2026-11-23 用 `git ls-remote --heads origin` 核对，该分支在远端存在且指向 `57f4e7a`（与本地一致）。是否可以推送以仓库所有者为准（待确认）。
