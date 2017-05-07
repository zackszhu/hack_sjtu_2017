package team.enlighten.rexcited;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import team.enlighten.rexcited.article.Article;
import team.enlighten.rexcited.article.Score;
import team.enlighten.rexcited.article.TextParser;

public class ReportActivity extends AppCompatActivity {
    ImageView[] stars;
    String liulishuoResult;
    TextView pctFluency;
    TextView pctAccuracy;
    Article article;
    ImageView btnUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        liulishuoResult = getIntent().getStringExtra("liulishuo_result");
        Log.d("Liulishuo Result", liulishuoResult);
        String id = getIntent().getStringExtra("article");
        for (Article article : FileManager.getInstance().articles) {
            if (article.id.equals(id)) {
                this.article = article;
                break;
            }
        }

        pctFluency = (TextView) findViewById(R.id.pct_fluency);
        pctAccuracy = (TextView) findViewById(R.id.pct_accuracy);
        btnUpload = (ImageView) findViewById(R.id.btn_upload);

        stars = new ImageView[4];
        stars[0] = (ImageView) findViewById(R.id.img_star_1);
        stars[1] = (ImageView) findViewById(R.id.img_star_2);
        stars[2] = (ImageView) findViewById(R.id.img_star_3);
        stars[3] = (ImageView) findViewById(R.id.img_star_4);

        try {
            JSONObject result = new JSONObject(liulishuoResult);
            pctFluency.setText(Integer.toString(result.getInt("fluency")));
            pctAccuracy.setText(Integer.toString(result.getInt("integrity")));
            for (int i = 0; i < result.getInt("overall") / 25; i++)
                stars[i].setImageDrawable(getDrawable(R.drawable.ic_full_star));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject result = new JSONObject(liulishuoResult);
                    final Score score = new Score(result.getInt("overall"), result.getInt("fluency"), result.getInt("integrity"), article);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                HttpHandler.getInstance().PostTask(article.id, TextParser.EArticleType.Article, score);
                                btnUpload.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), "Upload success", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
