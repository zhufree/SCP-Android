import csv
import sqlite3
from scp_file_handler import *
from scp_spider import *

# 根据旧文件更新

# SCP系列+Cn
# 以1000为范围更新，4000+和cn1000系列更新频繁一些
def update_scp_series():
    old_scp_list = get_scps_from_file('scp/scp-series.csv')
    for old_scp in old_scp_list[6000:]:
        detail = get_single_detail(old_scp['link'])
        old_scp['detail'] = detail
        if detail =="<h3>抱歉，该页面尚无内容</h3>":
            old_scp['not_found'] = 'true'
        else:
            old_scp['not_found'] = 'false'

    write_to_csv(old_scp_list, 'scp/scp_series.csv')


# 故事外围 tales
# 时间作为属性，不重复抓取
def update_tales():
    # 先抓目录，与本地对比，存储
    # old_tales_scp = get_scps_from_file('scp/scp-tales.csv')
    # tales_scp = []
    # tales_scp = tales_scp + thread_get_tales('false')
    # tales_scp = tales_scp + thread_get_tales('true')
    # print(len(old_tales_scp))
    # print(len(tales_scp))
    # for tale in tales_scp:
    #     already_in = False
    #     for old_tale in old_tales_scp:
    #         if tale['link'] == old_tale['link']:
    #             tale['detail'] = old_tale['detail']
    #             tale['tags'] = old_tale['tags']
    # old_tales_scp = get_scps_from_file('scp/scp_tales.csv')
    # time_tales_scp = get_tales_cn_by_time()
    # print(len(time_tales_scp))
    # for tale in time_tales_scp:
    #     already_in = False
    #     for old_tale in old_tales_scp:
    #         if tale['link'] == old_tale['link']:
    #             already_in = True
    #             old_tale['month'] = tale['month']
    #     if already_in == False:
    #         print(tale['link'])
    old_scp_list = get_scps_from_file('scp/scp-tales.csv')
    for old_scp in old_scp_list[:1000]:
        detail = get_single_detail(old_scp['link'])
        old_scp['detail'] = detail
        if detail =="<h3>抱歉，该页面尚无内容</h3>":
            old_scp['not_found'] = 'true'
        else:
            old_scp['not_found'] = 'false'
    write_to_csv(old_tales_scp, 'scp/scp_tales.csv')


# 搞笑作品
def update_jokes():
    old_scp_list = get_scps_from_file('scp/scp-joke.csv')
    for old_scp in old_scp_list:
        if old_scp['link'].startswith('http://scp-wiki-cn.wikidot.com'):
            print(old_scp['link'])

            old_scp['link'] = old_scp['link'].replace('http://scp-wiki-cn.wikidot.com','')
        # detail = get_single_detail(old_scp['link'])
        # old_scp['detail'] = detail
        # if detail =="<h3>抱歉，该页面尚无内容</h3>":
        #     old_scp['not_found'] = 'true'
        # else:
        #     old_scp['not_found'] = 'false'

    write_to_csv(old_scp_list, 'scp/scp_joke.csv')

# 设定和故事系列
def update_settings_and_story_series():
    # total_scp_list = []
    # # 故事系列
    # total_scp_list = total_scp_list + thread_get_story_series('true', 0,)
    # total_scp_list = total_scp_list + thread_get_story_series('false', 1,)
    # total_scp_list = total_scp_list + thread_get_story_series('false', 2,)
    # print(len(total_scp_list))
    # # 设定
    # total_scp_list = total_scp_list + thread_get_setting('false')
    # total_scp_list = total_scp_list + thread_get_setting('true')
    # print(len(total_scp_list))
    # write_sub_cate_to_csv(total_scp_list, "scp-sub-cate.csv")
    cate_scp_list = get_scps_from_file('scp/scp-sub-cate.csv')
    for old_scp in cate_scp_list:
        print(old_scp['link'])
        detail = get_single_detail(old_scp['link'])
        old_scp['detail'] = detail
        if detail =="<h3>抱歉，该页面尚无内容</h3>":
            old_scp['not_found'] = 'true'
        else:
            old_scp['not_found'] = 'false'
    write_sub_cate_to_csv(cate_scp_list, 'scp/scp_sub_cate.csv')

if __name__ == '__main__':
    update_settings_and_story_series()