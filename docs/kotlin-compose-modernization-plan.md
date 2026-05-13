# SCP Android Kotlin + Compose 现代化重构方案

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

## 当前剩余重点

- 进入第 2 阶段，开始按 `data / domain / ui` 重整包结构和依赖边界。
- 优先把 `Activity` / `Fragment` 里直接访问数据库、Helper、Firebase 的逻辑继续外提到 Repository / ViewModel。
- 持续清理新工具链下暴露的 warning，特别是旧 Java 8 编译参数、废弃 Firebase KTX API、旧 Room Builder 写法等。

## 当前工作方式

- 默认不再频繁停下来确认小决策，而是按“修改一块 -> 完整编译 -> 继续下一块”的节奏持续推进。
- 只有在遇到会影响功能路线、数据结构或发布行为的非显然分叉时，才会主动停下来与你对齐。
