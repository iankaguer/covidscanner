package com.aztechlabs.covidscanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.google.zxing.Result;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private CodeScanner mCodeScanner;
    private static final int PERMISSION_CAMERA_CODE = 200;
    TextView txtnom, txtprenom, txtnaiss, txtdoc, txttvaccin, txtdvaccin;
    Button itsok;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CodeScannerView scannerView = findViewById(R.id.scanner_view);
    
        txtnom = findViewById(R.id.txtnom);
        txtprenom = findViewById(R.id.txtprenom);
        txtnaiss = findViewById(R.id.txtnaiss);
        txtdoc = findViewById(R.id.txtdoc);
        txttvaccin = findViewById(R.id.txttvaccin);
        txtdvaccin = findViewById(R.id.txtdvaccin);
        itsok = findViewById(R.id.itsok);
        LottieAnimationView lottieAnimationView = (LottieAnimationView) findViewById(R.id.animation);
    
        if (checkPermission()) {
            mCodeScanner = new CodeScanner(this, scannerView);
            mCodeScanner.setDecodeCallback(new DecodeCallback() {
                @Override
                public void onDecoded(@NonNull final Result result) {
                    runOnUiThread(new Runnable() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void run() {
                            ViewGroup.LayoutParams params = scannerView.getLayoutParams();
                            params.height = 350;
                            scannerView.setLayoutParams(params);
                          //  Toast.makeText(MainActivity.this, result.getText(), Toast.LENGTH_SHORT).show();
                            OkHttpClient client = new OkHttpClient();
                            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                            StrictMode.setThreadPolicy(policy);
    
                            HttpUrl.Builder queryUrlBuilder = HttpUrl.get("https://cvdscan.dreamcomestrue.td/apicheck.php").newBuilder();
                            queryUrlBuilder.addQueryParameter("tocheck", result.getText());
    
                            Request request = new Request.Builder()
                                    .url(queryUrlBuilder.build())
                                    .build();
    
                            try {
                                Response response = client.newCall(request).execute();
                                //System.out.println(response.body().string());
                                
                                String result = response.body().string();
                                Log.e("api reponse", result);
    
                                
                                
                                if (result.contains("tvaccin")){
                                    
                                   
                                    lottieAnimationView.setAnimation("success.json");
                                    lottieAnimationView.loop(false); lottieAnimationView.playAnimation();
    
                                    JSONObject jsonObject = new JSONObject(result);
                                    txtnom.setText((String) jsonObject.get("nom"));
                                    txtprenom.setText((String) jsonObject.get("prenom"));
                                    txtnaiss.setText(jsonObject.get("lnaiss") +", " + jsonObject.get("dnaiss"));
                                    txtdoc.setText(jsonObject.get("tpiece") +", " + jsonObject.get("npiece"));
                                    txttvaccin.setText((String) jsonObject.get("tvaccin"));
                                    txtdvaccin.setText((String) jsonObject.get("dvaccin"));
                                }else{
    
                                    lottieAnimationView.setAnimation("failed.json");
                                    lottieAnimationView.loop(false); lottieAnimationView.playAnimation();
                                    
                                }
                            } catch (IOException  | JSONException e) {
                                Log.e("api error", e.getMessage());
                                //System.out.println(e);
                            }
                            itsok.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                                    scannerView.setLayoutParams(params);
                                }
                            });
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
        
        } else {
            requestPermission();
        }
        
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
    
    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            return false;
        }
        return true;
    }
    
    private void requestPermission() {
        
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                PERMISSION_CAMERA_CODE);
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CAMERA_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                    
                    // main logic
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                                requestPermission();
                        }
                    }
                }
                break;
        }
    }
    
    
    public void onBackPressed(){
        super.onBackPressed();
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
        
    }
}