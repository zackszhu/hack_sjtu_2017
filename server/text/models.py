# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models
from mongoengine import *
import datetime

class Article(Document):
    paragraphs = StringField()
    length = IntField()
    title = StringField()
    create_time = DateTimeField(default=datetime.datetime.now)
    user_id = StringField()
    permission = IntField() # 0 public 1 private
    info_id = StringField()
    type = IntField()
    weight = StringField()
    
class Paragraph(Document):
    length = IntField()
    text = StringField()
    article_id = StringField()
    info_id = StringField()
    type = IntField()
    points = StringField()
    weight = StringField()
    name = StringField()
    
class Point(Document):
    text = StringField()
    paragraph_id = StringField()
    length = IntField()

# 0 Article, 1 Paragraph, 2 Point
class ContentInfo(Document):
    type = IntField()
    content_id = StringField()
    times = IntField()
    total_score = StringField()
    
class Task(Document):
    type = IntField()
    content_id = StringField()
    score = StringField()
    user_id = StringField()
    create_time = DateTimeField(default=datetime.datetime.now)
    
class History(Document):
    user_id = StringField()
    article_id = StringField()
    article_title = StringField()
    score = StringField()
    update_time = DateTimeField(default=datetime.datetime.now)
    type = IntField()
'''
class ArticleInfo(Document):
    article_id = StringField()
    times = IntField()
    total_score = StringField()

class ParagraphInfo(Document):
    paragraph_id = StringField()
    times = IntField()
    total_score = StringField()

class PointInfo(Document):
    point_id = StringField()
    times = IntField()
    total_score = StringField()
'''