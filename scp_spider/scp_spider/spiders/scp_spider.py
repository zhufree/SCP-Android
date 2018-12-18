import scrapy
import csv
from pyquery import PyQuery as pq
from scp_spider.items import *

class ScpSpider(scrapy.Spider): #需要继承scrapy.Spider类
    
    name = "scp" # 定义蜘蛛名
    # allowed_domains = 'scp-wiki-cn.wikidot.com'

    start_urls = [
        # scp系列1-5
        # 'http://scp-wiki-cn.wikidot.com/scp-series',
        # 'http://scp-wiki-cn.wikidot.com/scp-series-2',
        # 'http://scp-wiki-cn.wikidot.com/scp-series-3',
        # 'http://scp-wiki-cn.wikidot.com/scp-series-4',
        # 'http://scp-wiki-cn.wikidot.com/scp-series-5',
        # scp cn 系列
        # 'http://scp-wiki-cn.wikidot.com/scp-series-cn',
        # 'http://scp-wiki-cn.wikidot.com/scp-series-cn-2',
        # tag
        # 'http://scp-wiki-cn.wikidot.com/system:page-tags/',
    ]


    spider_header = {
    'user-agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36'}

    

    def parse(self, response):
        pq_doc = pq(response.body)
        # uls = response.css('div#page-content ul')
        # for ul in uls[1:-3]:
        #     for li in ul.css('li'):
        #         link = li.css('a::attr(href)').extract_first()
        #         new_scp = ScpSpiderItem(
        #             title = '',
        #             link = '',
        #             cn = '',
        #             scp_type= '',
        #             detail='',
        #             not_found = '',
        #             author = '',
        #             desc = '',
        #             snippet = '',
        #             subtext = '',
        #             contest_name = '',
        #             contest_link = '',
        #             created_time = '',
        #             month = '',
        #             event_type = '',
        #             page_code = '',
        #         )   
        #         new_scp['title'] = ''.join(li.css('::text').extract())
        #         new_scp['link'] = link
        #         new_scp['cn'] = 'true'
        #         new_scp['scp_type'] = 'series'
                # detail_request =  scrapy.Request(response.urljoin(link), callback=self.parse_detail, headers = self.spider_header)
                # detail_request.meta['item'] = new_scp
                # yield detail_request
                # yield new_scp
        for a in pq_doc('div.pages-tag-cloud-box a').items():
            tag_name = a.text()
            link = a.attr('href')
            tag_request = scrapy.Request(response.urljoin(link), callback=self.parse_tag, headers = self.spider_header)
            tag_request.meta['tag_name'] = tag_name 
            yield tag_request


               
        #         self.scp_list.append(new_article)
        # self.write_to_csv(self.scp_list, 'scp_files/scp_list.csv')
        # self.log('保存文件')

    def parse_detail(self, response):
        item = response.meta['item']
        if response.status != 404:
            detail_dom = response.css('div#page-content')[0]
            item['detail'] = detail_dom.extract().replace('  ', '').replace('\n', '')
            item['not_found'] = 'false'
        else:
            item['detail'] = "<h3>抱歉，该页面尚无内容</h3>"
            item['not_found'] = 'true'
        # for category in total_scps_list:
        #     if category['link'] == link:
        #         category['not_found'] = "false"
        #         category['detail'] = detail_dom.html().replace('  ', '').replace('\n', '')
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

    def parse_tag(self, response):
        pq_doc = pq(response.body)
        tag_name = response.meta['tag_name']
        for article_div in pq_doc('div#tagged-pages-list div.pages-list-item').items():
            new_article = ScpArticleItem(
                title = article_div.text(),
                link = article_div('a').attr('href'),
                tags = tag_name,
                detail = ''
                )
            yield new_article

