[![forthebadge](https://forthebadge.com/images/badges/built-for-android.svg)](https://forthebadge.com)


# SCP Android

SCP 基金会中文站（[scp-wiki-cn.wikidot.com](http://scp-wiki-cn.wikidot.com)）的第三方 Android 阅读客户端，应用名“SCP基金会”，包名 `info.free.scp`。

`kotlin` `jetpack-compose` `room`

## 项目简介

应用内置 SCP 中文站的文档目录，可以按分类浏览、搜索、收藏和阅读文档。正文支持在线阅读，也可下载离线正文或导入整库，供离线阅读与全文搜索。阅读记录、收藏夹、草稿等个人数据保存在本地，可备份与恢复。

文档内容来自 SCP 基金会中文站，遵循 CC BY-SA 3.0；应用代码使用 MIT 许可（见 [LICENSE](LICENSE)）。

## 功能

以下均能在代码中找到对应页面：

- **浏览**：首页（公告、分类入口、最近原创/翻译、最高评分、Tag）、图书馆、SCP 国际版、GOI 格式、艺术作品、背景资料与指导；SCP / SCP-CN / 故事 / 设定 / 征文 / 被放逐者图书馆等分类，列表支持反序、隐藏已读。
- **查找**：标题搜索、全文搜索（需离线正文库）、Tag 检索与 Tag 云、编号直达（CN/J 等类型）、随机（全部 / SCP / 故事 / 搞笑）。
- **阅读**：WebView 阅读页，白天/夜间主题、字号、上一篇/下一篇、在线/离线切换、评论查看（可在详情页保存网页 Cookie 与 User-Agent）、浏览器打开、复制链接、截图分享。
- **个人数据**：收藏与收藏夹、已读、待读列表（支持批量导入编号）、阅读历史、草稿（创作沙盒）、昵称/职位/积分。
- **数据管理**包括检查目录更新、下载全部或按块安装离线正文、导入整库及个人数据备份与恢复（SAF）。
- **其他**：桌面随机小部件、游戏推荐与站点传送门（来自 Firebase 远程配置）、设置、关于/捐赠页。
- **渠道**：`Default`、`firim`、`GooglePlay`、`Github` 四个渠道（见 [构建与发布](docs/build-and-release.md)）。

## 技术栈

| 类别 | 内容 |
| --- | --- |
| 语言 / 构建 | Kotlin 2.4.21、Gradle 9.9.2、AGP 9.5.2、KSP 2.3.14、Java 17 字节码 |
| UI | Compose BOM 2025.11.01（Material 2）、AppCompat（Activity/Fragment 宿主）、WebView（阅读页）、Coil（图片） |
| 架构 | ViewModel + LiveData/StateFlow，`data/*` Repository 边界 |
| 本地存储 | Room 2.7.2（3 个数据库）、SharedPreferences |
| 网络 | Retrofit 3.0.0 + Gson、OkHttp 5.5.1、jsoup 1.24.1（评论解析） |
| 远程配置 | Firebase Realtime Database（BoM 33.16.0） |
| SDK | `minSdk 21`、`targetSdk 37`、`compileSdk 37` |
| 测试 | JUnit 4（JVM 单元测试） |

## 构建与运行

环境：JDK 17+（推荐 21）、Android 17 SDK Platform（`android-37.0`），使用仓库自带的 Gradle wrapper。

1. 克隆仓库并切换到需要的分支（当前重构分支为 `compose-refactor`）。
2. 准备三个被 `.gitignore` 忽略的本地文件，否则无法编译：`local.properties`（`sdk.dir`）、`app/google-services.json`（Firebase 配置）、`app/src/main/java/info/free/scp/PrivateConstants.kt`（`FEED_API_URL`、`FIREBASE_URL` 两个常量）。可使用占位值完成编译，Feed 接口会回退到公开 Bridge，模板与说明见 [docs/build-and-release.md](docs/build-and-release.md)。
3. 构建 debug APK 并运行 JVM 单元测试：

   ```bash
   ./gradlew :app:assembleDefaultProductionDebug :app:testDefaultProductionDebugUnitTest
   ```

   APK 位于 `app/build/outputs/apk/DefaultProduction/debug/`。Windows 使用 `gradlew.bat`；如用 Android Studio，选择 `DefaultProductionDebug` 变体。首次联网构建后，使用同一个 `GRADLE_USER_HOME` 可追加 `--offline`。

占位配置只保证能编译（交接文档记录开发机即如此）；在线接口与远程配置需要真实的后端地址和 Firebase 配置，获取方式待确认，见构建文档。

## 数据库与数据来源

- **目录库** `scp_category_v2.db` 随开发版 APK 内置，包含新版目录的 35,811 条文档条目。
- **正文库** `scp_detail_v2.db` 由 5 个固定正文块合并而成。在「数据更新」里可选择「下载全部正文」或按块安装。全部下载约 211 MB，安装后的正文库约 731 MB，安装时还需预留下载、解压和合并用的临时空间。单块文件不能作为整库在「离线与备份」中手动导入。手动导入须使用整库文件，并在下次冷启动后生效。
- **用户库** `scp_info.db`：收藏、已读、待读/历史、草稿（Room `AppInfoDatabase`）。
- **在线接口**：Feed 接口依次使用有效的 Firebase 缓存地址、`PrivateConstants.FEED_API_URL` 和公开 Bridge `https://api.zhufree.fun/`。评论直接解析中文站页面。
- **SCP-Spider** 从中文站抓取目录与正文。清单、目录库和 5 个正文块来自同一个 `db-YYYYMMDD-N` 数据 Release。[SCP-Spider 最新数据 Release](https://github.com/zhufree/SCP-Spider/releases/latest) · [数据发布说明](docs/build-and-release.md#数据-release)

以上数据更新功能已在开发版 APK 上完成真机验证，App 尚未发版。

表结构、取值含义和迁移历史见 [docs/database.md](docs/database.md)。

## 目录结构

```
SCP-Android/
├── app/
│   ├── build.gradle            # 模块配置：flavor、依赖、版本号
│   ├── proguard-rules.pro
│   ├── schemas/                # Room 导出的 schema JSON
│   ├── libs/                   # 本地 jar（jcc-bate-0.7.3.jar）
│   ├── <渠道>Production/release/   # 已提交的发布产物（APK、output-metadata）
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── assets/         # 内置目录库 + 详情页 WebView 用的 JS/CSS
│       │   ├── res/            # 资源（仅保留 layout_widget.xml 一个布局）
│       │   └── java/info/free/scp/
│       │       ├── MainActivity.kt, ScpApplication.kt, ScpWidgetProvider.kt, SCPConstants.kt
│       │       ├── bean/       # Room 实体与网络 DTO
│       │       ├── db/         # Room 数据库与 DAO
│       │       ├── data/       # Repository / DataSource / SettingsStore
│       │       ├── service/    # Retrofit、HttpManager、评论 HTML 解析
│       │       ├── ui/         # Compose Screen 与主题
│       │       ├── view/       # Activity / Fragment / ViewModel 宿主
│       │       ├── viewmodel/  # Firebase 数据 ViewModel 等
│       │       ├── components/ # 通用 Compose 组件
│       │       └── util/       # 偏好、文件、主题、更新检查等
│       └── testDefaultProductionDebug/java/   # JVM 单元测试
├── docs/                       # 项目文档（见下）
├── build.gradle, settings.gradle, gradle.properties, gradle/
├── LICENSE
└── SCP基金会.png               # SCP 中文站内容结构思维导图
```

## 文档索引

| 文档 | 内容 |
| --- | --- |
| [docs/architecture.md](docs/architecture.md) | 分层、模块、导航、数据流 |
| [docs/database.md](docs/database.md) | 三个数据库的表结构、迁移、数据来源 |
| [docs/build-and-release.md](docs/build-and-release.md) | 环境、构建变体、命令、发布流程 |
| [docs/contributing.md](docs/contributing.md) | 贡献指南、代码与测试约定 |
| [docs/refactor-handoff.md](docs/refactor-handoff.md) | Compose 重构的快速交接 |
| [docs/kotlin-compose-modernization-plan.md](docs/kotlin-compose-modernization-plan.md) | 重构完整方案与历史记录 |

## 当前重构状态（2026-11-18）

当前项目处于从旧 `Activity + Fragment + XML` 体系迁移到 Kotlin + Compose 的重构后半程。

- 当前应用版本：`0.3.5`（versionCode 31）（见 `app/build.gradle`）。
- 已完成：构建工具链升级、Anko/EasyPermissions 等旧依赖清理、主导航 Compose 壳层、普通页面 XML/Adapter/RecyclerView 迁移。
- 当前判断：第 0、1、2A、2B、2C、3、4、5B 阶段已完成。阶段 5A 的旧依赖和工具链收口已完成，包括 Java 17、KSP、Firebase 主模块，以及 Glide、multidex、显式 vectordrawable 的删除。剩下的是真机平台复验。
- 资源现状：`res/layout + res/menu` 当前仅保留桌面小部件 RemoteViews 所需的 `layout_widget.xml`，普通页面不再依赖 XML 布局。
- 后续重点：连接设备后复验 WebView、Cookie、SAF、FileProvider 与桌面小部件。
- 快速交接：见 [docs/refactor-handoff.md](docs/refactor-handoff.md)；完整计划与历史记录见 [docs/kotlin-compose-modernization-plan.md](docs/kotlin-compose-modernization-plan.md)。

主要验证命令：

```powershell
.\gradlew.bat assembleDefaultProductionDebug
.\gradlew.bat testDefaultProductionDebugUnitTest
.\gradlew.bat assembleDefaultProductionReleaseStaging
.\gradlew.bat assembleDebug
```

最近验证：2026-11-18，62 项 JVM 测试 0 失败，主 debug、release staging（含 R8/资源压缩）和全部 debug 渠道通过。工具链 warning 清理后的复跑同样通过。

以上命令在 macOS/Linux 上使用 `./gradlew`，Windows 上使用 `.\gradlew.bat`。

下面保留的是历史功能计划和版本记录，当前重构任务以 `docs/kotlin-compose-modernization-plan.md` 为准。其中的配置说明（如 v0.0.2 的 bmob）已过时，当前配置见上文和 `docs/build-and-release.md`。

---

# 历史记录与旧 TODO

# Recommend game list
- 以撒
- distrust
- 幽浮
- 绣湖/逃离方块
- 恶灵附身
- 艾迪芬奇的记忆
- 克苏鲁的呼唤：官方游戏
- 冷鲜肉

---

TODO

# 后续
- [ ] 阅读体验优化
  - [x] 稍后阅读功能，并且在稍后阅读列表中的上下篇是列表中切换
  - [ ] 功能引导提示
  - [ ] 优化输入姓名和选择职业流程（新人引导
  - [ ] 背景图（？）和主题色切换
- [ ] 创作相关
  - [ ] 保持登录状态（cookie）
- [ ] 最新
  - [ ] 最近更新/修改/新增的条目 0.12
  - [ ] 随机[原创]scp/故事
  - [x] 评分最高/最低
- [ ] 更多内容
  - [x] 放逐者图书馆
  - [x] 国际版
  - [x] tag检索
  - [x] GOI格式
- [ ] 其他
  - [ ] 设置
  - [ ] 名片分享功能（咕咕咕）
  - [x] 数据库重构
- [ ] 分出一些重点/特色明显/科普类文档放在显眼位置
  - [ ] 传承页面
  - [ ] crossover页面 爬虫重构
- [x] offset抓取
- [ ] 根据某些规则筛选scp（抓tag数据？）
- [ ] 目录更新自动化
  - [ ] scrapy爬取录入数据库
- [ ] 内容更新自动化
- [ ] 文件读取系统迁移

# 下个版本 - v0.3.5
- [ ] 进正文时带列表
- [ ] banner

# 当前开发版本- v0.3.0
- [x] 隐私协议提示
- [x] 更换后端接口
  - [x] 游戏
  - [x] 传送门
- [x] tag检索(初版)
- [x] 游戏列表改为api获取
- [x] 恰饭屋改为其他作品
- [x] 数据库更新
- [x] 随机列表下拉更多
- [x] 一些全局设置项移到阅读设置页面
- [ ] 多线路下载

# UI改版-v0.2.5
- [x]HomeFragment
- [x] Search
  - [x] UI
  - [x] 功能
  - [x] ViewPager+Tab
    - [x] 滑动切换N个Fragment（首页-故事-图书馆...）
- 首页UI
  - [x] 轮播图
  - [x] 目录入口 （GridLayout）
  - [x] Latest Top3
  - [x] Top Rate Page入口
- [x] 直达
- [x] 随机
  - [x] 入口UI
  - [x] 随机文档列表
- [x] 我的
  - [x] UI
- [x] 支持开发者
- [x] 最高评分
- [x] category
  - [x] 文档
  - [x] 故事
  - [x] 图书馆tab
  - [x] SCP国际
  - [x] 资料
  - [x] 列表页reverse
- [x] 待读（收藏列表待有收藏后测试
- [x] 历史
- [x] 数据库更新
- [x] 内置数据库
- [x] 接口更新
- [x] 文档缓存
- [x] 在线模式
- [x] 本地detail数据库
- [x] 首页feed
- [x] 待读和已读操作检验
    - [x] UI
- [x] 增加爱发电和面包多入口(视google play审核情况调整)
- [x] 夜间模式
- [x] 头像图片存储 Q
- [x] 备份
- [x] 正文部分检查，加载detail流程优化
- [x] 职位选择
- [x] 替换服务器上的数据库
- [x] 数据库无法读取的问题


 
# bug fix
- [x] 随机出现null
- [x] 国际分部条目重复（爬虫问题，数据库重复，重新抓
- [-] 随机跳过已读文档
- [x] 下一篇按钮失效
- [x] 么和幺文字替换bug(去掉了设置里的繁简设置，还是手动调吧)
- [x] 征文竞赛空白（数据库问题
- [x] 上一篇下一篇重复几次之后history list列表过多
- [x] 离线模式下随机的第一篇文档无法收藏

- [ ] 历史记录不记录随机出来的文章（？
- [ ] 历史记录满了，无法记录了（？

# v0.2.0
- [x] 抓取评论
  - [x] 修改接口添加评论用户名
- [x] 更新游戏列表链接
- [ ] 在线模式功能再多加一点
  - [x] 随机
  - [x] 直达
  - [x] 上下篇
- [ ] 广告部分优化？
- [x] 公告可隐藏
- [x] 草稿列表加指导页面
- [x] 繁简设置放到设置页

# bug fix
- [x] 修复随机模式Bug
- [x] 随机toast没有在主线程调用


# bug
- [x] 点击正文闪退（vivo手机特有，webView渲染bug，回退包版本解决）
- [x] 首页不显示入口（布局bug，添加minHeight）
- [x] 点击我的闪退（关键代码回退到0.1.3版本）

# v0.1.5
- [x] 修改个人页面
- [x] 增加【站点传送门】页面（主站，主站B站账号，SCP配音团等）
- [x] 增加国际分部文档
- [x] 增加恰饭(guanggao)页面，广告位招租
- [x] 增加收藏夹的操作
- [ ] 目录条目bug（还没找到原因）
- [x] 增加武汉加油入口
- [x] 增加在线模式
  - [x] range fix
  - [x] 正文部分细节
  - [x] 待读和收藏数据在列表显示
  - [?] 默认收藏夹不见了
  - [x] reverse问题
  - [x] 在下载页加个开关
 
历史版本
# v0.1.4
- [x] 解决数据库下载慢的问题
- [x] 更新一遍数据库
- [x] [最高评分页面](http://scp-wiki-cn.wikidot.com/top-rated-pages)
  - [x] 最受欢迎的原创SCP请[点击这里](http://scp-wiki-cn.wikidot.com/top-rated-pages/pagescp_limit/1/all_range/-/scp_range/others)。
  - [x] 最受欢迎的原创故事请[点击这里](http://scp-wiki-cn.wikidot.com/top-rated-pages/pagetale_limit/1/all_range/-/tale_range/others)。
  - [x] 最受欢迎的原创GoI格式[请点击这里](http://scp-wiki-cn.wikidot.com/top-rated-pages/pagetale_limit/1/all_range/-/tale_range/others)。
  - [x] 最受欢迎的原创被放逐者之图书馆作品请[点击这里](http://scp-wiki-cn.wikidot.com/top-rated-pages/pagetale_limit/1/all_range/-/tale_range/others)。
  - [-] 本月最受欢迎的页面请参阅[本月最高评分的页面](http://scp-wiki-cn.wikidot.com/top-rated-pages/pagetale_limit/1/all_range/-/tale_range/others)。
- [x] 收藏夹可分类
  - [x] 升级测试
  - [x] 取消收藏
- [x] 正文页面添加滑动条
- [x] 数字键盘形状优化&“其他SCP”的介绍
- [x] 首页放一个公告位置
- [x] 离线页面修改
- [x] 备份pref和db
- [x] 直达页面UI和bug
- [x] 捐赠页面去掉广告，增加捐赠列表



FIX BUG
- [ ] 历史记录时间丢失

# v0.1.3
- [x] 支持文档中frame的显示
- [x] 繁简转换功能
- [x] 阅读文档时可以在菜单中切换夜间模式
- [x] 新增草稿列表（可编辑保存多个草稿
- [x] 按钮样式优化
- [x] 修复部分已知bug
- [x] 下载总数据库时检查本地文件

# v0.1.2
- [x] 存储权限申请
- [x] 数据库存储部分重构
  - [x] like和读过部分数据迁移,
  - [x] 数据部分重构+测试通过
  - [x] 准备数据库文件
  - [x] detail添加downloadType
- [ ] 新手引导部分，字符串文案
- [x] 主数据库自动下载
- [x] 清除阅读记录
- [x] 切换数据库读取问题
- [x] index不正确bug

fix bug
- [x] 中分故事系列检索不出
- [x] 搜索结果无

# v0.1.1
- [x] 最近原创和最近翻译（30条）
- [x] tab功能修复
- [x] 正文右上角菜单添加【加入待读】功能
- [x] 沙盒功能初版(只能保存一个草稿)
- [x] 竞赛部分优化（不在更新信息中提及
- [x] 导入待读列表
- [x] 随机分【全部|SCP|故事|搞笑】（需要已离线该部分内容
- [x] 添加一个随机小组件
- [x] 正文抓取范围扩大

测试期修复
- 横竖屏切换时fragment保存问题修复
- 增加了对于sd卡没有权限的提示
---

# v0.1
- [x] 内容架构调整
  - [x] reverse功能补完
  - [x] 编号跳转功能
  - [x] 接口版本更新
  - [x] 按目录离线
- [x] 随机文档以列表形式切换，不完全随机
- [x] 隐藏事故记录
- [x] 已读按钮点击变化
- [x] 待读列表
- [x] 读过列表
- [x] 设置页更新
- [x] 收藏列表排序（按项目编号）
- [x] 搜索结果加待读按钮
- [x] 埋点/夜间模式


---
已发布版本更新内容

# v0.0.9
- 主要新增功能
    - [x] 游戏相关链接整理汇总
- 优化
    - [x] UI优化
    - [x] 折叠功能优化
    - [x] 随机功能改成页面
    - [x] 全文检索
    - [x] 已读改为手动标记，正文内替换随机按钮
    - [x] 隐藏已读文档
    - [x] 本地数据备份功能
    - [x] 全文截图分享功能
- 测试期优化
    - [x] 首页目录添加背景图
    - [x] 目录样式自定义：字体大小，数目，高度，间距
    - [x] 正文字体大小调整
    - [x] 切换上下章按钮在顶部也加一个
    - [x] 正文底部添加版权声明
    - [x] 搜索页UI调整

# v0.0.8
功能优化
- [x] 减少目录条目数量 500->200
- [x] 正文按钮位置调整，toolbar可收缩
- [x] sdk更新，fragment替换成v4版本
- [x] 等级和积分系统
- [x] 导航返回键返回之前打开的文章
- [x] 优化正文中折叠条目的操作
- [x] 用户调查问卷
- [x] 取消已读功能

# v0.0.7
功能添加
- [x] 搜索功能（标题搜索）
- [x] 随机文档
- [x] 收藏
- [x] 读过
- [x] 正文阅读滑动到底部时弹出上下篇
- [x] 重新抓数据并解决以下问题
  - [x] 404页面添加标识，在目录页就展示出来
  - [x] 为了统一竞赛页等特殊页面的全链接，对链接进行统一处理
- [x] 积分系统准备工作
- [x] 数据更新信息显示

# v0.0.6
离线阅读，夜间模式
- [x] 添加事故记录部分内容
- [x] 添加中国分部原创故事（按时间排序）
- [x] 添加数据加载的可选项
- [x] 优化数据存取过程，减少卡顿
- [x] 添加阅读页面的可选功能
  - [x] 切换阅读模式
  - [x] 反馈问题
- [x] 文案修改
- [x] 升级测试通过
- [x] 夜间模式
- [x] 页面内本地跳转

# v0.0.5
- [x] 修改数据表结构，腾出空间
- [x] 检测更新功能
- [x] 修改初始加载逻辑，避免每次加载时间过长

# v0.0.4
开发scp图书馆部分
- [x] SCP图书馆
  - [x] 基金会故事/CN
  - [x] 故事系列/CN
  - [x] 设定中心/CN
  - [x] 征文比赛/CN

# v0.0.3
梳理全站架构，完成scp系列部分的开发，√表示内容抓取也已完成
- [x] SCP系列
  - [x] SCP系列 √
  - [x] SCP-CN系列 √
  - [x] SCP故事版 √
  - [x] 归档内容 √
  - [x] 相关信息 √

# v0.0.2
（历史说明，已过时：当前不再使用 bmob，`PrivateConstants` 现在只含 `FEED_API_URL` 与 `FIREBASE_URL`。）
数据来自爬虫抓取，存储在bmob平台，自己的api和key是私下存储的在PrivateConstants这个类里
需要尝试这个开源项目的可以自己创建一个bmob账号然后把数据（后续会把爬虫和数据都整理一份上传）传到后台，填上自己的key：
```
object PrivateConstants {
    const val APP_ID = "xxx"
    const val API_KEY = "xxx"
}
```

- [x] 搭建网络请求框架
- [x] 完成基本数据库存储功能
- [x] 重新抓取scp系列，抓取标题
- [x] 添加一个反转列表功能

爬虫及数据已上传


# v0.0.1
预计开发完001-4999系列的目录，阅读直接用webView打开链接

- [x] 整理adapter基类
- [x] 整理scp网站的内容结构，为之后划分板块做准备 （PNG已上传)
- [x] toolbar展开菜单theme修改
- [x] 整理笔记


