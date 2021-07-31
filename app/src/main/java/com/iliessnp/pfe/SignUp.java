package com.iliessnp.pfe;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.vishnusivadas.advanced_httpurlconnection.PutData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class SignUp extends AppCompatActivity {

    TextInputEditText textInputEditTextFname , textInputEditTextLname , textInputEditTextPhone
            , textInputEditTextEmail ,textInputEditTextPassword , textInputEditTextPasswordRepeat;
    Button btnSignUp;
    TextView TextViewlogin;
    ProgressBar progressBar;
    Spinner spWilaya,spDaira,spCommune;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        textInputEditTextFname = findViewById(R.id.fname);
        textInputEditTextLname = findViewById(R.id.lname);
        textInputEditTextPhone = findViewById(R.id.phone);
        textInputEditTextEmail = findViewById(R.id.email);
        textInputEditTextPassword = findViewById(R.id.password);
        textInputEditTextPasswordRepeat = findViewById(R.id.passwordRepeat);
        TextViewlogin = findViewById(R.id.loginText);
        btnSignUp = findViewById(R.id.btnSignUp);

        progressBar = findViewById(R.id.progress);

        TextViewlogin.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(),Login.class);
            startActivity(intent);
            finish();
        });


        //wilaya spinner
        spWilaya = findViewById(R.id.spWilaya);
        spDaira = findViewById(R.id.spDaira);
        spCommune = findViewById(R.id.spCommune);

        String jsonn = null;
        try {
            InputStream is = getAssets().open("wilayas.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonn = new String(buffer, "UTF-8");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        ArrayList<String> arrayWilayas=new ArrayList<>();
        final ArrayList<String> arraydairas=new ArrayList<>();
        final ArrayList<String> arraycommunes=new ArrayList<>();
        try {
            JSONObject json= new JSONObject(jsonn);
            final JSONArray wilaya=json.getJSONArray("wilayas");

            //JSONObject wilayas=json.getJSONObject("wilayas");

            for ( int i=0; i<wilaya.length();i++){
                JSONObject w= wilaya.getJSONObject(i)  ;
                String wilayasString=w.getString("name");
                int wilayasid=w.getInt("id");
                arrayWilayas.add(wilayasid+":"+wilayasString);
                JSONArray dairas=w.getJSONArray("dairas");
            }
            ArrayAdapter<String> adapterwilaya=new ArrayAdapter<>(this,R.layout.support_simple_spinner_dropdown_item,arrayWilayas);

            spWilaya.setAdapter(adapterwilaya);
            spWilaya.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        arraydairas.clear();
                        arraycommunes.clear();

                        JSONObject wjson= wilaya.getJSONObject((int) id);
                        final JSONArray dairas=wjson.getJSONArray("dairas");

                        for ( int i=0; i<dairas.length();i++){
                            JSONObject dairasjsonJSONObject= dairas.getJSONObject(i)  ;
                            String dairasString=dairasjsonJSONObject.getString("name");
                            arraydairas.add(dairasString);
                        }
                        ArrayAdapter adapterdaira=new ArrayAdapter<>(SignUp.this
                                ,R.layout.support_simple_spinner_dropdown_item,arraydairas);
                        spDaira.setAdapter(adapterdaira);
                        spDaira.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                try {
                                    arraycommunes.clear();
                                    JSONObject djson= dairas.getJSONObject((int) id);
                                    JSONArray communes=djson.getJSONArray("communes");
                                    for ( int k=0; k<communes.length();k++){
                                        JSONObject communesjsonJSONObject= communes.getJSONObject(k)  ;
                                        String communesString=communesjsonJSONObject.getString("name");
                                        int communesid=communesjsonJSONObject.getInt("id");
                                        arraycommunes.add(communesString);
                                    }
                                    ArrayAdapter adaptercommunes=new ArrayAdapter<>(SignUp.this,R.layout.support_simple_spinner_dropdown_item,arraycommunes);
                                    spCommune.setAdapter(adaptercommunes);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
            spWilaya.getSelectedItem().toString();
        }
        catch (Exception ex){}

        // btn singUp
        btnSignUp.setOnClickListener(v -> {
            String  fname ,  lname ,  phone , email , password , passwordRepeat ,  wilaya , daira;

            fname = String.valueOf(textInputEditTextFname.getText());
            lname = String.valueOf(textInputEditTextLname.getText());
            phone = String.valueOf(textInputEditTextPhone.getText());
            email = String.valueOf(textInputEditTextEmail.getText());
            password = String.valueOf(textInputEditTextPassword.getText());
            passwordRepeat = String.valueOf(textInputEditTextPasswordRepeat.getText());

            wilaya = spWilaya.getSelectedItem().toString()  ;
            daira = spDaira.getSelectedItem().toString()  ;

            if( TextUtils.isEmpty(textInputEditTextFname.getText())){
                String emptyField = getString(R.string.emptyField);
                textInputEditTextFname.setError( emptyField );
            }
            if( TextUtils.isEmpty(textInputEditTextLname.getText())){
                String emptyField = getString(R.string.emptyField);
                textInputEditTextLname.setError( emptyField );
            }
            if( TextUtils.isEmpty(textInputEditTextPhone.getText())){
                String emptyField = getString(R.string.emptyField);
                textInputEditTextPhone.setError( emptyField );
            }
            if( TextUtils.isEmpty(textInputEditTextPassword.getText())){
                String emptyField = getString(R.string.emptyField);
                textInputEditTextPassword.setError( emptyField );
            }
            if( TextUtils.isEmpty(textInputEditTextPasswordRepeat.getText())){
                String emptyField = getString(R.string.emptyField);
                textInputEditTextPasswordRepeat.setError( emptyField );
            }

            if (password.equals(passwordRepeat)){
                if (!fname.equals("") && !lname.equals("") && (!phone.equals("") || !email.equals("")) && !password.equals("") && !passwordRepeat.equals("") && !wilaya.equals("") &&!daira.equals("")){
                    progressBar.setVisibility(View.VISIBLE);
                    Handler handler = new Handler();
                    handler.post(() -> {
                        String[] field = new String[7];
                        field[0] = "f_name";
                        field[1] = "l_name";
                        field[2] = "phone";
                        field[3] = "email";
                        field[4] = "password";
                        field[5] = "wilaya";
                        field[6] = "daira";

                        String[] data = new String[7];
                        data[0] = fname;
                        data[1] = lname;
                        data[2] = phone;
                        data[3] = email;
                        data[4] = password;
                        data[5] = wilaya;
                        data[6] = daira;

                        PutData putData = new PutData("https://helptech29.000webhostapp.com/signup.php", "POST", field, data);
                        if (putData.startPut()) {
                            if (putData.onComplete()) {
                                progressBar.setVisibility(View.GONE);
                                String result = putData.getResult();
                                if (result.equals("Sign_Up_Success")){
                                    Toast.makeText(getApplicationContext(),result,Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(),Login.class);
                                    startActivity(intent);
                                    //finish();
                                }else {
                                    Toast.makeText(getApplicationContext(),result,Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }else {
                    String emptyFields = getString(R.string.emptyFields);
                    Toast.makeText(getApplicationContext(),emptyFields,Toast.LENGTH_SHORT).show();
                }
            }else {
                String PasswordIncorrect = getString(R.string.PasswordIncorrect);
                textInputEditTextPasswordRepeat.setError( PasswordIncorrect );
                textInputEditTextPassword.setError( PasswordIncorrect );
                Toast.makeText(getApplicationContext(),PasswordIncorrect,Toast.LENGTH_SHORT).show();
            }
        });

        // Floating Action Button
        String myname = getString(R.string.myname);
        FloatingActionButton fab = findViewById(R.id.contact);
        fab.setOnClickListener(view -> Snackbar.make(view, myname, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());
    }
}