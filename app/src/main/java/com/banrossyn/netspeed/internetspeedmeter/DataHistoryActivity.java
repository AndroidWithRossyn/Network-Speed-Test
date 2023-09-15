package com.banrossyn.netspeed.internetspeedmeter;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.banrossyn.netspeed.internetspeedmeter.fragment.MonthFragment;
import com.banrossyn.netspeed.internetspeedmeter.services.DataService;

public class DataHistoryActivity extends AppCompatActivity {
    private boolean doubleBackToExitPressedOnce;
    private Handler mHandler = new Handler();

    SharedPreferences dataPref;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_history);




//      clear data history
        this.dataPref = PreferenceManager.getDefaultSharedPreferences(DataHistoryActivity.this);


        if (!DataService.service_status) {
            startService(new Intent(this, DataService.class));
        }

        Intent intentBC = new Intent();
        intentBC.setAction("com.banrossyn.internetspeedmeter.internetspeedmeter");
        sendBroadcast(intentBC);

        Fragment fragment = new MonthFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragment);
        ft.commit();
        doubleBackToExitPressedOnce = false;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menux, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.setting: {
                SharedPreferences sp_today = DataHistoryActivity.this.getSharedPreferences(DataService.TODAY_DATA, 0);
                SharedPreferences sp_month = DataHistoryActivity.this.getSharedPreferences(DataService.MONTH_DATA, 0);
                SharedPreferences.Editor editor = sp_today.edit();
                SharedPreferences.Editor edito2 = sp_month.edit();
                editor.clear();
                edito2.clear();
                editor.apply();
                edito2.apply();
            }

        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onStop() {
        super.onStop();
    }
}
