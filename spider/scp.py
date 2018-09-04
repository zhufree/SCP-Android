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
    # doc = pq('http://scp-wiki-cn.wikidot.com/scp-series-2')
        for ul in list(doc('div#page-content ul').items())[1:-3]:
            for li in ul('li').items():
                new_article = {}
                new_article['link'] = li('a').attr('href')
                new_article['title'] = li.text()
                print(new_article)
                article_list.append(new_article)
    
    write_to_csv(article_list, 'scp-series.csv')

def get_series_story():
    article_list = []
    story_count = 0
    # for i in range(1, 4):
    doc = pq('http://scp-wiki-cn.wikidot.com/scp-series-3-tales-edition', 
        headers={'user-agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36'})
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
                sub_article_count += 1
                print(new_sub_article)
                article_list.append(new_sub_article)
        else:
            new_article['number'] = str(story_count)
        new_article['title'] = story_li.remove('ul').text()
        story_count += 1
        print(new_article)
        article_list.append(new_article)

    write_series_story_to_csv(article_list, 'series-story-3.csv')
            
def get_series_cn():
    article_list = []
    doc = pq('http://scp-wiki-cn.wikidot.com/scp-series-cn')
    # doc = pq('http://scp-wiki-cn.wikidot.com/scp-series-2')
    for ul in list(doc('div#page-content ul').items())[1:-1]:
        for li in ul('li').items():
            new_article = {}
            new_article['title'] = li.text()
            new_article['link'] = li('a').attr('href')
            print(new_article)
            article_list.append(new_article)
    write_to_csv(article_list, 'scp-series-cn.csv')
    # write_to_file(str(article_list), 'scp-series-cn.json')

def get_joke_cn_scp():
    article_list = []
    doc = pq('http://scp-wiki-cn.wikidot.com/joke-scps-cn')
    # doc = pq('http://scp-wiki-cn.wikidot.com/scp-series-2')
    for li in list(doc('div.content-panel>ul>li').items()):
        new_article = {}
        new_article['title'] = li.text()
        new_article['link'] = li('a').attr('href')
        print(new_article)
        article_list.append(new_article)
    write_to_csv(article_list, 'joke-scps-cn.csv')

def get_joke_scp():
    article_list = []
    doc = pq('http://scp-wiki-cn.wikidot.com/joke-scps')
    # doc = pq('http://scp-wiki-cn.wikidot.com/scp-series-2')
    for li in list(doc('div.content-panel>ul>li').items()):
        new_article = {}
        new_article['title'] = li.text()
        new_article['link'] = li('a').attr('href')
        print(new_article)
        article_list.append(new_article)
    write_to_csv(article_list, 'joke-scps.csv')

def get_archived_scp():
    article_list = []
    doc = pq('http://scp-wiki-cn.wikidot.com/archived-scps')
    # doc = pq('http://scp-wiki-cn.wikidot.com/scp-series-2')
    for li in list(doc('div#page-content div.content-panel ul li').items()):
        new_article = {}
        new_article['title'] = li.text()
        new_article['link'] = li('a').attr('href')
        print(new_article)
        article_list.append(new_article)
    write_to_csv(article_list, 'archived-scps.csv')

def get_ex_scp():
    article_list = []
    doc = pq('http://scp-wiki-cn.wikidot.com/scp-ex')
    # doc = pq('http://scp-wiki-cn.wikidot.com/scp-series-2')
    for li in list(doc('div.content-panel>ul>li').items()):
        new_article = {}
        new_article['title'] = li.text()
        new_article['link'] = li('a').attr('href')
        print(new_article)
        article_list.append(new_article)
    write_to_csv(article_list, 'ex-scps.csv')

def get_decommissioned_scps():
    article_list = []
    doc = pq('http://scp-wiki-cn.wikidot.com/decommissioned-scps-arc')
    # doc = pq('http://scp-wiki-cn.wikidot.com/scp-series-2')
    for li in list(doc('div.content-panel ul li').items()):
        new_article = {}
        new_article['title'] = li.text()
        new_article['link'] = li('a').attr('href')
        print(new_article)
        article_list.append(new_article)
    write_to_csv(article_list, 'decommissioned-scps.csv')

def get_removed_scp():
    article_list = []
    doc = pq('http://scp-wiki-cn.wikidot.com/scp-removed')
    # doc = pq('http://scp-wiki-cn.wikidot.com/scp-series-2')
    for li in list(doc('div.content-panel ul li').items()):
        new_article = {}
        new_article['title'] = li.text()
        new_article['link'] = li('a').attr('href')
        print(new_article)
        article_list.append(new_article)
    write_to_csv(article_list, 'removed-scps.csv')
def get_setting():
    article_list = []
    doc = pq('http://scp-wiki-cn.wikidot.com/canon-hub')
    # doc = pq('http://scp-wiki-cn.wikidot.com/canon-hub-cn')
    for div in list(doc('div.content-panel').items())[:-1]:
        new_article = {}
        new_article['title'] = div('div.canon-title>p').text()
        new_article['link'] = div('div.canon-title>p>a').attr('href')
        new_article['desc'] = div('div.canon-description').text()
        new_article['snippet'] = div('div.canon-snippet').text()
        new_article['subtext'] = div('div.canon-snippet-subtext').text()
        print(new_article)
        article_list.append(new_article)
    write_setting_to_csv(article_list, 'scp-setting.csv')
    # write_setting_to_csv(article_list, 'scp-setting-cn.csv')

def get_series_archived():
    article_list = []
    for i in range(1,3):
        doc = pq('http://scp-wiki-cn.wikidot.com/series-archive/p/' + str(i))
        # doc = pq('http://scp-wiki-cn.wikidot.com/canon-hub-cn')
        for tr in list(doc('div.list-pages-box tr').items())[1:]:
            new_article = {}
            tds = list(tr('td').items())
            new_article['title'] = tds[0].text()
            new_article['link'] = tds[0]('a').attr('href')
            new_article['author'] = tds[1].text()
            new_article['snippet'] = tds[2].text()
            print(new_article)
            article_list.append(new_article)
    write_series_to_csv(article_list, 'scp-series-archive.csv')

def write_to_file(list_str, file_name):
    with open(file_name, 'w+', encoding='utf-8') as f:
        f.write(list_str.replace("'",'"'))

def write_to_csv(article_list, file_name):
    with open(file_name, 'w+', encoding='utf-8') as f:
        f.write("title,link\n")
        for i in article_list:
            f.write(i['title'].replace(',', '，') + ',' + i['link']+'\n')

def write_series_story_to_csv(article_list, file_name):
    with open(file_name, 'w+', encoding='utf-8') as f:
        f.write("title,link,number\n")
        for i in article_list:
            f.write(i['title'].replace(',', '，') + ',' + i['link'] + ',' + i['number'] + '\n')

def write_setting_to_csv(article_list, file_name):
    with open(file_name, 'w+', encoding='utf-8') as f:
        f.write("title,link,desc,snippet,subtext\n")
        for i in article_list:
            concat_str = i['title'] + ',' + i['link'] + ','+ i['desc'] + ',' + i['snippet'] + ','+ i['subtext']
            f.write(concat_str.replace('\n', '') +'\n')

def write_series_to_csv(article_list, file_name):
    with open(file_name, 'w+', encoding='utf-8') as f:
        f.write("title,link,author,snippet\n")
        for i in article_list:
            concat_str = i['title'] + ',' + i['link'] + ','+ i['author'] + ',' + i['snippet']
            f.write(concat_str.replace('\n', '') +'\n')

get_series_story()