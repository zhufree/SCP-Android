# SCP Android

SCP 基金会中文站的第三方 Android 阅读客户端，应用名“SCP基金会”，包名 `info.free.scp`。应用使用 Kotlin、Jetpack Compose（Material 3）、Room 和 WebView。[SCP 基金会中文站](http://scp-wiki-cn.wikidot.com)

## 功能

- 按分类浏览文档，查看最近原创、最近翻译和最高评分。
- 按标题、正文或 Tag 搜索，支持编号直达和随机阅读。全文搜索需要安装离线正文。
- 在线或离线阅读，支持日夜主题、字号调整、上下篇切换、评论查看和截图分享。
- 在本地保存收藏、已读、待读、浏览历史和草稿，支持备份与恢复。
- 提供随机文档桌面小部件、游戏推荐和站点传送门。

## 构建与运行

使用 JDK 17 或更高版本、Android 17 SDK Platform（`android-37.0`）和仓库自带的 Gradle wrapper。先按构建文档配置 `local.properties`、`app/google-services.json` 和 `PrivateConstants.kt`。[构建环境与配置](docs/build-and-release.md)

```bash
./gradlew :app:assembleDefaultProductionDebug :app:testDefaultProductionDebugUnitTest
```

APK 位于 `app/build/outputs/apk/DefaultProduction/debug/`。Android Studio 中选择 `DefaultProductionDebug` 变体。Windows 使用 `gradlew.bat`。

## 数据库与数据来源

- **目录库** `scp_category_v2.db` 随 APK 内置，可通过「数据更新」安装新版目录。
- **正文库** `scp_detail_v2.db` 可在「数据更新」中全部下载或按块安装，也可在「离线与备份」中导入整库。整库导入在下次冷启动时生效。
- **用户库** `scp_info.db` 保存收藏、已读、待读、浏览历史和草稿。
- SCP-Spider 从中文站抓取目录和正文，将更新清单、目录库和正文块放在同一个数据 Release 中。[最新数据 Release](https://github.com/zhufree/SCP-Spider/releases/latest) · [数据发布契约](docs/build-and-release.md#数据-release)

## 文档索引

| 文档 | 内容 |
| --- | --- |
| [架构](docs/architecture.md) | 分层、导航、主题、数据流 |
| [数据库](docs/database.md) | 表结构、迁移和数据来源 |
| [构建与发布](docs/build-and-release.md) | 本地配置、构建变体和发布步骤 |
| [贡献指南](docs/contributing.md) | 代码与测试约定 |

## 许可

SCP 中文站文档使用 CC BY-SA 3.0 许可。应用代码使用 MIT 许可。[LICENSE](LICENSE)
