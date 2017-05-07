package team.enlighten.rexcited;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import team.enlighten.rexcited.article.Article;
import team.enlighten.rexcited.article.TextParser;

public class EditFileActivity extends AppCompatActivity {
    EditText content;
    EditText title;
    Button btnSave, btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_file);

        content = (EditText) findViewById(R.id.edit_content);
        title = (EditText) findViewById(R.id.edit_title);
        btnSave = (Button) findViewById(R.id.btn_save);
        btnCancel = (Button) findViewById(R.id.btn_cancel);

        if (getIntent().getStringExtra("content") != null) {
            content.setText(getIntent().getStringExtra("content"));
        }

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSave.setEnabled(false);
                final Article article = new TextParser().Parse("# " + title.getText().toString() + "\n" + content.getText().toString());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            FileManager.getInstance().postArticle(article, TextParser.EArticleType.Article, TextParser.EArticlePerm.Public);
                            btnSave.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Upload success", Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            });
                        } catch (Exception e) {
                            btnSave.post(new Runnable() {
                                @Override
                                public void run() {
                                    btnSave.setEnabled(true);
                                }
                            });
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }
}
