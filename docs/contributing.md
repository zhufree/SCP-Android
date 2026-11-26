# 贡献指南

## 开始之前

1. 阅读项目入口和模块划分。[README](../README.md) · [架构](architecture.md)
2. 配置 SDK 和本地文件。`local.properties`、`google-services.json` 和 `PrivateConstants.kt` 应留在本地。[构建与发布](build-and-release.md)

## 提交约定

每次提交聚焦一项改动。提交信息可使用 `refactor:`、`fix:`、`feat:` 或 `docs:` 前缀。

## 代码约定

- UI 通过 `data/*` 的 Repository 访问数据库与网络，通过 SettingsStore 读写偏好。ViewModel 向 Composable 提供 UI State。
- 新增数据访问时，沿用 DataSource 接口和 Repository 模式，以便测试注入数据源。Activity 和 Fragment 负责 Intent、WebView、剪贴板、截图等平台交互。
- 新页面使用 Compose（Material 3）和 `MainTheme`，通过 `MaterialTheme.colorScheme` 取色。桌面小部件用 RemoteViews 和 `res/layout/layout_widget.xml`。[架构与组件约定](architecture.md#8-弹窗菜单与提示)
- 修改偏好键或类型时兼容已有值。[阅读偏好](architecture.md#阅读偏好)
- 修改 Room 表结构时，提升数据库版本，提供 `Migration`，提交 `app/schemas/` 下生成的 schema JSON，并更新数据库文档。修改数据库文件名或备份格式时需处理旧数据的兼容性。[数据库](database.md)
- 包名、Intent action 和 flavor 配置属于现行接口，调整时需考虑调用方兼容性。
- 界面文案优先放在 `res/values/strings.xml`。

## 测试

JVM 测试使用 JUnit 4，放在 `app/src/testDefaultProductionDebug/java`。Repository 和纯 Kotlin 逻辑的测试按对应包组织。涉及 WebView、Cookie、SAF、FileProvider 或桌面小部件的改动，在设备上验证相关行为，并在 PR 中说明结果。

```bash
./gradlew :app:testDefaultProductionDebugUnitTest :app:assembleDefaultProductionDebug
./gradlew :app:assembleDefaultProductionReleaseStaging  # 涉及 R8、资源或依赖时
```

## 数据与内容

目录与正文由 SCP-Spider 从 SCP 基金会中文站抓取。数据发布在 SCP-Spider 中完成，本仓库携带 APK 使用的内置目录库。[数据 Release](build-and-release.md#数据-release)

站点内容使用 CC BY-SA 3.0 许可，应用代码使用 MIT 许可。[LICENSE](../LICENSE)
