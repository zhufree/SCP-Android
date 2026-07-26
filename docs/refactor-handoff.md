# Kotlin + Compose 重构交接

- 最后核对：2026-07-26
- 工作分支：`compose-refactor`
- 阶段 2 代码锚点：`35e98f6`（`refactor: remove later child fragments`）

本文件是继续重构时的快速入口。完整背景、阶段定义和历史执行记录见
[`kotlin-compose-modernization-plan.md`](kotlin-compose-modernization-plan.md)。

## 当前结论

重构已经完成 UI 迁移主线和阶段 2 的数据/状态/宿主收敛。第 0、1、2A、2B、2C、3、4、5B 阶段完成；下一主线是阶段 5A 的旧依赖、工具链 warning 与平台能力发布复验。

| 阶段 | 状态 | 当前证据 |
| --- | --- | --- |
| 0 构建与工具链基线 | 完成 | 主 debug 与 release staging 可构建 |
| 1 旧依赖首批清理 | 完成 | Anko、EasyPermissions 等已移除 |
| 2A 安全网与验证矩阵 | 完成 | 12 个测试文件、62 项 JVM 测试；debug/release staging 构建通过 |
| 2B Repository 边界 | 完成 | 阅读、目录、搜索、Tag、草稿、随机、详情、备份、直达、Feed 与语义化设置边界均已收敛 |
| 2C 状态模型与宿主瘦身 | 完成 | 主要宿主已消费稳定 UI State；详情平台外逻辑已拆分；待读页两个子 Fragment 已移除 |
| 3 Compose 应用壳层 | 完成 | `MainActivity` 使用 Compose 承载主导航 |
| 4 页面 Compose 化 | 完成 | 普通页面 XML、RecyclerView/Adapter、菜单资源已清理；仅保留 RemoteViews 小部件布局 |
| 5A 发布收口 | 未开始 | 工具链 warning 和平台能力手动复验仍待处理 |
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

以下命令已在 2026-07-26、阶段 2 最终工作树上重新执行并通过：

```powershell
.\gradlew.bat testDefaultProductionDebugUnitTest
.\gradlew.bat assembleDefaultProductionDebug --stacktrace
.\gradlew.bat assembleDefaultProductionReleaseStaging --stacktrace
```

- JVM 测试源集位于 `app/src/testDefaultProductionDebug/java`，当前有 12 个测试文件、62 个 `@Test`。
- 使用该定制源集是因为项目的 `Test` product flavor 在 Windows 不区分大小写的文件系统上会与常规 `app/src/test` 冲突。
- release staging 已覆盖 Kotlin/Java 编译、R8 minify、资源压缩和 APK 打包。
- 当前构建仍报告 `flatDir`、Java 8 source/target、Room kapt 参数和 Gradle 9 兼容性等 warning；它们属于阶段 5A，不应与 Repository 切片混改。
- `adb devices -l` 当前没有连接设备；WebView 在线/离线切换、Cookie 编辑/登录、SAF 备份恢复、FileProvider 分享和桌面小部件统一列入阶段 5A 的设备级发布复验，不作为阶段 2 的数据/状态代码收敛项。

## 阶段 2 完成摘要

- 阶段 2B：UI/View 层对 `PreferenceUtil`、`APP_SP` 的扫描为 0 命中；对旧数据库/网络单例的扫描仅命中 `importDetailDatabase` 方法名，不存在直接依赖。数据访问已进入 Repository，偏好访问已进入语义化 SettingsStore。
- 阶段 2C：`CategoryListUiState`、`RandomListUiState`、`SearchUiState`、`LaterUiState`、`DraftListUiState`、`DetailUiState` 已由 ViewModel 暴露并由宿主消费。
- `DetailActivity` 保留 WebView、Intent、剪贴板、截图等平台交互；HTML 生成、菜单 ID 映射、Cookie 对话框构建和阅读积分编排已经拆出并获得定向测试/编译保护。
- Fragment 决策：删除只承载 Compose 列表的 `RecordListFragment`、`LikeBoxListFragment`；保留 `HomeFragment`、`LaterFragment` 作为 `MainActivity` 固定 tag/生命周期边界，保留 `ScpListFragment` 作为首页多分类页独立 ViewModel 与恢复边界。
- 没有修改 Room schema、数据库文件名、备份格式、偏好 key、深链路或 flavor 配置。

## 下一步：阶段 5A 发布收口

阶段 2 已全部完成。下一阶段按“小批依赖清理 → warning 清单与修复 → 设备级平台路径复验”的顺序推进。

建议验收标准：

1. 每次只删除一组已确认无调用方的旧依赖，并执行完整构建矩阵。
2. 处理 `flatDir`、Java 8 source/target、Room kapt 参数和 Gradle 9 兼容性 warning；大版本升级单独切片。
3. 连接设备后手动复验桌面小部件、SAF 导入/备份/恢复、FileProvider 截图分享、WebView 在线/离线与 Cookie 登录。
4. 补各渠道 flavor 验证并保留 release shrink/minify 证据。

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

开始阶段 5A 前应确认：已理解分支相对 `origin/compose-refactor` 的预期差异、工作区干净、62 项 JVM 测试通过。每个阶段的完成证据必须同时包含定向扫描、自动测试/构建结果，以及涉及平台行为时的手动验证结果。
