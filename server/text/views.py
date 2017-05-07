# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.shortcuts import render
from django.http import HttpResponse
from django.http import HttpRequest

from text.models import *
from main.models import *

import json, re

def articleUpload(request):
    # 0: article 1: points
    if request.session.get("id", None) == None:
        return HttpResponse(json.dumps({"status": "error", "msg": "login first"}))
    id = request.session["id"]
    params = ["type", "content"]
    for param in params:
        if not request.POST.has_key(param):
            return HttpResponse(json.dumps({"status": "error", "msg": "no param %s" % param}))
    type = request.POST["type"]
    content = request.POST["content"]
    permission = request.POST.get("permission", "0")
    if type == "0":
        # article
        arr = json.loads(content)
        article = Article()
        article.title = arr["title"]
        article.user_id = id
        article.permission = int(permission)
        article.type = 0
        article.save()
        paragraphs = []
        lengths = []
        for para in arr["paragraphs"]:
            lengths.append(len(re.split('[ ]+', para)))
            paragraph = Paragraph(type = 0, text = para, article_id = str(article.id), length = lengths[-1])
            paragraph.save()
            para_info = ContentInfo(type = 1, content_id = str(paragraph.id), times = 0, total_score = json.dumps({"score": 0.0, "accuracy": 0.0, "fluency": 0.0}))
            para_info.save()
            paragraph.info_id = str(para_info.id)
            paragraph.save()
            paragraphs.append(str(paragraph.id))
        article.paragraphs = json.dumps(paragraphs)
        article.length = sum(lengths)
        article.weight = json.dumps(map(lambda x: x * 1.0 / article.length, lengths))
        article_info = ContentInfo(type = 0, content_id = str(article.id), times = 0, total_score = json.dumps({"score": 0.0, "accuracy": 0.0, "fluency": 0.0}))
        article_info.save()
        article.info_id = str(article_info.id)
        article.save()
        history = History(user_id=id, \
            article_id=str(article.id), \
            article_title=article.title, \
            type=0, \
            score=json.dumps({ \
                "score":{"score": 0.0, "accuracy": 0.0, "fluency": 0.0}, \
                "paragraphs": [{"score": {"score": 0.0, "accuracy": 0.0, "fluency": 0.0}}] * len(paragraphs) \
            }) \
        )
        history.save()
        return HttpResponse(json.dumps({"status": "success", "msg": "upload success"}))
    elif type == "1":
        # points
        arr = json.loads(content)
        article = Article()
        article.title = arr["title"]
        article.user_id = id
        article.permission = int(permission)
        article.type = 1
        article.save()
        paragraphs = []
        lengths = []
        paragraph_score = []
        for para in arr["paragraphs"]:
            paragraph = Paragraph(type = 1, article_id = str(article.id), name = para["name"])
            paragraph.save()
            lengths1 = []
            points = []
            for point in para["points"]:
                # print "point: ", point
                lengths1.append(len(re.split('[ ]+', point)))
                point = Point(text = point, paragraph_id = str(paragraph.id), length = lengths1[-1])
                point.save()
                point_info = ContentInfo(type = 1, content_id = str(point.id), times = 0, total_score = json.dumps({"score": 0.0, "accuracy": 0.0, "fluency": 0.0}))
                point_info.save()
                point.info_id = str(point_info.id)
                point.save()
                points.append(str(point.id))
            paragraph_score.append({"score": {"score": 0.0, "accuracy": 0.0, "fluency": 0.0}, "points": [{"score": {"score": 0.0, "accuracy": 0.0, "fluency": 0.0}}] * len(points)})
            paragraph.points = json.dumps(points)
            paragraph.length = sum(lengths1)
            paragraph.weight = json.dumps(map(lambda x: x * 1.0 / paragraph.length, lengths1))
            lengths.append(paragraph.length)
            para_info = ContentInfo(type = 1, content_id = str(paragraph.id), times = 0, total_score = json.dumps({"score": 0.0, "accuracy": 0.0, "fluency": 0.0}))
            para_info.save()
            paragraph.info_id = str(para_info.id)
            paragraph.save()
            # print str(paragraph.id)
            paragraphs.append(str(paragraph.id))
        article.paragraphs = json.dumps(paragraphs)
        article.length = sum(lengths)
        article.weight = json.dumps(map(lambda x: x * 1.0 / article.length, lengths))
        article_info = ContentInfo(type = 0, content_id = str(article.id), times = 0, total_score = json.dumps({"score": 0.0, "accuracy": 0.0, "fluency": 0.0}))
        article_info.save()
        article.info_id = str(article_info.id)
        article.save()
        history = History(user_id=id, \
            article_id=str(article.id), \
            article_title=article.title, \
            type=0, \
            score=json.dumps({ \
                "score":{"score": 0.0, "accuracy": 0.0, "fluency": 0.0}, \
                "paragraphs": paragraph_score \
            }) \
        )
        history.save()
        return HttpResponse(json.dumps({"status": "success", "msg": "upload success"}))
    else:
        return HttpResponse(json.dumps({"status": "error", "msg": "no such type: %s" % type}))

def get_article_detail(article):
    ans = {}
    content = []
    print article.type
    if article.type == 0:
        # article
        ans["type"] = 0
        # ans["permission"] = article.permission
        ans["title"] = article.title
        # ans["length"] = article.length
        # ans["create_time"] = article.create_time.strftime('%Y-%m-%d %H:%M:%S')
        for para in json.loads(article.paragraphs):
            paragraph = Paragraph.objects.get(id = para)
            info = ContentInfo.objects.get(id = paragraph.info_id)
            # content.append({"text": paragraph.text, "length": paragraph.length, "times": info.times, "total_score": info.total_score})
            content.append(paragraph.text)
        ans["paragraphs"] = content
        # print article.info_id
        info = ContentInfo.objects.get(id = article.info_id)
        ans["times"] = info.times
        ans["total_score"] = info.total_score
    else:
        ans["type"] = 1
        ans["title"] = article.title
        print article.paragraphs
        for para in json.loads(article.paragraphs):
            paragraph = Paragraph.objects.get(id = para)
            content1 = []
            for p in json.loads(paragraph.points):
                point = Point.objects.get(id = p)
                print point.text
                content1.append(point.text)
            content.append({"points": content1, "name": paragraph.name})
        ans["paragraphs"] = content
        info = ContentInfo.objects.get(id = article.info_id)
        ans["times"] = info.times
        ans["total_score"] = info.total_score
    return ans

def get_article_in_short(article):
    ans = {}
    content = []
    if article.type == 0:
        # article
        ans["type"] = 0
        ans["id"] = str(article.id)
        ans["permission"] = article.permission
        ans["title"] = article.title
        ans["length"] = article.length
        ans["create_time"] = article.create_time.strftime('%Y-%m-%d %H:%M:%S')
        ans["paragraphs"] = article.paragraphs
        info = ContentInfo.objects.get(id = article.info_id)
        ans["times"] = info.times
        ans["total_score"] = info.total_score
    else:
        ans["type"] = 1
        ans["id"] = str(article.id)
        ans["permission"] = article.permission
        ans["title"] = article.title
        ans["length"] = article.length
        ans["create_time"] = article.create_time.strftime('%Y-%m-%d %H:%M:%S')
        ans["paragraphs"] = article.paragraphs
        info = ContentInfo.objects.get(id = article.info_id)
        ans["times"] = info.times
        ans["total_score"] = info.total_score
    return ans
    
def articleDownload(request, article_id):
    if request.session.get("id", None) == None:
        return HttpResponse(json.dumps({"status": "error", "msg": "login first"}))
    id = request.session.get("id", None)
    try:
        article = Article.objects.get(id = article_id)
        if article.permission != 0 and str(article.user_id) != id:
            return HttpResponse(json.dumps({"status": "error", "msg": "permission denied"}))
        ans = get_article_detail(article)
        return HttpResponse(json.dumps({"status": "success", "msg": ans}))
    except Exception, e:
        print Exception, e
        return HttpResponse(json.dumps({"status": "error", "msg": "no such article id: %s" % article_id}))

def all(request, offset, limit):
    try:
        offset = int(offset)
        limit = int(limit)
    except:
        return HttpResponse(json.dumps({"status": "error", "msg": "offset or limit is not an integer"}))
    articles = Article.objects.all().filter(permission = 0).order_by("-create_time")[offset:offset+limit]
    return HttpResponse(json.dumps({"status": "success", "msg": map(get_article_in_short, articles)}))

def mine(request, offset, limit):
    try:
        offset = int(offset)
        limit = int(limit)
    except:
        return HttpResponse(json.dumps({"status": "error", "msg": "offset or limit is not an integer"}))
    if request.session.get("id", None) == None:
        return HttpResponse(json.dumps({"status": "error", "msg": "login first"}))
    articles = Article.objects.all().filter(user_id = request.session["id"]).order_by("-create_time")[offset:offset+limit]
    return HttpResponse(json.dumps({"status": "success", "msg": map(get_article_in_short, articles)}))

def add_score(score1, score2):
    # print "s1:", score1
    # print "s2:", score2
    s1 = json.loads(score1)
    s2 = json.loads(score2)
    score = {}
    score["accuracy"] = s1.get("accuracy", 0) + s2["accuracy"]
    score["fluency"] = s1.get("fluency", 0) + s2["fluency"]
    score["score"] = s1.get("score", 0) + s2["score"]
    return json.dumps(score)
    
def doTask(request):
    if request.session.get("id", None) == None:
        return HttpResponse(json.dumps({"status": "error", "msg": "login first"}))
    id = request.session["id"]
    params = ["type", "content_id", "score"]
    for param in params:
        if not request.POST.has_key(param):
            return HttpResponse(json.dumps({"status": "error", "msg": "no param %s" % param}))
    type = request.POST["type"]
    content_id = request.POST["content_id"]
    score = request.POST["score"]
    if type == "0":
        # article
        try:
            article = Article.objects.get(id = content_id)
        except Exception, e:
            print Exception, e
            return HttpResponse(json.dumps({"status": "error", "msg": "no such article"}))
        try:
            history = History.objects.get(user_id = id, article_id = content_id)
        except Exception, e:
            print Exception, e
            return HttpResponse(json.dumps({"status": "error", "msg": "no such history"}))
        task = Task(type = 0, content_id = content_id, score = score, user_id = id)
        task.save()
        old_score = json.loads(history.score)
        new_score = json.loads(score)
        score_sum = 0.0
        accuracy_sum = 0.0
        fluency_sum = 0.0
        weight = json.loads(article.weight)
        paragraphs = json.loads(article.paragraphs)
        for i in range(0, len(new_score["paragraphs"])):
            if new_score["paragraphs"][i]["score"] > old_score["paragraphs"][i]["score"]:
                old_score["paragraphs"][i]["score"]["score"] = new_score["paragraphs"][i]["score"]["score"]
                old_score["paragraphs"][i]["score"]["accuracy"] = new_score["paragraphs"][i]["score"]["accuracy"]
                old_score["paragraphs"][i]["score"]["fluency"] = new_score["paragraphs"][i]["score"]["fluency"]
            score_sum = score_sum + old_score["paragraphs"][i]["score"]["score"] * weight[i]
            accuracy_sum = score_sum + old_score["paragraphs"][i]["score"]["accuracy"] * weight[i]
            fluency_sum = score_sum + old_score["paragraphs"][i]["score"]["fluency"] * weight[i]
            paragraph_info = ContentInfo.objects.get(content_id = paragraphs[i])
            paragraph_info.times = paragraph_info.times + 1
            paragraph_info.total_score = add_score(paragraph_info.total_score, json.dumps(new_score["score"]))
            paragraph_info.save()
        old_score["score"]["score"] = score_sum
        old_score["score"]["accuracy"] = accuracy_sum
        old_score["score"]["fluency"] = fluency_sum
        history.score = json.dumps(old_score)
        history.update_time = datetime.datetime.now()
        history.save()
        print str(article.info_id)
        article_info = ContentInfo.objects.get(id = article.info_id)
        article_info.times = article_info.times + 1
        article_info.total_score = add_score(article_info.total_score, json.dumps(new_score["score"]))
        article_info.save()
        user_info = UserInfo.objects.get(user_id = id)
        user_info.article_task_times = user_info.article_task_times + 1
        user_info.article_total_score = add_score(user_info.article_total_score, json.dumps(new_score["score"]))
        user_info.save()
        return HttpResponse(json.dumps({"status": "success", "msg": "upload success"}))
    elif type == "1":
    # points
        try:
            article = Article.objects.get(id = content_id)
        except Exception, e:
            print Exception, e
            return HttpResponse(json.dumps({"status": "error", "msg": "no such article"}))
        try:
            history = History.objects.get(user_id = id, article_id = content_id)
        except Exception, e:
            print Exception, e
            return HttpResponse(json.dumps({"status": "error", "msg": "no such history"}))
        task = Task(type = 0, content_id = content_id, score = score, user_id = id)
        task.save()
        old_score = json.loads(history.score)
        new_score = json.loads(score)
        score_sum = 0.0
        accuracy_sum = 0.0
        fluency_sum = 0.0
        weight = json.loads(article.weight)
        paragraphs = json.loads(article.paragraphs)
        for i in range(0, len(new_score["paragraphs"])):
            score_sum1 = 0.0
            accuracy_sum1 = 0.0
            fluency_sum1 = 0.0
            paragraph = Paragraph.objects.get(id = paragraphs[i])
            print paragraph.points
            points = json.loads(paragraph.points)
            weight1 = json.loads(paragraph.weight)
            for j in range(0, len(new_score["paragraphs"][i]["points"])):
                # print new_score["paragraphs"][i]["points"]
                if new_score["paragraphs"][i]["points"][j]["score"]["score"] > new_score["paragraphs"][i]["points"][j]["score"]["score"]:
                    old_score["paragraphs"][i]["points"][j]["score"]["score"] = new_score["paragraphs"][i]["points"][j]["score"]["score"]
                    old_score["paragraphs"][i]["points"][j]["score"]["accuracy"] = new_score["paragraphs"][i]["points"][j]["score"]["accuracy"]
                    old_score["paragraphs"][i]["points"][j]["score"]["fluency"] = new_score["paragraphs"][i]["points"][j]["score"]["fluency"]
                # print "old:", old_score
                # print "new:", new_score
                # print old_score["paragraphs"][i]
                # print "ne:", len(new_score["paragraphs"][i]["points"])
                # print "old:", len(old_score["paragraphs"][i]["points"])
                # print old_score["paragraphs"][i]["points"], j
                score_sum1 = score_sum1 + old_score["paragraphs"][i]["points"][j]["score"]["score"] * weight1[j]
                accuracy_sum1 = score_sum1 + old_score["paragraphs"][i]["points"][j]["score"]["accuracy"] * weight1[j]
                fluency_sum1 = score_sum1 + old_score["paragraphs"][i]["points"][j]["score"]["fluency"] * weight1[j]
                point_info = ContentInfo.objects.get(content_id = points[j])
                point_info.times = point_info.times + 1
                point_info.total_score = add_score(point_info.total_score, json.dumps(new_score["paragraphs"][i]["points"][j]["score"]))
                point_info.save()
            old_score["paragraphs"][i]["score"]["score"] = score_sum1
            old_score["paragraphs"][i]["score"]["accuracy"] = accuracy_sum1
            old_score["paragraphs"][i]["score"]["fluency"] = fluency_sum1
            score_sum = score_sum + old_score["paragraphs"][i]["score"]["score"] * weight[i]
            accuracy_sum = score_sum + old_score["paragraphs"][i]["score"]["accuracy"] * weight[i]
            fluency_sum = score_sum + old_score["paragraphs"][i]["score"]["fluency"] * weight[i]
            paragraph_info = ContentInfo.objects.get(content_id = paragraphs[i])
            paragraph_info.times = paragraph_info.times + 1
            paragraph_info.total_score = add_score(paragraph_info.total_score, json.dumps(new_score["paragraphs"][i]["score"]))
            paragraph_info.save()
        old_score["score"]["score"] = score_sum
        old_score["score"]["accuracy"] = accuracy_sum
        old_score["score"]["fluency"] = fluency_sum
        history.score = json.dumps(old_score)
        history.update_time = datetime.datetime.now()
        history.save()
        # print str(article.info_id)
        article_info = ContentInfo.objects.get(id = article.info_id)
        article_info.times = article_info.times + 1
        article_info.total_score = add_score(article_info.total_score, json.dumps(new_score["score"]))
        article_info.save()
        user_info = UserInfo.objects.get(user_id = id)
        user_info.article_task_times = user_info.article_task_times + 1
        user_info.article_total_score = add_score(user_info.article_total_score, json.dumps(new_score["score"]))
        user_info.save()
        return HttpResponse(json.dumps({"status": "success", "msg": "upload success"}))
    '''
    elif type == "2":
        try:
            paragraph = Paragraph.objects.get(id = content_id)
            article = Article.objects.get(id = paragraph.user_id)
        except Exception, e:
            print Exception, e
            return HttpResponse(json.dumps({"status": "error", "msg": "no such paragraph or article"}))
        try:
            history = History.objects.get(user_id = id, article_id = article.id)
        except Exception, e:
            print Exception, e
            return HttpResponse(json.dumps({"status": "error", "msg": "no such history"}))
    '''

def get_history_detail(history):
    ans = {}
    ans["update_time"] = history.update_time.strftime('%Y-%m-%d %H:%M:%S')
    ans["article_title"] = history.article_title
    ans["score"] = json.loads(history.score)
    ans["article_id"] = history.article_id
    return ans
    
        
def getHistory(request, offset, limit):
    try:
        offset = int(offset)
        limit = int(limit)
    except:
        return HttpResponse(json.dumps({"status": "error", "msg": "offset or limit is not an integer"}))
    if request.session.get("id", None) == None:
        return HttpResponse(json.dumps({"status": "error", "msg": "login first"}))
    id = request.session["id"]
    histories = History.objects.filter(user_id = id).order_by("-create_time")[offset:offset+limit]
    return HttpResponse(json.dumps({"status": "success", "msg": map(get_history_detail, histories)}))
    
def overview(request):
    if request.session.get("id", None) == None:
        return HttpResponse(json.dumps({"status": "error", "msg": "login first"}))
    id = request.session["id"]
    user_info = UserInfo.objects.get(user_id = id)
    return HttpResponse(json.dumps({"status": "success", "msg": {"article_task_times": user_info.article_task_times, "article_total_score": json.loads(user_info.article_total_score)}}))
    
def getParagraph(request, paragraph_id):
    if request.session.get("id", None) == None:
        return HttpResponse(json.dumps({"status": "error", "msg": "login first"}))
    id = request.session["id"]
    paragraph = Paragraph.objects.get(id = paragraph_id)
    return HttpResponse(json.dumps({"status": "success", "msg": {"text": paragraph.text}}))
    
def get_task_detail(task):
    ans = {}
    ans["create_time"] = task.create_time.strftime('%Y-%m-%d %H:%M:%S')
    ans["article_id"] = task.content_id
    return ans
    
def getTask(request, offset, limit):
    try:
        offset = int(offset)
        limit = int(limit)
    except:
        return HttpResponse(json.dumps({"status": "error", "msg": "offset or limit is not an integer"}))    
    if request.session.get("id", None) == None:
        return HttpResponse(json.dumps({"status": "error", "msg": "login first"}))
    id = request.session["id"]
    tasks = Task.objects.filter(user_id = id).order_by("-create_time")[offset:offset + limit]
    return HttpResponse(json.dumps({"status": "success", "msg": map(get_task_detail, tasks)}))    