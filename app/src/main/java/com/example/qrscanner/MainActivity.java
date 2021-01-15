package com.example.qrscanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PermissionGroupInfo;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.zxing.Result;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private CodeScanner mCodeScanner;

    LocationTrack locationTrack;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Activity activity = this;

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(this, new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String newToken = instanceIdResult.getToken();
                Log.e("TAG", newToken);
            }
        });

        FirebaseMessaging.getInstance().subscribeToTopic("QRScanner")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = "subscribed";
                        if (!task.isSuccessful()) {
                            msg = "subscribe_failed";
                        }
                        Log.d("TAG", msg);
                        Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });


        final CodeScannerView scannerView = findViewById(R.id.scanner_view);
        final EditText tv_text = (EditText) findViewById(R.id.tv_text);
        final RelativeLayout layout_View = (RelativeLayout) findViewById(R.id.layout_View);
        final Button btn_Back = (Button) findViewById(R.id.btn_Back);
        final Button btn_QRScanner = (Button) findViewById(R.id.btn_QRScanner);


        final Button btn_Location = (Button) findViewById(R.id.btn_Location);
        final LinearLayout ll_LatLong = (LinearLayout) findViewById(R.id.ll_LatLong);
        final EditText tv_Lat = (EditText) findViewById(R.id.tv_Lat);
        final EditText tv_Long = (EditText) findViewById(R.id.tv_Long);
        ll_LatLong.setVisibility(View.GONE);
        tv_text.setVisibility(View.GONE);
        btn_Location.setVisibility(View.GONE);
        btn_Back.setVisibility(View.GONE);
        scannerView.setVisibility(View.GONE);
        btn_QRScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_QRScanner.setVisibility(View.GONE);
                scannerView.setVisibility(View.VISIBLE);
            }
        });

        mCodeScanner = new CodeScanner(this, scannerView);

        btn_Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                finish();
                startActivity(intent);

                /*layout_View.setVisibility(View.GONE);
                scannerView.setVisibility(View.VISIBLE);
                btn_Location.setVisibility(View.GONE);
                tv_Lat.setText(null);
                tv_Long.setText(null);*/
            }
        });

        /*btn_Location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPermissionGranted()) {
                        ll_LatLong.setVisibility(View.VISIBLE);
                        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                        @SuppressLint("MissingPermission") Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        double longitude = location.getLongitude();
                        double latitude = location.getLatitude();
                        tv_Lat.setText(""+latitude);
                        tv_Long.setText(""+longitude);
                }
            }
        });*/

        btn_Location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPermissionGranted()) {
                    locationTrack = new LocationTrack(MainActivity.this);
                    if (locationTrack.canGetLocation()) {
                        double longitude = locationTrack.getLongitude();
                        double latitude = locationTrack.getLatitude();
                        if (longitude == 0.0 && latitude == 0.0){
                            LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
                            boolean enabled = service
                                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

                            // check if enabled and if not send user to the GSP settings
                            // Better solution would be to display a dialog and suggesting to
                            // go to the settings
                            if (!enabled) {
                                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                                alertDialog.setTitle("GPS is not Enabled!");
                                alertDialog.setMessage("Do you want to turn on GPS?");
                                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                        startActivity(intent);
                                    }
                                });
                                alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                                alertDialog.show();

                            } else
                            Toast.makeText(getApplicationContext(), "Accessing your exact Location.\n Wait a moment", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            ll_LatLong.setVisibility(View.VISIBLE);
                            tv_Lat.setText(""+latitude);
                            tv_Long.setText(""+longitude);
                            btn_Location.setClickable(false);
                            btn_Location.setText("Your device location is");
                            Toast.makeText(getApplicationContext(), "Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude), Toast.LENGTH_SHORT).show();

                        }
                    } else {
                        locationTrack.showSettingsAlert();
                    }
                }
            }
        });


        mCodeScanner.setDecodeCallback(new DecodeCallback() {
            @Override
            public void onDecoded(@NonNull final Result result) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        scannerView.setVisibility(View.GONE);
                        btn_Back.setVisibility(View.VISIBLE);
                        layout_View.setVisibility(View.VISIBLE);
                        btn_Location.setVisibility(View.VISIBLE);
                        tv_text.setVisibility(View.VISIBLE);
                        Toast.makeText(getApplicationContext(), result.getText(), Toast.LENGTH_SHORT).show();
                        tv_text.setText(result.getText());
                    }
                });
            }
        });
        scannerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCodeScanner.startPreview();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCodeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        mCodeScanner.releaseResources();
        super.onPause();
    }

    public boolean isPermissionGranted() {
            if (Build.VERSION.SDK_INT >= 23) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED ) {
                    Log.v("TAG","Permission is granted");
                    return true;
                } else {
                    Log.v("TAG","Permission is revoked");
                    ActivityCompat.requestPermissions(this ,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
                    return false;
                }
            }
            else { //permission is automatically granted on sdk<23 upon installation
                Log.v("TAG","Permission is granted");
                return true;
            }
    }




}