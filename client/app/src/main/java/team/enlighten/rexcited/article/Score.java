package team.enlighten.rexcited.article;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZacksMsi on 2017/5/7.
 */
public class Score {
    public double Score;
    public double Fluency;
    public double Accuracy;
    public List<Score> children;

    public Score() {
        children = new ArrayList<Score>();
    }

    public Score(int score, int accuracy, int fluency) {
        children = new ArrayList<Score>();
        Accuracy = accuracy;
        Fluency = fluency;
        Score = score;
    }

    public Score(int score, int accuracy, int fluency, Article article) {
        children = new ArrayList<Score>();
        Score = score;
        Accuracy = accuracy;
        Fluency = fluency;
        for (int i = 0; i < article.Headers.size(); i++) {
            Score tmp = new Score(score, accuracy, fluency);
            Paragraph p = article.Headers.get(i);
            for (int j = 0; j < p.content.size(); j++) {
                tmp.children.add(new Score(score, accuracy, fluency));
            }
            children.add(tmp);
        }
    }

    public void InitFromJSON(JSONObject jsonObject) throws JSONException {
        children = new ArrayList<Score>();
        Score = jsonObject.getDouble("score");
        Fluency = jsonObject.getDouble("fluency");
        Accuracy = jsonObject.getDouble("accuracy");
    }

    public JSONObject ToJson(int index) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        JSONObject scoreObject = new JSONObject();
        scoreObject.put("score", Score);
        scoreObject.put("fluency", Fluency);
        scoreObject.put("accuracy", Accuracy);
        jsonObject.put("score", scoreObject);
        JSONArray jsonArray = new JSONArray();
        for (Score score :
                children) {
            jsonArray.put(score.ToJson(index + 1));
        }
        if (index == 0) {
            jsonObject.put("paragraphs", jsonArray);
        } else {
            jsonObject.put("points", jsonArray);
        }
        return jsonObject;
    }
}
