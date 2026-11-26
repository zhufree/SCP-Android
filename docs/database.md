# 数据库表结构说明

应用使用三个 Room 数据库，文件名定义在 `SCPConstants`。三者都启用了 `allowMainThreadQueries()`。

| 数据库类 | 文件名 | 代码版本 | 来源 | 实体 |
| --- | --- | --- | --- | --- |
| `ScpDatabase` | `scp_category_v2.db` | 2 | APK 内置或 SCP-Spider Release（冷启动安装） | `scps` |
| `DetailDatabase` | `scp_detail_v2.db` | 2 | 按块合并，或经 SAF 导入整库 | `scp_detail` |
| `AppInfoDatabase` | `scp_info.db` | 8 | 首次运行时本地创建 | `records`、`like_table`、`like_box_table`、`draft` |

数据库文件位于应用私有目录 `databases/`，路径由 Android `Context.getDatabasePath()` 提供。

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
| `sub_scp_type` | TEXT | 子分类。故事（7/8）使用标题首字母或 `0-9`，国际分部（23）形如 `俄国分部-series` |

主要查询（`ScpDao`）：按类型取全表、按 `(type, sub_type)` 取故事、国际分部按 `sub_scp_type LIKE`（固定 `scp_type = 23`）、标题 `LIKE` 搜索、按 link 批量查询、随机（`ORDER BY random()`）、按编号 `title LIKE`。

### `scp_type` 取值（`SCPConstants.ScpType`）与内置库条目数

内置目录共 35,811 条。表中“—”表示内置目录没有该类型的行。

| 值 | 常量 | 内置库条数 |
| --- | --- | --- |
| 1 | `SAVE_SERIES`（SCP 系列） | 10994 |
| 2 | `SAVE_SERIES_CN`（SCP-CN 系列） | 4999 |
| 3 / 4 | `SAVE_JOKE` / `SAVE_JOKE_CN` | 474 / 182 |
| 5 / 6 | `SAVE_EX` / `SAVE_EX_CN` | 67 / 49 |
| 7 / 8 | `SAVE_TALES` / `SAVE_TALES_CN` | 3366 / 5339 |
| 9 / 10 | `SAVE_CANON` / `SAVE_CANON_CN` | 43 / 25 |
| 11 / 12 | `SAVE_STORY_SERIES` / `SAVE_STORY_SERIES_CN` | 151 / 56 |
| 13 | `SAVE_REPORT` | 493 |
| 14 | `SAVE_ANOMALOUS_CN` | 26 |
| 15 | `SAVE_SHORT_STORY` | 7 |
| 16 | `SAVE_LIBRARY_PAGE` | 5 |
| 17 | `SAVE_GOI` | 1228 |
| 18 | `SAVE_ART` | 1379 |
| 19 / 20 | `SAVE_CONTEST` / `SAVE_CONTEST_CN` | 60 / 66 |
| 21 / 22 | `SAVE_WANDER` / `SAVE_WANDER_CN` | 565 / 1592 |
| 23 | `SAVE_INTERNATIONAL` | 2958 |
| 24 | `SAVE_INFO_PAGE` | 21 |
| 100 / 101 | `SAVE_OFFSET` / `SAVE_TALES_BY_TIME` | 1666 / — |

### 目录库格式与安装

- 目录库使用 `app/schemas/info.free.scp.db.ScpDatabase/2.json` 的八列结构，空子类用空串。发布文件的 `PRAGMA user_version` 为 0，Room 数据库版本为 2。
- 内置库的 `spider_meta` 与 `assets/scp_category_v2.meta.json` 记录同一导出。替换内置库时，先按发布清单校验压缩包和解压文件的大小与 SHA-256，再用库内 `spider_meta(key, value)` 生成 JSON。
- 目录要求契约版本 `1`。按 `export_id` 识别同一导出，按 `exported_at` 比较不同导出。下载后校验压缩包和原始文件的 SHA-256，再执行只读 SQLite 的 `quick_check`、列和元数据检查，并用临时 Room 实例校验一次副本。原始库暂存为 `scp_category_v2.staging.db`，冷启动时再次用 Room 开库并查询一行。
- 首次冷启动从 APK 复制目录。有本地目录时，冷启动用 Room 开库并查询 `scps`，再比较导出版本。内置目录会升级无 `spider_meta` 的 legacy 目录或导出时间更早的目录，保留同版和更新的目录。同一导出的发布清单也不会触发目录下载提示。目录替换只操作目录库文件。暂存安装完成前保留旧库 `.bak`，新库验证失败则回滚。现有目录打不开时，将文件和旁路文件隔离到 `.corrupt`，从 assets 独立恢复。恢复失败会提示重启重试，当前进程暂停目录查询。

## 2. DetailDatabase（正文库）

### `scp_detail`（实体 `ScpDetail`）

| 列 | 类型 | 说明 |
| --- | --- | --- |
| `link` | TEXT PK NOT NULL | 与 `scps.link` 对应 |
| `detail` | TEXT | 正文 HTML |
| `not_found` | INTEGER | 页面是否 404（发布库用 1 表示已记录的 404，0 表示正常） |
| `tags` | TEXT | Tag 字符串（`getLinksByTag` 用 `LIKE` 匹配） |

`DetailDao` 可按文章键读出正文、404 标记与 Tag，另提供按 Tag 查文章、Tag 云和正文全文搜索。详情先查规范键，再查历史别名。整库旧别名与分块规范键共存时优先使用规范键。

SAF 整库文件支持 v0 和 v2。暂存文件经只读完整性、列与类型检查，再用 Room 验证 schema 和至少一条有效正文。冷启动再次验证正式文件，成功后删除旧库 `.bak` 并启用离线优先。Room 禁用破坏性迁移，旧库读取失败时可以重新导入整库。

### 分块导入使用的本地表

| 表名 | 字段 | 用途 |
| --- | --- | --- |
| `x_detail_part` | `link`（主键）、`part_id` | 记录当前写入该正文的块 |
| `x_part_state` | `part_id`（主键）、`parts_version`、`digest`、`export_id`、`sha256`、`rows`、`installed_at` | 记录已装块及校验信息，时间为 Unix 毫秒 |

快照合并包含 `not_found=1` 的行。删除仅作用于仍归当前块、且本次快照中缺失的行。分块规则从 1 升到 2 时，沿用 ID 的块也须重装。首个成功的 v2 合并事务会一并清理清单已撤除的旧版块及仍归属于它们的行，失败时整个事务回滚。同规则下暂时缺席的块保留。本地已有新版块时会拒绝按更早的规则安装。SAF 整库导入走冷启动文件替换，整库中尚无归属的行在后续按块导入时保留。两张 `x_` 表由导入器维护，正文仍通过原有实体和 DAO 读取。

## 3. AppInfoDatabase（用户数据库）

当前 schema 为 `app/schemas/info.free.scp.db.AppInfoDatabase/8.json`。

`records.link` 和 `like_table.link` 是用户库的全部链接键，均为主键。`like_table.boxId` 记录收藏夹归属，引用收藏夹的独立整数 ID。`like_box_table` 与 `draft` 没有链接键。

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

默认收藏夹的创建逻辑在 `ReadingListRepository`。

### `draft`（`DraftModel`）——草稿

| 列 | 类型 | 说明 |
| --- | --- | --- |
| `draftId` | INTEGER PK | 草稿 id |
| `lastModifyTime` | INTEGER | 最后修改时间（毫秒） |
| `title` | TEXT | 标题 |
| `content` | TEXT | 内容 |

新增草稿由 SQLite 在一条 INSERT 语句里按现有最大 ID 分配主键。编辑对已有主键执行 UPDATE。

### 迁移（`AppInfoDatabase`）

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
- **SAF 备份**包含 `scp_info.db`、`level.xml`、`app.xml`。SQLite 用 `VACUUM INTO` 生成含已提交 WAL 数据的单文件快照。旧版 SQLite 在同一个读源事务里复制用户表及序列号。
- **SAF 恢复**先暂存三件，在只读 SQLite 中校验文件完整性、版本和必需表，校验偏好 XML，再用 Room 在副本上跑迁移和读取验证。全部通过才发布待恢复目录。下次冷启动先保留三件的 `.bak`，再替换并读取验证。中途失败或进程中断时按记录还原原文件。前台会显示安装、失败或回滚结果。

## 5. 数据来源与 SCP-Spider

目录库在冷启动时整体替换，正文可按块合并。用户库中的链接状态通过 7→8 保留。单篇目录查询先查规范键，失败后兼容大小写和旧的 `#pages` 后缀。

- 目录库与正文库的内容来自对 SCP 基金会中文站（`http://scp-wiki-cn.wikidot.com`，`SCPConstants.SCP_SITE_URL`）的抓取，抓取工具为 SCP-Spider。
- 应用通过 SCP-Spider Release 上的固定 `manifest.json` 检查数据更新，再用清单的标签和文件名拼接 HTTPS 下载地址。分发契约见 [数据 Release](build-and-release.md#数据-release)。
