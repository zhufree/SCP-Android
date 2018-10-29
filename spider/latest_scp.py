# coding: utf-8

from pyquery import PyQuery as pq
import csv
import multiprocessing

spider_header = {
    'user-agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36'}


def get_latest_article():
    article_list = []
    # doc = pq('http://scp-wiki-cn.wikidot.com/most-recently-created-cn/p/1')
    doc = pq('http://scp-wiki-cn.wikidot.com/most-recently-created-translated/p/1')
    for i in list(doc('table>tr').items())[2:]:
        new_article = {}
        info_list = list(i('td').items())
        new_article['title'] = info_list[0]('a').text()
        new_article['link'] = info_list[0]('a').attr('href')
        new_article['created_time'] = info_list[1]('span').text()
        new_article['rank'] = info_list[2].text()
        print(new_article)
        article_list.append(new_article)