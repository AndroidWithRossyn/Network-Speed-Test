package com.banrossyn.netspeed.internetspeedmeter;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.banrossyn.netspeed.internetspeedmeter.Notifaction.Data;
import com.banrossyn.netspeed.internetspeedmeter.fragment.OptionFragment;
import com.banrossyn.netspeed.internetspeedmeter.services.SpeedMeter;

public class NotificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifaction);
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



        getFragmentManager().beginTransaction()
                .replace(R.id.container, new OptionFragment())
                .commit();

    }

    @Override
    protected void onDestroy() {
        Data.flag = false;
        super.onDestroy();
    }
    @Override
    protected void onResume() {
        Data.flag = true;
        super.onResume();
    }
    @Override
    protected void onStop() {
        Data.flag = false;
        super.onStop();
    }

}
