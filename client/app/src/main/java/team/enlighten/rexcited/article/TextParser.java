package team.enlighten.rexcited.article;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ZacksMsi on 2017/5/6.
 */
public class TextParser {
    public enum EArticleType {
        Article,
        Point
    }

    public enum EArticlePerm {
        Public,
        Private
    }

    private final String TitleReg = "# [\\w \\p{P}\\p{S}]+[\r\n]+";
    private final String HeaderReg = "- [\\w \\p{P}\\p{S}]+[\r\n]+";
    private final String TextReg = "\\* [\\w \\p{P}\\p{S}]+[\r\n]+";
    private final String contentReg = "[\\w \\p{P}\\p{S}]+";

    public Article Parse(String content) {
        Article article = ParseArticle(content);
        return article;
    }

    private List<Paragraph> ParseParagraph(String content) {
        Pattern p = Pattern.compile(HeaderReg);
        Matcher m = p.matcher(content);
        List<Paragraph> paragraphs = new ArrayList<Paragraph>();
        int start = -1;
        int end = -1;
        while (m.find()) {
            Paragraph paragraph = new Paragraph();
            paragraph.Title = GetContent(m.group().substring(2));
            end = m.start();
            if (start != -1) {
                paragraphs.get(paragraphs.size() - 1).content.addAll(ParseSubparagraph(content.substring(start, end)));
            }
            start = m.end();
            paragraphs.add(paragraph);
        }
        if (paragraphs.size() != 0) {
            paragraphs.get(paragraphs.size() - 1).content.addAll(ParseSubparagraph(content.substring(start)));
        } else {
            Paragraph paragraph = new Paragraph();
            paragraph.Title = GetContent(content);
            paragraphs.add(paragraph);
        }
        return paragraphs;
    }

    private List<Subparagraph> ParseSubparagraph(String content) {
        Pattern p = Pattern.compile(TextReg);
        Matcher m = p.matcher(content);
        List<Subparagraph> subparagraphs = new ArrayList<Subparagraph>();
        while (m.find()) {
            Subparagraph subparagraph = new Subparagraph();
            subparagraph.Content = GetContent(m.group().substring(2));
            subparagraphs.add(subparagraph);
        }
        if (subparagraphs.size() == 0) {
            Subparagraph subparagraph = new Subparagraph();
            subparagraph.Content = GetContent(content);
            subparagraphs.add(subparagraph);
        }
        return subparagraphs;
    }

    private Article ParseArticle(String content) {
        Article article = new Article();

        Pattern p = Pattern.compile(TitleReg);
        Matcher m = p.matcher(content);
        if (m.find()) {
            article.Title = GetContent(m.group().substring(2));
            article.Headers = ParseParagraph(content.substring(m.end()));
        }
        return article;
    }

    private String GetContent(String content) {
        Pattern p2 = Pattern.compile(contentReg);
        Matcher m2 = p2.matcher(content);
        if (m2.find()) {
            return m2.group();
        } else return null;
    }
}
