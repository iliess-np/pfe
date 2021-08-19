package com.iliessnp.pfe;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
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

import java.lang.reflect.Method;

public class Login extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    TextInputEditText textInputEditTextPhone, textInputEditTextEmail, textInputEditTextPassword;
    Button btnLogin;
    TextView btnRegister;
    ProgressBar progressBar;
    ProgressDialog mProgressDialog;

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

        mProgressDialog = new ProgressDialog(Login.this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMessage(getString(R.string.progress_detail));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setProgressNumberFormat(null);
        mProgressDialog.setProgressPercentFormat(null);

        btnRegister.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SignUp.class);
            startActivity(intent);
            finish();
        });

        btnLogin.setOnClickListener(v -> {
            String emptyField = getString(R.string.emptyField);
            String emptyFields = getString(R.string.emptyFields);
            if (TextUtils.isEmpty(textInputEditTextPassword.getText())) {
                textInputEditTextPassword.setError(emptyField);
                return;
            }
            if (TextUtils.isEmpty(textInputEditTextPhone.getText()) && TextUtils.isEmpty(textInputEditTextEmail.getText())) {
                textInputEditTextPhone.setError(emptyField);
                textInputEditTextEmail.setError(emptyField);
                Toast.makeText(getApplicationContext(), emptyFields, Toast.LENGTH_SHORT).show();
                return;
            }

            mProgressDialog.show();
            if (checkWifiOnAndConnected() || checkMobileOnAndConnected()) {
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
                                mProgressDialog.dismiss();
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
                    Toast.makeText(getApplicationContext(), emptyFields, Toast.LENGTH_SHORT).show();
                }
            } else {
                mProgressDialog.dismiss();
                Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                        "you don't have internet connection", Snackbar.LENGTH_LONG);
                snackbar.show();
                OnWIFI();
            }
            mProgressDialog.dismiss();
        });
        String myName = getString(R.string.myname);
        FloatingActionButton fab = findViewById(R.id.contact);
        fab.setOnClickListener(view -> Snackbar.make(view, myName, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());
    }

    private boolean checkWifiOnAndConnected() {
        WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        assert wifiMgr != null;
        if (wifiMgr.isWifiEnabled()) { // Wi-Fi is ON
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            if (wifiInfo.getIpAddress() <= 0) {
                return false; // Not connected to an AP
            }
            return true; // Connected to an AP
        } else {
            return false; // Wi-Fi is OFF
        }
    }

    private boolean checkMobileOnAndConnected() {
        boolean mobileDataEnabled = false; // Assume disabled
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            assert cm != null;
            Class cmClass = Class.forName(cm.getClass().getName());
            Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
            method.setAccessible(true); // Make the method callable
            // get the setting for "mobile data"
            mobileDataEnabled = (Boolean) method.invoke(cm);
            Toast.makeText(this, "checkMobileOnAndConnected " + mobileDataEnabled, Toast.LENGTH_SHORT).show();
            return mobileDataEnabled;
        } catch (Exception e) {
            // Some problem accessible private API
            // TODO do whatever error handling you want here
        }
        return mobileDataEnabled;
    }


    private void OnWIFI() {
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder
                .setMessage("Enable WIFI")
                .setCancelable(false)
                .setPositiveButton("YES", (dialog, which) ->
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)))
                .setNegativeButton("NO", (dialog, which) ->
                        dialog.cancel());

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}
