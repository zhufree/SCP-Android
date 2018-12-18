# coding: utf-8

from pyquery import PyQuery as pq
import csv
import multiprocessing
import os
import sys
from scp_file_handler import *

spider_header = {
    'user-agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36'}

link_list = []
new_found_link_list = [] # 从正文抓到的所有新link
new_found_category_list = [] # 从正文抓到的所有新文章

# 抓取所有目录信息之后，把所有链接保存到列表，然后遍历链接抓正文，对于正文中的链接，如果再列表里就不抓，否则就再抓一篇
# 抓取正文内容方法，顺便如果正文中有别的链接也抓下来
def get_detail(new_article, article_list):
    try:
        if 'http://scp-wiki-cn.wikidot.com' in new_article['link']:
            new_article['link'] = new_article['link'][30:]
        detail_doc = pq('http://scp-wiki-cn.wikidot.com' + new_article['link'])
        new_article['not_found'] = "false"
        detail_dom = list(detail_doc('div#page-content').items())[0]
        new_article['detail'] = detail_dom.html().replace('  ', '').replace('\n', '')
        a_in_detail = detail_dom.remove('.footer-wikiwalk-nav')('a')
        for a in a_in_detail.items():
            href = a.attr('href')
            if "/" in href:
                print(href)
    except:
        new_article['detail'] = "<h3>抱歉，该页面尚无内容</h3>"
        new_article['not_found'] = "true"

        
# scp系列
def thread_get_series(i):
    article_list = []
    if (i == 1):
        doc = pq('http://scp-wiki-cn.wikidot.com/scp-series', headers=spider_header)
    else:
        doc = pq('http://scp-wiki-cn.wikidot.com/scp-series-' + str(i), headers=spider_header)
    for ul in list(doc('div#page-content ul').items())[1:-3]:
        for li in ul('li').items():
            link = li('a').attr('href')
            new_article = {
                'title': li.text(),
                'link': link,
                'cn': 'false',
                'type': 'series'
            }
            # link_list.append(link)
            print(link)
            article_list.append(new_article)

    return article_list
    # write_to_csv(article_list, 'scps/series-' + str(i) + '.csv')

# scpCn系列，抓一次，无多线程
def get_series_cn():
    article_list = []
    doc = pq('http://scp-wiki-cn.wikidot.com/scp-series-cn-2', headers=spider_header)
    for ul in list(doc('div#page-content ul').items())[1:-1]:
        for li in ul('li').items():
            link = li('a').attr('href')
            new_article = {
                'title': li.text(),
                'link': link,
                'cn': 'true',
                'scp_type': 'series'
            }
            # link_list.append(link)
            print(link)
            article_list.append(new_article)
    return article_list
    # write_to_csv(article_list, 'scps/series-cn.csv')

# 归档信息
def thread_get_archives(doc_link, pq_path, cn, archives_type):
    article_list = []
    doc = pq(doc_link, headers=spider_header)
    for li in list(doc(pq_path).items()):
        link = li('a').attr('href')
        new_article = {
            'title': li.text(),
            'link': link,
            'cn': cn,
            'type': archives_type
        }
        # link_list.append(link)
        print(link)
        article_list.append(new_article)
    return article_list
    # if cn == 'true':
    #     write_to_csv(article_list, 'scps/archives-'+ archives_type + '-cn.csv')
    # else:
    #     write_to_csv(article_list, 'scps/archives-'+ archives_type + '.csv')

# 故事版
def thread_get_story(i):
    article_list = []
    story_count = 0
    doc = pq('http://scp-wiki-cn.wikidot.com/scp-series-' + str(i) + '-tales-edition', headers=spider_header)
    for story_li in doc('div.content-panel>ul>li').items():
        new_article = {}
        new_article['link'] = story_li('a').attr('href')
        sub_list = list(story_li('ul').items())
        if len(sub_list) > 0:
            sub_article_count = 1
            new_article['number'] = str(story_count) + "-0"
            for sub_li in story_li('ul>li').items():
                new_sub_article = {}
                new_sub_article['link'] = sub_li('a').attr('href')
                new_sub_article['title'] = sub_li.text()
                new_sub_article['number'] = str(story_count) + "-" + str(sub_article_count)
                new_sub_article['story_num'] = str(i)
                new_sub_article['type'] = 'story'
                # link_list.append(new_sub_article['link'])
                print(new_sub_article['link'])
                article_list.append(new_sub_article)
                sub_article_count += 1
        else:
            new_article['number'] = str(story_count)
        new_article['title'] = story_li.remove('ul').text()
        new_article['story_num'] = str(i)
        new_article['type'] = 'story'
        # link_list.append(new_article['link'])
        print(new_article['link'])
        article_list.append(new_article)
        story_count += 1
    return article_list
    # write_to_csv(article_list, 'scps/series-story-'+str(i) + '.csv')

# 故事系列
def thread_get_story_series(cn, i):
    article_list = []
    if cn == 'true':
        doc = pq('http://scp-wiki-cn.wikidot.com/series-archive-cn', headers=spider_header)
    else:
        doc = pq('http://scp-wiki-cn.wikidot.com/series-archive/p/' + str(i) , headers=spider_header)
    for tr in list(doc('div.list-pages-box tr').items())[1:]:
        new_article = {}
        tds = list(tr('td').items())
        new_article['title'] = tds[0].text()
        new_article['link'] = tds[0]('a').attr('href')
        new_article['author'] = tds[1].text()
        new_article['snippet'] = tds[2].text()
        new_article['cn'] = cn
        new_article['scp_type'] = 'story_series'
        # link_list.append(new_article['link'])
        print(new_article['link'])
        article_list.append(new_article)
    # if cn == 'true':
    #     write_to_csv(article_list, 'scps/story-series-cn.csv')
    # else:
    #     write_to_csv(article_list, 'scps/story-series-' + str(i) + '.csv')
    return article_list

# 基金会故事
def thread_get_tales(cn):
    article_list = []
    letter_list = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
                   'U', 'V', 'W', 'X', 'Y', 'Z', '0-9']
    if cn == 'false':
        doc = pq('http://scp-wiki-cn.wikidot.com/tales-cn-by-page-name', headers=spider_header)
    else:
        doc = pq('http://scp-wiki-cn.wikidot.com/tales-by-page-name', headers=spider_header)
    for i in range(0, 27):
        for section_tr in list(list(doc('div#page-content .section').items())[i]('div.list-pages-box tr').items()):
            new_article = {}
            tds = list(section_tr('td').items())
            new_article['title'] = tds[0].text()
            new_article['link'] = tds[0]('a').attr('href')
            new_article['author'] = tds[1].text()
            new_article['created_time'] = tds[2].text()
            new_article['page_code'] = letter_list[i]
            new_article['cn'] = cn
            new_article['scp_type'] = 'tale'
            # link_list.append(new_article['link'])
            # print(new_article['link'])
            article_list.append(new_article)
    # if cn == 'true':
    #     write_to_csv(article_list, 'scps/tales-cn.csv')
    # else:
    #     write_to_csv(article_list, 'scps/tales.csv')
    return article_list

# 中国原创故事ByTime
def get_tales_cn_by_time():
    article_list = []
    doc = pq('http://scp-wiki-cn.wikidot.com/tales-cn-by-create-time', headers=spider_header)
    for month_div in doc('div#page-content .section').items():
        current_month = month_div('h3').text()
        for trs in month_div('div.list-pages-box tr').items():
            tds = list(trs('td').items())
            new_article = {
                'title': tds[0]('a').text(),
                'link': tds[0]('a').attr('href'),
                'author': tds[1]('span').text(),
                'created_time': tds[2]('span').text(),
                'cn': 'true',
                'scp_type': 'tale',
                'month': current_month
            }
            # link_list.append(new_article['link'])
            print(new_article['link'])
            article_list.append(new_article)
    # write_to_csv(article_list, 'scps/tales-cn-by-time.csv')
    return article_list

# 设定中心
def thread_get_setting(cn):
    article_list = []
    if cn == 'false':
        doc = pq('http://scp-wiki-cn.wikidot.com/canon-hub', headers=spider_header)
    else:
        doc = pq('http://scp-wiki-cn.wikidot.com/canon-hub-cn', headers=spider_header)
    for div in list(doc('div.content-panel').items())[:-1]:
        new_article = {}
        new_article['title'] = div('div.canon-title a').text()
        new_article['link'] = div('div.canon-title a').attr('href')
        new_article['desc'] = div('div.canon-description').text()
        new_article['snippet'] = div('div.canon-snippet').text()
        new_article['subtext'] = div('div.canon-snippet-subtext').text()
        new_article['cn'] = cn
        new_article['scp_type'] = 'setting'
        # link_list.append(new_article['link'])
        print(new_article['link'])
        article_list.append(new_article)
    # if cn == 'true':
    #     write_to_csv(article_list, 'scps/setting-cn.csv')
    # else:
    #     write_to_csv(article_list, 'scps/setting.csv')
    return article_list

# 征文竞赛
def get_contest():
    article_list = []
    last_contest_name = ""
    last_contest_link = ""
    doc = pq('http://scp-wiki-cn.wikidot.com/contest-archive', headers=spider_header)
    for section_tr in list(doc('div#page-content .content-type-description>table tr').items())[2:]:
        new_article = {}
        tds = list(section_tr('td').items())
        current_contest_name = tds[0].text()
        current_contest_link = tds[0]('a').attr('href')
        if current_contest_name != None and len(current_contest_name) > 2:
            last_contest_name = current_contest_name
            last_contest_link = current_contest_link
        else:
            current_contest_name = last_contest_name
            current_contest_link = last_contest_link

        if len(list(tds[2]('br').items())) != 0:
            new_plus_article = {}
            double_a = list(tds[2]('a').items())
            double_author = str(tds[3].html()).split('<br />')
            new_article['title'] = double_a[0].text()
            new_article['link'] = double_a[0].attr('href')
            new_article['author'] = double_author[0].replace(',', '，')
            new_plus_article['title'] = double_a[1].text().replace(',', '，')
            new_plus_article['link'] = double_a[1].attr('href')
            new_plus_article['author'] = double_author[1].replace(',', '，')
            new_article['contest_name'] = current_contest_name.replace(',', '，')
            new_article['contest_link'] = current_contest_link
            new_plus_article['contest_name'] = current_contest_name.replace(',', '，')
            new_plus_article['contest_link'] = tds[0]('a').attr('href')
            new_article['cn'] = 'false'
            new_article['type'] = 'contest'
            new_plus_article['cn'] = 'false'
            new_plus_article['type'] = 'contest'

            # link_list.append(new_article['link'])
            # link_list.append(new_plus_article['link'])

            if new_article['link'] != None:
                print(new_article['link'])
                article_list.append(new_article)
            if new_plus_article['link'] != None:
                print(new_plus_article['link'])
                article_list.append(new_plus_article)
        else:
            new_article['title'] = tds[2].text()
            new_article['link'] = tds[2]('a').attr('href')
            new_article['author'] = tds[3].text().replace(',', '，')
            new_article['contest_name'] = current_contest_name.replace(',', '，')
            new_article['contest_link'] = current_contest_link
            new_article['cn'] = 'false'
            new_article['type'] = 'contest'
            
            if new_article['link'] != None:
                # link_list.append(new_article['link'])
                print(new_article['link'])
                article_list.append(new_article)
    # write_to_csv(article_list, 'scps/contest.csv')
    return article_list

# 中国分部竞赛
def get_contest_cn():
    article_list = []
    doc = pq('http://scp-wiki-cn.wikidot.com/contest-archive-cn', headers=spider_header)
    h3_list = list(doc('div#main-content h3').items())
    for i in range(len(h3_list)):
        h3 = h3_list[i]
        current_p = list(h3.siblings('p').items())[i]
        current_holder = list(current_p('span:first').items())[0]
        for a in current_holder.siblings('a').items():
            new_article = {}
            new_article['title'] = a.text()
            new_article['link'] = a.attr('href')
            new_article['author'] = a.next('span.printuser>a:last').text().replace(',', '，')
            new_article['contest_name'] = h3('span').text().replace(',', '，')
            new_article['contest_link'] = h3('span>a').attr('href')
            new_article['cn'] = 'true'
            new_article['type'] = 'contest'
            # link_list.append(new_article['link'])
            print(new_article['link'])
            article_list.append(new_article)
    # write_to_csv(article_list, 'scps/contest-cn.csv')
    return article_list

# 事故记录
def thread_get_event_record(i):
    article_list = []
    # for i in range(1, 6):
    # 6页，每页5个tab
    doc = pq('http://scp-wiki-cn.wikidot.com/incident-reports-eye-witness-interviews-and-personal-logs/p/' + str(i)
             , headers=spider_header)
    for j in range(0, 5):
        for li in doc('#wiki-tab-0-' + str(j) + ' .list-pages-box>ul>li').items():
            new_article = {}
            new_article['link'] = li('a').attr('href')
            new_article['title'] = li('a').text()
            new_article['type'] = 'event'
            if j == 0:
                new_article['event_type'] = 'lab_record'
            elif j == 1:
                new_article['event_type'] = 'discovery_report'
            elif j == 2:
                new_article['event_type'] = 'event_report'
            elif j == 3:
                new_article['event_type'] = 'interview'
            elif j == 4:
                new_article['event_type'] = 'addon'
            # link_list.append(new_article['link'])
            print(new_article['link'])
            article_list.append(new_article)
    # write_to_csv(article_list, 'scps/event-scps-' + str(i) + '.csv')
    return article_list

def get_archives():
    thread_joke_cn = multiprocessing.Process(target=thread_get_archives, args=('http://scp-wiki-cn.wikidot.com/joke-scps-cn',\
        'div.content-panel>ul>li', 'true', 'joke',))
    thread_joke_cn.start()
    thread_joke_cn.join()
    thread_joke = multiprocessing.Process(target=thread_get_archives, args=('http://scp-wiki-cn.wikidot.com/joke-scps',\
        'div.content-panel>ul>li', 'false', 'joke',))
    thread_joke.start()
    thread_joke.join()
    thread_archived = multiprocessing.Process(target=thread_get_archives, args=('http://scp-wiki-cn.wikidot.com/archived-scps',\
        'div#page-content div.content-panel ul li', 'false', 'archived',))
    thread_archived.start()
    thread_archived.join()
    thread_ex = multiprocessing.Process(target=thread_get_archives, args=('http://scp-wiki-cn.wikidot.com/scp-ex',\
        'div.content-panel>ul>li', 'false', 'ex',))
    thread_ex.start()
    thread_ex.join()
    thread_decommissioned = multiprocessing.Process(target=thread_get_archives, args=('http://scp-wiki-cn.wikidot.com/decommissioned-scps-arc',\
        'div.content-panel>ul>li', 'false', 'decommissioned',))
    thread_decommissioned.start()
    thread_decommissioned.join()
    thread_removed = multiprocessing.Process(target=thread_get_archives, args=('http://scp-wiki-cn.wikidot.com/scp-removed',\
        'div.content-panel>ul>li', 'false', 'removed',))
    thread_removed.start()
    thread_removed.join()

def get_series():
    for i in range(1, 6):
        thread = multiprocessing.Process(target=thread_get_series, args=(i,))
        thread.start()
        thread.join()

# 故事版
def get_series_story():
    for i in range(1, 4):
        thread = multiprocessing.Process(target=thread_get_story, args=(i,))
        thread.start()
        thread.join()

def get_tales():
    # 基金会故事
    thread_tales = multiprocessing.Process(target=thread_get_tales, args=('false',))
    thread_tales.start()
    thread_tales_cn = multiprocessing.Process(target=thread_get_tales, args=('true',))
    thread_tales_cn.start()
    thread_tales.join()
    thread_tales_cn.join()

def get_others():
    # 故事系列
    thread_story_series_cn = multiprocessing.Process(target=thread_get_story_series, args=('true', 0,))
    thread_story_series_cn.start()
    thread_story_series_1 = multiprocessing.Process(target=thread_get_story_series, args=('false', 1,))
    thread_story_series_1.start()
    thread_story_series_2 = multiprocessing.Process(target=thread_get_story_series, args=('false', 2,))
    thread_story_series_2.start()
    # 设定
    thread_setting = multiprocessing.Process(target=thread_get_setting, args=('false',))
    thread_setting.start()
    thread_setting_cn = multiprocessing.Process(target=thread_get_setting, args=('true',))
    thread_setting_cn.start()
    

    # 中国原创故事和contest独立
    thread_tales_cn_by_time = multiprocessing.Process(target=get_tales_cn_by_time)
    thread_tales_cn_by_time.start()
    thread_contest = multiprocessing.Process(target=get_contest)
    thread_contest.start()
    thread_contest_cn = multiprocessing.Process(target=get_contest_cn)
    thread_contest_cn.start()

    thread_story_series_cn.join()
    thread_story_series_1.join()
    thread_story_series_2.join()
    thread_setting.join()
    thread_setting_cn.join()
    thread_tales_cn_by_time.join()
    thread_contest.join()
    thread_contest_cn.join()

def get_events():
    # 事故记录
    for i in range(1, 6):
        thread_event = multiprocessing.Process(target=thread_get_event_record, args=(i,))
        thread_event.start()
        thread_event.join()

# 旧spider，抓取所有目录及正文
def run_old_spider():
    get_archives()
    get_series()
    get_series_cn()
    get_series_story()
    get_tales()
    get_events()
    get_others()

# 新spider先不爬正文，储存基本信息和链接，链接去重后挨个抓正文，
# 正文中有的链接再存储一遍，和之前的链接列表对比再次去重抓取
def run_category_spider():
    total_scp_list = []
    # 把所有链接print到文件里保存
    sys.stdout = Logger("link_list.txt")
    for i in range(1, 6):
        total_scp_list = total_scp_list + thread_get_series(i)
    total_scp_list = total_scp_list + get_series_cn()
    # 故事版不抓
    # for i in range(1, 4):
        # total_scp_list = total_scp_list + thread_get_story(i)
    total_scp_list = total_scp_list + thread_get_tales('false')
    total_scp_list = total_scp_list + thread_get_tales('true')
    # 故事系列
    total_scp_list = total_scp_list + thread_get_story_series('true', 0,)
    total_scp_list = total_scp_list + thread_get_story_series('false', 1,)
    total_scp_list = total_scp_list + thread_get_story_series('false', 2,)
    # 设定
    total_scp_list = total_scp_list + thread_get_setting('false')
    total_scp_list = total_scp_list + thread_get_setting('true')
    
    # 中国原创故事和contest独立
    total_scp_list = total_scp_list + get_tales_cn_by_time()
    total_scp_list = total_scp_list + get_contest()
    total_scp_list = total_scp_list + get_contest_cn()
    for i in range(1, 6):
        total_scp_list = total_scp_list + thread_get_event_record(i)

    total_scp_list = total_scp_list + thread_get_archives('http://scp-wiki-cn.wikidot.com/joke-scps-cn',\
        'div.content-panel>ul>li', 'true', 'joke')
    total_scp_list = total_scp_list + thread_get_archives('http://scp-wiki-cn.wikidot.com/joke-scps',\
        'div.content-panel>ul>li', 'false', 'joke')
    total_scp_list = total_scp_list + thread_get_archives('http://scp-wiki-cn.wikidot.com/archived-scps',\
        'div#page-content div.content-panel ul li', 'false', 'archived')
    total_scp_list = total_scp_list + thread_get_archives('http://scp-wiki-cn.wikidot.com/scp-ex',\
        'div.content-panel>ul>li', 'false', 'ex')
    total_scp_list = total_scp_list + thread_get_archives('http://scp-wiki-cn.wikidot.com/decommissioned-scps-arc',\
        'div.content-panel>ul>li', 'false', 'decommissioned')
    total_scp_list = total_scp_list + thread_get_archives('http://scp-wiki-cn.wikidot.com/scp-removed',\
        'div.content-panel>ul>li', 'false', 'removed')

    write_to_csv(total_scp_list, "scp-category.csv")


class Logger(object):
    def __init__(self, filename="default_log.txt"):
        self.terminal = sys.stdout
        self.log = open(filename, "a")
 
    def write(self, message):
        self.terminal.write(message)
        self.log.write(message)
 
    def flush(self):
        pass

# 根据链接抓取内容，更新到category文件中，同时把正文中新出现的链接记录下来
def get_detail_by_link(link, total_link_list, total_scps_list, new_link):
    try:
        print(link)
        if 'http://scp-wiki-cn.wikidot.com' in link:
            detail_doc = pq(link)
        else:
            detail_doc = pq('http://scp-wiki-cn.wikidot.com' + link)
        detail_dom = list(detail_doc('div#page-content').items())[0]
        for category in total_scps_list:
            if category['link'] == link:
                category['not_found'] = "false"
                category['detail'] = detail_dom.html().replace('  ', '').replace('\n', '')
        a_in_detail = detail_dom.remove('.footer-wikiwalk-nav')('a')
        if len(list(a_in_detail.items())) > 30:
            return
        for a in a_in_detail.items():
            href = a.attr('href')
            if href.startswith('/') and href not in total_link_list:
                print('new link = ' + href)
                new_link.append(href)
                new_found_link_list.append(href)
                title = a.text()
                new_category = {
                    'title': title,
                    'link': href,
                    'type': 'none'
                }
                new_found_category_list.append(new_category)
    except:
        for category in total_scps_list:
            if category['link'] == link:
                category['detail'] = "<h3>抱歉，该页面尚无内容</h3>"
                category['not_found'] = "true"

# 链接列表遍历
def get_detail_by_link_list(link_list, total_link_list, total_scps_list):
    new_link = []
    for link in link_list:
        get_detail_by_link(link, total_link_list, total_scps_list, new_link)
    sys.stdout = Logger("new_found_link_1.txt")
    for l in new_link:
        print(l)

# 获取单篇详情
def get_single_detail(link):
    try:
        if 'http://scp-wiki-cn.wikidot.com' in link:
            detail_doc = pq(link)
        else:
            detail_doc = pq('http://scp-wiki-cn.wikidot.com' + link)
        detail_dom = list(detail_doc('div#page-content').items())[0]
        return detail_dom.html().replace('  ', '').replace('\n', '')
    except:
        return "<h3>抱歉，该页面尚无内容</h3>"

def get_detail_order():
    # 第一次爬虫跑完所有的链接
    total_link_list = get_list_from_file('link_list.txt') # 去重
    # 第一次爬虫跑完所有的scp
    total_scps_list = get_scps_from_file("scp-category.csv")
    print('real list size:' + str(len(total_link_list)))
    print('category list size:' + str(len(total_scps_list)))
    # for link in total_link_list:
    #     print(link)
    #     get_detail_by_link(link, total_link_list, total_scps_list)

    sys.stdout = Logger("new_found_link.txt")
    get_detail_by_link(total_link_list[1000:2000], total_link_list, total_scps_list)

    write_to_csv(new_found_category_list, "new-found-category-2.csv")
    write_to_csv(total_scps_list, "scp-category-new-2.csv")


# if __name__ == '__main__':
    
    