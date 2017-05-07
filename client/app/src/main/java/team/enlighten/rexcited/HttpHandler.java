package team.enlighten.rexcited;

import android.util.Base64;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import team.enlighten.rexcited.article.Article;
import team.enlighten.rexcited.article.Score;
import team.enlighten.rexcited.article.TextParser;


/**
 * Created by ZacksMsi on 2017/5/6.
 */
public class HttpHandler {
    private String registerURL = "http://139.198.15.85:8000/main/register/";
    private String loginURL = "http://139.198.15.85:8000/main/login/";
    private String postArticleURL = "http://139.198.15.85:8000/text/article/upload/";
    private String postTaskURL = "http://139.198.15.85:8000/text/task/upload/";

    private String getArticlesURL = "http://139.198.15.85:8000/text/article/all/";
    private String downloadArticleURL = "http://139.198.15.85:8000/text/article/download/";
    private String getOverviewURL = "http://139.198.15.85:8000/text/overview/";

    private HttpClient client;
    private MessageDigest md;

    private static HttpHandler instance = new HttpHandler();

    public static HttpHandler getInstance() {
        return instance;
    }

    private HttpHandler() {
        client = new DefaultHttpClient();
        try {
            md = MessageDigest.getInstance("md5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public void Register(String username, String password, String nickname) throws Exception {

        List<BasicNameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("username", username));
        parameters.add(new BasicNameValuePair("password", Base64.encodeToString(md.digest(password.getBytes()), Base64.DEFAULT).trim()));
        parameters.add(new BasicNameValuePair("nickname", nickname));

        PostParam(registerURL, parameters);

    }

    private synchronized void PostParam(String url, List<BasicNameValuePair> parameters) throws Exception {
        HttpPost post = new HttpPost(url);
        String result = "";
        try {
            post.setEntity(new UrlEncodedFormEntity(parameters, "utf-8"));
            HttpResponse response = client.execute(post);
            HttpEntity httpEntity = response.getEntity();
            result = EntityUtils.toString(httpEntity, "utf-8");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        } finally {

            JSONObject jsonObject = new JSONObject(result);
            String status = jsonObject.getString("status");
            if (!status.equals("success")) {
                throw new Exception(jsonObject.getString("msg"));
            }
            System.out.println(status);
        }
    }

    private synchronized String GetParam(String url, List<NameValuePair> params) throws Exception {
        String result = "";
        try {
            URI uri = new URI(url);
            HttpGet httpget = new HttpGet(uri);
            HttpResponse response = client.execute(httpget);
            HttpEntity httpEntity = response.getEntity();
            result = EntityUtils.toString(httpEntity, "utf-8");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            JSONObject jsonObject = new JSONObject(result);
            String status = jsonObject.getString("status");
            if (!status.equals("success")) {
                throw new Exception(jsonObject.getString("msg"));
            } else {
                System.out.println(result);
                return result;
            }
        }

    }

    public void Login(String username, String password) throws Exception {
        Log.d("Login", "username: " + username);
        Log.d("Login", "password: " + password);
        List<BasicNameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("username", username));
        parameters.add(new BasicNameValuePair("password", Base64.encodeToString(md.digest(password.getBytes()), android.util.Base64.DEFAULT).trim()));
        Log.d("Login", "param: " + parameters.toString());

        PostParam(loginURL, parameters);
    }

    public void PostArticle(Article article, TextParser.EArticleType type, TextParser.EArticlePerm perm) throws Exception {
        List<BasicNameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("type", Integer.toString(type.ordinal())));
        parameters.add(new BasicNameValuePair("permission", Integer.toString(perm.ordinal())));

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("title", article.Title);
        if (type == TextParser.EArticleType.Article) {
            jsonObject.put("paragraphs", new JSONArray(article.toListString()));
        } else {
            jsonObject.put("paragraphs", article.toListArray());
        }
        System.out.println(jsonObject.toString());

        parameters.add(new BasicNameValuePair("content", jsonObject.toString()));

        PostParam(postArticleURL, parameters);
    }

    public List<Article> GetAllArticle(int startFrom, int quantity) throws Exception {
        String result = GetParam(getArticlesURL + startFrom + "/" + quantity, null);
        JSONObject jsonObject = new JSONObject(result);
        JSONArray jsonArray = jsonObject.getJSONArray("msg");
        List<Article> articles = new ArrayList<Article>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject articleJSON = jsonArray.getJSONObject(i);
            String id = articleJSON.getString("id");
            Article article = DownloadArticle(id);
            article.create_time = articleJSON.getString("create_time");
            article.id = id;
            articles.add(article);
        }
        return articles;
    }

    public List<Article> GetHistory(int startFrom, int length) throws Exception {
        String url = "http://139.198.15.85:8000/text/history/" + startFrom + "/" + length;
        String result = GetParam(url, null);
        JSONObject jsonObject = new JSONObject(result);
        JSONArray jsonArray = jsonObject.getJSONArray("msg");
        List<Article> ret = new ArrayList<Article>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject historyJson = jsonArray.getJSONObject(i);
            Article article = DownloadArticle(historyJson.getString("article_id"));
            article.create_time = historyJson.getString("update_time");
            article.id = historyJson.getString("article_id");
            ret.add(article);
        }
        return ret;
    }

    public List<Article> GetAllTask(int startFrom, int length) throws Exception {
        String url = "http://139.198.15.85:8000/text/task/" + startFrom + "/" + length + "/";
        String result = GetParam(url, null);
        JSONObject jsonObject = new JSONObject(result);
        JSONArray jsonArray = jsonObject.getJSONArray("msg");
        List<Article> ret = new ArrayList<Article>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject historyJson = jsonArray.getJSONObject(i);
            Article article = DownloadArticle(historyJson.getString("article_id"));
            article.create_time = historyJson.getString("create_time");
            article.id = historyJson.getString("article_id");
            ret.add(article);
        }
        return ret;
    }

    public Article DownloadArticle(String articleID) throws Exception {
        String articleResult = GetParam(downloadArticleURL + articleID, null);
        System.out.println(articleResult);
        JSONObject articleObject = new JSONObject(articleResult);
        JSONObject articleDetail = articleObject.getJSONObject("msg");
        Article article = new Article(articleDetail);
        System.out.println(article);
        return article;
    }

    public void PostTask(String id, TextParser.EArticleType type, Score score) throws Exception {
        List<BasicNameValuePair> parameters = new ArrayList<>();
        parameters.add(new BasicNameValuePair("type", Integer.toString(type.ordinal())));
        parameters.add(new BasicNameValuePair("content_id", id));
        System.out.println(score.ToJson(0).toString());
        parameters.add(new BasicNameValuePair("score", score.ToJson(0).toString()));

        PostParam(postTaskURL, parameters);
    }

    public int GetOverview(Score score) throws Exception {
        String result = GetParam(getOverviewURL, null);
        System.out.println(result);
        JSONObject jsonObject = new JSONObject(result);

        JSONObject overviewJson = jsonObject.getJSONObject("msg");
        int time = overviewJson.getInt("article_task_times");
        if (time != 0) {
            JSONObject scoreJson = overviewJson.getJSONObject("article_total_score");
            score.InitFromJSON(scoreJson);
        } else {
            score.Accuracy = 0;
            score.Fluency = 0;
            score.Score = 0;
        }
        return time;
    }
}
