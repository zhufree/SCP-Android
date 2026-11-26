# 架构概览

应用是单模块（`:app`）Android 工程，包名 `info.free.scp`。界面使用 Kotlin、Jetpack Compose（Material 3）和 Activity/Fragment 宿主，阅读页使用 WebView。

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

`data/` 下的子包

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

`view` 与 `ui` 通过 Repository 访问数据，通过 SettingsStore 访问偏好。三个 Room 数据库都启用了 `allowMainThreadQueries()`。

## 2. 导航

应用没有统一的 `NavHost`，导航由三层组成：

1. **主壳层**：`MainActivity` 使用 Compose `Scaffold` 和 `NavigationBar`，三个标签 `HOME`（首页）、`LATER`（待读）、`USER`（我的）分别对应三个主 Fragment。Fragment 通过固定 tag（`main.fragment.home/later/user`）添加到 `AndroidView` 提供的 `flMainContainer`，切换时显示或隐藏。选中标签通过 `onSaveInstanceState` 保存。
2. **首页二级标签**：`HomeFragment` 的顶区是 `HomeTopBar`（绿色容器：搜索胶囊 + `PrimaryScrollableTabRow`，状态栏区域由 Activity 窗口背景画成同色），切换 6 个子页，所选标签用 `rememberSaveable` 在 Activity 重建后保留，替换到 `fl_home_container`：首页（`HomePageFragment`）、图书馆（`ScpListFragment`，`LIBRARY_DOC`）、SCP 国际版（`CategoryFragment`，`INTERNATIONAL_DOC`）、GOI 格式（`CategoryFragment`，`GOI_DOC`）、艺术作品（`CategoryFragment`，`ART_DOC`）、背景资料与指导（`ScpListFragment`，`INFORMATION_DOC`）。
3. **页面跳转**：其余页面都是独立 Activity，用显式 `Intent` + extras 启动，在 `AndroidManifest.xml` 中注册。例如 `DetailActivity` 读取 extras `link`、`title`、`index`、`scp_type`、`read_type`、`random_index`；`RandomActivity` 读取 `random_type`、`random_title`。

`navigation-compose` 仅在 `TagDetailActivity` 内部使用（一个 `NavHost`，起点 `"tag"`）。

Manifest 将 `MainActivity` 声明为 LAUNCHER，并注册了 `FileProvider`（`${applicationId}.fileprovider`）和桌面小部件 `ScpWidgetProvider`。部分 Activity（如 `AboutAppActivity`、`SettingsActivity`、`DraftEditActivity`）以类名为 intent action。

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

`UpdateManager.checkAppData()` 和“设置 → 检查数据更新”共享 `DataUpdates`。常规联网检查间隔为 24 小时，设置页可读取上次有效清单。如果本地已有清单但无法解析，下次联网检查会立即重新拉取，包括启动时的自动检查。目录库在不计费网络自动下载，计费网络需要用户确认。正文只在点击时下载，默认仅 Wi-Fi，可在设置中关闭。下载进度与待冷启动安装状态通过 `StateFlow` 显示。

正文合并使用独立 SQLite 连接，以块文件为主库，`ATTACH` 本地正文库后执行一个写事务。失败时自动改为游标分批复制，行上限约 2 MB。分块合并与整库 SAF 导入使用同一互斥锁。

运行期用共享的 `normalizeLink` 处理链接键，包括目录与正文查询、搜索结果打开和用户状态读写。用户库版本为 8，7→8 使用独立的固定规则更新历史、待读、收藏和已读键，并合并同一文章的用户状态。SAF 恢复在暂存副本上执行同一升级链，冷启动验证新库后提交替换。Activity 重建和暂存恢复期间继续使用进程内的用户库及草稿订阅。具体规则见 [数据库表结构说明](database.md)。

### 后端接口（`service/ApiService.kt`）

`PreferenceUtil.getApiUrl()` 依次选择有效的 Firebase `config/api_url` 缓存、`PrivateConstants.FEED_API_URL` 和公开 Bridge `https://api.zhufree.fun/`。`HttpManager` 在每次请求时读取有效地址，地址变化后才重建 Retrofit 服务，所有 Feed 请求共用同一个 OkHttp 客户端。

| 路径 | 用途 |
| --- | --- |
| `/latest_index/` | 首页最近原创/翻译 |
| `/latest_cn/{page}`、`/latest_translated/{page}` | 最近原创、最近翻译 |
| `/top_rated_all/{page}`、`/top_rated_scp/{page}`、`/top_rated_tale/{page}`、`/top_rated_goi`、`/top_rated_wanderers/{page}` | 最高评分各分类 |
| `/get_detail/{link}`、`/get_scp_tag/{link}` | 在线正文、Tag |
| `/get_direct/{scpType}/{numberString}` | 在线编号直达 |
| `/get_tag_scp/{tag}`、`/get_all_tags/` | Tag 检索与 Tag 云 |

### 一次“打开文档”的典型流程

1. 列表 Screen 点击条目 → 宿主发 `Intent`（`link`/`title`/`index`/`scp_type`…）启动 `DetailActivity`。
2. `DetailActivity` 将 Intent 或站内跳转的链接规范化为文档键，网页导航仍使用原始地址。`DetailViewModel` 通过 `DetailRepository` → `ScpCatalogRepository` 取目录条目；`ReadingListRepository` 写入历史并读取收藏/已读状态。
3. `loadDetail(link)` 按模式走离线 `DetailDao` 或在线 `/get_detail`；结果汇入 `DetailUiState`（`document`、`content`、`tag`、`comments`、`isOfflineReadingEnabled`）。
4. `DetailActivity` 观察状态，调用 `DetailHtmlComposer` 生成 HTML，写入 WebView；收藏/已读/待读/上下篇等操作回到 `DetailViewModel`，并同时增加阅读积分。

## 4. 主题

`ScpApplication` 初始化 `ThemeUtil`，从偏好读取应用内的日夜选择。用户在「我的」切换主题时，`MainActivity.refreshTheme()` 向三个主 Fragment 传播，Compose 通过 `themeVersion` 触发重组。应用主题独立于系统深色模式。

日夜色值统一定义在 `res/values/colors.xml` 的 `md_day_*` 和 `md_night_*` 中。[配色表](build-and-release.md#配色表)

- **View 侧**：`DayTheme` 和 `NightTheme` 分别继承 `Theme.Material3.Light.NoActionBar` 和 `Theme.Material3.Dark.NoActionBar`，提供 Material 3 角色和 `ThemeUtil` 使用的自定义属性。Manifest 的默认 `AppTheme` 继承 `DayTheme`。
- **Compose 侧**：`MainTheme` 按 `ThemeUtil.currentTheme` 选择 `lightColorScheme` 或 `darkColorScheme`，两套色彩都从资源读取。新增页面使用 `MaterialTheme.colorScheme`。形状定义在 `ui/Shape.kt`。
- `Scaffold` 的内容应使用其传入的 padding。固定高度的卡片内可用 `Row.fillMaxHeight()` 实现垂直居中。
- 标题栏使用 `toolbarBg` 和 `onToolbar`。程序化 Toolbar 使用 `AppTheme.AppBarOverlay`，详情页把星标和溢出菜单放在 Toolbar 内的 `ComposeView` 中。
- `targetSdk 35+` 的应用在 Android 15+ 使用 edge-to-edge。`view/base/ActivityInsets.kt` 的 `keepContentInsideSystemBars()` 把内容放到系统栏内侧，并用窗口背景给状态栏区域画 `toolbarBg`、其余区域画 `containerBg`；`refreshSystemBars()` 负责状态栏/导航栏图标深浅，并在主题切换时刷新。新增 Activity 若不继承 `BaseActivity`，需要自己调用 `keepContentInsideSystemBars()`。
- 启动主题为 `Theme.App.Splash`。Android 12+ 使用系统 SplashScreen 属性，API 21–30 使用 `windowBackground`。`BaseActivity.onCreate` 通过 `ThemeUtil.setTheme` 应用日夜主题。
- Manifest 启用预测性返回。标题栏返回箭头调用 `finish()`，自定义返回逻辑使用 `OnBackPressedCallback`。
- 详情页 WebView 的夜间效果：离线正文由 `DetailHtmlComposer` 注入夜间 CSS 和 `darkreader.min.js`；在线网页在 `onPageStarted` 中注入 DarkReader；“文章不存在”等消息页在 `showArticleMessage` 里按 `ThemeUtil.containerBg`/`darkText` 生成 CSS。

## 5. 桌面小部件

`ScpWidgetProvider` 与 `ScpRemoteView` 使用 `res/layout/layout_widget.xml` 渲染 RemoteViews，随机条目复用 `RandomRepository`。`widget_info.xml` 中的更新周期为 24 小时。

## 6. 测试

JVM 单元测试位于 `app/src/testDefaultProductionDebug/java`，覆盖 Repository、解析器、详情菜单动作、链接规范化和 v8 合并规则。

## 7. 首页、设置与个人页

三页通过 `MaterialTheme.colorScheme` 或 `ThemeUtil` 取色。

**首页**（`view/home/HomeFragment` + `HomePageFragment`）

- `HomeTopBar` 包含搜索框、编号直达、随机菜单和六个标签。随机类型下标对应 `RandomActivity` 的 `random_type`。
- `HomePageScreen` 显示公告、分类入口、最近原创与翻译、最高评分和全部标签。公告可展开或关闭。
- `FeedListViewModel.feedIndexState` 提供加载、内容、空和失败状态。分区内的重试调用 `loadFeedIndex(force = true)`，刷新失败时保留已有内容。ViewModel 归 `HomeFragment` 持有，切换子页时复用。整页列表请求通过 `apiCall` 用 Toast 提示失败。

**设置页**（`view/user/SettingsActivity` + `ui/settings/*`）

- `SettingsActivity` 继承 `BaseActivity`，通过 Compose `SettingsScreen` 展示设置。
- `SettingsUpdateModel.kt` 从 `DataUpdateState` 派生状态横幅、分块行状态和已装计数。「检查数据更新」在缓存目录需要计费网络确认时调用 `UpdateManager.confirmCatalogDownload`，否则调用 `check(manual = true)`。

### 阅读偏好

除 Wi-Fi 下载开关外，以下偏好存放在 `read_settings`。

| 键 | 类型 | 说明 |
| --- | --- | --- |
| `hide_finished_article` | Boolean，默认 false | 隐藏已读文档，由 `ScpCatalogRepository` 和 `RandomRepository` 读取 |
| `detail_text_size` | String，默认 `"16px"` | `DetailHtmlComposer` 将字号写入正文 CSS。设置页提供 12/14/16/18/20 五档，其他值按最接近的档位显示 |
| `detail_hanz_type` | String，`"0"` 简体、`"1"` 繁体 | `readHanzType()` 兼容 String 和 Int 读取，写入时用 String。繁体正文由 `JChineseConvertor.s2t` 转换，转换失败时保留原文 |
| Wi-Fi 下载开关 | Boolean，默认 true | 存放在 `data_updates`，由 `DataUpdates.wifiOnly` 管理 |

**“我的”页**（`UserFragment` 与 `UserDashboardScreen`）

- 标题栏提供日夜切换。档案卡显示代号、编号、职务、已读和收藏数。
- 编号首次读取时随机生成（0–599），写入 `level` 的 `user_code` 后复用。
- 页面入口按四列布局，由 `entryRowSpans()` 计算每项跨度。

## 8. 弹窗、菜单与提示

Compose 页面使用 `ui/common/Dialogs.kt` 中的 Material 3 组件，View 宿主使用 `util/ScpDialogs.kt`。日夜主题由 `ThemeUtil.currentTheme` 决定。

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
- `util/ScpDialogs.kt`：提供 `showMessage`、`showChoice`、`showActionSheet`、`showInput` 和 `snackbar`。自定义内容可使用 `inputField` 与 `withDialogPadding`。
- `ui/detail/DetailToolbarActions.kt`：详情页将星标和溢出菜单嵌入 Toolbar 的 `ComposeView`。菜单中的主题和阅读模式项显示切换的目标状态，动作映射定义在 `DetailMenuAction`。

**View 主题**：`DayTheme` 和 `NightTheme` 为对话框、底部面板、Snackbar 和弹出菜单设置主题属性。Compose 的 Snackbar 反色角色与 View 侧使用同一组 `md_*_inverse*` 颜色。

**主题与窗口**

- `BaseActivity.refreshTheme()` 在运行时应用当前主题，使后台栈里的 Activity 也能更新。
- `ScpDialogs.themed(context)` 按当前日夜设置包装 Activity context。弹窗需要 Activity 的窗口 token。
- Compose 的 `AlertDialog`、`ModalBottomSheet`、`DropdownMenu` 应在 `MainTheme` 内调用。
