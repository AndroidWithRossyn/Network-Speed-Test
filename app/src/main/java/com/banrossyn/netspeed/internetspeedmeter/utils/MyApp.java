package com.banrossyn.netspeed.internetspeedmeter.utils;

import android.app.Application;
import android.content.Intent;
import android.os.Build;

import com.banrossyn.netspeed.internetspeedmeter.services.MyService;
import com.banrossyn.netspeed.internetspeedmeter.services.DataService;
import com.banrossyn.netspeed.internetspeedmeter.Notifaction.Data;
import com.banrossyn.netspeed.internetspeedmeter.services.SpeedMeter;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.messaging.FirebaseMessaging;


public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        startService(new Intent(this, MyService.class));

        Intent dataintent= new Intent(this,DataService.class);
        try {
            startService(dataintent);
        }catch ( Exception e1){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                this.startForegroundService(dataintent);
            }else {
                this.startService(dataintent);
            }
        }


        if (!Data.sflag) {
            Intent intent1 = new Intent(this, SpeedMeter.class);
            try {
                startService(intent1);
            }catch ( Exception e1){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    this.startForegroundService(intent1);
                }else {
                    this.startService(intent1);
                }
            }
        }

        FirebaseMessaging.getInstance().subscribeToTopic("all");

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
    }
}
