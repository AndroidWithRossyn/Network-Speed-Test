package com.yeshbinc.speedtest.netspeed.speedmeter.utils;

import android.app.Application;
import android.content.Intent;
import android.os.Build;

import com.google.firebase.FirebaseApp;
import com.yeshbinc.speedtest.netspeed.speedmeter.services.MyService;
import com.yeshbinc.speedtest.netspeed.speedmeter.services.DataService;
import com.yeshbinc.speedtest.netspeed.speedmeter.Notifaction.Data;
import com.yeshbinc.speedtest.netspeed.speedmeter.services.SpeedMeter;
import com.google.android.gms.ads.MobileAds;


public class MyApplication extends Application {

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


        FirebaseApp.initializeApp(this);

        MobileAds.initialize(this, initializationStatus -> {
        });
    }
}
