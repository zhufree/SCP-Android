# 架构概览

> 依据 `compose-refactor` 分支（提交 `57f4e7a`）的代码整理，核对日期 2026-11-23。重构的阶段背景与历史见 [`refactor-handoff.md`](refactor-handoff.md) 和 [`kotlin-compose-modernization-plan.md`](kotlin-compose-modernization-plan.md)。标注“待确认”的内容在代码中无法核实。

应用是单模块（`:app`）Android 工程，包名 `info.free.scp`，Kotlin + Jetpack Compose（Material 2），仍保留少量 Fragment/Activity 宿主和程序化 View（详情页 WebView）。

## 1. 分层与包结构

源码位于 `app/src/main/java/info/free/scp/`（另有顶层文件 `app/src/main/java/Ext.kt`，提供 `apiCall`、`executeResponse`、`toast`、`info` 等无包名的辅助函数）。

| 层 | 包/文件 | 职责 |
| --- | --- | --- |
| 应用入口 | `ScpApplication`、`MainActivity`、`ScpWidgetProvider`、`SCPConstants`、`PrivateConstants`（gitignore） | Application 先安装待生效数据库，再初始化 Firebase 引用与主题；`MainActivity` 是 Compose 主壳层；桌面小部件；全局常量 |
| 宿主/视图 | `view/*`（`home`、`category`、`detail`、`later`、`feed`、`random`、`search`、`tag`、`draft`、`user`、`base`） | Activity/Fragment 宿主：Intent、生命周期、WebView、剪贴板、SAF 等平台交互，持有 ViewModel 并消费其状态 |
| ViewModel | `view/*/*ViewModel.kt`、`viewmodel/` | 暴露 `LiveData`/`StateFlow` 形式的 UI State（如 `DetailUiState`、`CategoryListUiState`、`SearchUiState`、`LaterUiState`、`DraftListUiState`、`UserDashboardUiState`） |
| Compose UI | `ui/*`、`components/CommonTopBar.kt`、`ui/Theme.kt` 等 | 各页面的 `@Composable` Screen 和主题（`MainTheme`） |
| 数据层 | `data/*` | Repository + DataSource 接口（Room 实现），是 UI 访问数据的唯一边界 |
| 持久化 | `db/*`、`bean/*` | 三个 Room 数据库、DAO、实体和网络 DTO |
| 网络 | `service/*` | Retrofit `ApiService` + `HttpManager`；`HtmlParserService`（jsoup 抓取评论） |
| 工具 | `util/*` | `PreferenceUtil`、`FileUtil`、`ThemeUtil`、`UpdateManager`、`NewbieManager`、`DownloadUtil` 等 |

`data/` 下的子包（均在代码中存在）：

| 子包 | 主要类 | 职责 |
| --- | --- | --- |
| `reading` | `ReadingListRepository`、`ReadListImportParser` | 收藏、收藏夹、已读、待读、历史记录；待读导入文本解析 |
| `catalog` | `ScpCatalogRepository` | 按分类分页取目录、标题/全文搜索、单篇与上下篇查询、隐藏已读过滤 |
| `detail` | `DetailRepository`、`DetailLinkResolver`、`DetailHtmlComposer`、`DetailTitleFormatter` | 离线/在线正文、Tag、评论；链接归一化；详情 HTML 拼装；截图标题格式化 |
| `tag` | `TagRepository` | 本地 Tag 查询、远程接口 fallback、Tag 计数 |
| `draft` | `DraftRepository` | 草稿增删改查 |
| `random` | `RandomRepository` | 随机批次获取与过滤（隐藏已读、缺失离线正文），桌面小部件复用 |
| `direct` | `DirectDocumentRepository` | 编号直达：离线查本地库，在线走 `/get_direct` |
| `feed` | `FeedRepository` | 最近更新/最高评分（仅在线接口） |
| `update` | `DataUpdates`、`UpdateDatabases`、`DetailPartImporter` | Release 检查、校验、冷启动安装、正文事务合并 |
| `backup` | `AppDataBackupRepository`、`BackupFilePlan` | 详情库导入、个人数据备份/恢复（SAF） |
| `settings` | `DetailSettingsStore`、`PreferenceSettingsStore` | 对 `PreferenceUtil` 的语义化封装 |
| `user` | `UserDashboardRepository` | “我的”页面统计与最近历史 |

约定（来自 `refactor-handoff.md`）：`view` 与 `ui` 不应再直接访问 `AppInfoDatabase`/`ScpDatabase`/`DetailDatabase`/`HttpManager`/`PreferenceUtil`，需经 Repository 或 SettingsStore。

> 注意：所有 Room 数据库都启用了 `allowMainThreadQueries()`，Repository 内的数据库访问并不保证在后台线程。

## 2. 导航

应用没有统一的 `NavHost`，导航由三层组成：

1. **主壳层**：`MainActivity` 使用 Compose `Scaffold + BottomNavigation`，三个标签 `HOME`（首页）、`LATER`（待读）、`USER`（我的），分别对应 `HomeFragment`、`LaterFragment`、`UserFragment`。Fragment 通过固定 tag（`main.fragment.home/later/user`）add/show/hide 到 Compose `AndroidView` 提供的 `flMainContainer`，选中标签通过 `onSaveInstanceState` 保存。
2. **首页二级标签**：`HomeFragment` 用 Compose `ScrollableTabRow` 切换 6 个子页，替换到 `fl_home_container`：首页（`HomePageFragment`）、图书馆（`ScpListFragment`，`LIBRARY_DOC`）、SCP 国际版（`CategoryFragment`，`INTERNATIONAL_DOC`）、GOI 格式（`CategoryFragment`，`GOI_DOC`）、艺术作品（`CategoryFragment`，`ART_DOC`）、背景资料与指导（`ScpListFragment`，`INFORMATION_DOC`）。
3. **页面跳转**：其余页面都是独立 Activity，用显式 `Intent` + extras 启动，在 `AndroidManifest.xml` 中注册。例如 `DetailActivity` 读取 extras `link`、`title`、`index`、`scp_type`、`read_type`、`random_index`；`RandomActivity` 读取 `random_type`、`random_title`。

`navigation-compose` 仅在 `TagDetailActivity` 内部使用（一个 `NavHost`，起点 `"tag"`）。

Manifest 中仅 `MainActivity` 是 LAUNCHER；部分 Activity（`AboutAppActivity`、`SettingsActivity`、`DraftEditActivity` 等）声明了以其类名为 action 的 intent-filter，未发现 http(s) scheme 的 `<data>` 深链。另声明了 `FileProvider`（`${applicationId}.fileprovider`）和桌面小部件 `ScpWidgetProvider`。

### 主要页面入口

| 入口 | 页面 |
| --- | --- |
| 首页 | 公告、分类入口（`GroupListActivity` → `DocListActivity`）、最近更新（`FeedActivity`）、最高评分（`TopRatedActivity`）、Tag（`TagCloudActivity`） |
| 搜索栏 | 编号直达（`DirectActivity`）、随机（`RandomActivity`）、标题/全文搜索（`SearchActivity`） |
| 待读 | 待读列表、收藏夹（`LikeListActivity`）、待读导入 |
| 我的 | 草稿（`DraftListActivity`）、游戏（`GameListActivity`）、传送门（`PortalListActivity`）、设置（`SettingsActivity`）、离线与备份（`DownloadActivity`）、关于、其他作品、捐赠、历史（`LaterAndHistoryActivity`） |
| 阅读 | `DetailActivity`（WebView 阅读页） |

## 3. 数据来源与数据流

```
                 ┌──────────── 在线模式 ─────────────┐
 UI (Compose) → ViewModel → Repository ─┤ HttpManager → Retrofit ApiService  → 后端接口
                                        │            └→ HtmlParserService → scp-wiki-cn.wikidot.com（评论）
                                        └──────────── 本地 ─────────────┐
                                          ScpDatabase    目录（内置 / Release）
                                          DetailDatabase 正文（分块 / 整库导入，离线模式）
                                          AppInfoDatabase 用户数据
                                          SharedPreferences 设置/积分/Cookie
```

- **目录**：`ScpDatabase` 在冷启动中安装较新的内置库或已校验的 Release 暂存文件。Room 开库并读取 `scps` 失败时隔离旧库，从 assets 重装。目录可读后才向分类、搜索、上下篇、编号直达和随机入口提供 DAO。分类列表保留目录行，搜索和随机按规范链接保留第一条。
- **正文**：成功安装整库或分块后启用离线优先。按文章键读取正文、Tag 和 `not_found`。本地缺页或读取出错时，联网调用 `GET /get_detail/{link}`、`GET /get_scp_tag/{link}`，必要时加载原网页。无网络和 404 均有明确提示。换篇清空 WebView、Tag 和评论状态，并取消旧篇请求。全文搜索和本地 Tag 检索覆盖已安装正文。
  - `DetailActivity` 把正文交给 `DetailHtmlComposer` 拼上 `assets` 内的 `style.css`、`jquery-ui.min.js`、`init.combined.js`、`tabview-min.js`、`WIKIDOT.combined.js`（夜间模式再加 `darkreader.min.js`），用 `loadDataWithBaseURL("file:///android_asset/", …)` 渲染。
- **评论**：始终在线。`HtmlParserService` 用 OkHttp 请求 `https://scp-wiki-cn.wikidot.com/<页面>`，找到讨论链接后用 jsoup 解析帖子；请求带用户在详情页 Cookie 对话框中保存的 `Cookie` 与 `User-Agent`（`PreferenceUtil.getCookie/getAgent`）。
- **最近更新 / 最高评分 / 首页 Feed**：仅在线，走后端接口（见下表）。离线数据库不包含这些页面（`download_notice` 文案）。
- **用户数据**：`AppInfoDatabase`（`scp_info.db`）是进程级单例，保存收藏、收藏夹、已读、待读/历史、草稿。积分、昵称、职位等在 SharedPreferences（`level`、`app`、`init` 等文件）。
- **远程配置**：`ScpApplication.database` 指向 Firebase Realtime Database（地址来自 `PrivateConstants.FIREBASE_URL`）。
  - `UpdateManager.getConfig()` 读 `config` 节点：`app_version`（与 `BuildConfig.VERSION_CODE` 比较，较大则弹更新对话框）、`update_desc`、`update_link`、`notice`、`api_url`、`query_link`。
  - `FireViewModel` 读 `games`、`portals` 节点（游戏列表、站点传送门）。

`UpdateManager.checkAppData()` 和“设置 → 检查数据更新”共享 `DataUpdates`。有网络时最多每 24 小时检查一次 manifest，设置页可读取上次有效清单。目录库在不计费网络自动下载，计费网络需要用户确认。正文只在点击时下载，默认仅 Wi-Fi，可在设置中关闭。下载进度与待冷启动安装状态通过 `StateFlow` 显示。

正文合并使用独立 SQLite 连接，以块文件为主库，`ATTACH` 本地正文库后执行一个写事务。失败时自动改为游标分批复制，行上限约 2 MB。分块合并与整库 SAF 导入使用同一互斥锁。Android SQLite 的 WAL、`ATTACH`、游标回退和 Room 读取行为待设备验证。

运行期用共享的 `normalizeLink` 处理链接键，包括目录与正文查询、搜索结果打开和用户状态读写。用户库版本为 8，7→8 使用独立的固定规则更新历史、待读、收藏和已读键，并合并同一文章的用户状态。SAF 恢复在暂存副本上执行同一升级链，冷启动验证新库后提交替换。Activity 重建和暂存恢复期间继续使用进程内的用户库及草稿订阅。具体规则见 [数据库表结构说明](database.md)。

### 后端接口（`service/ApiService.kt`）

基地址：`PreferenceUtil.getApiUrl()`，即 Firebase `config/api_url` 缓存值，为空时回退 `PrivateConstants.FEED_API_URL`。`HttpManager` 是单例，Retrofit 在构造时读取一次基地址，因此新地址在下次进程启动才会生效。

| 路径 | 用途 |
| --- | --- |
| `/latest_index/` | 首页最近原创/翻译 |
| `/latest_cn/{page}`、`/latest_translated/{page}` | 最近原创、最近翻译 |
| `/top_rated_all/{page}`、`/top_rated_scp/{page}`、`/top_rated_tale/{page}`、`/top_rated_goi`、`/top_rated_wanderers/{page}` | 最高评分各分类 |
| `/get_detail/{link}`、`/get_scp_tag/{link}` | 在线正文、Tag |
| `/get_random/{typeRange}` | 随机（`HttpManager.getRandom` 已定义，但本次核查未发现调用方，待确认是否仍使用） |
| `/get_direct/{scpType}/{numberString}` | 在线编号直达 |
| `/get_tag_scp/{tag}`、`/get_all_tags/` | Tag 检索与 Tag 云 |

后端服务的实现不在本仓库内（待确认其所在仓库）。

### 一次“打开文档”的典型流程

1. 列表 Screen 点击条目 → 宿主发 `Intent`（`link`/`title`/`index`/`scp_type`…）启动 `DetailActivity`。
2. `DetailActivity` 将 Intent 或站内跳转的链接规范化为文档键，网页导航仍使用原始地址。`DetailViewModel` 通过 `DetailRepository` → `ScpCatalogRepository` 取目录条目；`ReadingListRepository` 写入历史并读取收藏/已读状态。
3. `loadDetail(link)` 按模式走离线 `DetailDao` 或在线 `/get_detail`；结果汇入 `DetailUiState`（`document`、`content`、`tag`、`comments`、`isOfflineReadingEnabled`）。
4. `DetailActivity` 观察状态，调用 `DetailHtmlComposer` 生成 HTML，写入 WebView；收藏/已读/待读/上下篇等操作回到 `DetailViewModel`，并同时增加阅读积分。

## 4. 主题

`ThemeUtil` 维护白天/夜间两套颜色（`DAY_THEME=0`/`NIGHT_THEME=1`）并保存在偏好中；`ScpApplication.onCreate` 初始化；`BaseActivity`/`BaseFragment.refreshTheme()` 在切换时由 `MainActivity.refreshTheme()` 向三个主 Fragment 传播，Compose 侧通过 `themeVersion` 触发重组。**应用内的日夜选择（“我的”页月亮按钮）是唯一来源**：`MainTheme` 不再读取 `isSystemInDarkTheme()`，系统深色模式不会联动应用；系统“强制深色”也用 `android:forceDarkAllowed=false` 关闭。

配色为 Material 3 色彩角色，以启动图标（黑底荧光绿 + 白色标志）为依据：主色深绿 `#1B5E3A`，夜间用柔和亮绿 `#81C995`，夜间背景 `#0E1512`。色值只在 `res/values/colors.xml` 的 `md_day_*` / `md_night_*` 写一次，色值表和对比度见 `docs/build-and-release.md` 第 10 节。

- **View 侧**：`DayTheme` 继承 `Theme.Material3.Light.NoActionBar`，`NightTheme` 继承 `Theme.Material3.Dark.NoActionBar`（都不是 DayNight 基类，所以不跟随系统 uiMode，也不需要 `values-night`）。两个 style 逐项设置 M3 角色属性（`colorPrimary`、`colorSurfaceContainer*`、`colorOutline`、`colorError*` 等）和 `ThemeUtil` 读取的自定义属性（`toolbarBg`、`onToolbar`、`containerBg`、`itemBoxBg`、`accentColor`、`darkText` 等）。`AppTheme` 等同 `DayTheme`，是 Manifest 里的应用默认主题。
- **Compose 侧**：`MainTheme`（`ui/Theme.kt`）按 `ThemeUtil.currentTheme` 选 `lightColorScheme`/`darkColorScheme`，值由 `colorResource(R.color.md_*)` 取得，与 View 侧同源；组件全部是 `androidx.compose.material3`（已移除 Material 2 依赖）。形状见 `ui/Shape.kt`（小 8dp、中 12dp、大 16dp），卡片统一 12dp 圆角、1dp 阴影。新增 Compose 页面优先用 `MaterialTheme.colorScheme.*`；沿用的 `Color(ThemeUtil.xxx)` 读取是快照状态，切换主题后自动重组，不要缓存成普通变量。
- **M3 Scaffold 注意**：M3 的 `Scaffold` 内容区从屏幕顶端开始，必须使用 lambda 传入的 `padding`，否则内容会被标题栏盖住（M2 不需要）。M3 `Card` 的内容是 `Column`，固定高度的条目里要给 `Row` 加 `fillMaxHeight()` 才能垂直居中。
- 标题栏（`CommonTopBar` 和 `DetailActivityLayout` 的 Toolbar）日夜都是深色底，文字和图标用 `ThemeUtil.onToolbar`；程序化构建的 Toolbar 必须放在 `AppTheme.AppBarOverlay` 主题下（`DetailActivityLayout` 用 `ContextThemeWrapper` 包裹 `AppBarLayout`），弹出菜单用 `ToolbarPopupTheme`（颜色取自当前 Activity 的 ThemeUtil 属性）。
- 设置页 Preference：开关换成 Material 3 `MaterialSwitch`（`SwitchPreferenceCompat` + `layout/preference_widget_material_switch.xml`），分类标题颜色来自 `colorAccent`。
- `targetSdk 35+` 在 Android 15+ 强制 edge-to-edge，`window.statusBarColor` 不再生效。`view/base/ActivityInsets.kt` 的 `keepContentInsideSystemBars()` 把内容放到系统栏内侧，并用窗口背景给状态栏区域画 `toolbarBg`、其余区域画 `containerBg`；`refreshSystemBars()` 负责状态栏/导航栏图标深浅，并在主题切换时刷新。新增 Activity 若不继承 `BaseActivity`，需要自己调用 `keepContentInsideSystemBars()`。
- 启动页：`MainActivity` 的 Manifest 主题是 `Theme.App.Splash`（继承 `DayTheme`）。Android 12+ 用系统 SplashScreen 属性（`values-v31`：近黑底 + 白色标志），更低版本用 `windowBackground`；进入 `BaseActivity.onCreate` 后 `ThemeUtil.setTheme` 换成日/夜主题，所以 `ThemeUtil` 仍是唯一切换点。没有使用 `androidx.core:core-splashscreen`（见 build-and-release 第 10 节）。
- 预测性返回：Manifest `<application android:enableOnBackInvokedCallback="true">`。代码里没有重写 `onBackPressed`/`OnBackPressedCallback`/`KEYCODE_BACK`，标题栏返回箭头直接调用 `finish()`；以后新增自定义返回逻辑请用 `OnBackPressedCallback`，不要重写 `onBackPressed`。
- 详情页 WebView 的夜间效果：离线正文由 `DetailHtmlComposer` 注入夜间 CSS 和 `darkreader.min.js`；在线网页在 `onPageStarted` 中注入 DarkReader；“文章不存在”等消息页在 `showArticleMessage` 里按 `ThemeUtil.containerBg`/`darkText` 生成 CSS。

## 5. 桌面小部件

`ScpWidgetProvider` + `ScpRemoteView` 使用 RemoteViews（`res/layout/layout_widget.xml` 是仓库里唯一保留的 XML 布局），随机条目复用 `RandomRepository`。`widget_info.xml` 中 `updatePeriodMillis=86400000`。

## 6. 测试

JVM 单元测试位于 `app/src/testDefaultProductionDebug/java`，覆盖 `data/*` 各 Repository/解析器、`DetailMenuAction`、链接规范化及 v8 合并规则。没有 instrumentation 测试。WebView/Cookie、SAF、FileProvider 与桌面小部件尚未在真机上复验（见 `refactor-handoff.md`）。

## 7. 已知的技术债（代码中可见）

- 全部 Room 数据库使用主线程查询；`DetailActivity` 仍是体量较大的宿主。
- `ScpTable.kt` 保留了旧版手写 SQL 常量（旧 `ScpTable`/`DetailTable` 建表语句）；`AppInfoDatabase` 的 4→5 迁移引用了其中的常量，其余部分是否仍被使用待确认。
- `EventUtil.onEvent` 的实现被注释掉（友盟调用已移除），目前是空操作；`firebase-analytics` 依赖在源码中未发现 `FirebaseAnalytics` 调用。
- `TabFragmentPager` 在源码中未发现引用；它依赖的 `androidx.viewpager2` 未在 `app/build.gradle` 中显式声明（来自传递依赖）。
