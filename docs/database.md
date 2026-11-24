# 数据库表结构说明

> 依据 Room 实体/DAO、`app/schemas/` 导出文件和 `app/src/main/assets/scp_category_v2.db` 的实际内容整理，核对日期 2026-11-23。标注“待确认”的内容在代码中无法核实。

应用使用三个 Room 数据库，文件名定义在 `SCPConstants`。三者都启用了 `allowMainThreadQueries()`。

| 数据库类 | 文件名 | 代码版本 | 来源 | 实体 |
| --- | --- | --- | --- | --- |
| `ScpDatabase` | `scp_category_v2.db` | 2 | APK 内置或 SCP-Spider Release（冷启动安装） | `scps` |
| `DetailDatabase` | `scp_detail_v2.db` | 2 | 按块合并，或经 SAF 导入整库 | `scp_detail` |
| `AppInfoDatabase` | `scp_info.db` | 8 | 首次运行时本地创建 | `records`、`like_table`、`like_box_table`、`draft` |

数据库文件位于应用私有目录 `databases/`（`FileUtil.privateDbDirPath`）。

## 1. ScpDatabase（目录库）

### `scps`（实体 `ScpItemModel` 继承 `ScpModel`）

| 列 | 类型 | 说明 |
| --- | --- | --- |
| `_id` | INTEGER PK（NOT NULL） | 主键 |
| `_index` | INTEGER NOT NULL | 同一 `scp_type` 内的次序，用于上一篇/下一篇（`getNextScp`/`getPreviewScp` 以 `_index±1` 查询） |
| `title` | TEXT NOT NULL | 标题，如 `SCP-001 - 等待解密[已锁]` |
| `link` | TEXT NOT NULL | 站内相对路径，如 `/scp-001`。在线接口与离线库都以它为文档标识 |
| `scp_type` | INTEGER NOT NULL | 文档类型，取值见下表 |
| `author` | TEXT | 作者（可空） |
| `created_time` | TEXT | 创建时间（故事类使用，可空） |
| `sub_scp_type` | TEXT | 子分类：内置库中 `scp_type` 7/8（故事）为标题首字母或 `0-9`；`scp_type` 23（国际分部）形如 `俄国分部-series`；其余类型的取值未逐一核对（待确认） |

主要查询（`ScpDao`）：按类型取全表、按 `(type, sub_type)` 取故事、国际分部按 `sub_scp_type LIKE`（固定 `scp_type = 23`）、标题 `LIKE` 搜索、按 link 批量查询、随机（`ORDER BY random()`）、按编号 `title LIKE`。

### `scp_type` 取值（`SCPConstants.ScpType`）与内置库条目数

内置库共 22281 条（2026-11-23 用 `sqlite3` 统计）。表中“—”表示内置库没有该类型的行。

| 值 | 常量 | 内置库条数 |
| --- | --- | --- |
| 1 | `SAVE_SERIES`（SCP 系列） | 7998 |
| 2 | `SAVE_SERIES_CN`（SCP-CN 系列） | 2999 |
| 3 / 4 | `SAVE_JOKE` / `SAVE_JOKE_CN` | 383 / 107 |
| 5 / 6 | `SAVE_EX` / `SAVE_EX_CN` | 46 / 33 |
| 7 / 8 | `SAVE_TALES` / `SAVE_TALES_CN` | 2480 / 3113 |
| 9 / 10 | `SAVE_CANON` / `SAVE_CANON_CN` | — / 20 |
| 11 / 12 | `SAVE_STORY_SERIES` / `SAVE_STORY_SERIES_CN` | 120 / 83 |
| 13 | `SAVE_REPORT` | 479 |
| 14 | `SAVE_ANOMALOUS_CN` | 7 |
| 15 | `SAVE_SHORT_STORY` | 3 |
| 16 | `SAVE_LIBRARY_PAGE` | 5 |
| 17 | `SAVE_GOI` | 728 |
| 18 | `SAVE_ART` | 431 |
| 19 / 20 | `SAVE_CONTEST` / `SAVE_CONTEST_CN` | 9 / 46 |
| 21 / 22 | `SAVE_WANDER` / `SAVE_WANDER_CN` | 463 / 950 |
| 23 | `SAVE_INTERNATIONAL` | 1757 |
| 24 | `SAVE_INFO_PAGE` | 21 |
| 100 / 101 | `SAVE_OFFSET` / `SAVE_TALES_BY_TIME` | — / — |

常量 `SAVE_OFFSET`、`SAVE_TALES_BY_TIME`、`SAVE_CANON` 在代码里有引用（`ScpCatalogRepository`）而内置库没有对应行，原因待确认（可能由其他数据文件或旧版本使用）。

### 与导出 schema 的出入

- 内置库的实际建表语句是 `scps(_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, _index, title, link, scp_type, author, created_time, sub_scp_type)`，与 `app/schemas/info.free.scp.db.ScpDatabase/2.json` 的列一致。
- 该目录下另有 `1.json`、`3.json`、`4.json`。其中 `4.json` 含 `scps`（`sId`、`index`、`createdAt`…）和 `details` 表，与当前实体不一致，应是更早的数据模型（当时代码版本号更高）。**当前以代码中的 `version = 2` 与 `2.json` 为准**。
- 发布目录保持上述 8 列，空子类用空串。发布文件的 `PRAGMA user_version` 为 0，Room 的本地版本为 2。
- 当前内置库没有 `spider_meta`。`assets/scp_category_v2.meta.json` 标记为 `legacy`、契约 `0`，用于无本地副本时初始化。以后更新内置库时，用新库的 `spider_meta(key, value)` 生成同名 JSON。
- 新目录要求契约版本 `1`。按 `export_id` 识别同一导出，按 `exported_at` 比较不同导出。下载后校验压缩包和原始文件的 SHA-256，再执行只读 SQLite 的 `quick_check`、列和元数据检查，并用临时 Room 实例校验一次副本。原始库保留为 `scp_category_v2.staging.db`。
- 冷启动在 Room 打开前安装目录。旧库先移到 `.bak`，清理 `-wal`、`-shm`、`-journal` 后将 staging 改名为正式库。独立打开失败时还原旧库。

## 2. DetailDatabase（正文库）

### `scp_detail`（实体 `ScpDetail`）

| 列 | 类型 | 说明 |
| --- | --- | --- |
| `link` | TEXT PK NOT NULL | 与 `scps.link` 对应 |
| `detail` | TEXT | 正文 HTML |
| `not_found` | INTEGER | 页面是否 404（发布库用 1 表示已记录的 404，0 表示正常） |
| `tags` | TEXT | Tag 字符串（`getLinksByTag` 用 `LIKE` 匹配） |

`DetailDao`：取正文/Tag（含 `LiveData` 版本）、按 Tag 取 link、取全部 Tag 字符串（Tag 云计数）、全文 `detail LIKE :keyword` 搜索。

迁移：`MIGRATION_1_2` 先 `DROP TABLE scp_detail` 再重建；另设置了 `fallbackToDestructiveMigration(dropAllTables = true)`。

`DetailDatabase.getInstance()` 只有在 `scp_detail_v2.db` 文件存在时才会创建实例，并把应用模式设为离线；文件不存在时返回 `null`，调用方按“未离线”处理。

### 分块导入使用的本地表

| 表名 | 字段 | 用途 |
| --- | --- | --- |
| `x_detail_part` | `link`（主键）、`part_id` | 记录当前写入该正文的块 |
| `x_part_state` | `part_id`（主键）、`parts_version`、`digest`、`export_id`、`sha256`、`rows`、`installed_at` | 记录已装块及校验信息，时间为 Unix 毫秒 |

快照合并包含 `not_found=1` 的行。删除仅作用于仍归当前块、且本次快照中缺失的行。分块规则升级后，首个成功的重装事务也会清理归属于已撤除块的行。同规则下暂时缺席的块保留。SAF 整库导入走冷启动文件替换，整库中尚无归属的行在后续按块导入时保留。两张 `x_` 表由导入器维护，正文仍通过原有实体和 DAO 读取。Room 对附加表的校验和破坏性重建时的清理行为待设备验证。

## 3. AppInfoDatabase（用户数据库）

当前 schema 为 `app/schemas/info.free.scp.db.AppInfoDatabase/8.json`，保留 5–7 供历史升级核对。8 的表结构与 7 相同。

`records.link` 和 `like_table.link` 是用户库的全部链接键，均为主键。`like_table.boxId` 记录收藏夹归属，引用收藏夹的独立整数 ID。`like_box_table` 与 `draft` 没有链接键。历史用户表 `LaterAndHistoryTable.link` 与 `LikeAndReadTable.link` 在 4→5 已分别转入以上两列。

文章键采用小写路径，保留百分号编码并将其十六进制写成大写，移除 query、fragment 和末尾斜杠。例如 `/SCP-CN-2901/?x=1#pages` 对应 `/scp-cn-2901`。中文站完整 URL 按不区分大小写的主机名识别，使用站内路径作为键。其他 HTTP(S) URL 保留主机信息，避免历史用户记录中不同站点的同名路径合并。

### `records`（`ScpRecordModel`）——历史与待读

| 列 | 类型 | 说明 |
| --- | --- | --- |
| `link` | TEXT PK | 文档链接（同一 link 只有一行，`REPLACE`） |
| `title` | TEXT | 标题 |
| `viewListType` | INTEGER | `SCPConstants.HISTORY_TYPE = 0`（历史）/ `LATER_TYPE = 1`（待读） |
| `viewTime` | INTEGER | 时间戳（`Date`，经 `Converters` 转 Long） |

### `like_table`（`ScpLikeModel`）——收藏与已读

| 列 | 类型 | 说明 |
| --- | --- | --- |
| `link` | TEXT PK | 文档链接 |
| `title` | TEXT | 标题 |
| `like` | INTEGER | 是否收藏（布尔） |
| `hasRead` | INTEGER | 是否已读（布尔） |
| `boxId` | INTEGER | 所属收藏夹 id，默认 0（迁移 6→7 添加，默认值 0） |

### `like_box_table`（`ScpLikeBox`）——收藏夹

| 列 | 类型 | 说明 |
| --- | --- | --- |
| `id` | INTEGER PK AUTOINCREMENT | 收藏夹 id |
| `name` | TEXT | 名称 |

默认收藏夹的创建逻辑在 `ReadingListRepository`（`ReadingListRepositoryTest` 覆盖“默认收藏夹创建”）。

### `draft`（`DraftModel`）——草稿

| 列 | 类型 | 说明 |
| --- | --- | --- |
| `draftId` | INTEGER PK | 草稿 id |
| `lastModifyTime` | INTEGER | 最后修改时间（毫秒） |
| `title` | TEXT | 标题 |
| `content` | TEXT | 内容 |

### 迁移历史（`AppInfoDatabase`）

| 迁移 | 内容 |
| --- | --- |
| 1→2、2→3、3→4 | 空实现 |
| 4→5 | 删除旧 `ScpTable`/`DetailTable`；把 `LaterAndHistoryTable` 重建为 `records`，`LikeAndReadTable` 重建为 `like_table` |
| 5→6 | 新建 `draft` |
| 6→7 | `like_table` 增加 `boxId`；新建 `like_box_table` |
| 7→8 | 在 Room 升级事务中规范化两列链接键并合并碰撞。待读和历史共用一行，取时间较新的记录和列表类型，同时间取较大 `rowid`。收藏和已读分别取逻辑或，标题与归属优先取收藏夹仍存在的收藏行，其次取收藏行，同等优先级取较大 `rowid`。两张表都没有计数列。 |

`AppInfoDatabase` 没有 `fallbackToDestructiveMigration`，缺失迁移路径时会抛出异常。

## 4. 数据库之外的本地数据

- **SharedPreferences**：`init`（首次安装、隐私确认、各版本首次启动标记）、`app`（公告、`api_url`、应用模式、Cookie/User-Agent 等）、`level`（积分、昵称、职位、草稿缓存等）、`read_settings`（隐藏已读、目录条数/高度/间距、字号等）。键名定义在 `util/PreferenceUtil.kt`。
- **备份**（`data/backup/BackupFilePlan`）：备份/恢复三个文件：`scp_info.db`（含 `-shm`/`-wal` 旁路文件）、`level.xml`、`app.xml`。**不包含** `scp_detail_v2.db`。恢复前会关闭 `AppInfoDatabase`，覆盖文件后重建并主动打开数据库，旧备份由 Room 执行对应迁移（包括 7→8）。

## 5. 数据来源与 SCP-Spider

目录库在冷启动时整体替换，正文可按块合并。用户库中的链接状态通过 7→8 保留。现有内置目录库仍包含旧格式链接，单篇查询先查规范键，失败后兼容大小写和旧的 `#pages` 后缀。

- 目录库与正文库的内容来自对 SCP 基金会中文站（`http://scp-wiki-cn.wikidot.com`，`SCPConstants.SCP_SITE_URL`）的抓取。本仓库历史记录可以佐证：旧版 README 写明“数据来自爬虫抓取”；提交 `55404a7`（2019-01-06，“删除爬虫代码做独立项目”）把爬虫从本仓库拆出，成为独立项目，即 SCP-Spider。
- 应用内“离线与备份”页面有“如何更新数据库”按钮（代码里的回调名为 `onOpenSpiderDoc`），跳转到一篇外部文档；该文档的内容不在仓库中。
- 应用通过 SCP-Spider Release 上的固定 `manifest.json` 检查数据更新，再用清单的标签和文件名拼接 HTTPS 下载地址。分发契约见 [数据 Release](build-and-release.md#数据-release)。
- 在线接口（见 [`architecture.md`](architecture.md)）由独立后端提供，实现不在本仓库，**待确认**其代码位置，以及与 SCP-Spider 的关系。
