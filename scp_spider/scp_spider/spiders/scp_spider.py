import scrapy

class mingyan(scrapy.Spider): #需要继承scrapy.Spider类
    
    name = "scp" # 定义蜘蛛名

    start_urls = [  #另外一种写法，无需定义start_requests方法
        'http://scp-wiki-cn.wikidot.com/scp-series',
        'http://scp-wiki-cn.wikidot.com/scp-series-1',
        'http://scp-wiki-cn.wikidot.com/scp-series-2',
        'http://scp-wiki-cn.wikidot.com/scp-series-3',
        'http://scp-wiki-cn.wikidot.com/scp-series-4',
    ]


    def parse(self, response):

        '''
        start_requests已经爬取到页面，那如何提取我们想要的内容呢？那就可以在这个方法里面定义。
        这里的话，并木有定义，只是简单的把页面做了一个保存，并没有涉及提取我们想要的数据，后面会慢慢说到
        也就是用xpath、正则、或是css进行相应提取，这个例子就是让你看看scrapy运行的流程：
        1、定义链接；
        2、通过链接爬取（下载）页面；
        3、定义规则，然后提取数据；
        就是这么个流程，似不似很简单呀？
        '''
        uls = response.css('div#page-content ul')    #根据上面的链接提取分页,如：/page/1/，提取到的就是：1
        for ul in uls[1:-3]:
            for li in ul.css('li'):
                link = li.css('a::attr(href)').extract_first()
                new_article = {
                    'title': li.css('::text'),
                    'link': link,
                    'cn': 'false',
                    'type': 'series'
                }
                # link_list.append(link)
                print(link)
            # article_list.append(new_article)
        # with open(filename, 'wb') as f:        #python文件操作，不多说了；
        #     f.write(response.body)             #刚才下载的页面去哪里了？response.body就代表了刚才下载的页面！
        # self.log('保存文件: %s' % filename)      # 打个日志