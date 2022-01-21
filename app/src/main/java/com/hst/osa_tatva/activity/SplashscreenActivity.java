package com.hst.osa_tatva.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hst.osa_tatva.R;
import com.hst.osa_tatva.bean.database.SQLiteHelper;
import com.hst.osa_tatva.utils.OSAValidator;
import com.hst.osa_tatva.utils.PreferenceStorage;

public class SplashscreenActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();
    SQLiteHelper database;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//

        setContentView(R.layout.activity_splashscreen);
        database = new SQLiteHelper(getApplicationContext());
        final int getStatus = database.appInfoCheck();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Boolean b = PreferenceStorage.isFirstTimeLaunch(getApplicationContext());
                String id = PreferenceStorage.getUserId(getApplicationContext());
                if (getStatus != 0 && OSAValidator.checkNullString(id)) {
                    Intent i = new Intent(SplashscreenActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                } else {
                    Intent i = new Intent(getApplicationContext(), WelcomeActivity.class);
                    FirebaseMessaging.getInstance().getToken()
                            .addOnCompleteListener(new OnCompleteListener<String>() {
                                @Override
                                public void onComplete(@NonNull Task<String> task) {
                                    if (!task.isSuccessful()) {
                                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                                        return;
                                    }
                                    String token = task.getResult();
                                    PreferenceStorage.saveGCM(getApplicationContext(), token);
                                }
                            });
                    startActivity(i);
                    finish();
                }
            }
        }, 2000);

    }

//    public void hashFromSHA1() {
//        sh = "28:D6:E7:CF:CC:66:02:16:B1:97:3D:9A:FE:E7:8E:CB:20:1B:DE:03";
//        String[] arr = sh.split(":");
//        byte[] byteArr = new  byte[arr.length];
//
//        for (int i = 0; i< arr.length; i++) {
//            byteArr[i] = Integer.decode("0x" + arr[i]).byteValue();
//        }
//
//        Log.e("hash : ", Base64.encodeToString(byteArr, Base64.NO_WRAP));
//    }
}
