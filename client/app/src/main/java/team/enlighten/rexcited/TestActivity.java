package team.enlighten.rexcited;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import team.enlighten.rexcited.ai.Robot;
import team.enlighten.rexcited.article.Article;

public class TestActivity extends AppCompatActivity {
    SimpleAdapter adapter;
    List<Map<String, Object>> data;
    ListView chat;
    ProgressBar progress;
    Article article;
    FloatingActionButton btnSpeak;
    LiuLiShuo liuLiShuo;
    Robot robot;
    AtomicBoolean reciting = new AtomicBoolean();
    JSONObject score;
    String reftext;

    @Override
    protected void onPause() {
        try {
            liuLiShuo.stopScore();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            liuLiShuo.stopRecongition();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        String id = getIntent().getStringExtra("article");
        for (Article article : FileManager.getInstance().articles) {
            if (article.id.equals(id)) {
                this.article = article;
                break;
            }
        }

        chat = (ListView) findViewById(R.id.chat_list);
        progress = (ProgressBar) findViewById(R.id.chat_progress);
        btnSpeak = (FloatingActionButton) findViewById(R.id.btn_speak);

        data = new ArrayList<Map<String, Object>>();

        adapter = new SimpleAdapter(getApplicationContext(), data, R.layout.layout_chat_entry, new String[]{
                "text"
        }, new int[]{
                R.id.chat_content
        });
        adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Object data, String textRepresentation) {
                if (view.getId() == R.id.chat_content) {
                    TextView textview = (TextView) view;
                    ConstraintLayout layout = (ConstraintLayout) textview.getParent();
                    ConstraintSet set = new ConstraintSet();
                    set.clone(layout);
                    if (textRepresentation.startsWith("me:")) {
                        textview.setText(textRepresentation.substring(3));
                        textview.setBackgroundResource(R.drawable.bubble_me);
                        ConstraintLayout.LayoutParams layoutParams =
                                new ConstraintLayout.LayoutParams(
                                        ViewGroup.LayoutParams.WRAP_CONTENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT);
                        layoutParams.setMargins(10, 10, 10, 10);
                        layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                        layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                        layoutParams.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
                        textview.setLayoutParams(layoutParams);
                        layout.requestLayout();
                    } else if (textRepresentation.startsWith("other:")) {
                        textview.setText(textRepresentation.substring(6));
                        textview.setBackgroundResource(R.drawable.bubble_other);
                        ConstraintLayout.LayoutParams layoutParams =
                                new ConstraintLayout.LayoutParams(
                                        ViewGroup.LayoutParams.WRAP_CONTENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT);
                        layoutParams.setMargins(10, 10, 10, 10);
                        layoutParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                        layoutParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
                        layoutParams.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
                        textview.setLayoutParams(layoutParams);
                        layout.requestLayout();
                    } else
                        return false;
                    return true;
                } else
                    return false;
            }
        });

        chat.setAdapter(adapter);

        reciting.set(false);
        liuLiShuo = new LiuLiShuo(getApplicationContext());
        robot = new Robot();
        final Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("text", "other:" + msg.getData().getString("msg"));
                data.add(map);
                adapter.notifyDataSetChanged();
                return true;
            }
        });
        robot.setSpeaker(new Robot.Speaker() {
            @Override
            public void speak(String msg) {
                Message handlerMsg = new Message();
                handlerMsg.what = 0;
                Bundle data = new Bundle();
                data.putString("msg", msg);
                handlerMsg.setData(data);
                handler.sendMessage(handlerMsg);
            }

            @Override
            public void pleaseRecite(String reftext) {
                synchronized (robot) {
                    reciting.set(true);
                    Log.d("Robot", "please recite");
                    TestActivity.this.reftext = reftext;
                }
            }

            @Override
            public void result(String reftext, String answer, JSONObject result) {
                Intent intent = new Intent(getApplicationContext(), AnswerActivity.class);
                intent.putExtra("article", article.id);
                intent.putExtra("robot_result", result.toString());
                intent.putExtra("liulishuo_result", score.toString());
                intent.putExtra("reftext", reftext);
                intent.putExtra("answer", answer);
                startActivity(intent);
                finish();
            }
        });
        robot.sendArticle(article);

        progress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.setEnabled(false);
                synchronized (robot) {
                    try {
                        Log.d("Robot", "got " + (reciting.get() ? "recite" : "message"));
                        if (reciting.get())
                            score = liuLiShuo.stopScore();
                        String msg = liuLiShuo.stopRecongition();
                        if (msg != null && msg.length() != 0) {
                            Map<String, Object> map = new HashMap<String, Object>();
                            map.put("text", "me:" + msg);
                            data.add(map);
                            adapter.notifyDataSetChanged();
                            boolean _reciting = reciting.get();
                            reciting.set(false);
                            if (_reciting)
                                robot.sendReciteText(msg);
                            else
                                robot.sendMsg(msg);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                btnSpeak.setVisibility(View.VISIBLE);
                btnSpeak.setEnabled(true);
                progress.setVisibility(View.INVISIBLE);
                btnSpeak.bringToFront();
            }
        });
        btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSpeak.setEnabled(false);
                synchronized (robot) {
                    Log.d("User", "I'm " + (reciting.get() ? "reciting" : "speaking"));
                    try {
                        if (reciting.get())
                            liuLiShuo.startScore(reftext);
                        liuLiShuo.startRecongition();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                btnSpeak.setVisibility(View.INVISIBLE);
                progress.setVisibility(View.VISIBLE);
                progress.setEnabled(true);
                progress.bringToFront();
            }
        });

        btnSpeak.setEnabled(false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.RECORD_AUDIO
            }, 0);
        } else
            btnSpeak.setEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        btnSpeak.setEnabled(true);
    }
}
