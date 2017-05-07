from django.conf.urls import url

import views

urlpatterns = [
    url(r'^article/upload/', views.articleUpload, name='articleUpload'),
    url(r'^article/download/(\w+)/', views.articleDownload, name='articleDownload'),
    url(r'^article/all/(\d+)/(\d+)/', views.all, name='all'),
    url(r'^article/mine/(\d+)/(\d+)/', views.mine, name='mine'),
    url(r'^task/upload/', views.doTask, name='doTask'),
    url(r'^history/(\d+)/(\d+)/', views.getHistory, name='getHistory'),
    url(r'^task/(\d+)/(\d+)/', views.getTask, name='getTask'),
    url(r'^overview/', views.overview, name='overview'),
    url(r'^paragraph/(\w+)/', views.getParagraph, name='getParagraph'),
]