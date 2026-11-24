# 贡献指南

> 本文基于仓库现状整理，核对日期 2026-11-23。仓库没有 `AGENTS.md`、`CONTRIBUTING.md`、CI 配置（`.github` 下只有 `FUNDING.yml`）或代码格式化配置；这里的约定均来自现有代码和 `docs/` 中的记录。需要维护者确认的地方标注“待确认”。

## 1. 开始之前

1. 阅读 [`README.md`](../README.md) 和 [`architecture.md`](architecture.md)，了解模块划分。
2. 按 [`build-and-release.md`](build-and-release.md) 准备环境，注意需要自行创建三个被 gitignore 的本地文件（`local.properties`、`app/google-services.json`、`PrivateConstants.kt`），**不要提交它们**。
3. 确认能通过：

   ```bash
   ./gradlew testDefaultProductionDebugUnitTest
   ./gradlew assembleDefaultProductionDebug
   ```

## 2. 分支与提交

- 仓库默认分支是 `master`，当前重构在 `compose-refactor` 分支上进行。向哪个分支提交 PR，待确认。
- 提交信息历史上以中文或 `type: 描述` 为主，近期重构提交使用英文小写前缀，例如 `refactor: remove later child fragments`、`docs: record phase two completion`。建议沿用 `refactor:`/`fix:`/`feat:`/`docs:` 前缀，一次提交只做一件事。
- 重构按“小步、保持可构建”进行：每个切片都跑完整验证再继续（见 `kotlin-compose-modernization-plan.md` 的“重构原则”）。

## 3. 代码约定（来自现有代码与重构文档）

**分层**

- UI（`ui/`、`view/`）不要直接访问 `AppInfoDatabase`、`ScpDatabase`、`DetailDatabase`、`HttpManager`、`PreferenceUtil`。数据访问通过 `data/*` 的 Repository，偏好访问通过 `data/settings` 的 SettingsStore。
- 新增数据能力时沿用现有模式：`XxxDataSource` 接口 + Room/网络实现 + `XxxRepository`（构造参数默认值指向真实实现，便于测试注入假数据源），并补 JVM 测试。
- 页面状态通过 ViewModel 暴露 UI State（如 `DetailUiState`），Composable 不直接查询数据。
- `DetailActivity` 只保留 WebView、Intent、剪贴板、截图等平台交互；HTML 生成、菜单动作、Cookie 对话框等逻辑放在 `data/detail`、`ui/detail`、`DetailViewModel`。

**UI**

- 新页面使用 Compose（Material 2）与 `MainTheme`；颜色目前通过 `ThemeUtil` 取值。
- 不要新增 ViewBinding、RecyclerView/Adapter、XML 页面布局或菜单资源。`res/layout/layout_widget.xml` 是唯一保留的 XML 布局，服务于桌面小部件 RemoteViews。
- 设置页仍使用 `PreferenceFragmentCompat`（`res/xml/pref_read_settings.xml`），属于保留项。

**不要随意改变的行为**（除非单独立项并补迁移验证；来自 `refactor-handoff.md`）：

- Room schema、数据库文件名、备份文件格式
- 包名、深链路/intent action、flavor 配置
- 偏好 key

修改 Room 实体时需要同时：升级 `@Database(version)`、提供 `Migration`、提交新生成的 `app/schemas/**/<version>.json`（`room.schemaLocation` 指向 `app/schemas`），并同步更新 [`database.md`](database.md)。

**语言与文案**

- 界面文案以中文为主，位于 `res/values/strings.xml`，部分 Compose 代码里的中文以 `\uXXXX` 转义写在源码中。新增文案优先放进 `strings.xml`。

## 4. 测试

- JVM 测试位于 `app/src/testDefaultProductionDebug/java`（不是 `app/src/test`，原因见构建文档），框架为 JUnit 4。
- 测试目录按包镜像 `data/*`：有 Repository 或纯 Kotlin 逻辑的改动请同时补测试。
- 当前没有 instrumentation 测试。涉及 WebView、Cookie、SAF、FileProvider、桌面小部件的改动，需要在 PR 描述里写明手动验证结果，或补最小设备测试。
- 阶段性重构的完成证据要求同时包含：定向扫描结果、自动测试/构建结果，以及涉及平台行为时的手动验证结果。

建议在提交前运行：

```bash
./gradlew testDefaultProductionDebugUnitTest
./gradlew assembleDefaultProductionDebug
./gradlew assembleDefaultProductionReleaseStaging   # 改动涉及 R8/资源/依赖时
```

重构期间使用的迁移扫描（来自交接文档）：

```bash
rg -n "PreferenceUtil|APP_SP" app/src/main/java/info/free/scp/view app/src/main/java/info/free/scp/ui
```

结果应为 0 命中。

## 5. 数据与内容

- 目录库（`app/src/main/assets/scp_category_v2.db`）和离线正文库来自对中文站的抓取，抓取工具是独立项目 SCP-Spider（见 [`database.md`](database.md) 第 5 节）。更新数据请在该项目中完成，本仓库只放最终的内置目录库。
- 内容版权：正文来自 SCP 基金会中文站，遵循 CC BY-SA 3.0（应用内“关于”页链接了 `creativecommons.org/licenses/by-sa/3.0/deed.zh` 与站点授权指南）。应用代码本身使用 MIT 许可（见 `LICENSE`）。

## 6. 提交 PR 前的检查清单

- [ ] 没有提交 `PrivateConstants.kt`、`google-services.json`、`local.properties` 或任何密钥/真实接口地址
- [ ] 测试和 `assembleDefaultProductionDebug` 通过
- [ ] 没有违反分层约定（UI 未直连数据库/网络/偏好）
- [ ] 改动了数据库结构的话，已加迁移、schema JSON 和 `database.md`
- [ ] 改动了构建配置、依赖或流程的话，已更新 `build-and-release.md`
- [ ] 平台相关改动已写明手动验证结果
- [ ] 没有顺手提交 `app/*/release/` 下的构建产物（这些目录目前已被 git 跟踪，是否应继续提交待确认）
