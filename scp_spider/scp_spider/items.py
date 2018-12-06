# -*- coding: utf-8 -*-

# Define here the models for your scraped items
#
# See documentation in:
# https://doc.scrapy.org/en/latest/topics/items.html

import scrapy


class ScpSpiderItem(scrapy.Item):
    # define the fields for your item here like:
    # name = scrapy.Field()
    link = scrapy.Field()
    title = scrapy.Field()
    scp_type = scrapy.Field()
    detail = scrapy.Field()
    cn = scrapy.Field()
    not_found = scrapy.Field()
    author = scrapy.Field()
    desc = scrapy.Field()
    snippet = scrapy.Field()
    subtext = scrapy.Field()
    contest_name = scrapy.Field()
    contest_link = scrapy.Field()
    created_time = scrapy.Field()
    month = scrapy.Field()
    event_type = scrapy.Field()
    page_code = scrapy.Field()