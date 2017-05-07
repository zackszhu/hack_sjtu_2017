package team.enlighten.rexcited.article;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZacksMsi on 2017/5/6.
 */
public class Article {
    public String id;
    public String Title;
    public List<Paragraph> Headers = new ArrayList<Paragraph>();
    public String create_time;

    public Article() {
        Title = "";
    }

    public Article(JSONObject jsonObject) throws JSONException {
        Title = jsonObject.getString("title");
        if (jsonObject.getInt("type") == 0) {
            JSONArray jsonArray = jsonObject.getJSONArray("paragraphs");
            for (int i = 0; i < jsonArray.length(); i++) {
                Paragraph p = new Paragraph();
                p.Title = jsonArray.getString(i);
                Headers.add(p);
            }
        } else {
            JSONArray jsonArray = jsonObject.getJSONArray("paragraphs");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject paragraphJson = jsonArray.getJSONObject(i);
                Paragraph p = new Paragraph(paragraphJson);
                Headers.add(p);
            }
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(Title + "\n");
        for (Paragraph item :
                Headers) {
            sb.append(item.toString() + "\n");
        }
        return sb.toString();
    }

    public List<String> toListString() {
        List<String> ret = new ArrayList<String>();
        for (Paragraph item :
                Headers) {
            ret.add(item.Title);
        }
        return ret;
    }

    public JSONArray toListArray() throws JSONException {
        JSONArray ret = new JSONArray();
        for (Paragraph item :
                Headers) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", item.Title);
            JSONArray jsonArray = new JSONArray();
            for (Subparagraph subItem :
                    item.content) {
                jsonArray.put(subItem.Content);
            }
            jsonObject.put("points", jsonArray);
            ret.put(jsonObject);
        }
        return ret;
    }
}
