SCP Android 第三方客户端

`kotlin`

---

# v0.0.1
预计开发完001-4999系列的目录，标题和链接都很简单不涉及数据库存储和网络加载，阅读直接用webView打开链接

TODO 8.23
- [x] 整理adapter基类
- [x] 整理scp网站的内容结构，为之后划分板块做准备 （PNG已上传git)
- [x] toolbar展开菜单theme修改
- [x] 整理笔记


# v0.0.2
发现原来scp系列也还是有标题，看来需要重抓了
数据来自爬虫抓取，存储在bmob平台，自己的api和key是私下存储的在PrivateConstants这个类里
需要尝试这个开源项目的可以自己创建一个bmob账号然后把数据（后续会把爬虫和数据都整理一份上传）传到后台，填上自己的key：
```
object PrivateConstants {
    const val APP_ID = "xxx"
    const val API_KEY = "xxx"
}
```

TODO
- [x] 搭建网络请求框架
- [x] 完成基本数据库存储功能
- [x] 重新抓取scp系列，抓取标题
- [x] 添加一个反转列表功能
- [ ] 考虑抓取正文html内容

爬虫及数据已上传
