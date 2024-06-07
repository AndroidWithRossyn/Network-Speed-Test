package com.yeshbinc.speedtest.netspeed.speedmeter.broadcastreceivers;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.yeshbinc.speedtest.netspeed.speedmeter.services.DataService;

public class DataReceiver extends BroadcastReceiver {


    public void onReceive(Context context, Intent intent) {
        if (!DataService.service_status) {
            context.startService(new Intent(context, DataService.class));
        }
    }
}
