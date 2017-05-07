package team.enlighten.rexcited;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import team.enlighten.rexcited.article.Article;

/**
 * Created by lzn on 2017-05-06.
 */

public class PreviewActivity extends AppCompatActivity {
    Article article;
    TextView titleView;
    TextView contentView;
    Button startTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        titleView = (TextView) findViewById(R.id.preview_title);
        startTest = (Button) findViewById(R.id.btn_preview_start_test);
        contentView = (TextView) findViewById(R.id.preview_content);

        final String id = getIntent().getStringExtra("article");
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FileManager.getInstance().fetchArticle();
                } catch (final Exception e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                    return;
                }
                for (Article article : FileManager.getInstance().articles) {
                    if (article.id.equals(id)) {
                        PreviewActivity.this.article = article;
                        break;
                    }
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        titleView.setText(article.Title);
                        contentView.setText(article.toString().substring(article.Title.length() + 1));
                        startTest.setEnabled(true);
                    }
                });
            }
        }).start();

        startTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), TestActivity.class);
                intent.putExtra("article", article.id);
                startActivity(intent);
                finish();
            }
        });
        startTest.setEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.preview_menu, menu);
        return true;
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
