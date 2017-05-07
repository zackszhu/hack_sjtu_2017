package team.enlighten.rexcited.ai;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import team.enlighten.rexcited.article.Article;
import team.enlighten.rexcited.article.Paragraph;
import team.enlighten.rexcited.article.Subparagraph;

/**
 * Created by monkey_d_asce on 17-5-6.
 */
public class Robot {
    Article article = null;
    Map<Integer, Integer> para = new TreeMap<Integer, Integer>();
    int paraNum = 0;
    boolean isFuzzy = false;

    public interface Speaker {
        void speak(String msg);

        void pleaseRecite(String reftext);

        void result(String reftext, String answer, JSONObject result);
    }

    Speaker speaker;

    public void setSpeaker(Speaker speaker) {
        this.speaker = speaker;
    }

    private void showMessage(String msg) {
        if (speaker != null)
            speaker.speak(msg);
    }

    public void sendArticle(Article article) {
        this.article = article;
        List<Paragraph> paragraphs = article.Headers;
        showMessage("Let's start \"" + article.Title + " \"");

        for (int pNum = 0; pNum < paragraphs.size(); pNum++) {
            para.put(pNum, pNum);
        }


        isFuzzy = true;
        paraNum = 0;

        waitParaNum();
    }

    public void waitParaNum() {
        if (para.size() == 1) {
            waitReciteMode();
        } else if (para.size() > 1) {
            showMessage("There are " + para.size() + " paragraphs, choose one or let system to choose");
        }
    }

    public void sendParaNum(int paraNum) {
        if (paraNum == -1) //Random
        {
            this.paraNum = (int) Math.random() * article.Headers.size();
        } else
            this.paraNum = paraNum;
        showMessage("What is \"" + article.Headers.get(this.paraNum).Title + " \"?");
        waitReciteMode();
    }

    public void waitReciteMode() {
        showMessage("Please choose a way to judge reciting： (complete or fuzzy)");
    }

    public void sendReciteMode(boolean isFuzzy) {
        this.isFuzzy = isFuzzy;
        waitReciteOver();
    }

    public void waitReciteOver() {
        Paragraph paragraph = article.Headers.get(paraNum);
        String stdText = "";
        if (paragraph.content.isEmpty())
            stdText = paragraph.Title;
        else {
            for (Subparagraph subparagraph : paragraph.content)
                stdText += subparagraph.Content;
        }
        speaker.pleaseRecite(stdText);
        showMessage("Now, start your reciting！");
    }

    public void sendReciteText(String userText) throws JSONException {
        showMessage("The intelligent system is judging, please wait a minute！");
        //get std paragraph text
        Paragraph paragraph = article.Headers.get(paraNum);
        String stdText = "";
        if (paragraph.content.isEmpty())
            stdText = paragraph.Title;
        else {
            for (Subparagraph subparagraph : paragraph.content)
                stdText += subparagraph.Content;
        }
        showResult(stdText, userText, isFuzzy
                ? NlpClient.getInstance().fuzzyMatch(stdText.toLowerCase(), userText.toLowerCase())
                : NlpClient.getInstance().exactMatch(stdText.toLowerCase(), userText.toLowerCase()));
        waitRestart();
    }

    private void waitRestart() {
        showMessage("what to do next?");
    }

    public void showResult(String reftext, String answer, JSONObject result) {
        if (speaker != null)
            speaker.result(reftext, answer, result);
    }

    public void sendMsg(String message) {
        final String[] numbers = {"one", "two", "three", "four", "five", "six", "seven", "eight", "nine"};
        // choose para num
        message = message.toLowerCase();
        if (message.contains("random")) {
            sendParaNum(-1);
            return;
        }

        for (int i = 0; i < Math.min(numbers.length, para.size()); i++) {
            if (message.contains(numbers[i]) || message.contains(Integer.toString(i + 1))) {
                sendParaNum(i);
                return;
            }
        }

        // choose recite fuzzy or complete
        if (message.contains("fuzzy")) {
            sendReciteMode(true);
            return;
        }

        if (message.contains("complete")) {
            sendReciteMode(false);
            return;
        }

        showMessage("Sorry, please input again");
    }


}
