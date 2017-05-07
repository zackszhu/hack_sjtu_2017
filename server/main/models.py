# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models
from mongoengine import *
import datetime

class User(Document):
    username = StringField(required=True, max_length=20, unique=True)
    nickname = StringField(required=True, max_length=20)
    password = StringField(required=True, max_length=256)
    create_time = DateTimeField(default=datetime.datetime.now)
    avatar = StringField()
    info_id = StringField()
    
class UserInfo(Document):
    article_task_times = IntField()
    article_total_score = StringField()
    user_id = StringField()
# Create your models here.
