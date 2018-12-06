# -*- coding: utf-8 -*-
from scrapy.exceptions import DropItem
import csv
# Define your item pipelines here
#
# Don't forget to add your pipeline to the ITEM_PIPELINES setting
# See: https://doc.scrapy.org/en/latest/topics/item-pipeline.html


class ScpSpiderPipeline(object):
    def process_item(self, item, spider):
        raw_list = self.get_scps_from_file('scp_files/scp_list.csv')
        raw_list.append(dict(item))
        self.write_to_csv(raw_list, 'scp_files/scp_list.csv')
        return item

    def parse_detail(self, response):
        detail_dom = response.css('div#page-content')[0]
        # for category in total_scps_list:
        #     if category['link'] == link:
        #         category['not_found'] = "false"
        #         category['detail'] = detail_dom.html().replace('  ', '').replace('\n', '')
        print(detail_dom.css('::text')).extract()
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

    def item_completed(self, results, item, info):
        return item

    def write_to_csv(self, article_list, file_name):
        with open(file_name, 'w+', encoding='utf-8', newline='') as f:
            # 统一header，方便后续合并文件一起上传
            writer = csv.DictWriter(f, ['link', 'title', 'scp_type', 'detail', 'cn', 'not_found', \
                'author', 'desc', 'snippet', 'subtext', 'contest_name', 'contest_link', \
                'created_time', 'month', 'event_type', 'page_code'])
            writer.writeheader()
            writer.writerows(article_list)
    def get_scps_from_file(self, filename):
        with open(filename, 'r', encoding='utf-8', newline='') as f:
            # 统一header，方便后续合并文件一起上传
            reader = csv.DictReader(f)
            category_list = [dict(order_dict) for order_dict in reader]
            return category_list

