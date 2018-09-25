from pyquery import PyQuery as pq

def get_latest_article():
    article_list = []
    # doc = pq('http://scp-wiki-cn.wikidot.com/most-recently-created-cn/p/1')
    doc = pq('http://scp-wiki-cn.wikidot.com/most-recently-created-translated/p/1')
    for i in list(doc('table>tr').items())[2:]:
        new_article = {}
        info_list = list(i('td').items())
        new_article['title'] = info_list[0]('a').text()
        new_article['link'] = info_list[0]('a').attr('href')
        new_article['create_time'] = info_list[1]('span').text()
        new_article['rank'] = info_list[2].text()
        print(new_article)
        article_list.append(new_article)

def get_series():
    article_list = []
    for i in range(1, 6):
        if i > 1:
            doc = pq('http://scp-wiki-cn.wikidot.com/scp-series-'+str(i))
        else:
            doc = pq('http://scp-wiki-cn.wikidot.com/scp-series')
        for ul in list(doc('div#page-content ul').items())[1:-3]:
            for li in ul('li').items():
                new_article = {}
                new_article['link'] = li('a').attr('href')
                new_article['title'] = li.text()
                new_article['cn'] = 'false'
                new_article['type'] = 'series'
                print(new_article)
                article_list.append(new_article)
    
    write_to_csv(article_list, 'scp-series.csv')

def get_series_cn():
    article_list = []
    doc = pq('http://scp-wiki-cn.wikidot.com/scp-series-cn')
    for ul in list(doc('div#page-content ul').items())[1:-1]:
        for li in ul('li').items():
            new_article = {}
            new_article['title'] = li.text()
            new_article['link'] = li('a').attr('href')
            new_article['cn'] = 'true'
            new_article['type'] = 'series'
            print(new_article)
            article_list.append(new_article)
    write_to_csv(article_list, 'scp-series-cn.csv')

def get_joke_scp_cn():
    article_list = []
    doc = pq('http://scp-wiki-cn.wikidot.com/joke-scps-cn')
    for li in list(doc('div.content-panel>ul>li').items()):
        new_article = {}
        new_article['title'] = li.text()
        new_article['link'] = li('a').attr('href')
        new_article['cn'] = 'true'
        new_article['type'] = 'joke'
        print(new_article)
        article_list.append(new_article)
    write_to_csv(article_list, 'joke-scps-cn.csv')

def get_joke_scp():
    article_list = []
    doc = pq('http://scp-wiki-cn.wikidot.com/joke-scps')
    for li in list(doc('div.content-panel>ul>li').items()):
        new_article = {}
        new_article['title'] = li.text()
        new_article['link'] = li('a').attr('href')
        new_article['cn'] = 'false'
        new_article['type'] = 'joke'
        print(new_article)
        article_list.append(new_article)
    write_to_csv(article_list, 'joke-scps.csv')

# 被归档的SCP项目
def get_archived_scp():
    article_list = []
    doc = pq('http://scp-wiki-cn.wikidot.com/archived-scps')
    for li in list(doc('div#page-content div.content-panel ul li').items()):
        new_article = {}
        new_article['title'] = li.text()
        new_article['link'] = li('a').attr('href')
        new_article['cn'] = 'false'
        new_article['type'] = 'archived'
        print(new_article)
        article_list.append(new_article)
    write_to_csv(article_list, 'archived-scps.csv')

# 已解明SCP
def get_ex_scp():
    article_list = []
    doc = pq('http://scp-wiki-cn.wikidot.com/scp-ex')
    # doc = pq('http://scp-wiki-cn.wikidot.com/scp-series-2')
    for li in list(doc('div.content-panel>ul>li').items()):
        new_article = {}
        new_article['title'] = li.text()
        new_article['link'] = li('a').attr('href')
        new_article['cn'] = 'false'
        new_article['type'] = 'ex'
        print(new_article)
        article_list.append(new_article)
    write_to_csv(article_list, 'ex-scps.csv')

# 废除scp（这个页面本身也被废除
def get_decommissioned_scps():
    article_list = []
    doc = pq('http://scp-wiki-cn.wikidot.com/decommissioned-scps-arc')
    for li in list(doc('div.content-panel ul li').items()):
        new_article = {}
        new_article['title'] = li.text()
        new_article['link'] = li('a').attr('href')
        new_article['cn'] = 'false'
        new_article['type'] = 'decommissioned'
        print(new_article)
        article_list.append(new_article)
    write_to_csv(article_list, 'decommissioned-scps.csv')

def get_removed_scp():
    article_list = []
    doc = pq('http://scp-wiki-cn.wikidot.com/scp-removed')
    for li in list(doc('div.content-panel ul li').items()):
        new_article = {}
        new_article['title'] = li.text()
        new_article['link'] = li('a').attr('href')
        new_article['cn'] = 'false'
        new_article['type'] = 'removed'
        print(new_article)
        article_list.append(new_article)
    write_to_csv(article_list, 'removed-scps.csv')

def get_series_story():
    article_list = []
    story_count = 0
    for i in range(1, 4):
        doc = pq('http://scp-wiki-cn.wikidot.com/scp-series-' + str(i) + '-tales-edition')
        for story_li in doc('div.content-panel>ul>li').items():
            new_article = {}
            new_article['link'] = story_li('a').attr('href')
            sub_lis = list(story_li('ul').items())
            if len(sub_lis) > 0:
                sub_article_count = 1
                new_article['number'] = str(story_count) + "-0"
                for sub_li in story_li('ul>li').items():
                    new_sub_article = {}
                    new_sub_article['link'] = sub_li('a').attr('href')
                    new_sub_article['title'] = sub_li.text()
                    new_sub_article['number'] = str(story_count) + "-" + str(sub_article_count)
                    new_sub_article['story_num'] = str(i)
                    sub_article_count += 1
                    print(new_sub_article)
                    article_list.append(new_sub_article)
            else:
                new_article['number'] = str(story_count)
            new_article['title'] = story_li.remove('ul').text()
            new_article['story_num'] = str(i)
            story_count += 1
            print(new_article)
            article_list.append(new_article)

    write_series_story_to_csv(article_list, 'series-story.csv')

def get_series_archived():
    article_list = []
    # for i in range(1,3):
    doc = pq('http://scp-wiki-cn.wikidot.com/series-archive-cn')
    for tr in list(doc('div.list-pages-box tr').items())[1:]:
        new_article = {}
        tds = list(tr('td').items())
        new_article['title'] = tds[0].text()
        new_article['link'] = tds[0]('a').attr('href')
        new_article['author'] = tds[1].text()
        new_article['snippet'] = tds[2].text()
        print(new_article)
        article_list.append(new_article)
    write_series_to_csv(article_list, 'scp-story-series-cn.csv')

def get_tales():
    article_list = []
    letter_list = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',\
    'V', 'W', 'X', 'Y','Z', '0-9']
    doc = pq('http://scp-wiki-cn.wikidot.com/tales-by-page-name')
    for i in range(0, 27):
        for section_tr in list(list(doc('div#page-content .section').items())[i]('div.list-pages-box tr').items()):
            new_article = {}
            tds = list(section_tr('td').items())
            new_article['title'] = tds[0].text()
            new_article['link'] = tds[0]('a').attr('href')
            new_article['author'] = tds[1].text()
            new_article['created_time'] = tds[2].text()
            new_article['page_code'] = letter_list[i]
            new_article['cn'] = 'false'
            print(new_article)
            article_list.append(new_article)
    write_tales_to_csv(article_list, 'scp-tales.csv')

def get_tales_cn():
    article_list = []
    letter_list = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U',\
    'V', 'W', 'X', 'Y','Z', '0-9']
    doc = pq('http://scp-wiki-cn.wikidot.com/tales-cn-by-page-name')
    for i in range(0, 27):
        for section_tr in list(list(doc('div#page-content .section').items())[i]('div.list-pages-box tr').items()):
            new_article = {}
            tds = list(section_tr('td').items())
            new_article['title'] = tds[0].text()
            new_article['link'] = tds[0]('a').attr('href')
            new_article['author'] = tds[1].text()
            new_article['created_time'] = tds[2].text()
            new_article['page_code'] = letter_list[i]
            new_article['cn'] = 'true'
            print(new_article)
            article_list.append(new_article)
    write_tales_to_csv(article_list, 'scp-tales-cn.csv')

def get_setting():
    article_list = []
    doc = pq('http://scp-wiki-cn.wikidot.com/canon-hub')
    for div in list(doc('div.content-panel').items())[:-1]:
        new_article = {}
        new_article['title'] = div('div.canon-title>p').text()
        new_article['link'] = div('div.canon-title>p>a').attr('href')
        new_article['desc'] = div('div.canon-description').text()
        new_article['snippet'] = div('div.canon-snippet').text()
        new_article['subtext'] = div('div.canon-snippet-subtext').text()
        new_article['cn'] = 'false'
        new_article['type'] = 'setting'
        print(new_article)
        article_list.append(new_article)
    write_setting_to_csv(article_list, 'scp-setting.csv')

def get_setting_cn():
    article_list = []
    doc = pq('http://scp-wiki-cn.wikidot.com/canon-hub-cn')
    for div in list(doc('div.content-panel').items())[:-1]:
        new_article = {}
        new_article['title'] = div('div.canon-title>p').text()
        new_article['link'] = div('div.canon-title>p>a').attr('href')
        new_article['desc'] = div('div.canon-description').text()
        new_article['snippet'] = div('div.canon-snippet').text()
        new_article['subtext'] = div('div.canon-snippet-subtext').text()
        new_article['cn'] = 'true'
        new_article['type'] = 'setting'
        print(new_article)
        article_list.append(new_article)
    write_setting_to_csv(article_list, 'scp-setting-cn.csv')

def get_contest():
    article_list = []
    last_contest_name = ""
    last_contest_link = ""
    doc = pq('http://scp-wiki-cn.wikidot.com/contest-archive')
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
            print(str(tds[3].html()))
            double_author = str(tds[3].html()).split('<br />')
            new_article['title'] = double_a[0].text()
            new_article['link'] = double_a[0].attr('href')
            new_article['author'] = double_author[0]
            new_plus_article['title'] = double_a[1].text()
            new_plus_article['link'] = double_a[1].attr('href')
            new_plus_article['author'] = double_author[1]
            new_article['contest_name'] = current_contest_name
            new_article['contest_link'] = current_contest_link
            new_plus_article['contest_name'] = current_contest_name
            new_plus_article['contest_link'] = tds[0]('a').attr('href')
            new_article['cn'] = 'false'
            new_article['type'] = 'contest'
            new_plus_article['cn'] = 'false'
            new_plus_article['type'] = 'contest'
            print(new_article)
            print(new_plus_article)
            if new_article['link'] != None:
                article_list.append(new_article)
            if new_plus_article['link'] != None:
                article_list.append(new_plus_article)
        else:
            new_article['title'] = tds[2].text()
            new_article['link'] = tds[2]('a').attr('href')
            new_article['author'] = tds[3].text()
            new_article['contest_name'] = current_contest_name
            new_article['contest_link'] = current_contest_link
            new_article['cn'] = 'false'
            new_article['type'] = 'contest'
            print(new_article)
            if new_article['link'] != None:
                article_list.append(new_article)
    write_contest_to_csv(article_list, 'scp-contest.csv')

def get_contest_cn():
    article_list = []
    doc = pq('http://scp-wiki-cn.wikidot.com/contest-archive-cn')
    h3_list = list(doc('div#main-content h3').items())
    for i in range(len(h3_list)):
        h3 = h3_list[i]
        current_p = list(h3.siblings('p').items())[i]
        current_holder = list(current_p('span:first').items())[0]
        for a in current_holder.siblings('a').items():
            new_article = {}
            new_article['title'] = a.text()
            new_article['link'] = a.attr('href')
            new_article['author'] = a.next('span.printuser>a:last').text()
            new_article['contest_name'] = h3('span').text()
            new_article['contest_link'] = h3('span>a').attr('href')
            new_article['cn'] = 'true'
            new_article['type'] = 'contest'
            print(new_article)
            article_list.append(new_article)
    write_contest_to_csv(article_list, 'scp-contest-cn.csv')

def write_to_file(list_str, file_name):
    with open(file_name, 'w+', encoding='utf-8') as f:
        f.write(list_str.replace("'",'"'))

def write_to_csv(article_list, file_name):
    with open(file_name, 'w+', encoding='utf-8') as f:
        f.write("title,link,cn,type\n")
        for i in article_list:
            f.write(i['title'].replace(',', '，') + ',' + i['link'] + ',' + i['cn'] + ',' + i['type'] + '\n')

def write_series_story_to_csv(article_list, file_name):
    with open(file_name, 'w+', encoding='utf-8') as f:
        f.write("title,link,story_num,number\n")
        for i in article_list:
            f.write(i['title'].replace(',', '，') + ',' + i['link'] + ',' + i['story_num'] + ',' + i['number'] + '\n')

def write_setting_to_csv(article_list, file_name):
    with open(file_name, 'w+', encoding='utf-8') as f:
        f.write("title,link,desc,snippet,subtext,cn,type\n")
        for i in article_list:
            concat_str = i['title'] + ',' + i['link'] + ','+ i['desc'] + ',' + i['snippet'] \
             + ','+ i['subtext'] + ','+ i['cn']+ ','+ i['type']
            f.write(concat_str.replace('\n', '') +'\n')

def write_series_to_csv(article_list, file_name):
    with open(file_name, 'w+', encoding='utf-8') as f:
        f.write("title,link,author,snippet\n")
        for i in article_list:
            concat_str = i['title'] + ',' + i['link'] + ','+ i['author'] + ',' + i['snippet']
            f.write(concat_str.replace('\n', '') +'\n')

def write_tales_to_csv(article_list, file_name):
    with open(file_name, 'w+', encoding='utf-8') as f:
        f.write("title,link,author,created_time,page_code,cn\n")
        for i in article_list:
            concat_str = i['title'].replace(',', '，') + ',' + i['link'] + ','+ i['author'] + ','  + i['created_time'].replace(',', '，') + ',' + i['page_code'] + ',' + i['cn']
            f.write(concat_str.replace('\n', '') +'\n')

def write_contest_to_csv(article_list, file_name):
    with open(file_name, 'w+', encoding='utf-8') as f:
        f.write("title,link,author,contest_name,contest_link,cn,type\n")
        for i in article_list:
            concat_str = i['title'].replace(',', '，') + ',' + i['link'] + ','+ i['author'].replace(',', '，') + ',' \
             + i['contest_name'].replace(',', '，') + ',' + i['contest_link'] + ','+ i['cn']+ ','+ i['type']
            f.write(concat_str.replace('\n', '') +'\n')

# get_series()
# get_series_cn()
# get_joke_scp()
# get_joke_scp_cn()
# get_archived_scp()
# get_ex_scp()
# get_decommissioned_scps()
# get_removed_scp()
# get_series_story()
# get_tales()
# get_tales_cn()
get_setting()
get_setting_cn()
get_contest()
get_contest_cn()