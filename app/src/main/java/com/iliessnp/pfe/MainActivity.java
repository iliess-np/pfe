package com.iliessnp.pfe;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    public static final int DEFAULT_UPDATE_INTERVALE = 3;
    public static final int FAST_UPDATE_INTERVAL = 5;
    public static final String KEY_SENDERID = "sender_id";
    private static final String TAG = "MainActivity";
    private static final int PERMISSIONS_FINE_LOCATION = 99;
    public static boolean firstTime = true;

    //accelerometer
    TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed, tv_sensor, tv_updates, tv_address;
    SwitchMaterial sw_locationsUpdates, sw_gps;
    String senderId, myLocation = "", accuracy, myLocationPrv = "1";
    double lon, lat;
    LocationRequest locationRequest;
    LocationCallback locationCallback;
    FusedLocationProviderClient fusedLocationProviderClient;
    //Fetch data
    Button btnFetch, btnShowMap, btnPanic;
    ListView listview;
    ProgressDialog mProgressDialog;
    String f_name, l_name, phone;
    //QR code
    Button btnGen, btnSendData;
    ImageView ivOutput;
    LocationManager locationManager;
    SensorManager sensorManager;
    Sensor accelerometer;
    TextView x, y, z, sum, jump, fall, tv_result;
    float xx, yy, zz, summ;
    int falll, jumpp;
    ArrayList<Float> xData = new ArrayList<>();
    ArrayList<Float> yData = new ArrayList<>();
    ArrayList<Float> zData = new ArrayList<>();
    ArrayList<Float> acceleration = new ArrayList<>();
    long timeNow, timePrv = 0;
    String alertTypes = "";
    private String TransitionTypeInitial = "", TransitionTypePrv = "tv_result";

    // this method convert any stream to string
    public static String ConvertInputToStringNoChange(InputStream inputStream) {

        BufferedReader bureader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String linereultcal = "";

        try {
            while ((line = bureader.readLine()) != null) {
                linereultcal += line;
            }
            inputStream.close();
        } catch (Exception ignored) {
            Log.e(TAG, "catch inputStream ConvertInputToStringNoChange");
        }

        return linereultcal;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            senderId = intent.getExtras().getString("id");
        }
        //QR code
        btnGen = findViewById(R.id.btn_generate);
        ivOutput = findViewById(R.id.iv_output);

        //Map
        btnShowMap = findViewById(R.id.btn_showMap);

        //GPS
        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_speed = findViewById(R.id.tv_speed);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_updates = findViewById(R.id.tv_updates);
        tv_address = findViewById(R.id.tv_address);
        sw_gps = findViewById(R.id.sw_gps);
        sw_locationsUpdates = findViewById(R.id.sw_locationsupdates);
        btnSendData = findViewById(R.id.btn_sendData);
        btnPanic = findViewById(R.id.btn_panic);

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVALE);
        locationRequest.setFastestInterval(1000 * FAST_UPDATE_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        //Fetch data
        btnFetch = (Button) findViewById(R.id.btnfetch);
        listview = (ListView) findViewById(R.id.listView);
        //QR code
        btnGen = findViewById(R.id.btn_generate);
        ivOutput = findViewById(R.id.iv_output);
        //accelerometer
        x = findViewById(R.id.x);
        y = findViewById(R.id.y);
        z = findViewById(R.id.z);
        sum = findViewById(R.id.sum);
        jump = findViewById(R.id.jump);
        fall = findViewById(R.id.fall);
        tv_result = findViewById(R.id.tv_result);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        sw_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sw_gps.isChecked()) {
                    //most accurate position
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    tv_sensor.setText(R.string.sw_gps_singGPS);
                } else {
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    tv_sensor.setText(R.string.sw_gps_singWifi);
                }
            }
        });

        sw_locationsUpdates.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sw_locationsUpdates.isChecked()) {
                    startLocationsUpdates();
                } else {
                    stopLocationsUpdates();
                }
            }
        });

        btnFetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (senderId.equals("")) {
                    Toast.makeText(MainActivity.this, "Please Enter Detail", Toast.LENGTH_SHORT).show();
                } else {
                    GetMatchData();
                }
            }
        });

        btnGen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetMatchData();
                try {
                    alertTypes = "QR_code_scanned";
                    sendAlertQR(alertTypes);
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "catch sendAlertQR btnGen");

                    e.printStackTrace();
                }
                String sText = f_name + "\n" + l_name + "\n" + phone;

                MultiFormatWriter writer = new MultiFormatWriter();
                try {
                    BitMatrix matrix = writer.encode(sText, BarcodeFormat.QR_CODE, 350, 350);
                    BarcodeEncoder encoder = new BarcodeEncoder();
                    Bitmap bitmap = encoder.createBitmap(matrix);
                    ivOutput.setImageBitmap(bitmap);
                } catch (WriterException e) {
                    Log.e(TAG, "catch QR btnGen");
                    e.printStackTrace();
                }
            }
        });

        btnSendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    send();
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "catch send btnSendData");
                    e.printStackTrace();
                }
            }
        });

        btnShowMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMap();
            }
        });

        btnPanic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    alertTypes = "user_in_Panic";
                    sendAlertQR(alertTypes);
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "catch sendAlertQR btnPanic");
                    e.printStackTrace();
                }
                if (myLocation == null) {
                    updateGPS();
                }
                Intent intent = new Intent(getApplicationContext(), HelpMe.class);
                intent.putExtra("id", senderId);
                intent.putExtra("phone", phone);
                intent.putExtra("myLocation", myLocation);
                startActivity(intent);
            }
        });

        try {
            Thread.sleep(4 * 1000);
        } catch (InterruptedException ie) {
            Log.e(TAG, "catch Thread sleep");
            Thread.currentThread().interrupt();
        }


        //Check gps is enable or not
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        assert locationManager != null;
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            OnGPS();
        }

        assert sensorManager != null;
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener((SensorEventListener) MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        //this  will, be called wen interval is met
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    //TODO:  check it
                    //Fixed
                    updateUIValues(location);
                }
            }
        };
        updateGPS();
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                //TODO: check it
                //Fixed
                updateGPS();
                if (!myLocation.equals(myLocationPrv)) {
                    try {
                        //TODO:  check it
                        //Fixed
                        send();
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "catch send Timer");
                        e.printStackTrace();
                    }
                    myLocationPrv = myLocation;
                }
                TransitionTypeInitial = String.valueOf(GeofenceBroadcastReceiver.getTransitionType());
                if (!TransitionTypeInitial.equals(TransitionTypePrv)) {
                    String str;
                    switch (TransitionTypeInitial) {
                        case "1":
                            str = "GEOFENCE_TRANSITION_ENTER";
                            break;
                        case "2":
                            str = "GEOFENCE_TRANSITION_EXIT";
                            break;
                        case "4":
                            str = "GEOFENCE_TRANSITION_DWELL";
                            break;
                        default:
                            str = "TransitionTypeInitial";
                    }
                    tv_result.setText(str);
                    try {
                        //TODO:  check it
                        //Fixed
                        sendAlertQR(str);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "catch sendAlertQR Timer");
                        e.printStackTrace();
                    }
                    TransitionTypePrv = TransitionTypeInitial;
                }
            }
        }, 0, 10000);

    }

    public void showMap() {
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        intent.putExtra("id", senderId);
        startActivity(intent);
    }

    //Fetch data
    private void GetMatchData() {
        mProgressDialog = new ProgressDialog(MainActivity.this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setMessage(getString(R.string.progress_detail));
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(true);
        mProgressDialog.setProgress(0);
        mProgressDialog.setProgressNumberFormat(null);
        mProgressDialog.setProgressPercentFormat(null);
        //change added
        mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mProgressDialog.dismiss();
            }
        });
        mProgressDialog.show();
        mProgressDialog.setProgress(25);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, Config5.MATCHDATA_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        mProgressDialog.setProgress(43);
                        mProgressDialog.setProgress(86);
                        showJSON(response);
                        mProgressDialog.setProgress(100);
                        mProgressDialog.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //i think this one is where its shows when going back to home screen
                        Toast.makeText(MainActivity.this, "error onErrorResponse GetMatchData " + error, Toast.LENGTH_LONG).show();
                        Log.e(TAG,"error onErrorResponse GetMatchData "+error);
                        mProgressDialog.dismiss();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
                map.put(KEY_SENDERID, senderId);
                return map;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
        mProgressDialog.dismiss();

    }

    private void showJSON(String response) {
        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray result = jsonObject.getJSONArray(Config5.JSON_ARRAY);

            for (int i = 0; i < result.length(); i++) {
                JSONObject jo = result.getJSONObject(i);
                f_name = jo.getString(Config5.KEY_FNAME);
                l_name = jo.getString(Config5.KEY_LNAME);
                phone = jo.getString(Config5.KEY_PHONE);

                final HashMap<String, String> employees = new HashMap<>();
                employees.put(Config5.KEY_FNAME, "f_name = " + f_name);
                employees.put(Config5.KEY_LNAME, "l_name = " + l_name);
                employees.put(Config5.KEY_PHONE, "phone = " + phone);

                list.add(employees);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ListAdapter adapter = new SimpleAdapter(
                MainActivity.this, list, R.layout.list_item,
                new String[]{Config5.KEY_FNAME, Config5.KEY_LNAME, Config5.KEY_PHONE},
                new int[]{R.id.tv_fname, R.id.tv_lname, R.id.tv_phone});

        listview.setAdapter(adapter);
    }

    //GPS tracking
    private void stopLocationsUpdates() {
        tv_updates.setText(R.string.not_tracking_location);
        tv_lat.setText(R.string.not_tracking_location);
        tv_lon.setText(R.string.not_tracking_location);
        tv_speed.setText(R.string.not_tracking_location);
        tv_address.setText(R.string.not_tracking_location);
        tv_accuracy.setText(R.string.not_tracking_location);
        tv_altitude.setText(R.string.not_tracking_location);
        tv_sensor.setText(R.string.not_tracking_location);

        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void startLocationsUpdates() {
        tv_updates.setText(R.string.location_tracked);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //get permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
            }
//            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
        updateGPS();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

//        if (requestCode == PERMISSIONS_FINE_LOCATION) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            updateGPS();
        } else {
            //TODO: its crash here
            Toast.makeText(this, "pleas grant Permission to the APP so it can function", Toast.LENGTH_SHORT).show();
            finish();
        }
//        }
    }

    //get permission
    //get current location
    //update UI
    private void updateGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
//                                lat = location.getLatitude();
//                                lon = location.getLongitude();
//                                accuracy = String.valueOf(location.getAccuracy());
//                                double latRound = Math.round(lat * 10000.0) / 10000.0;
//                                double lonRound = Math.round(lon * 10000.0) / 10000.0;
//                                myLocation = latRound + "," + lonRound;
                                updateUIValues(location);
                            } else {
                                startLocationsUpdates();
                            }
                        }
                    });
        } else {
//            //get permission
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
//            }
            startLocationsUpdates();
        }
    }

    //update UI Values
    private void updateUIValues(Location location) {

        lat = location.getLatitude();
        lon = location.getLongitude();
        accuracy = String.valueOf(location.getAccuracy());
        double latRound = Math.round(lat * 10000.0) / 10000.0;
        double lonRound = Math.round(lon * 10000.0) / 10000.0;
        myLocation = latRound + "," + lonRound;

        tv_lat.setText(String.valueOf(lat));
        tv_lon.setText(String.valueOf(lon));
        tv_accuracy.setText(String.valueOf(accuracy));

        if (location.hasAltitude()) {
            tv_altitude.setText(String.valueOf(location.getAltitude()));
        } else {
            tv_altitude.setText(R.string.not_Available);
        }

        if (location.hasSpeed()) {
            tv_speed.setText(String.valueOf(location.getSpeed()));
        } else {
            tv_speed.setText(R.string.not_Available);
        }

        Geocoder geocoder = new Geocoder(MainActivity.this);

        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            tv_address.setText(addresses.get(0).getAddressLine(0));
        } catch (Exception e) {
            Log.e(TAG, "catch addresses updateUIValues");
            tv_address.setText(R.string.addresses);
        }
    }

    //dialog to enable gps
    private void OnGPS() {
        final android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder
                .setMessage("Enable GPS")
                .setCancelable(false)
                .setPositiveButton("YES", (dialog, which) ->
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                .setNegativeButton("NO", (dialog, which) ->
                        dialog.cancel());

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    // someone scanned QR code
    public void sendAlertQR(String alertType) throws UnsupportedEncodingException {
        String url;
        if (senderId != null) {
            if (accuracy == null || myLocation == null) {
                updateGPS();
            } else {
                url = "https://helptech29.000webhostapp.com/sendAlert.php?" +
                        "sender_id=" + senderId +
                        "&alert=" + alertType.trim() +
                        "&gps_location=" + java.net.URLEncoder.encode(myLocation, "UTF-8");
                new MyAsyncTaskgetNews().execute(url);

            }
        }
    }

    //***********************************************************
    public void send() throws UnsupportedEncodingException {
        String url;
        if (senderId != null) {
            if (accuracy == null || myLocation == null) {
                updateGPS();
            } else {
                url = "https://helptech29.000webhostapp.com/sendData.php?" +
                        "sender_id=" + senderId +
                        "&accuracy=" + accuracy.trim() +
                        "&gps_location=" + java.net.URLEncoder.encode(myLocation, "UTF-8");
                new MyAsyncTaskgetNews().execute(url);
            }
        }

    }

    //accelerometer
    @Override
    public void onSensorChanged(SensorEvent event) {
        xx = event.values[0];
        yy = event.values[1];
        zz = event.values[2];
        xData.add(event.values[0]);
        yData.add(event.values[1]);
        zData.add(event.values[2]);

        float t = (float) (Math.pow(xx, 2) + Math.pow(yy, 2) + Math.pow(zz, 2));
        summ = (float) Math.sqrt(t);

        timeNow = System.currentTimeMillis();

        falll = Integer.parseInt(fall.getText().toString());
        jumpp = Integer.parseInt(jump.getText().toString());

        if (summ >= 65) {
            jumpp = +1;
            jump.setText(String.valueOf(jumpp));
        } else if (summ > 35 && timeNow > timePrv + 20000) {
            Toast.makeText(this, "you Fallen \nsumm is=> " + summ, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "\ntime now: " + timeNow + "\ntime prev: " + timePrv + "\nsumm: " + summ);
            falll = +1;
            fall.setText(String.valueOf(falll));
            alertTypes = "patient_has_fallen";
            try {
                sendAlertQR(alertTypes);
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "catch sendAlertQR onSensorChanged");
                e.printStackTrace();
            }
            acceleration.add(summ);
            timePrv = timeNow;
        }

        x.setText(String.valueOf(xx));
        y.setText(String.valueOf(yy));
        z.setText(String.valueOf(zz));
        sum.setText(String.valueOf(summ));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    // get news from server
    public class MyAsyncTaskgetNews extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            //before works
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String NewsData;
                //define the url we have to connect with
                URL url = new URL(params[0]);
                //make connect with url and send request
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                //waiting for 7000ms for response
                urlConnection.setConnectTimeout(7000);//set timeout to 5 seconds

                try {
                    //getting the response data
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    //convert the stream to string
                    NewsData = ConvertInputToStringNoChange(in);
                    //send to display data
                    publishProgress(NewsData);
                } finally {
                    //end connection
                    urlConnection.disconnect();
                }
            } catch (Exception ignored) {
            }
            return null;
        }

        protected void onProgressUpdate(String... progress) {
            try {
                //display response data
                JSONObject json = new JSONObject(progress[0]);
                String message = json.getString("msg");

                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            } catch (Exception ignored) {
            }
        }

        protected void onPostExecute(String result2) {
        }
    }


}