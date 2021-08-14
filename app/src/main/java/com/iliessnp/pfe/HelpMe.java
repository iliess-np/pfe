package com.iliessnp.pfe;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class HelpMe extends AppCompatActivity {
    private static final int REQUEST_CALL = 10001;
    private static final String TAG = "HelpMe";
    Button btnShowHome, btnSMS, btnCall;
    String senderId, phone, myLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_me);

        btnShowHome = findViewById(R.id.btn_showHome);
        btnSMS = findViewById(R.id.btn_sms);
        btnCall = findViewById(R.id.btn_call);

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            senderId = intent.getExtras().getString("id");
            phone = intent.getExtras().getString("phone");
            myLocation = intent.getExtras().getString("myLocation");
        }
        btnShowHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("id", senderId);
                startActivity(intent);
            }
        });


        btnSMS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                senSMS();
            }
        });
        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makePhoneCall();
            }
        });
    }

    private void senSMS() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                try {
                    String txtMobile = phone;
                    String locationUrl = "http://maps.google.com/maps?q=loc:" + myLocation;
                    Log.d(TAG, myLocation);
                    String txtMessage = "please check on me, i need help his is where i am => " + locationUrl;
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(txtMobile, null, txtMessage, null, null);
                    Toast.makeText(HelpMe.this, "SMS Sent Successfully", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(HelpMe.this, "SMS Failed to Send, Please try again", Toast.LENGTH_SHORT).show();
                }
            } else {
                requestPermissions(new String[]{Manifest.permission.SEND_SMS}, 1);
            }
        }
    }


    private void makePhoneCall() {
        if (phone.trim().length() > 0) {

            if (ContextCompat.checkSelfPermission(HelpMe.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(HelpMe.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
            } else {
                String dial = "tel:" + phone;
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
            }

        } else {
            Toast.makeText(HelpMe.this, "Enter Phone Number", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }
}