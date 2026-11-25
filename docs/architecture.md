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
| `feed` | `FeedRepository`、`FeedIndexState` | 最近更新/最高评分（仅在线接口）；首页用 `FeedIndexUiState`（每个区块 Loading/Content/Empty/Failed）区分加载中、成功、空、失败 |
| `update` | `DataUpdates`、`UpdateDatabases`、`DetailPartImporter` | Release 检查、校验、冷启动安装、正文事务合并 |
| `backup` | `AppDataBackupRepository`、`BackupFilePlan` | 详情库导入、个人数据备份/恢复（SAF） |
| `settings` | `DetailSettingsStore`、`PreferenceSettingsStore`、`ReadSettingValues` | 对 `PreferenceUtil` 的语义化封装；`ReadSettingValues.kt` 放阅读设置的键、字号档位和繁简/编号的兼容读写（纯函数，有 JVM 测试） |
| `user` | `UserDashboardRepository` | “我的”页面统计与最近历史 |

约定（来自 `refactor-handoff.md`）：`view` 与 `ui` 不应再直接访问 `AppInfoDatabase`/`ScpDatabase`/`DetailDatabase`/`HttpManager`/`PreferenceUtil`，需经 Repository 或 SettingsStore。

> 注意：所有 Room 数据库都启用了 `allowMainThreadQueries()`，Repository 内的数据库访问并不保证在后台线程。

## 2. 导航

应用没有统一的 `NavHost`，导航由三层组成：

1. **主壳层**：`MainActivity` 使用 Compose `Scaffold + BottomNavigation`，三个标签 `HOME`（首页）、`LATER`（待读）、`USER`（我的），分别对应 `HomeFragment`、`LaterFragment`、`UserFragment`。Fragment 通过固定 tag（`main.fragment.home/later/user`）add/show/hide 到 Compose `AndroidView` 提供的 `flMainContainer`，选中标签通过 `onSaveInstanceState` 保存。
2. **首页二级标签**：`HomeFragment` 的顶区是 `HomeTopBar`（绿色容器：搜索胶囊 + `PrimaryScrollableTabRow`，状态栏区域由 Activity 窗口背景画成同色），切换 6 个子页，所选标签用 `rememberSaveable` 在 Activity 重建后保留，替换到 `fl_home_container`：首页（`HomePageFragment`）、图书馆（`ScpListFragment`，`LIBRARY_DOC`）、SCP 国际版（`CategoryFragment`，`INTERNATIONAL_DOC`）、GOI 格式（`CategoryFragment`，`GOI_DOC`）、艺术作品（`CategoryFragment`，`ART_DOC`）、背景资料与指导（`ScpListFragment`，`INFORMATION_DOC`）。
3. **页面跳转**：其余页面都是独立 Activity，用显式 `Intent` + extras 启动，在 `AndroidManifest.xml` 中注册。例如 `DetailActivity` 读取 extras `link`、`title`、`index`、`scp_type`、`read_type`、`random_index`；`RandomActivity` 读取 `random_type`、`random_title`。

`navigation-compose` 仅在 `TagDetailActivity` 内部使用（一个 `NavHost`，起点 `"tag"`）。

Manifest 中仅 `MainActivity` 是 LAUNCHER；部分 Activity（`AboutAppActivity`、`SettingsActivity`、`DraftEditActivity` 等）声明了以其类名为 action 的 intent-filter，未发现 http(s) scheme 的 `<data>` 深链。另声明了 `FileProvider`（`${applicationId}.fileprovider`）和桌面小部件 `ScpWidgetProvider`。

### 主要页面入口

| 入口 | 页面 |
| --- | --- |
| 首页 | 公告、分类入口（`GroupListActivity` → `DocListActivity`）、最近原创/翻译（“查看更多”→`FeedActivity`）、最高评分（`TopRatedActivity`）、Tag（`TagCloudActivity`） |
| 搜索栏 | 胶囊内右侧两个图标：编号直达（`DirectActivity`）、随机（下拉 4 项 → `RandomActivity`）；回车 → 标题/全文搜索（`SearchActivity`） |
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

`PreferenceUtil.getApiUrl()` 依次选择有效的 Firebase `config/api_url` 缓存、`PrivateConstants.FEED_API_URL` 和公开 Bridge `https://api.zhufree.fun/`。`HttpManager` 在每次请求时读取有效地址，地址变化后才重建 Retrofit 服务，所有 Feed 请求共用同一个 OkHttp 客户端。

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
- 标题栏（`CommonTopBar` 和 `DetailActivityLayout` 的 Toolbar）日夜都是深色底，文字和图标用 `ThemeUtil.onToolbar`；程序化构建的 Toolbar 必须放在 `AppTheme.AppBarOverlay` 主题下（`DetailActivityLayout` 用 `ContextThemeWrapper` 包裹 `AppBarLayout`），详情页标题栏右侧的星标和溢出菜单不再用 Toolbar 菜单，改为嵌入 Toolbar 的 `ComposeView`（见第 9 节）。
- 设置页是 Compose（见第 8 节），开关用 Material 3 `Switch`，不再使用 Preference 与 `MaterialSwitch` 布局资源。
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

## 8. 首页 / 设置页 / “我的”页结构与偏好键

按 `scp-design` 设计稿（2026-11-25）重做。目录页和分类列表页没有改。三页都只用 `MaterialTheme.colorScheme` 或 `ThemeUtil` 取色，不写硬编码色值。

**首页**（`view/home/HomeFragment` + `HomePageFragment`）

- 顶区 `ui/home/HomeTopBar`：`toolbarBg` 底，里面是 `SearchBarScreen`（48dp 胶囊，`BasicTextField`，回车 `ImeAction.Search`）和 6 个 Tab。直达（拨号图标）、随机（交叉箭头图标，点开 `DropdownMenu`：随机全部/SCP/故事/搞笑）各 48dp，带 `contentDescription` 和长按提示。随机类型下标与 `RandomActivity` 的 `random_type` 一致，埋点事件不变。
- `ui/home/HomePageScreen`：公告条（默认折叠 2 行，点击展开；关闭键独立；关闭后用 Snackbar 提示“公告已隐藏，可在app使用说明中查看”，语义同旧 Toast）、4 个入口、“最近原创/最近翻译”标题行 + 描边列表、底部并排“最高评分页面/全部标签”。
- 4 个入口继续用原位图（`scp_logo`、`img_logo_cn`、`img_break_logo`、`wander_logo`）。图是黑/灰线条的，所以放在浅色底衬上（日间 `primaryContainer`、夜间 `onPrimaryContainer`）；放逐者图标是白线条，放在深绿底衬上（日间 `primary`、夜间 `primaryContainer`）。
- 最近原创/翻译的状态来自 `FeedListViewModel.feedIndexState`（`StateFlow<FeedIndexUiState>`）：请求中显示 3 行骨架；有数据显示列表；接口返回空显示“暂无内容”；失败显示区块内错误态（“网络请求出错” + “重试”），不再弹 Toast。“重试”调用 `loadFeedIndex(force = true)` 真正重新请求；已有内容时刷新失败保留旧内容。`FeedListViewModel` 挂在 `HomeFragment` 上（`requireParentFragment()`），切换 Tab 再回来不会重复请求。
- `FeedRepository.fetchFeedIndex()` 不弹 Toast；`loadFeedList`（最近更新等整页列表）仍沿用 `apiCall` 的 Toast，没有改。

**设置页**（`view/user/SettingsActivity` + `ui/settings/*`）

- `SettingsActivity` 仍是 `BaseActivity`，入口不变；内容是 Compose `SettingsScreen`，不再有 `PreferenceFragmentCompat`、`res/xml/pref_read_settings.xml`、`res/values/array.xml`、`preference_widget_material_switch.xml`（`androidx.preference` 依赖仍在 `app/build.gradle`，已无代码使用，是否移除待确认）。
- `SettingsUpdateModel.kt` 是从 `DataUpdateState` 派生展示状态的纯函数（状态横幅、分块行状态、可点击条件、已装计数），逻辑照搬旧 `renderUpdates`。“检查数据更新”的点击逻辑与旧版一致：`canConfirmCachedCatalog()` 为真走 `UpdateManager.confirmCatalogDownload`（计费网络确认弹窗），否则 `check(manual = true)`。`DataUpdateState` 新增 `failed`（`reportFailure` 置位，`status()`/开始新操作时清除），用于“失败色 + 重试”。
- 计费网络的目录下载确认仍由 `UpdateManager.checkAppData()` 注册在 `MainActivity` 上，设置页没有改动。

**偏好键**（`SharedPreferences` 文件 `read_settings`，键与类型沿用旧 Preference 时代，升级用户的值直接可读）

| 键 | 类型 | 说明 |
| --- | --- | --- |
| `hide_finished_article` | Boolean，缺省 false | 隐藏已读文档。`ScpCatalogRepository`、`RandomRepository` 读取。Compose 设置页点击时显式写入 `PreferenceUtil.setHideFinished`（以前由 `SwitchPreferenceCompat` 自动落盘） |
| `detail_text_size` | String，如 `"16px"`，缺省 `"16px"` | 正文字号，`DetailActivity` 经 `DetailHtmlComposer` 拼进 CSS。设置页是 12/14/16/18/20 五档滑块，旧值不在档位内时取最近档位显示 |
| `detail_hanz_type` | String `"0"` 简体 / `"1"` 繁体 | 旧 `DropDownPreference` 存的是字符串，而旧 `PreferenceUtil.getHanzType()` 用 `getInt`，且全工程没有调用点。现在 `readHanzType()` 按实际类型读取（String/Int 都接受，缺省按简体），仍以字符串写入。`DetailActivity.setDetail` 在选“繁体”时用 `JChineseConvertor.s2t` 转换正文（转换库读取失败则保持原文） |
| `detail_wifi_only` | 不在 `read_settings` 中 | 在 `data_updates` 文件里（`DataUpdates.wifiOnly`，缺省 true），设置页开关直接读写它，与旧版一致 |

“我的”页的 `level` 文件新增 `user_code`（Int，0–599）：“编号”首次读取时随机生成并保存，之后保持不变（以前每次重组都 `Random(System.currentTimeMillis())`，编号会跳变）。

**“我的”页**（`view/user/UserFragment` + `ui/user/UserDashboardScreen`）

- 绿色 `TopAppBar`（标题“我的” + 日夜切换键，日间显示月亮、夜间显示太阳），回调仍是 `ThemeUtil.changeTheme`，Compose 立即重组；状态栏图标深浅由 `ActivityInsets.refreshSystemBars` 处理（标题栏日夜都是深色，图标始终为浅色）。
- 档案卡：抬头条（“控制 收容 保护”+“机密人员”）、头像（点击更换，右下相机角标）、代号/编号/职务三行（代号、职务带铅笔，点击行为不变：代号 → `checkUserInfo()`，职务 → `checkJob()`；编号只读、等宽字）、已读/收藏两格。夜间默认头像放在浅色底衬上。
- 入口网格：4 列，`entryRowSpans()` 算跨度（7 项时最后一项跨 2 列，“其他作品”显示的 8 项为 4×2）。
- 赞助入口保留原大图 `bg_donation`，压到 120dp，叠半透明遮罩，中间放一个纯色胶囊（作者头像 + “支持开发者” + 箭头）保证文字清晰。
- 浏览历史：行高 64dp，标题单行省略，时间等宽字；无记录时显示“暂无浏览历史”。

## 9. 弹窗 / 菜单 / 提示组件约定

全应用统一用 Material 3 组件，日夜只由 `ThemeUtil.currentTheme` 决定。View 与 Compose 各有一处封装，新增弹窗请先用它们，不要再直接 `AlertDialog.Builder`（平台版）、`Toast`（Activity 内）或自己拼 `EditText`。

**选型规则**

| 场景 | 组件 |
| --- | --- |
| 确认一个决定（含危险操作） | 对话框。删除/清空/覆盖类用错误色确认键（Compose：`ScpConfirmDialog(destructive = true)`） |
| 文字输入 | 对话框 + 描边输入框 + 浮动标签（`ScpInputDialog` / `ScpDialogs.showInput`）；`allowBlank = false` 时空输入禁用确认键并提示 |
| 必须选一项的短列表（≤ 6 项，如职业、了解程度） | M3 简单对话框（`ScpDialogs.showChoice`） |
| 从列表里选一个去执行（选收藏夹、选下载线路、长按条目的操作） | 底部面板（Compose `ScpActionSheet` = `ModalBottomSheet`；View `ScpDialogs.showActionSheet` = `BottomSheetDialog`）；点选即关闭。长标题（文章名）作为面板标题，最多两行 |
| 锚定在按钮旁的短菜单（随机类型、文章页更多） | `ScpDropdownMenu` + `ScpMenuItem`（12dp 圆角、`surfaceContainerHigh`、前置图标、可勾选项在行尾显示对勾），分组用 `HorizontalDivider` |
| 操作后的轻量反馈 | `ScpDialogs.snackbar(activity, msg)`；Compose 页面用 `SnackbarHostState`。页面马上要关闭、或跨应用跳转时（草稿保存失败、捐赠跳转提示）继续用 `Toast` |

**文件**

- `ui/common/Dialogs.kt`：`ScpConfirmDialog`、`ScpInputDialog`、`ScpActionSheet`（`SheetAction` 可带图标、`destructive`）、`ScpDropdownMenu`、`ScpMenuItem`。
- `util/ScpDialogs.kt`：`builder`（`MaterialAlertDialogBuilder`）、`showMessage`、`showChoice`、`showActionSheet`、`showInput`、`inputField`/`withDialogPadding`（自定义内容对话框，见 `DetailCookieDialog`）、`snackbar`。
- `ui/detail/DetailToolbarActions.kt`：详情页标题栏的星标 + 溢出菜单。`DetailActivity` 把它放进 Toolbar 右侧的 `ComposeView`；`onlineMode`、收藏状态是 Compose 状态，日夜项显示切换后的模式、阅读模式项显示切换目标。菜单项顺序与旧版一致，`DetailMenuAction` 及其 id 映射保留（有单元测试）。

**主题（遗留调用自动跟随）**：`DayTheme`/`NightTheme` 都设置了 `materialAlertDialogTheme`/`alertDialogTheme`（`ThemeOverlay.Scp.MaterialAlertDialog`）、`bottomSheetDialogTheme`、`snackbarStyle`、`popupMenuStyle`/`actionOverflowMenuStyle`（12dp 圆角背景 `drawable/scp_popup_menu_background`）和 `colorSurfaceInverse`/`colorOnSurfaceInverse`/`colorPrimaryInverse`（Snackbar 配色；Compose 的 `inverse*` 与之同源，色值 `md_*_inverse*`）。

**主题陷阱**

- 没有 `values-night`，日夜靠 `Activity.setTheme` 运行时切换，而 `changeTheme` 只对触发切换的那个 Activity 调用。`BaseActivity.refreshTheme()` 现在会再调一次 `ThemeUtil.setTheme(this)`，让后台栈里的 Activity 也换到当前主题。
- `ScpDialogs.themed(context)` 每次都按 `ThemeUtil.currentTheme` 给传入的 context 套一层 `DayTheme`/`NightTheme`，所以 View 弹窗不依赖 Activity 的 theme 是否已更新。传入的必须是 Activity（弹窗需要窗口 token），不要传 `ScpApplication.context`；`Application.setTheme` 只在冷启动时调用一次，不会随运行时切换更新。
- Compose 的 `AlertDialog`/`ModalBottomSheet`/`DropdownMenu` 是独立窗口，但共享组合的 `MaterialTheme`，必须在 `MainTheme` 内调用。

**没有用到的旧组件**：代码中没有 `ProgressDialog`、`PopupMenu`、`DialogFragment`、`res/menu`；备份/恢复/导入期间目前没有进度提示（待确认是否需要）。
