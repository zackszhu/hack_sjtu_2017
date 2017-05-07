package team.enlighten.rexcited;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import team.enlighten.rexcited.article.Article;
import team.enlighten.rexcited.article.Score;

public class MeActivity extends AppCompatActivity {
    ListView activityList;
    List<Map<String, Object>> data;
    SimpleAdapter adapter;
    View btnArticle, btnKnowledge;
    TextView txtCount;
    TextView txtLastRate;
    TextView txtName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_me);

        activityList = (ListView) findViewById(R.id.activity_list);
        btnArticle = findViewById(R.id.btn_me_article);
        btnKnowledge = findViewById(R.id.btn_me_knowledge);
        txtCount = (TextView) findViewById(R.id.txt_count);
        txtLastRate = (TextView) findViewById(R.id.txt_last_rate);
        txtName = (TextView) findViewById(R.id.txt_name);
        txtName.setText(getIntent().getStringExtra("nickname"));

        data = new ArrayList<Map<String, Object>>();

        adapter = new SimpleAdapter(
                getApplicationContext(),
                data,
                R.layout.layout_activity_entry,
                new String[]{
                        "title",
                        "time",
                        "desc"
                },
                new int[]{
                        R.id.activity_title,
                        R.id.activity_time,
                        R.id.activity_desc
                });
        activityList.setAdapter(adapter);
        activityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), PreviewActivity.class);
                Article article = (Article) data.get(position).get("article");
                intent.putExtra("article", article.id);
                startActivity(intent);
            }
        });

        btnArticle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FilesActivity.class);
                intent.putExtra("type", "article");
                startActivity(intent);
            }
        });
        btnKnowledge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FilesActivity.class);
                intent.putExtra("type", "knowledge");
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Score score = new Score();
                    final int count = HttpHandler.getInstance().GetOverview(score);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            txtCount.setText(Integer.toString(count));
                            if (count == 0)
                                txtLastRate.setText(Integer.toString(0));
                            else
                                txtLastRate.setText(Integer.toString((int) (score.Score / count)));
                        }
                    });
                    data.clear();
                    for (Article article : HttpHandler.getInstance().GetAllTask(0, 10)) {
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("title", article.Title);
                        map.put("desc", article.toString().substring(article.Title.length() + 1));
                        map.put("time", article.create_time);
                        map.put("article", article);
                        data.add(map);
                    }
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
