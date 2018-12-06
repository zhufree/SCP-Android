import scrapy
import csv
from scp_spider.items import ScpSpiderItem

class ScpSpider(scrapy.Spider): #需要继承scrapy.Spider类
    
    name = "scp" # 定义蜘蛛名
    # allowed_domains = 'scp-wiki-cn.wikidot.com'

    start_urls = [  #另外一种写法，无需定义start_requests方法
        # scp系列1-5
        'http://scp-wiki-cn.wikidot.com/scp-series',
        'http://scp-wiki-cn.wikidot.com/scp-series-2',
        'http://scp-wiki-cn.wikidot.com/scp-series-3',
        'http://scp-wiki-cn.wikidot.com/scp-series-4',
    ]


    spider_header = {
    'user-agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36'}

    

    def parse(self, response):
        uls = response.css('div#page-content ul')
        for ul in uls[1:-3]:
            for li in ul.css('li'):
                link = li.css('a::attr(href)').extract_first()
                new_scp = ScpSpiderItem(
                    title = '',
                    link = '',
                    cn = '',
                    scp_type= '',
                    detail='',
                    not_found = '',
                    author = '',
                    desc = '',
                    snippet = '',
                    subtext = '',
                    contest_name = '',
                    contest_link = '',
                    created_time = '',
                    month = '',
                    event_type = '',
                    page_code = '',
                )
                new_scp['title'] = ''.join(li.css('::text').extract())
                new_scp['link'] = link
                new_scp['cn'] = 'false'
                new_scp['scp_type'] = 'series'
                
                detail_request =  scrapy.Request(response.urljoin(link), callback=self.parse_detail, headers = self.spider_header)
                detail_request.meta['item'] = new_scp
                yield detail_request
        #         self.scp_list.append(new_article)
        # self.write_to_csv(self.scp_list, 'scp_files/scp_list.csv')
        # self.log('保存文件')

    def parse_detail(self, response):
        item = response.meta['item']
        detail_dom = response.css('div#page-content')[0]
        # for category in total_scps_list:
        #     if category['link'] == link:
        #         category['not_found'] = "false"
        #         category['detail'] = detail_dom.html().replace('  ', '').replace('\n', '')
        item['detail'] = detail_dom.extract().replace('  ', '').replace('\n', '')
        yield item
        # a_in_detail = detail_dom.remove('.footer-wikiwalk-nav')('a')
        # if len(list(a_in_detail.items())) > 30:
        #     return
        # for a in a_in_detail.items():
        #     href = a.attr('href')
        #     if href.startswith('/') and href not in total_link_list:
        #         print('new link = ' + href)
        #         new_link.append(href)
        #         new_found_link_list.append(href)
        #         title = a.text()
        #         new_category = {
        #             'title': title,
        #             'link': href,
        #             'type': 'none'
        #         }
        #         new_found_category_list.append(new_category)


