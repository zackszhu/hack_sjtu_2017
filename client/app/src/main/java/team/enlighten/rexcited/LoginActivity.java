package team.enlighten.rexcited;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {
    EditText login_username;
    EditText login_password;
    Button btnCreate, btnLogin;
    LiuLiShuo liuLiShuo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setCustomView(R.layout.layout_center_title);

        login_username = ((EditText) findViewById(R.id.login_username));
        login_password = ((EditText) findViewById(R.id.login_password));
        btnCreate = (Button) findViewById(R.id.btn_create);
        btnLogin = (Button) findViewById(R.id.btn_login);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (login_username.getText().toString().length() == 0) {
                    Toast.makeText(getApplicationContext(), "Please input username", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (login_password.getText().toString().length() == 0) {
                    Toast.makeText(getApplicationContext(), "Please input password", Toast.LENGTH_SHORT).show();
                    return;
                }
                btnLogin.setEnabled(false);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            HttpHandler.getInstance().Login(login_username.getText().toString(), login_password.getText().toString());
                            Intent intent = new Intent(getApplicationContext(), MeActivity.class);
                            intent.putExtra("nickname", login_username.getText().toString());
                            startActivity(intent);
                            finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                            btnLogin.setEnabled(true);
                        }
                    }
                }).start();
            }
        });
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}
