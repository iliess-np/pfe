package com.iliessnp.pfe;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.vishnusivadas.advanced_httpurlconnection.PutData;

public class Login extends AppCompatActivity {
    TextInputEditText textInputEditTextPhone, textInputEditTextEmail, textInputEditTextPassword;
    Button btnLogin;
    TextView btnRegister;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        textInputEditTextPhone = findViewById(R.id.phone);
        textInputEditTextEmail = findViewById(R.id.email);
        textInputEditTextPassword = findViewById(R.id.password);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progress);
        btnRegister = findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SignUp.class);
            startActivity(intent);
            finish();
        });

        btnLogin.setOnClickListener(v -> {
            String phone, email, password;
            phone = String.valueOf(textInputEditTextPhone.getText());
            email = String.valueOf(textInputEditTextEmail.getText());
            password = String.valueOf(textInputEditTextPassword.getText());

            if ((!phone.equals("") || !email.equals("")) && !password.equals("")) {
                progressBar.setVisibility(View.VISIBLE);
                Handler handler = new Handler();
                handler.post(() -> {
                    String[] field = new String[3];
                    field[0] = "phone";
                    field[1] = "email";
                    field[2] = "password";

                    String[] data = new String[3];
                    data[0] = phone;
                    data[1] = email;
                    data[2] = password;

                    PutData putData = new PutData("https://helptech29.000webhostapp.com/login.php", "POST", field, data);
                    if (putData.startPut()) {
                        if (putData.onComplete()) {
                            progressBar.setVisibility(View.GONE);
                            String result = putData.getResult();
                            Log.e("login: sender id ", result);
                            if (!result.equals("phone/email or Password wrong")) {
                                Intent intent = new Intent(Login.this, MapsActivity.class);
                                intent.putExtra("id", result);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), result, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
            } else {
                if (TextUtils.isEmpty(textInputEditTextPassword.getText())) {
                    String emptyField = getString(R.string.emptyField);
                    textInputEditTextPassword.setError(emptyField);
                }
                if (TextUtils.isEmpty(textInputEditTextPhone.getText())) {
                    String emptyField = getString(R.string.emptyField);
                    textInputEditTextPhone.setError(emptyField);
                }
                if (TextUtils.isEmpty(textInputEditTextEmail.getText())) {
                    String emptyField = getString(R.string.emptyField);
                    textInputEditTextEmail.setError(emptyField);
                }
                String emptyFields = getString(R.string.emptyFields);
                Toast.makeText(getApplicationContext(), emptyFields, Toast.LENGTH_SHORT).show();
            }
        });
        String myname = getString(R.string.myname);
        FloatingActionButton fab = findViewById(R.id.contact);
        fab.setOnClickListener(view -> Snackbar.make(view, myname, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());
    }
}
