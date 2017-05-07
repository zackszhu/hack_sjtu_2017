package team.enlighten.rexcited;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends AppCompatActivity {
    Button btnCreate;
    EditText txtUsername, txtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        btnCreate = (Button) findViewById(R.id.btn_create);
        txtUsername = (EditText) findViewById(R.id.reg_username);
        txtPassword = (EditText) findViewById(R.id.reg_password);

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (txtUsername.getText().toString().length() == 0) {
                    Toast.makeText(getApplicationContext(), "Please input username", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (txtPassword.getText().toString().length() == 0) {
                    Toast.makeText(getApplicationContext(), "Please input password", Toast.LENGTH_SHORT).show();
                    return;
                }
                btnCreate.setEnabled(false);
                final String username = txtUsername.getText().toString();
                final String password = txtPassword.getText().toString();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            HttpHandler.getInstance().Register(username, password, username);
                            HttpHandler.getInstance().Login(username, password);
                            btnCreate.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "Create success", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), MeActivity.class);
                                    intent.putExtra("nickname", username);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            btnCreate.setEnabled(true);
                        }
                    }
                }).start();
            }
        });
    }
}
