[![forthebadge](https://forthebadge.com/images/badges/built-for-android.svg)](https://forthebadge.com)


SCP Android 第三方客户端

`kotlin`

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
  - [ ] tag检索
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

# 当前开发版本- v0.2.7
- [x] 隐私协议提示
- [ ] 更换后端接口
- [ ] 进正文时带列表
- [ ] tag检索
- [ ] 文件读取系统迁移
- [ ] 详情页
- [ ] banner
- [ ] 游戏列表改为api获取

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


