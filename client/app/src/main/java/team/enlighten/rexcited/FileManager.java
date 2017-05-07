package team.enlighten.rexcited;

import java.util.List;

import team.enlighten.rexcited.article.Article;
import team.enlighten.rexcited.article.TextParser;

/**
 * Created by lzn on 2017-05-06.
 */

public class FileManager {
    private static FileManager instance = new FileManager();

    public static FileManager getInstance() {
        return instance;
    }

    public List<Article> articles = null;

    public synchronized void fetchArticle() throws Exception {
        if (articles != null)
            return;
        articles = HttpHandler.getInstance().GetAllArticle(0, 10);
    }

    public synchronized void postArticle(Article article, TextParser.EArticleType type, TextParser.EArticlePerm perm) throws Exception {
        HttpHandler.getInstance().PostArticle(article, type, perm);
        if (articles != null)
            articles.add(article);
    }
}
