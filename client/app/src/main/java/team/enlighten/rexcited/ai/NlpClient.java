package team.enlighten.rexcited.ai;

import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.Features;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.KeywordsOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.KeywordsResult;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.SemanticRolesOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.SemanticRolesResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by monkey_d_asce on 17-5-6.
 */
public class NlpClient {


    private static class NlpClientHolder {
        public final static String URL = "https://gateway.watsonplatform.net/natural-language-understanding/api";
        public final static String USERNAME = "b3397c97-2766-450b-b83f-9536e33ca769";
        public final static String PASSWORD = "YtpbXlmdVvvT";
        public static NlpClient instance = new NlpClient();
    }

    public static NlpClient getInstance() {
        return NlpClientHolder.instance;
    }

    NaturalLanguageUnderstanding service = null;
    Features features = null;

    private NlpClient() {
        service = new NaturalLanguageUnderstanding(
                NaturalLanguageUnderstanding.VERSION_DATE_2017_02_27,
                NlpClientHolder.USERNAME,
                NlpClientHolder.PASSWORD
        );
//        EntitiesOptions entitiesOptions = new EntitiesOptions.Builder()
//                .build();

        KeywordsOptions keywordsOptions = new KeywordsOptions.Builder()
                .build();

        SemanticRolesOptions semanticRoles = new SemanticRolesOptions.Builder()
                .build();

        features = new Features.Builder()
                //.entities(entitiesOptions)
                .keywords(keywordsOptions)
                .semanticRoles(semanticRoles)
                .build();


    }

    public AnalysisResults simpleAnalyze(String text) {
        final AnalyzeOptions parameters = new AnalyzeOptions.Builder()
                .text(text)
                .language("en")
                .features(features)
                .build();
        final ConcurrentLinkedQueue<AnalysisResults> queue = new ConcurrentLinkedQueue<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                queue.add(service
                        .analyze(parameters)
                        .execute());
            }
        }).start();
        while (queue.isEmpty())
            try {
                Thread.sleep(100, 0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        //System.out.println(response);
        return queue.peek();
    }

    public Set<String> getKeyWordSet(String text) {
        AnalysisResults temp = simpleAnalyze(text);
        Set<String> result = new HashSet<String>();

        for (KeywordsResult keyword : temp.getKeywords()) {
            String keyText = keyword.getText();
            ArrayList<String> keyList = getWordList(keyText);
            result.addAll(keyList);
        }

        for (SemanticRolesResult sematic : temp.getSemanticRoles()) {
            String actionText = sematic.getAction().getText();
            ArrayList<String> keyList = getWordList(actionText);
            result.add(keyList.get(keyList.size() - 1));

            String subjectText = sematic.getSubject().getText();
            ArrayList<String> subjectList = getWordList(subjectText);
            result.add(subjectList.get(subjectList.size() - 1));
        }
        return result;
    }


    public JSONObject match(JSONArray lWords, JSONArray rWords) throws JSONException {
        int lLength = lWords.length();
        int rLength = rWords.length();

        int[][] t = new int[lLength + 1][rLength + 1];
        for (int i = lLength - 1; i >= 0; --i) {
            for (int j = rLength - 1; j >= 0; --j) {
                String lstr = lWords.getJSONObject(i).getString("str");
                String rstr = rWords.getJSONObject(j).getString("str");
                if (lstr.equals(rstr)) {
                    t[i][j] = t[i + 1][j + 1] + 1;
                } else {
                    t[i][j] = Math.max(t[i + 1][j], t[i][j + 1]);
                }
            }
        }
        JSONArray diff = new JSONArray();
        int l = 0, r = 0, p = 0;
        while (l < lLength && r < rLength) {
            JSONObject lObject = lWords.getJSONObject(l);
            JSONObject rObject = rWords.getJSONObject(r);
            String lstr = lObject.getString("str");
            String rstr = rObject.getString("str");
            if (lstr.equals(rstr)) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("diff", 0);
                jsonObject.put("std", lObject);
                jsonObject.put("user", rObject);
                diff.put(jsonObject);
                p++;
                l++;
                r++;
            } else {
                if (t[l + 1][r] > t[l][r + 1]) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("diff", -1);
                    jsonObject.put("std", lObject);
                    diff.put(jsonObject);
                    ++l;
                } else {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("diff", 1);
                    jsonObject.put("user", rObject);
                    diff.put(jsonObject);
                    ++r;
                }
            }
        }
        while (l < lLength) {
            JSONObject lObject = lWords.getJSONObject(l);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("diff", -1);
            jsonObject.put("std", lObject);
            diff.put(jsonObject);
            ++l;
        }

        while (r < rLength) {
            JSONObject rObject = rWords.getJSONObject(r);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("diff", 1);
            jsonObject.put("user", rObject);
            diff.put(jsonObject);
            ++r;
        }

        JSONObject result = new JSONObject();
        result.put("detail", diff);
        result.put("total_num", lWords.length());
        result.put("equal_num", p);

        return result;
    }


    public JSONObject exactMatch(String stdText, String userText) throws JSONException {
        JSONArray stdWords = getWords(stdText);
        JSONArray userWords = getWords(userText);

        return match(stdWords, userWords);
        //System.out.println(result.toString(1));
    }

    public JSONObject fuzzyMatch(String stdText, String userText) throws JSONException {
        JSONArray stdWords = getFuzzyWords(stdText);
        JSONArray userWords = getFuzzyWords(userText);
        //System.out.println(stdWords);
        //System.out.println(userWords);
        return match(stdWords, userWords);
    }

    public JSONArray getFuzzyWords(String text) throws JSONException {
        Set<String> keys = getKeyWordSet(text);
        JSONArray stdWords = getWords(text, keys);
        return stdWords;
    }

    private boolean isValidChar(char c) {
        return Character.isDigit(c) || Character.isLetter(c);
    }

    private ArrayList<String> getWordList(String s) {
        ArrayList<String> result = new ArrayList<String>();
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            if (isValidChar(s.charAt(i))) {
                temp.append(s.charAt(i));
            } else {
                if (temp.length() > 0)
                    result.add(temp.toString());
                temp.setLength(0);
            }
        }
        if (temp.length() > 0)
            result.add(temp.toString());

        return result;
    }


    private JSONArray getWords(String s) throws JSONException {
        return getWords(s, null);
    }

    private JSONArray getWords(String s, Set<String> keySet) throws JSONException {
        JSONArray result = new JSONArray();
        StringBuilder temp = new StringBuilder();
        int prePos = 0;
        for (int i = 0; i < s.length(); i++) {
            if (isValidChar(s.charAt(i))) {
                temp.append(s.charAt(i));
            } else {

                if (temp.length() > 0) {
                    if (keySet == null || keySet.contains(temp.toString())) {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("str", temp.toString());
                        jsonObject.put("from", prePos);
                        jsonObject.put("to", i - 1);
                        result.put(jsonObject);
                    }
                    prePos = i;
                }
                temp.setLength(0);
            }
        }
        if (temp.length() > 0 && (keySet == null || keySet.contains(temp.toString()))) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("str", temp.toString());
            jsonObject.put("from", prePos);
            jsonObject.put("to", s.length() - 1);
            result.put(jsonObject);
        }


        return result;
    }


}
