package team.enlighten.rexcited;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import team.enlighten.rexcited.article.Article;

public class AnswerActivity extends AppCompatActivity {
    boolean knowledge;
    String refText;
    String answer;
    TextView title;
    TextView refView;
    TextView answerView;
    Button viewScore;
    Button memorizeAgain;
    String robotResult;
    String liulishuoResult;
    Article article;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        knowledge = getIntent().getBooleanExtra("knowledge", false);

        if (knowledge) {
            setContentView(R.layout.activity_knowledge_answer);
        } else {
            setContentView(R.layout.activity_answer);

            String id = getIntent().getStringExtra("article");
            for (Article article : FileManager.getInstance().articles) {
                if (article.id.equals(id)) {
                    this.article = article;
                    break;
                }
            }

            refText = getIntent().getStringExtra("reftext");
            answer = getIntent().getStringExtra("answer");
            robotResult = getIntent().getStringExtra("robot_result");
            liulishuoResult = getIntent().getStringExtra("liulishuo_result");

            title = (TextView) findViewById(R.id.answer_title);
            refView = (TextView) findViewById(R.id.ref_content);
            answerView = (TextView) findViewById(R.id.answer_content);
            viewScore = (Button) findViewById(R.id.btn_view_score);
            memorizeAgain = (Button) findViewById(R.id.btn_memorize_again);

            title.setText(article.Title);
            Log.d("Robot result", getIntent().getStringExtra("robot_result"));
            try {
                JSONObject robot_result = new JSONObject(getIntent().getStringExtra("robot_result"));
                String coloredRefText = refText;
                JSONArray detail = robot_result.getJSONArray("detail");
                for (int i = detail.length() - 1; i >= 0; i--) {
                    JSONObject e = detail.getJSONObject(i);
                    if (e.getInt("diff") == -1) {
                        int from = e.getJSONObject("std").getInt("from");
                        int to = e.getJSONObject("std").getInt("to") + 1;
                        coloredRefText = coloredRefText.substring(0, from) + "<font color='red'>" + coloredRefText.substring(from, to) + "</font>" + coloredRefText.substring(to);
                    }
                }
                refView.setText(Html.fromHtml(coloredRefText));
            } catch (JSONException e) {
                refView.setText(refText);
                e.printStackTrace();
            }
            try {
                JSONObject robot_result = new JSONObject(getIntent().getStringExtra("robot_result"));
                String coloredAnswer = answer;
                JSONArray detail = robot_result.getJSONArray("detail");
                for (int i = detail.length() - 1; i >= 0; i--) {
                    JSONObject e = detail.getJSONObject(i);
                    if (e.getInt("diff") == 1) {
                        int from = e.getJSONObject("user").getInt("from");
                        int to = e.getJSONObject("user").getInt("to") + 1;
                        coloredAnswer = coloredAnswer.substring(0, from) + "<font color='red'>" + coloredAnswer.substring(from, to) + "</font>" + coloredAnswer.substring(to);
                    }
                }
                answerView.setText(Html.fromHtml(coloredAnswer));
            } catch (JSONException e) {
                answerView.setText(answer);
                e.printStackTrace();
            }
            viewScore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ReportActivity.class);
                    intent.putExtra("article", article.id);
                    intent.putExtra("liulishuo_result", liulishuoResult);
                    startActivity(intent);
                    finish();
                }
            });
            memorizeAgain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), PreviewActivity.class);
                    intent.putExtra("article", article.id);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }
}
