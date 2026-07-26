# Kotlin + Compose 重构交接

- 最后核对：2026-07-26
- 工作分支：`compose-refactor`
- 代码锚点：`d17efe8fb7e281ccf7457290dbd036f74f3fb5b8`（`refactor: move detail content repository to data layer`）

本文件是继续重构时的快速入口。完整背景、阶段定义和历史执行记录见
[`kotlin-compose-modernization-plan.md`](kotlin-compose-modernization-plan.md)。

## 当前结论

重构已经完成 UI 迁移主线，当前主线是收敛数据/偏好/平台边界。第 0、1、2A、3、4、5B 阶段完成；第 2B 阶段进行中；第 2C、5A 尚未开始。

| 阶段 | 状态 | 当前证据 |
| --- | --- | --- |
| 0 构建与工具链基线 | 完成 | 主 debug 与 release staging 可构建 |
| 1 旧依赖首批清理 | 完成 | Anko、EasyPermissions 等已移除 |
| 2A 安全网与验证矩阵 | 完成 | 8 个测试文件、50 项数据层 JVM 测试；debug/release staging 构建通过 |
| 2B Repository 边界 | 进行中 | 分类、搜索、Tag、草稿、随机、阅读状态、详情内容/导航及详情偏好已收敛；下载/备份和其他平台数据仍待处理 |
| 2C 状态模型与宿主瘦身 | 未开始 | 等待 2B 边界稳定后拆分 `DetailActivity` 等宿主 |
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

## 当前验证基线

以下命令已在 2026-07-26、当前详情偏好边界工作树上重新执行并通过：

```powershell
.\gradlew.bat testDefaultProductionDebugUnitTest --tests 'info.free.scp.data.*'
.\gradlew.bat assembleDefaultProductionDebug --stacktrace
.\gradlew.bat assembleDefaultProductionReleaseStaging --stacktrace
```

- 数据层测试源集位于 `app/src/testDefaultProductionDebug/java`，当前有 8 个测试文件、50 个 `@Test`。
- 使用该定制源集是因为项目的 `Test` product flavor 在 Windows 不区分大小写的文件系统上会与常规 `app/src/test` 冲突。
- release staging 已覆盖 Kotlin/Java 编译、R8 minify、资源压缩和 APK 打包。
- 当前构建仍报告 `flatDir`、Java 8 source/target、Room kapt 参数和 Gradle 9 兼容性等 warning；它们属于阶段 5A，不应与 Repository 切片混改。
- 当前未连接 Android 设备，因此 WebView 在线/离线切换、Cookie 编辑与评论入口仍需设备级手动复验。

## 本轮完成：详情偏好边界

- 新增 `DetailSettingsStore` 与 `PreferenceDetailSettingsStore`，集中承载详情阅读模式、字号、Cookie/User-Agent、提示状态和积分增减。
- `DetailActivity`、`DetailViewModel` 只依赖语义化设置接口，`view/detail` 对 `PreferenceUtil`、`APP_SP` 的定向扫描已无命中。
- 文档先前记录的 `APP_SP` 偏好监听入口在当前代码中并不存在，实际只有一个未使用 import，本轮已清理。
- 未拆分 `DetailActivity` UI、未改变导航、偏好 key、默认值和写入顺序。
- 数据层测试、主 debug 构建和 release staging 构建均通过；设备级 WebView/Cookie 复验待补。

## 下一步：下载/备份边界

下一切片应处理 `DownloadActivity` 对 `AppInfoDatabase`、`DetailDatabase` 的直接访问，同时保持现有 Storage Access Framework 协议、数据库文件名和备份格式不变。

建议验收标准：

1. `DownloadActivity` 不再直接持有数据库单例或 DAO，备份/恢复通过明确的数据边界执行。
2. SAF 的 `OpenDocument` / `OpenDocumentTree` 流程、持久化 URI 权限、数据库文件名和导入导出格式保持不变。
3. 为可抽离的文件选择/备份计划补最小 characterization tests；数据库文件操作至少通过构建矩阵和设备手动复验。
4. 不在同一切片升级 Room、修改 schema 或清理工具链 warning。

完成下载/备份边界后，按以下顺序推进：

1. 剩余入口：处理 `LaterFragment` 的目录直查、`DirectActivity` 的数据库/网络直连，以及仍位于 `view/feed` 的 Repository。
2. 用户与首页设置：收敛昵称、职位、积分、公告、草稿默认内容等 `PreferenceUtil` 访问。
3. 阶段 2C：建立稳定 UI State，拆分 `DetailActivity`，再决定 Fragment 过渡容器去留。
4. 阶段 5A：分批清依赖/warning，并手动复验桌面小部件、SAF、FileProvider、WebView、各 flavor 与 release shrink/minify。

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
rg -n "PreferenceUtil" app/src/main/java/info/free/scp/view/detail
.\gradlew.bat testDefaultProductionDebugUnitTest --tests 'info.free.scp.data.*'
```

开始下一切片前应确认：已理解分支相对 `origin/compose-refactor` 的预期差异、工作区干净、50 项数据层测试通过。每个阶段的完成证据必须同时包含定向扫描、自动测试/构建结果，以及涉及平台行为时的手动验证结果。
