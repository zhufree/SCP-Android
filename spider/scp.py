# coding: utf-8

# from pyquery import PyQuery as pq
import csv
import multiprocessing
import os
spider_header = {
    'user-agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36'}

def get_detail(new_article):
    try:
        if 'http://scp-wiki-cn.wikidot.com' in new_article['link']:
            new_article['link'] = new_article['link'][30:]
        detail_doc = pq('http://scp-wiki-cn.wikidot.com' + new_article['link'])
        new_article['not_found'] = "false"
        new_article['detail'] = list(detail_doc('div#page-content').items())[0].html() \
            .replace('  ', '').replace('\n', '') \
            .replace('style="display: none;"', '').replace('style="display:none"', '').replace('style="display:none;', '')
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
            try:
                detail_doc = pq('http://scp-wiki-cn.wikidot.com' + link)
                new_article['not_found'] = "false"
                new_article['detail'] = list(detail_doc('div#page-content').items())[0].html() \
                    .replace('  ', '').replace('\n', '') \
                    .replace('style="display: none;"', '').replace('style="display:none"', '').replace('style="display:none;', '')
            except:
                new_article['detail'] = "<h3>抱歉，该页面尚无内容</h3>"
                new_article['not_found'] = "true"
            print(new_article['type'] + link)
            article_list.append(new_article)

    write_to_csv(article_list, 'scps/series-' + str(i) + '.csv')

# scpCn系列，抓一次，无多线程
def get_series_cn():
    article_list = []
    doc = pq('http://scp-wiki-cn.wikidot.com/scp-series-cn', headers=spider_header)
    for ul in list(doc('div#page-content ul').items())[1:-1]:
        for li in ul('li').items():
            link = li('a').attr('href')
            new_article = {
                'title': li.text(),
                'link': link,
                'cn': 'true',
                'type': 'series'
            }
            try:
                detail_doc = pq('http://scp-wiki-cn.wikidot.com' + link)
                new_article['not_found'] = "false"
                new_article['detail'] = list(detail_doc('div#page-content').items())[0].html() \
                    .replace('  ', '').replace('\n', '') \
                    .replace('style="display: none;"', '').replace('style="display:none"', '').replace('style="display:none;', '')
            except:
                new_article['detail'] = "<h3>抱歉，该页面尚无内容</h3>"
                new_article['not_found'] = "true"
            print(new_article['type'] + link)
            article_list.append(new_article)
    write_to_csv(article_list, 'scps/series-cn.csv')

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
        try:
            detail_doc = pq('http://scp-wiki-cn.wikidot.com' + link)
            new_article['not_found'] = "false"
            new_article['detail'] = list(detail_doc('div#page-content').items())[0].html() \
                .replace('  ', '').replace('\n', '') \
                .replace('style="display: none;"', '').replace('style="display:none"', '').replace('style="display:none;', '')
        except:
            new_article['detail'] = "<h3>抱歉，该页面尚无内容</h3>"
            new_article['not_found'] = "true"
        print(new_article['type'] + link)
        article_list.append(new_article)
    if cn == 'true':
        write_to_csv(article_list, 'scps/archives-'+ archives_type + '-cn.csv')
    else:
        write_to_csv(article_list, 'scps/archives-'+ archives_type + '.csv')

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
                try:
                    detail_doc = pq('http://scp-wiki-cn.wikidot.com' + new_sub_article['link'])
                    new_sub_article['not_found'] = "false"
                    new_sub_article['detail'] = list(detail_doc('div#page-content').items())[0].html() \
                        .replace('  ', '').replace('\n', '') \
                        .replace('style="display: none;"', '').replace('style="display:none"', '').replace('style="display:none;', '')
                except:
                    new_sub_article['detail'] = "<h3>抱歉，该页面尚无内容</h3>"
                    new_sub_article['not_found'] = "true"
                print(new_sub_article['type'] + new_sub_article['link'])
                article_list.append(new_sub_article)
                sub_article_count += 1
        else:
            new_article['number'] = str(story_count)
        new_article['title'] = story_li.remove('ul').text()
        new_article['story_num'] = str(i)
        new_article['type'] = 'story'
        try:
            detail_doc = pq('http://scp-wiki-cn.wikidot.com' + new_article['link'])
            new_article['not_found'] = "false"
            new_article['detail'] = list(detail_doc('div#page-content').items())[0].html() \
                .replace('  ', '').replace('\n', '') \
                .replace('style="display: none;"', '').replace('style="display:none"', '').replace('style="display:none;', '')
        except:
            new_article['detail'] = "<h3>抱歉，该页面尚无内容</h3>"
            new_article['not_found'] = "true"
        print(new_article['type'] + new_article['link'])
        article_list.append(new_article)
        story_count += 1

    write_to_csv(article_list, 'scps/series-story-'+str(i) + '.csv')

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
        new_article['type'] = 'story_series'
        try:
            detail_doc = pq('http://scp-wiki-cn.wikidot.com' + new_article['link'])
            new_article['not_found'] = "false"
            new_article['detail'] = list(detail_doc('div#page-content').items())[0].html() \
                .replace('  ', '').replace('\n', '') \
                .replace('style="display: none;"', '').replace('style="display:none"', '').replace('style="display:none;', '')
        except:
            new_article['detail'] = "<h3>抱歉，该页面尚无内容</h3>"
            new_article['not_found'] = "true"
        print(new_article['type'] +' ' + new_article['link'])
        article_list.append(new_article)
    if cn == 'true':
        write_to_csv(article_list, 'scps/story-series-cn.csv')
    else:
        write_to_csv(article_list, 'scps/story-series-' + str(i) + '.csv')

# 基金会故事
def thread_get_tales(cn):
    article_list = []
    letter_list = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
                   'U', 'V', 'W', 'X', 'Y', 'Z', '0-9']
    if cn == 'true':
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
            new_article['type'] = 'tale'
            try:
                detail_doc = pq('http://scp-wiki-cn.wikidot.com' + new_article['link'])
                new_article['not_found'] = "false"
                new_article['detail'] = list(detail_doc('div#page-content').items())[0].html() \
                    .replace('  ', '').replace('\n', '') \
                    .replace('style="display: none;"', '').replace('style="display:none"', '').replace('style="display:none;', '')
            except:
                new_article['detail'] = "<h3>抱歉，该页面尚无内容</h3>"
                new_article['not_found'] = "true"
            print(new_article['type'] +' ' + new_article['link'])
            article_list.append(new_article)
    if cn == 'true':
        write_to_csv(article_list, 'scps/tales-cn.csv')
    else:
        write_to_csv(article_list, 'scps/tales.csv')

# 中国原创故事ByTime
def get_tales_cn_by_time():
    article_list = []
    doc = pq('http://scp-wiki-cn.wikidot.com/tales-cn-by-create-time', headers=spider_header)
    for month_div in doc('div#page-content .section').items():
        current_month = month_div('h3').text()
        print('current_month = ' + current_month)
        for trs in month_div('div.list-pages-box tr').items():
            tds = list(trs('td').items())
            new_article = {
                'title': tds[0]('a').text(),
                'link': tds[0]('a').attr('href'),
                'author': tds[1]('span').text(),
                'created_time': tds[2]('span').text(),
                'cn': 'true',
                'type': 'tale_by_time',
                'month': current_month
            }
            try:
                detail_doc = pq('http://scp-wiki-cn.wikidot.com' + new_article['link'])
                new_article['not_found'] = "false"
                new_article['detail'] = list(detail_doc('div#page-content').items())[0].html() \
                    .replace('  ', '').replace('\n', '') \
                    .replace('style="display: none;"', '').replace('style="display:none"', '').replace('style="display:none;', '')
            except:
                new_article['detail'] = "<h3>抱歉，该页面尚无内容</h3>"
                new_article['not_found'] = "true"
            print(new_article['type'] +' ' + new_article['link'])
            article_list.append(new_article)
    write_to_csv(article_list, 'scps/tales-cn-by-time.csv')

# 设定中心
def thread_get_setting(cn):
    article_list = []
    if cn == 'true':
        doc = pq('http://scp-wiki-cn.wikidot.com/canon-hub', headers=spider_header)
    else:
        doc = pq('http://scp-wiki-cn.wikidot.com/canon-hub-cn', headers=spider_header)
    for div in list(doc('div.content-panel').items())[:-1]:
        new_article = {}
        new_article['title'] = div('div.canon-title>p').text()
        new_article['link'] = div('div.canon-title>p>a').attr('href')
        new_article['desc'] = div('div.canon-description').text()
        new_article['snippet'] = div('div.canon-snippet').text()
        new_article['subtext'] = div('div.canon-snippet-subtext').text()
        new_article['cn'] = cn
        new_article['type'] = 'setting'
        try:
            detail_doc = pq('http://scp-wiki-cn.wikidot.com' + new_article['link'])
            new_article['detail'] = list(detail_doc('div#page-content').items())[0].html() \
                .replace('  ', '').replace('\n', '') \
                .replace('style="display: none;"', '').replace('style="display:none"', '').replace('style="display:none;', '')
            new_article['not_found'] = "false"
        except:
            new_article['detail'] = "<h3>抱歉，该页面尚无内容</h3>"
            new_article['not_found'] = "true"
        print(new_article['type'] +' ' + new_article['link'])
        article_list.append(new_article)
    if cn == 'true':
        write_to_csv(article_list, 'scps/setting-cn.csv')
    else:
        write_to_csv(article_list, 'scps/setting.csv')

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
            try:
                detail_doc = pq('http://scp-wiki-cn.wikidot.com' + new_article['link'])
                new_article['detail'] = list(detail_doc('div#page-content').items())[0].html() \
                    .replace('  ', '').replace('\n', '') \
                    .replace('style="display: none;"', '').replace('style="display:none"', '').replace('style="display:none;', '')
                new_article['not_found'] = "false"
            except:
                new_article['detail'] = "<h3>抱歉，该页面尚无内容</h3>"
                new_article['not_found'] = "true"
            print(new_article['type'] +' ' + new_article['link'])
            try:
                detail_doc = pq('http://scp-wiki-cn.wikidot.com' + new_plus_article['link'])
                new_plus_article['detail'] = list(detail_doc('div#page-content').items())[0].html() \
                    .replace('  ', '').replace('\n', '') \
                    .replace('style="display: none;"', '').replace('style="display:none"', '').replace('style="display:none;', '')
                new_plus_article['not_found'] = "false"
            except:
                new_plus_article['detail'] = "<h3>抱歉，该页面尚无内容</h3>"
                new_plus_article['not_found'] = "true"
            print(new_plus_article['type'] +' ' + new_plus_article['link'])
            if new_article['link'] != None:
                article_list.append(new_article)
            if new_plus_article['link'] != None:
                article_list.append(new_plus_article)
        else:
            new_article['title'] = tds[2].text()
            new_article['link'] = tds[2]('a').attr('href')
            new_article['author'] = tds[3].text().replace(',', '，')
            new_article['contest_name'] = current_contest_name.replace(',', '，')
            new_article['contest_link'] = current_contest_link
            new_article['cn'] = 'false'
            new_article['type'] = 'contest'
            try:
                detail_doc = pq('http://scp-wiki-cn.wikidot.com' + new_article['link'])
                new_article['detail'] = list(detail_doc('div#page-content').items())[0].html() \
                    .replace('  ', '').replace('\n', '') \
                    .replace('style="display: none;"', '').replace('style="display:none"', '').replace('style="display:none;', '')
                new_article['not_found'] = "false"
            except:
                new_article['detail'] = "<h3>抱歉，该页面尚无内容</h3>"
                new_article['not_found'] = "true"
            if new_article['link'] != None:
                print(new_article['type'] +' ' + new_article['link'])
                article_list.append(new_article)
    write_to_csv(article_list, 'scps/contest.csv')

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
            try:
                detail_doc = pq('http://scp-wiki-cn.wikidot.com' + new_article['link'])
                new_article['detail'] = list(detail_doc('div#page-content').items())[0].html() \
                    .replace('  ', '').replace('\n', '') \
                    .replace('style="display: none;"', '').replace('style="display:none"', '').replace('style="display:none;', '')
                new_article['not_found'] = "false"
            except:
                new_article['detail'] = "<h3>抱歉，该页面尚无内容</h3>"
                new_article['not_found'] = "true"
            print(new_article['type'] +' ' + new_article['link'])
            article_list.append(new_article)
    write_to_csv(article_list, 'scps/contest-cn.csv')

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
            try:
                detail_doc = pq('http://scp-wiki-cn.wikidot.com' + new_article['link'])
                new_article['detail'] = list(detail_doc('div#page-content').items())[0].html() \
                    .replace('  ', '').replace('\n', '') \
                    .replace('style="display: none;"', '').replace('style="display:none"', '').replace('style="display:none;', '')
                new_article['not_found'] = "false"
            except:
                new_article['detail'] = "<h3>抱歉，该页面尚无内容</h3>"
                new_article['not_found'] = "true"
            print(new_article['event_type'] + ' ' + new_article['link'])
            article_list.append(new_article)
    write_to_csv(article_list, 'scps/event-scps-' + str(i) + '.csv')

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

# 合并文件，把前缀相同的文件合并成一个
def merge_files(file_name_list, file_prefix):
    with open(file_name_list[0], 'r+', encoding='utf-8') as f:
        append_str = f.read()
    for i in range(1, len(file_name_list)):
        with open(file_name_list[i], 'r+', encoding='utf-8') as f:
            next(f)
            append_str += f.read()
    with open(file_prefix + '-merge.csv', 'w+', encoding='utf-8') as f:
        f.write(append_str)
    

def merge_all_file():
    files = ['scps/' + name for name in os.listdir('scps/')]
    print(files)
    merge_files(files[0:13], 'scp-1') # 7m
    merge_files(files[13:19], 'scp-2') # 40m
    merge_files(files[19:27], 'scp-3') # 31m
    merge_files(files[27:], 'scp-4') # 31m


def write_to_csv(article_list, file_name):
    with open(file_name, 'w+', encoding='utf-8', newline='') as f:
        # 统一header，方便后续合并文件一起上传
        writer = csv.DictWriter(f, ['link', 'title', 'type', 'detail', 'cn', 'not_found', \
            'author', 'desc', 'snippet', 'subtext', 'contest_name', 'contest_link', \
            'created_time', 'month', 'event_type', 'page_code', 'number', 'story_num'])
        writer.writeheader()
        writer.writerows(article_list)


if __name__ == '__main__':
    # get_series()
    # get_series_cn()
    # get_archives()
    # get_series_story()
    # get_tales()
    # get_tales_cn()
    # get_tales_cn_by_time()
    # get_others()
    # get_events()
    merge_all_file()
