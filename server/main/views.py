# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.shortcuts import render
from django.http import HttpResponse
from main.models import User, UserInfo

import json

from mongoengine import NotUniqueError
from mongoengine import DoesNotExist

def register(request):
    if request.method == 'POST':
        if request.POST.get('username', None) == None:
            return HttpResponse(json.dumps({"status": "error", "msg": "no username arg"}))
        if request.POST.get('password', None) == None:
            return HttpResponse(json.dumps({"status": "error", "msg": "no password arg"}))
        if request.POST.get('nickname', None) == None:
            return HttpResponse(json.dumps({"status": "error", "msg": "no nickname arg"}))
        user = User(username = request.POST["username"], password = request.POST["password"], nickname = request.POST["nickname"])
        print request.POST["username"], request.POST["password"], request.POST["nickname"]
        user.avatar = request.POST.get("avatar", "")
        try:
            user.save()
        except NotUniqueError:
            return HttpResponse(json.dumps({"status": "error", "msg": "username existed"}))
        # userinfo = UserInfo(article_task_times = 0, point_task_times = 0, article_total_score = "{}", point_total_score = "{}", user_id = str(user.id))
        userinfo = UserInfo(article_task_times = 0, article_total_score = "{}", user_id = str(user.id))
        userinfo.save()
        user.info_id = str(userinfo.id)
        user.save()
        request.session["id"] = str(user.id);
        request.session["nickname"] = user.nickname
        return HttpResponse(json.dumps({"status": "success", "msg": "register succeed!"}))
    return HttpResponse(json.dumps({"status": "error", "msg": "method error"}))

def login(request):
    if request.method == 'POST':
        if request.POST.get('username', None) == None:
            return HttpResponse(json.dumps({"status": "error", "msg": "no username arg"}))
        if request.POST.get('password', None) == None:
            return HttpResponse(json.dumps({"status": "error", "msg": "no password arg"}))
        try:
            user = User.objects.get(username=request.POST["username"])
        except DoesNotExist:
            return HttpResponse(json.dumps({"status": "error", "msg": "no such user"}))
        if user.password != request.POST["password"]:
            return HttpResponse(json.dumps({"status": "error", "msg": "unmatched user and password"}))
        request.session["id"] = str(user.id);
        request.session["nickname"] = user.nickname
        return HttpResponse(json.dumps({"status": "success", "msg": "login succeed"}))
    return HttpResponse(json.dumps({"status": "error", "msg": "method error"}))