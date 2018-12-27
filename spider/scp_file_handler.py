import csv
from scp_spider import *
import sqlite3
csv.field_size_limit(100000000)
bad_link_list = ['/', '/scp-series', '/scp-series-cn', '/series-archive',\
        '/series-archive-cn', '/tales-cn-by-page-name','/tales-by-page-name', \
        '/tales-cn-by-create-time', '/canon-hub', '/canon-hub-cn', '/contest-archive', \
        '/contest-archive-cn', 'joke-scps-cn', 'joke-scps', 'archived-scps', 'scp-ex', \
        '/decommissioned-scps-arc', '/scp-removed', '/foundation-tales']

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
    files = ['scp/' + name for name in os.listdir('scp/')]
    print(files)
    # merge_files(files[0:13], 'scp-1') # 7m
    # merge_files(files[13:19], 'scp-2') # 40m
    # merge_files(files[19:27], 'scp-3') # 31m
    # merge_files(files[27:], 'scp-4') # 31m
    merge_files(files, 'scp-category') # 31m

# 写入scp list
def write_to_csv(article_list, file_name):
    with open(file_name, 'w+', encoding='utf-8', newline='') as f:
        # 统一header，方便后续合并文件一起上传
        writer = csv.DictWriter(f, ['link', 'title', 'scp_type', 'download_type', 'detail', 'cn', 'not_found', \
            'author', 'desc', 'snippet', 'subtext', 'contest_name', 'contest_link', \
            'created_time', 'month', 'event_type', 'page_code', 'tags'])
        writer.writeheader()
        writer.writerows(article_list)
# 写入设定中心，故事系列这种有子目录的
def write_sub_cate_to_csv(sub_cate_list, filename):
    with open(filename, 'w+', encoding='utf-8', newline='') as f:
        # 统一header，方便后续合并文件一起上传
        writer = csv.DictWriter(f, ['link', 'title', 'scp_type', 'detail', 'cn', 'not_found', \
            'author', 'desc', 'snippet', 'subtext', 'tags', 'sub_scps'])
        writer.writeheader()
        writer.writerows(sub_cate_list)

def write_link_to_file(link_list, filename):
    with open(filename, 'w+', encoding='utf-8') as f:
        f.writelines([link.strip()+'\n' for link in link_list])

# 遍历csv文件，找到没有detail内容的scp，用于补漏
def get_not_found_category_from_file(filename):
    with open(filename, 'r', encoding='utf-8', newline='') as f:
        # 统一header，方便后续合并文件一起上传
        reader = csv.DictReader(f)
        category_list = [dict(order_dict) for order_dict in reader]
        not_found_list = []
        for c in category_list:
        	if c['detail'] == '':
        		not_found_list.append(c)
        return not_found_list

# 从文件中读取链接，自动去重
def get_list_from_file(filename):
    with open(filename, 'r') as f:
        # link_list = list(f.read()[2:-2].split("\', \'"))
        link_list = f.readlines()
        real_list = []
        for link in link_list:
            link = link.strip()
            if link not in real_list:
                real_list.append(link)
        return real_list

def get_scps_from_file(filename):
    with open(filename, 'r', encoding='utf-8', newline='') as f:
        # 统一header，方便后续合并文件一起上传
        reader = csv.DictReader(f)
        category_list = [dict(order_dict) for order_dict in reader]
        # for scp in category_list:
            # del scp["ID"]
        return category_list


def merge_new_link_to_old():
    bad_link_list = ['/', '/scp-series', '/scp-series-cn', '/series-archive',\
        '/series-archive-cn', '/tales-cn-by-page-name','/tales-by-page-name', \
        '/tales-cn-by-create-time', '/canon-hub', '/canon-hub-cn', '/contest-archive', \
        '/contest-archive-cn', 'joke-scps-cn', 'joke-scps', 'archived-scps', 'scp-ex', \
        '/decommissioned-scps-arc', '/scp-removed', '/foundation-tales']
    base_link_list = get_list_from_file('category_list.txt')
    new_link_list = get_list_from_file('new_found_link.txt')
    real_new_link_list = []
    for link in new_link_list:
        link = link.strip()
        if (('system:page-tags' in link) or (link in bad_link_list)):
            print(link)
        else:
            real_new_link_list.append(link)
    print('new found link size = ' + str(len(real_new_link_list)))
    for link in real_new_link_list:
        if link in base_link_list:
            print(link)
            real_new_link_list.remove(link)
        else:
            base_link_list.append(link)
    print('final new link size = ' + str(len(real_new_link_list)))
    print('final total link size = ' + str(len(base_link_list)))
    write_link_to_file(real_new_link_list, 'final_new_found_link.txt')
    write_link_to_file(base_link_list, 'final_category_list.txt')

def select_scp_by_real_link():
    new_found_scp = get_scps_from_file('new-found-category-9.csv')
    final_new_found_link = get_list_from_file('final_new_found_link.txt')
    real_new_scp = []
    for scp in new_found_scp:
        add_flag = True
        for s in real_new_scp:
            if scp['link'] == s['link'] or 'system:page-tags' in scp['link'] or scp['link'] in bad_link_list:
                add_flag = False
                break
        if add_flag == True:
            real_new_scp.append(scp)
    write_to_csv(real_new_scp, "final_new_scp.csv")
    print('new found scp size = ' + str(len(real_new_scp)))

def get_new_round_detail():
    final_total_link_list = get_list_from_file('final_category_list.txt')
    scp_list = get_scps_from_file("final_new_scp.csv")
    link_list = get_list_from_file('final_new_found_link.txt')
    get_detail_by_link_list(link_list, final_total_link_list, scp_list)
    write_to_csv(new_found_category_list, "new-found-category-1.csv")
    write_to_csv(scp_list, "scp-category-new-1.csv")

def fix_not_found():
    final_total_link_list = get_list_from_file('final_category_list.txt')
    not_found_list = get_not_found_category_from_file('scp-category-new-1.csv')
    all_list = get_scps_from_file('scp-category-new-1.csv')
    links = [s['link'] for s in not_found_list]
    get_detail_by_link_list(links, final_total_link_list, all_list)
    write_to_csv(all_list, "scp-category-new-1.csv")


def split_csv_file():
    all_scp = get_scps_from_file('scp.csv')
    # 4000一组
    for i in range(0, 2):
        if i*6000+6000 > len(all_scp):
            scp_group = all_scp[i*6000:]
        else:
            scp_group = all_scp[i*6000: i*6000+6000]
        write_to_csv(scp_group, "scp-split-" + str(i) + '.csv')

# 从数据库里拿tag更新上去
def update_tag_by_db(filename):
    con = sqlite3.connect("E:/scp.db")
    cur = con.cursor()
    tag_article_list = get_scps_from_file(filename)
    print(len(tag_article_list))
    for article in tag_article_list:
        # del article['story_num']
        # del article['number']
        cur.execute('''select tags from tag_scp where link = ?''', (article['link'],))
        for row in cur:
            if row[0] != None:
                article['tags'] = row[0]
    write_to_csv(tag_article_list, filename)
    # write_sub_cate_to_csv(tag_article_list, filename)

def write_to_db(filename):
    con = sqlite3.connect("E:/scp.db")
    cur = con.cursor()
    scp_list = get_scps_from_file(filename)
    print(len(scp_list))
    for scp in scp_list:
        # del scp['story_num']
        # del scp['number']
        cur.execute('''insert into scps values (NULL, ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)''',\
         (scp['title'],scp['link'],scp['detail'],scp['download_type'],scp['scp_type'],\
            scp['cn'],scp['not_found'],scp['author'],scp['desc'],scp['snippet'],scp['subtext'],\
            scp['contest_name'],scp['contest_link'],scp['created_time'],scp['month'],\
            scp['event_type'],scp['page_code'],scp['tags'],))
    
    con.commit()
    con.close()
    # write_subto_csv(scp_list,'scp/scp-sub-cate.csv')
    # write_sub_cate_to_csv(scp_list,'scp/scp_sub_cate.csv')
# 把not_found的值改为int保存
def fix_column():
    con = sqlite3.connect("E:/scp.db")
    cur = con.cursor()
    cur.execute('''UPDATE scps SET not_found = 0 WHERE not_found = 'false';''')
    cur.execute('''UPDATE scps SET not_found = 1 WHERE not_found = 'true';''')
    cur.execute('''UPDATE scps SET cn = 0 WHERE cn = 'false';''')
    cur.execute('''UPDATE scps SET cn = 1 WHERE cn = 'true';''')
    con.commit()
    con.close()


if __name__ == '__main__':
    split_csv_file()

