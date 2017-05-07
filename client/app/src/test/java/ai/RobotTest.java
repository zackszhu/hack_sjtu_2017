package ai;

import org.junit.Test;

import team.enlighten.rexcited.ai.Robot;
import team.enlighten.rexcited.article.Article;
import team.enlighten.rexcited.article.TextParser;

/**
 * Created by monkey_d_asce on 17-5-7.
 */
public class RobotTest {
    @Test
    public void singleCircle() throws Exception {
        Article article = null;
        String text = new String("# The Western legal tradition\n" +
                "- Features\n" +
                "* A fairly clear demarcation between legal institutions and other types of institutions\n" +
                "* The nature of legal doctrine which comprises the principal source of the law and the basis of legal training, knowledge, and institutional practice.\n" +
                "* The concept of legal as a coherent, organic body of rules and principles with its own internal logic.\n" +
                "* The existence and specialized training of lawyers and other legal personnel. \n" +
                "- Principles\n" +
                "* The absolute supremacy or predominance of regular law as opposed to the influence of arbitrary power.\n" +
                "* Equality before the law or the equal subjection of all classes to the ordinary courts.\n" +
                "* The law of the constitution is a consequence of the rights of individuals as defined and enforced by courts.");
        TextParser tp = new TextParser();
        article = tp.Parse(text);
        System.out.print(article.toString());

        Robot robot = new Robot();
        System.out.println("\t do load article");

        robot.sendArticle(article);
        System.out.println("\t random choose");

        //robot.sendMsg("random choose");
        robot.sendParaNum(-1);

        System.out.println("\t choose fuzzy mode");

        robot.sendReciteMode(true);

        System.out.println("\t balabala user speak");
        robot.sendReciteText("I love banana");

    }

    @Test
    public void singleCircleWithMessage() throws Exception {
        Article article = null;
        String text = new String("# The Western legal tradition\n" +
                "- Features\n" +
                "* A fairly clear demarcation between legal institutions and other types of institutions\n" +
                "* The nature of legal doctrine which comprises the principal source of the law and the basis of legal training, knowledge, and institutional practice.\n" +
                "* The concept of legal as a coherent, organic body of rules and principles with its own internal logic.\n" +
                "* The existence and specialized training of lawyers and other legal personnel. \n" +
                "- Principles\n" +
                "* The absolute supremacy or predominance of regular law as opposed to the influence of arbitrary power.\n" +
                "* Equality before the law or the equal subjection of all classes to the ordinary courts.\n" +
                "* The law of the constitution is a consequence of the rights of individuals as defined and enforced by courts.");
        TextParser tp = new TextParser();
        article = tp.Parse(text);
        System.out.print(article.toString());

        Robot robot = new Robot();
        System.out.println("\t do load article");

        robot.sendArticle(article);
        System.out.println("\t choose 2");

        robot.sendMsg("2");

        System.out.println("\t choose serious mode");

        robot.sendMsg("serious");

        System.out.println("\t balabala user speak");
        robot.sendReciteText("I love banana");

    }
}