# SCP Android Kotlin + Compose 现代化重构方案

## 2026-07-09 复盘结论

- 当前阶段判断：第 0 阶段“构建与工具链基线”、第 1 阶段“旧依赖清理”、第 3 阶段“Compose 应用壳层”和第 4 阶段“页面逐步 Compose 化”已经完成；第 2 阶段“架构边界整理”已经启动但尚未完成；第 5 阶段“平台能力与发布收口”尚未完成。
- 当前重构主线已经从“继续替换 XML 页面”切换到“收敛数据/平台边界、补验证安全网、准备发布收口”。普通页面 XML 和 RecyclerView/Adapter 体系已经清空，继续推进的主要收益来自降低旧宿主层的变更半径。
- 最新代码证据：`res/layout + res/menu` 当前仅剩桌面小部件 RemoteViews 所需的 `layout_widget.xml`；代码扫描未发现 ViewBinding import、普通页面 `R.layout` 引用、`RecyclerView`/`BaseAdapter` 残留或 `R.menu` 菜单资源引用。
- 包结构现状：`ui` 包已经包含分类、草稿、feed、首页、待读/收藏、随机、搜索、用户等 Compose Screen；但 `view` 包仍保留大量 Activity/Fragment 宿主和事件编排，且多处仍直接访问 `AppInfoDatabase`、`ScpDatabase`、`DetailDatabase`、`ScpDataHelper`、`PreferenceUtil`、`HttpManager`。
- 数据边界现状：`data/user` 已经落地 `UserDashboardRepository`，是第 2 阶段的有效样板；分类、搜索、详情、待读/收藏、草稿、Tag、随机等模块仍以旧 ViewModel/Activity/Fragment 直接访问数据库或全局单例为主。
- 验证现状：2026-07-09 已重新执行 `.\gradlew.bat assembleDebug --stacktrace` 并通过；当前仓库没有命中的本地单元测试或 instrumentation test 文件，后续重构需要先补 characterization tests 和构建矩阵。
- 工作区现状：当前迁移处于未提交的大批量变更中，`git diff --stat` 显示约 121 个文件发生变化，核心方向是删除 XML/layout/menu/Adapter 并新增 Compose Screen。

## 项目现状评审

- 项目主体已经是 Kotlin，但整体 UI 结构仍然是较老的 `Activity + Fragment + XML` 体系，依赖 ViewBinding、RecyclerView Adapter 和命令式跳转。
- Compose 已经开启，仓库里也有少量 Compose 代码，因此最稳妥的路线不是推倒重写，而是按页面和模块做渐进式迁移。
- 当前项目最先暴露的问题不是业务逻辑，而是构建链路偏旧：原始 Kotlin 与本地 JDK / AGP 组合不兼容，导致在业务代码编译前就会失败。
- 项目体量不小，包含多处核心功能面：`user`、`category`、`feed`、`later`、`detail`、`widget` 等；同时还保留了多产品风味、Firebase、本地数据库、文件备份恢复、桌面小部件等平台能力。
- 需要逐步淘汰的旧依赖与旧 API 包括：Anko、RxJava2、`LocalBroadcastManager`、`startActivityForResult`、旧式存储权限处理，以及只服务于 XML/AppCompat 的主题管线。
- 数据层目前散落在多个 Room 数据库、Helper 与单例调用中。如果不先稳住这些边界，后续 Compose 化会把 UI 和数据耦合原样搬过去，收益会打折。

## 重构原则

- 每一步都保持项目可构建、可回退，不做“改完一大片再统一修”的大爆破。
- 优先按纵向切片推进，一次解决一个相对完整的页面或交互链路，而不是同时改很多零散点。
- 优先在新边界引入 Compose：先壳层、再可复用组件、最后彻底替换 XML 页面。
- 在替换 UI 之前，先把业务逻辑、状态和数据访问从 `Activity` / `Fragment` 中抽离出来。
- 在 Compose 外壳稳定之前，数据库 schema、包名、资源入口、深链路、风味配置尽量保持不变。

## 第 0 阶段：构建与工具链基线

1. 升级 Kotlin 与 Compose Compiler，使其和当前 Android Gradle Plugin、本地 JDK 兼容。
2. 固化一条可重复执行的验证命令，确保每次改动后都能快速确认构建状态。
3. 先修掉阻塞编译的构建问题，再进入业务重构。
4. 保证 `assembleDebug` 可以在全部 debug 风味下稳定通过。

阶段完成标准：完整 debug 构建成功。

## 第 1 阶段：旧依赖清理

1. 用平台 API 或本地 Kotlin 扩展替换 Anko 的日志、Toast、Dialog、`startActivity`、尺寸辅助等能力。
2. 将 `LocalBroadcastManager` 相关主题广播替换为更现代的可观察状态方案，例如 `StateFlow`。
3. 将 `startActivityForResult` 逐步迁移到 Activity Result API。
4. 结合现有 Storage Access Framework 方案，清理过时的外部存储权限请求。

阶段完成标准：Anko 依赖可以被完全移除，项目仍可稳定构建。

## 第 2 阶段：架构边界整理

1. 为 `data`、`domain`、`ui` 建立更清晰的包结构与职责边界。
2. 将 Room、网络、Firebase 等访问收敛到 Repository 层，而不是在界面层直接调用。
3. 以 Repository 为边界，把 RxJava2 API 逐步迁移为挂起函数与 `Flow`。
4. 为首页、分类、详情、搜索、待读/历史、设置等高频页面建立明确的 UI State 模型。

阶段完成标准：主要页面通过 ViewModel 暴露状态，而不是在 UI 层直接拼装数据。

## 第 3 阶段：Compose 应用壳层

1. 将 `MainActivity` 改造成 Compose Host，必要时临时承载旧 Fragment。
2. 用 Compose Material Navigation 替换底部导航的 XML 实现。
3. 建立单一的 Compose Theme 入口，逐步替代旧主题分支。
4. 在迁移期间保持 `HomeFragment`、`LaterFragment`、`UserFragment` 等现有行为可用。

阶段完成标准：主导航壳层 Compose 化，旧页面内容仍能正常运行。

## 第 4 阶段：页面逐步 Compose 化

1. 先迁移低风险页面：关于、设置、站点入口、游戏列表、热门列表等。
2. 将可复用列表项从 XML/RecyclerView 迁移到 Compose Lazy 列表。
3. 等数据边界稳定后，再迁移分类、搜索、待读等中等复杂页面。
4. `DetailActivity` / WebView 相关页面最后处理，因为它的交互最重、兼容面最大。

阶段完成标准：XML 布局数量持续下降，已迁移页面具备可预览的 Composable。

## 第 5 阶段：平台能力与发布收口

1. 复验桌面小部件在 UI 重构后的行为是否正常。
2. 重新检查文件备份恢复、权限、FileProvider 等平台链路在新 Android 版本上的表现。
3. 覆盖主要 debug / release / staging 风味构建。
4. 删除不再使用的 XML、Adapter、旧资源以及废弃依赖。

阶段完成标准：发布构建通过，旧 UI 依赖基本清空。

## 当前执行记录

- 已升级 Kotlin Gradle Plugin、Kotlin Serialization Plugin、Kotlin Stdlib 与 Compose 编译插件，使项目适配 AGP `8.13.0` 与本地 JDK 21。
- 已把 `BuildConfig` 生成配置从废弃的全局 `gradle.properties` 迁移到 `app` 模块 `buildFeatures`。
- 已根据 AGP 8.13 的内存占用特征调整 `gradle.properties`，将 Gradle / Kotlin 堆提升到 `4G`，并限制 worker 并行度，保证完整构建可稳定跑完。
- 已将多处简单 Anko 日志、Toast、WindowManager、背景色、`dip/padding`、`startActivity` 等调用替换为 Android 平台 API 或现有工具方法。
- 已将若干简单交互弹窗迁移到 `AlertDialog.Builder`，包括草稿删除、阅读记录删除、待读建议、历史清空、搜索栏随机入口等。
- 已将搜索结果页中的 `doAsync/uiThread` 迁移到 `lifecycleScope`，避免继续依赖 Anko 线程辅助。
- 已将 `DownloadActivity` 重写为 Activity Result API 版本，改用 `OpenDocument` / `OpenDocumentTree` 处理数据包导入、备份和恢复。
- 已迁移新人引导与隐私确认流程，改为平台 Dialog 与显式 `Intent`。
- 已迁移分类列表底部操作菜单、收藏列表页操作菜单等交互，并在每个切片后重复执行完整构建验证。
- 已迁移收藏夹列表与用户页，移除 Anko 弹窗、跳转和尺寸扩展，并修复多处历史乱码文案。
- 已将 `DetailActivity` 中大部分 Anko 用法替换为平台 API，包括收藏夹弹窗、图片保存线程切换、提示消息、按钮尺寸和标签跳转。
- 已将 `MainActivity` 的旧式外部存储权限申请改为基于 `ActivityResultContracts.RequestMultiplePermissions()` 的分版本处理，仅在 Android 9 及以下保留请求。
- 已移除 `LocalBroadcastManager` 主题广播，改为进程内主题监听回调，并清理 `startActivityForResult` / `onActivityResult` 在主流程中的剩余调用。
- 已移除 `app/build.gradle` 中的 Anko 与 EasyPermissions 依赖，并将图片保存逻辑切换为分版本的 `MediaStore` / SAF 兼容实现。
- 已验证 `.\gradlew.bat assembleDebug --stacktrace` 在当前代码与新工具链上完整成功。
- 第二阶段已启动：新增 `data/user` 与 `ui/user` 边界，落地 `UserDashboardRepository + UserDashboardViewModel + UserDashboardUiState`，并将 `UserFragment` 的阅读/收藏统计与最近历史渲染改为仅消费 ViewModel 状态。
- 第三阶段已启动：`MainActivity` 已改造成 Compose Host，使用 Compose `Scaffold + BottomNavigation` 承载并切换 `HomeFragment`、`LaterFragment`、`UserFragment`，在保留原有页面行为的前提下完成主导航壳层 Compose 化。
- 第三阶段稳定性补丁：修复 Compose 容器与 Fragment 提交时序导致的闪退风险；补齐主导航选中态保存/恢复；在进程重建后通过固定 tag 复用已恢复 Fragment，避免重复实例与切换异常。

## 第三阶段结论

- 第 3 阶段“Compose 应用壳层”已完成。
- 完成依据：
- `MainActivity` 已切换为 Compose Host（`setContent + MainTheme + Scaffold`）。
- 底部导航已由 Compose Material 实现并驱动旧 Fragment 切换。
- 旧页面内容（`HomeFragment`、`LaterFragment`、`UserFragment`）保持可用，且已补齐重建与状态恢复稳定性。
- 完整 `assembleDebug --stacktrace` 构建通过。

## 第一阶段结论

- 第 1 阶段“旧依赖清理”已经完成。
- 完成依据：
- Anko 代码调用与 Gradle 依赖已移除。
- `LocalBroadcastManager` 已退出主题刷新链路。
- 主流程中的 `startActivityForResult` / `onActivityResult` 已迁移到 Activity Result API 或直接跳转。
- 旧式 `EasyPermissions` 已移除，存储权限策略已按 Android 版本收敛。
- 在 AGP `8.13.0`、Kotlin `2.2.0`、Room `2.7.2` 组合下，完整 `assembleDebug` 构建通过。

## 第三阶段后的剩余重点（历史记录）

- 进入第 2 阶段，开始按 `data / domain / ui` 重整包结构和依赖边界。
- 优先把 `Activity` / `Fragment` 里直接访问数据库、Helper、Firebase 的逻辑继续外提到 Repository / ViewModel。
- 持续清理新工具链下暴露的 warning，特别是旧 Java 8 编译参数、废弃 Firebase KTX API、旧 Room Builder 写法等。

## 当前工作方式

- 默认不再频繁停下来确认小决策，而是按“修改一块 -> 完整编译 -> 继续下一块”的节奏持续推进。
- 只有在遇到会影响功能路线、数据结构或发布行为的非显然分叉时，才会主动停下来与你对齐。

## 第四阶段执行记录

- 已补齐 Compose preview/tooling 依赖，支持已迁移页面保留 `@Preview`。
- 已将关于应用、捐赠入口、捐赠二维码页面迁移为 Compose 页面，并移除对应 XML 与版权弹窗 XML。
- 已将最高评分入口的 Activity/Fragment 迁移为 Compose 页面，并移除旧入口 XML。
- 已将最高评分详情列表迁移为 Compose Lazy 列表，`TopRatedListActivity` 不再通过 XML 容器中转 Fragment。
- 已将最近更新页迁移为 Compose Tab + Lazy 列表，移除 `FeedFragment`、`SubFeedFragment`、Feed Adapter 以及对应 XML。
- 已清理传送门与游戏列表早已不再使用的历史 XML/item 资源。
- 已将随机列表页迁移为 Compose Lazy 列表，移除 `RandomAdapter` 与对应 XML/item 资源。
- 已将文档列表页迁移为 Compose Lazy 列表，`DocListActivity` 与 `ScpListFragment` 均改为 Compose 承载，移除 `ScpAdapter`、文档列表 XML/item 以及旧底部操作面板 XML。
- 已将分类入口页迁移为 Compose Lazy 列表，保留分类点击积分与 `DocListActivity` 跳转参数，移除 `CategoryAdapter` 与对应 XML/item 资源。
- 已将草稿列表页迁移为 Compose Lazy 列表，保留打开、删除、帮助入口与新增草稿行为，移除 `DraftAdapter` 与对应 XML/item 资源。
- 已将收藏列表页迁移为 Compose Lazy 列表，保留打开、删除、移动收藏夹与反序排序行为，移除 `LikeListAdapter` 与对应 XML/item 资源。
- 已将收藏夹列表页迁移为 Compose Lazy 列表，保留打开收藏夹、删除并清空、删除并迁移到默认收藏夹、重命名行为，移除 `LikeBoxAdapter` 与对应 XML/item 资源。
- 已将搜索结果页迁移为 Compose Lazy 列表，保留标题搜索、全文搜索离线库检查、下载入口与详情跳转行为，移除 `SearchResultAdapter` 与对应 XML/item 资源。
- 已将待读/历史记录列表迁移为 Compose Lazy 列表，保留详情跳转、长按删除、外层刷新与反序滚动到顶部行为，移除 `RecordListAdapter` 与对应 XML/item 资源。
- 已将阅读历史页壳层迁移为 Compose `Scaffold + CommonTopBar`，保留反序、清空历史确认与详情跳转行为，移除 `activity_history.xml` 与历史页专用菜单资源。
- 已将隐私协议页迁移为 Compose 静态页面，恢复隐私说明文案并移除 `activity_privacy.xml`。
- 已将搜索页壳层迁移为 Compose `Scaffold + TabRow`，保留标题搜索、全文搜索、Tag 跳转与离线文档入口，移除 `SearchResultFragment` 与 `activity_search.xml`。
- 已删除无代码入口且已无法实际承载内容的旧 `ScpListActivity` 空壳，移除对应 Manifest 注册、`activity_category.xml` 与专用菜单资源。
- 已将分类分组入口页迁移为 Compose 双栏列表，保留系列切换、特殊分类直达与 `DocListActivity` 跳转参数，移除 `GroupListActivity` 对 ViewBinding 的依赖、`DocGroupItem` 自定义 View 以及对应 XML。
- 已删除列表迁移后不再有任何使用方的旧 `BaseAdapter`，代码与布局中已无 `RecyclerView` 依赖残留。
- 已清理主壳层旧 XML 与 BottomNavigation 菜单资源，保留显式 `flMainContainer` id 供 Fragment 过渡容器使用。
- 已将编号直达页迁移为 Compose 数字键盘，保留 CN/J 切换、离线/在线直达和详情跳转逻辑，并移除 `activity_direct.xml`。
- 已将草稿编辑页迁移为 Compose 表单，保留打开、编辑、保存和离开自动保存行为，并移除 `activity_draft_edit.xml` 与草稿编辑菜单资源。
- 已将设置页壳层迁移为 Compose `Scaffold + CommonTopBar`，偏好内容暂时复用 `PreferenceFragmentCompat`，移除 `activity_settings.xml`。
- 已将首页内容页迁移为 Compose，保留公告、四个分组入口、最近原创/翻译、最高评分和标签入口，并移除 `fragment_home_page.xml` 与首页 Feed item 自定义 View。
- 已将下载/备份页迁移为 Compose 静态说明与操作按钮，保留 SAF 数据库导入、备份与恢复逻辑，并移除 `activity_download.xml`。
- 已将用户页迁移为 Compose 仪表盘，保留头像选择、昵称/职位设置、入口宫格、捐赠入口、最近历史与统计刷新，并移除用户页 XML、旧 `EntryItem` / `SettingItem` / `HistoryListItem` 自定义 View 及对应 attrs。
- 已将待读/收藏夹主壳层迁移为 Compose 搜索栏、TabRow 与 FAB 导入入口，保留两个列表 Fragment 过渡承载、随机/搜索/直达入口与批量导入待读逻辑，并移除 `fragment_later.xml` 与导入大文本弹窗 XML。
- 已将首页主壳层迁移为 Compose 搜索栏、ScrollableTabRow 与 Fragment 过渡容器，复用新搜索栏并移除旧 `SearchBar` 自定义 View、`fragment_home.xml` 与 `layout_search_bar.xml`。
- 已删除无引用的旧 `layout_top_bar.xml` 与 `TopBar` 自定义属性；桌面小部件 `layout_widget.xml` 因 RemoteViews 仍在使用暂时保留。
- 已将详情页评论项改为程序化 View，保留 `CommentLayout.setData()` / `refreshTheme()` 接口并移除 `layout_comment.xml` 与对应 ViewBinding。
- 已将详情页 Cookie 设置弹窗改为程序化 View，保留网页登录、Cookie/Agent 保存与帮助入口，并移除 `layout_dialog_cookie.xml` 与对应 ViewBinding。
- 已将详情页主布局迁移为程序化 `DetailActivityLayout`，保留 WebView、评论容器、截图分享区域、底部阅读/收藏/翻页按钮、FAB 与垂直 SeekBar 行为，并移除 `activity_detail.xml` 与 `ActivityDetailBinding`。
- 已将详情页菜单改为程序化构建，保留收藏、主题切换、待读、在线/离线、浏览器打开、复制链接、截图分享与 Cookie 设置入口，并移除 `detail_menu.xml`。
- 当前 `res/layout` 中已无 `RecyclerView` 布局残留。
- 当前 `res/layout + res/menu` 仅剩 `layout_widget.xml`，该文件属于桌面小部件 RemoteViews，不能直接由 Compose 替代。
- 当前代码与资源中已无 ViewBinding import、`RecyclerView` / `BaseAdapter` 代码残留、`R.menu` 菜单引用或普通页面 XML 布局残留，`app/build.gradle` 已关闭 DataBinding / ViewBinding 生成。
- 当前验证：2026-07-09 重新执行 `.\gradlew.bat assembleDebug --stacktrace` 通过。

## 第四阶段结论

- 第 4 阶段“页面逐步 Compose 化”已经完成。
- 完成依据：
- 低风险静态页、列表页、中等复杂页面、主导航三大 Fragment 壳层和详情页均已完成 Compose 或程序化 View 迁移。
- 旧 XML / Adapter 列表体系已清空，普通页面布局不再依赖 `res/layout`。
- 仅保留桌面小部件 RemoteViews 所需的 `layout_widget.xml`，作为 Android 平台能力约束下的例外。
- 完整 `assembleDebug --stacktrace` 构建通过。

## 技术债务评估（2026-07-09）

**Mode:** Tech Debt Assessment
**Scope:** 全项目，以当前工作区代码、文档、构建配置和资源残留扫描为准。
**Health Score:** 63/100

总体判断：UI 迁移本身已经走到后半程，当前主要风险来自架构边界、验证缺口和发布链路，而不是页面是否已经 Compose 化。

### Critical

**Dependency Disorder - 数据入口仍以全局单例向 UI 暴露**
Symptom: 多个 Activity/Fragment/ViewModel 直接调用 `AppInfoDatabase.getInstance()`、`ScpDatabase.getInstance()`、`DetailDatabase.getInstance()`、`ScpDataHelper.getInstance()`、`HttpManager.instance`，Room Database 仍使用 `allowMainThreadQueries()`。
Source: Clean Architecture - Dependency Inversion Principle; A Philosophy of Software Design - Information Leakage.
Consequence: 任何数据库、网络、缓存或线程策略调整都会扩散到多个 UI 宿主，Compose 迁移后的页面仍然难以独立测试和替换。
Remedy: 以 Repository 为唯一数据边界，先把分类、搜索、待读/收藏、详情、草稿、Tag、随机模块的直接数据库访问外提，再移除主线程查询。

### Warning

**Cognitive Overload - `DetailActivity` 仍是高复杂度聚合点**
Symptom: `DetailActivity` 接近千行，混合 WebView 生命周期、HTML/CSS/JS 注入、阅读状态、收藏、菜单、Cookie、截图分享、上下篇导航和主题刷新。
Source: Refactoring - Long Method / Large Class; Code Complete - High-Quality Routines.
Consequence: 详情页是最高风险页面，任何 WebView 或阅读体验调整都容易牵连状态、副作用和菜单行为。
Remedy: 先抽出 `DetailRepository` 能承担的数据行为，再拆分阅读状态、菜单动作、Cookie 设置、WebView 内容生成等可测试单元。

**Change Propagation - 过渡期 Compose 壳层仍承载旧 Fragment 编排**
Symptom: `MainActivity`、`HomeFragment`、`LaterFragment` 使用 Compose Host/Tab 与 `AndroidView` 容器承载旧 Fragment，部分子页面仍由 Fragment 切换驱动。
Source: Refactoring - Shotgun Surgery; The Mythical Man-Month - Conceptual Integrity.
Consequence: 导航、生命周期、主题刷新和状态恢复需要同时理解 Compose 与 Fragment 两套模型，后续修改容易在宿主边界产生回归。
Remedy: 保持这套过渡壳层短期稳定，优先把剩余 Fragment 的数据和状态外提；等依赖边界稳定后再决定是否删除 Fragment 过渡容器。

**Knowledge Duplication - 列表状态与待读/已读逻辑重复表达**
Symptom: 文档列表、搜索结果、详情页、待读/收藏页等位置重复计算收藏、已读、待读状态，并重复调用 DAO/Helper 修改记录。
Source: The Pragmatic Programmer - DRY; Refactoring - Duplicate Code.
Consequence: 状态规则变化时需要同步多个页面，容易出现列表图标、详情按钮和记录数据库互相不一致。
Remedy: 建立 `ReadingListRepository` 或同等边界，统一收藏、已读、待读、历史记录的查询和命令接口。

**Accidental Complexity - 旧依赖还没有随 UI 迁移收口**
Symptom: `app/build.gradle` 中仍保留 RxJava2、Retrofit RxJava adapter、`lifecycle-extensions`、`legacy-support-v4`、CardView、ConstraintLayout、AppCompat/Material View 体系、Firebase KTX 等旧依赖。
Source: Software Engineering at Google - Dependency Management; Refactoring - Speculative Generality / Middle Man.
Consequence: 依赖升级、发布体积和废弃 API warning 会继续拖慢后续升级，且不易判断哪些库仍是必要平台能力。
Remedy: 建立依赖保留清单，按模块删除未使用依赖；Firebase KTX 迁移到非 KTX API；确认 AppCompat/Preference/Material View 只因剩余宿主或设置页保留。

### Suggestion

**Change Propagation - 缺少重构安全网**
Symptom: 当前没有命中的 `src/test` 或 `src/androidTest` Kotlin/Java 测试文件，文档主要记录 `assembleDebug` 构建通过。
Source: Working Effectively with Legacy Code - Characterization Tests; How Google Tests Software - Change Coverage.
Consequence: 后续边界重整会改变数据访问路径，仅靠构建无法发现搜索、随机、待读导入、详情状态等行为回归。
Remedy: 先补小而稳定的 characterization tests，覆盖纯 Kotlin 解析/状态逻辑和 Repository 查询包装；再补少量关键 instrumentation smoke checks。

## Debt Summary

| Risk | Findings | Avg Priority | Classification | Intent |
|------|----------|--------------|----------------|--------|
| Cognitive Overload | 1 | 6.0 | Scheduled | accidental |
| Change Propagation | 2 | 6.5 | Scheduled | intentional transitional debt |
| Knowledge Duplication | 1 | 6.0 | Scheduled | accidental |
| Accidental Complexity | 1 | 4.0 | Scheduled | accidental |
| Dependency Disorder | 1 | 9.0 | Critical | accidental |
| Domain Model Distortion | 0 | 0.0 | Monitored | n/a |

**Recommended focus:** 先处理 Dependency Disorder 和 Change Propagation，再处理旧依赖删除；不要先大规模重写 UI 细节。

## 重新制定的后续重构计划

### 阶段 2A：变更安全网与验证矩阵

1. 固化构建验证矩阵。
   - 验收标准：至少记录并执行 `.\gradlew.bat assembleDebug --stacktrace`、`.\gradlew.bat assembleDefaultProductionDebug` 或当前可用的主开发风味命令、一个 release/staging 预检命令。
   - 验证重点：普通 debug 构建、主要 flavor 组合、release shrink/minify 配置不被 UI 资源删除影响。

2. 补 characterization tests。
   - 验收标准：为待读导入解析、随机列表过滤、搜索结果映射、收藏/已读状态聚合、详情 URL/标题处理补最小测试。
   - 验证重点：测试可以在本地 JVM 或最小 instrumentation 环境运行，并成为后续 Repository 外提的保护网。

3. 建立迁移扫描清单。
   - 验收标准：文档化允许残留的 `R.layout.layout_widget`、设置页 `PreferenceFragmentCompat`、详情页程序化 View，并列出禁止新增的 ViewBinding/RecyclerView/XML 页面入口。
   - 验证重点：每次阶段结束用 `rg` 扫描 ViewBinding、RecyclerView、`R.layout`、`R.menu`、直接数据库单例访问。

### 阶段 2B：Repository 边界收敛

4. 收敛阅读状态边界。
   - 验收标准：收藏、已读、待读、历史记录查询和修改统一进入一个 Repository；`DocListActivity`、`ScpListFragment`、`DetailActivity`、`LaterViewModel` 不再直接拼 DAO/Helper 调用。
   - 依赖：阶段 2A 的状态类测试。

5. 收敛分类与搜索边界。
   - 验收标准：`GroupViewModel`、`SearchViewModel` 不直接持有 Room DAO；分类列表、标题搜索、全文搜索、Tag 搜索通过 Repository 暴露结果。
   - 依赖：阶段 2A 的搜索与列表测试。

6. 收敛详情页数据边界。
   - 验收标准：`DetailActivity` 不再直接读写 `AppInfoDatabase`、`ScpDatabase`、`PreferenceUtil` 的核心阅读状态；详情加载、评论、Tag、历史写入、待读转换由 `DetailRepository` 或拆分后的子 Repository 承担。
   - 依赖：阅读状态 Repository 完成。

7. 收敛草稿、下载/备份与用户配置边界。
   - 验收标准：草稿、备份恢复、用户统计、昵称/职位/偏好设置形成明确 Repository 或 SettingsStore；UI 宿主只发意图和消费状态。
   - 依赖：先保留 SAF/FileProvider 行为，不在此阶段改存储协议。

### 阶段 2C：状态模型与宿主瘦身

8. 为仍在 `view` 包里的主要宿主补 UI State。
   - 验收标准：分类、搜索、待读/收藏、草稿、详情页至少通过 ViewModel 暴露稳定 UI State；Composable 不直接查询数据库或全局偏好。
   - 验证重点：配置变更、进程重建、主题切换后状态仍可恢复。

9. 拆分 `DetailActivity`。
   - 验收标准：菜单动作、Cookie 设置、阅读状态、WebView 内容生成从 Activity 内拆成可测试类或小函数；Activity 保留生命周期和平台交互。
   - 验证重点：离线/在线模式、上下篇、收藏、待读、截图分享、Cookie 设置行为不变。

10. 决定 Fragment 过渡容器的去留。
    - 验收标准：在 Repository 和 UI State 稳定后，评估 `HomeFragment`、`LaterFragment`、`ScpListFragment`、`RecordListFragment`、`LikeBoxListFragment` 是否继续作为宿主，或迁移为纯 Compose 导航。
    - 验证重点：不要在数据边界未完成前同时重写导航和数据流。

## 阶段 2A/2B 执行记录（2026-07-09）

- 2A 已启动并完成第一批 characterization tests。
- 新增 `ReadListImportParserTest`：覆盖待读导入的逗号/分号/空白分隔、CN/J 类型识别、编号补零和无数字 token 忽略。
- 新增 `ReadingListRepositoryTest`：覆盖文档阅读状态聚合、已读切换、待读切换、确保加入待读、记录列表读取、默认收藏夹创建。
- 新增 `ReadListImportParser`，将 `LaterFragment` 中的待读导入字符串解析抽为纯 Kotlin 逻辑。
- 新增 `ReadingListRepository + ReadingListDataSource + RoomReadingListDataSource`，先把收藏、已读、待读、历史记录和收藏夹列表的读写边界集中到 `data/reading`。
- 新增 `ScpCatalogRepositoryTest`：覆盖图书馆聚合、分类分页、隐藏已读过滤、标题搜索和全文搜索 link 回填。
- 新增 `ScpCatalogRepository + ScpCatalogDataSource + RoomScpCatalogDataSource`，把分类列表、标题搜索、全文搜索与隐藏已读设置集中到 `data/catalog`。
- 新增 `TagRepositoryTest`：覆盖本地 tag link 分批查询、远程 fallback、本地 tag 计数、远程 tag 计数和单篇 tag 文本读取。
- 新增 `TagRepository + TagDataSource + RoomTagDataSource`，把离线 tag 查询、线上 tag API fallback 和 tag 文本读取集中到 `data/tag`。
- 新增 `DraftRepositoryTest`：覆盖新建草稿默认内容、读取已有草稿、保存更新时间戳与删除草稿。
- 新增 `DraftRepository + DraftDataSource + RoomDraftDataSource`，把草稿列表、读取、保存和删除集中到 `data/draft`。
- 已接入首批页面/宿主：`DocListActivity`、`ScpListFragment`、`LaterViewModel`、`LaterFragment` 的待读导入写入。
- 已接入分类/搜索边界：`GroupViewModel`、`SearchViewModel` 不再直接持有 `AppInfoDatabase`、`ScpDatabase`、`DetailDatabase` 或 `PreferenceUtil.getHideFinished()`。
- 已接入 Tag 边界：`TagViewModel` 与 `TagResultScreen` 不再直接访问 `ScpDatabase`、`DetailDatabase`、`HttpManager` 或 `apiCall`，相关访问仅保留在 `data/tag`。
- 已接入草稿边界：`DraftEditActivity`、`DraftListActivity`、`DraftListViewModel` 不再直接访问 `AppInfoDatabase` 或 `draftDao`，旧 `viewmodel/DraftRepository` 已删除。
- 分类分页修正：Repository 分页现在按每页最多 100 条返回，并保留整百边界和末页最后一条，避免旧 `subList(... size - 1)` 逻辑丢项。
- Tag 边界修正：本地 tag 查询现在按 10 条一组完整取回所有 link，不再漏掉最后一条；本地 tag 计数从 1 开始累计，避免旧逻辑把首次出现记为 0。
- 定向扫描结果：阅读相关宿主不再直接调用 `AppInfoDatabase.getInstance()` 或 `ScpDataHelper.getInstance()`；`GroupViewModel`、`SearchViewModel` 不再直接调用 `ScpDatabase.getInstance()`、`DetailDatabase.getInstance()` 或 `PreferenceUtil.getHideFinished()`；`view/tag` 不再直接调用 `ScpDatabase.getInstance()`、`DetailDatabase.getInstance()`、`HttpManager.instance` 或 `apiCall`；`view/draft` 与 `DraftListViewModel` 不再直接调用 `AppInfoDatabase.getInstance()` 或 `draftDao`；全局 ViewBinding / RecyclerView / `R.menu` 扫描仍无命中。
- 测试源集说明：由于项目存在名为 `Test` 的 product flavor，Windows 文件系统下 `app/src/test` 会与 `app/src/Test` flavor 源集冲突；当前 JVM 测试放在 `app/src/testDefaultProductionDebug/java`，先绑定主开发验证变体，后续如需跨 flavor 复用再单独整理 Gradle sourceSet。
- 当前验证：
  - `.\gradlew.bat testDefaultProductionDebugUnitTest --tests info.free.scp.data.catalog.* --tests info.free.scp.data.draft.* --tests info.free.scp.data.reading.* --tests info.free.scp.data.tag.* --rerun-tasks` 通过。
  - `.\gradlew.bat assembleDebug --stacktrace` 通过。
- 2A 尚未完成：仍需补构建矩阵中的 release/staging 预检命令，并继续增加随机、详情 URL/标题处理等 characterization tests。
- 2B 尚未完成：分类、标题/全文搜索、Tag 与草稿首批边界已落地；详情、下载/备份等仍有直接数据库/网络单例访问，需要按模块继续外提 Repository。
- 2C 尚未启动：详情页拆分、剩余宿主 UI State 化、Fragment 过渡容器去留决策应在 2B 边界进一步稳定后推进。

## 阶段 2A/2B 继续执行记录（2026-07-10）

- 已继续收敛阅读状态边界：收藏列表的查询、删除与移动，收藏夹的清空/迁移/重命名，以及阅读历史清空均统一进入 `ReadingListRepository`；`LikeListActivity`、`LikeBoxListFragment` 与 `LaterAndHistoryActivity` 不再直接访问 `AppInfoDatabase` 或 DAO。
- `ReadingListRepositoryTest` 已补收藏夹清空、迁移、重命名、历史清空、打开文章转历史、收藏/取消收藏和创建收藏夹测试；阅读域 Repository 测试当前共 16 项并通过。
- 新增 `data/random/RandomRepository + RandomDataSource`，将随机列表的数据入口与过滤规则从 `view/category` 和旧 Helper 移出；`GroupViewModel` 仅持有列表状态并编排刷新/追加。
- `RandomRepositoryTest` 已扩展为 5 项，覆盖刷新替换、追加与范围透传，以及隐藏已读、过滤缺失离线正文和过滤后补足批次。随机抽取算法已迁入 `data/random`，补足循环在空批次、数据不足或最多 20 次尝试时停止，不再存在旧 Helper 的跨请求计数与无限循环风险。
- 桌面小部件已改为复用 `RandomRepository`；`ScpDataHelper` 的随机、历史与待读辅助方法均已无调用方，文件已删除。
- 新增 `data/detail/DetailLinkResolver` 与 5 项 characterization tests，固化本地文档链接补全、绝对链接保留、站点/file 链接归一化和 API path 提取规则；`DetailActivity` 与旧 `DetailRepository` 已统一复用该纯 Kotlin 边界，不再各自硬编码截断长度或 `substring(1)`。
- 已将详情页的打开历史写入、待读移除/加入、已读切换、收藏/取消收藏和收藏夹创建接入 `ReadingListRepository`；`DetailActivity`、`DetailViewModel` 与旧 `DetailRepository` 不再直接访问 `AppInfoDatabase`、`ScpDataHelper`、`likeAndReadDao` 或 `readRecordDao`。
- 已扩展 `ScpCatalogRepository` 的单篇查询与上下篇导航接口，并补 3 项测试；`DetailActivity` 与旧 `DetailRepository` 不再直接持有 `ScpDatabase/scpDao`，站内链接解析和普通顺序阅读均通过目录 Repository。
- 迁移扫描结果：`view/later`、`LaterAndHistoryActivity` 与 `view/detail` 已无阅读数据库/Helper 或 `ScpDatabase` 直连；旧 `view/category/RandomRepository` 与全局 `ScpDataHelper` 已删除。详情页仍直接访问偏好和网络内容边界，属于下一批 2B 工作。
- 2026-07-10 验证矩阵：
  - `.\gradlew.bat testDefaultProductionDebugUnitTest --tests 'info.free.scp.data.*'` 通过。
  - `.\gradlew.bat assembleDefaultProductionDebug --stacktrace` 通过。
  - `.\gradlew.bat assembleDefaultProductionReleaseStaging --stacktrace` 通过，已覆盖 R8 minify 与资源压缩链路。
- 发布预检暴露但本切片未处理的 warning：`flatDir` 仓库、Java 8 编译目标、Firebase KTX、旧 Room migration/builder API，以及少量 Android/Compose 废弃 API；这些继续归入阶段 5A 的工具链 warning 清单，不与当前 Repository 重构混改。
- 当前阶段判断不变：第 0、1、3、4 阶段完成；2A 的主构建矩阵、详情 URL 与随机内部过滤测试已补齐，仅详情标题处理测试仍缺；2B 的分类、搜索、Tag、草稿、随机、阅读状态与详情目录导航边界已收敛，详情网络内容/偏好、下载/备份与部分设置/首页平台入口仍待处理；2C 尚未启动。

### 阶段 5A：旧依赖与发布收口

11. 删除未使用旧依赖。
    - 验收标准：确认并删除 RxJava2、Retrofit RxJava adapter、`lifecycle-extensions`、`legacy-support-v4`、CardView、ConstraintLayout、AppCompat/Material View 等不再需要的依赖；保留项必须写明原因。
    - 验证重点：删除一组依赖就跑一次构建矩阵，避免一次性删除导致定位困难。

12. 处理新工具链 warning。
    - 验收标准：Firebase KTX 废弃 API、Java 8 编译目标、旧 Room Builder 写法、过时仓库源等 warning 形成清单并逐步清理。
    - 验证重点：不要在同一个切片里同时升级大量第三方库和改业务代码。

13. 发布前平台能力复验。
    - 验收标准：桌面小部件 RemoteViews、SAF 导入/备份/恢复、FileProvider 截图分享、WebView 安全浏览、各渠道 flavor 构建、release shrink/minify 均复验通过。
    - 验证重点：`layout_widget.xml` 是明确保留例外，不作为普通页面 XML 债务处理。

### 阶段 5B：文档与交接

14. 更新 README 和重构方案索引。
    - 验收标准：README 明确当前版本、重构阶段、验证命令和详细计划入口；旧历史 TODO 标记为历史记录。

15. 建立阶段性完成标准。
    - 验收标准：每个后续阶段都以“扫描结果 + 构建结果 + 关键路径手动验证/测试结果”作为完成证据，不再只记录“已迁移某页面”。
