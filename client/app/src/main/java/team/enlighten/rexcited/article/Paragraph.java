package team.enlighten.rexcited.article;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Paragraph {
    public String Title;
    public List<Subparagraph> content = new ArrayList<Subparagraph>();

    public Paragraph() {
    }

    public Paragraph(JSONObject jsonObject) throws JSONException {
        Title = jsonObject.getString("name");
        JSONArray jsonArray = jsonObject.getJSONArray("points");
        for (int i = 0; i < jsonArray.length(); i++) {
            content.add(new Subparagraph(jsonArray.getString(i)));
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(Title + "\n");
        for (Subparagraph item :
                content) {
            sb.append(item.Content + "\n");
        }
        return sb.toString();
    }

    public List<String> toListString() {
        List<String> ret = new ArrayList<String>();
        for (Subparagraph item :
                content) {
            ret.add(item.Content);
        }
        return ret;
    }
}
