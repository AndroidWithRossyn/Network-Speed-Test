package com.banrossyn.netspeed.internetspeedmeter.Notifaction;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.banrossyn.netspeed.internetspeedmeter.services.SpeedMeter;

public class ServiceStarter extends BroadcastReceiver {
    boolean startup = true;

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        startup = prefs.getBoolean("startupSetting", true);
        if(startup){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(new Intent(context, SpeedMeter.class));
            } else {
                context.startService(new Intent(context, SpeedMeter.class));
            }
        }

    }

}