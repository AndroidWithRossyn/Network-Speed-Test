package com.banrossyn.netspeed.internetspeedmeter.services;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;
import android.util.Log;

import com.banrossyn.netspeed.internetspeedmeter.utils.NetworkUtil;
import com.banrossyn.netspeed.internetspeedmeter.utils.RetrieveData;
import com.banrossyn.netspeed.internetspeedmeter.utils.StoredData;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class DataService extends Service {
    public static final String MONTH_DATA = "monthdata";
    public static final String TODAY_DATA = "todaydata";
    public static boolean notification_status = true;
    public static boolean service_status = false;
    Thread dataThread;


    public void onCreate() {
        super.onCreate();
    }

    @SuppressLint("WrongConstant")
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences sp_day = getSharedPreferences(TODAY_DATA, 0);
        if (!sp_day.contains("today_date")) {
            Editor editor_day = sp_day.edit();
            editor_day.putString("today_date", new SimpleDateFormat("MMM dd, yyyy").format(Calendar.getInstance().getTime()));
            editor_day.apply();
        }
        if (!service_status) {
            service_status = true;
            this.dataThread = new Thread(new MyThreadClass(startId));
            this.dataThread.setName("showNotification");
            this.dataThread.start();
            if (!StoredData.isSetData) {
                StoredData.setZero();
            }
        }
        return 1;
    }

    public void onDestroy() {
        super.onDestroy();
        service_status = false;
    }

    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void getData() {
        String network_status = NetworkUtil.getConnectivityStatusString(getApplicationContext());
        List<Long> allData = RetrieveData.findData();
        Long mDownload = allData.get(0);
        Long mUpload = allData.get(1);
        long receiveData = mDownload + mUpload;
        storedData(mDownload, mUpload);

        long wifiData = 0;
        long mobileData = 0;
        long totalData;
        if (network_status.equals("wifi_enabled")) {
            totalData = receiveData;
            wifiData = receiveData;
        } else if (network_status.equals("mobile_enabled")) {
            totalData = receiveData;
            mobileData = receiveData;
        }
        Calendar ca = Calendar.getInstance();
        String tDate = new SimpleDateFormat("MMM dd, yyyy").format(ca.getTime());
        SharedPreferences sp_day = getSharedPreferences(TODAY_DATA, 0);
        String saved_date = sp_day.getString("today_date", "empty");
        if (saved_date.equals(tDate)) {
            long saved_mobileData = sp_day.getLong("MOBILE_DATA", 0);
            long saved_wifiData = sp_day.getLong("WIFI_DATA", 0);
            Editor day_editor = sp_day.edit();
            day_editor.putLong("MOBILE_DATA", mobileData + saved_mobileData);
            day_editor.putLong("WIFI_DATA", wifiData + saved_wifiData);
            day_editor.apply();
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("WIFI_DATA", sp_day.getLong("WIFI_DATA", 0));
            jsonObject.put("MOBILE_DATA", sp_day.getLong("MOBILE_DATA", 0));
            Editor month_editor = getSharedPreferences(MONTH_DATA, 0).edit();
            month_editor.putString(saved_date, jsonObject.toString());
            month_editor.apply();
            Editor day_editor = getSharedPreferences(TODAY_DATA, 0).edit();
            day_editor.clear();
            day_editor.putString("today_date", tDate);
            day_editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void storedData(Long mDownload, Long mUpload) {
        StoredData.downloadSpeed = mDownload;
        StoredData.uploadSpeed = mUpload;
        if (StoredData.isSetData) {
            StoredData.downloadList.remove(0);
            StoredData.uploadList.remove(0);
            StoredData.downloadList.add(mDownload);
            StoredData.uploadList.add(mUpload);
        }
        StringBuilder append = new StringBuilder().append("test ");
        Log.e("storeddata", append.append(String.valueOf(StoredData.downloadList.size())).toString());
    }

    private final class MyThreadClass implements Runnable {
        int service_id;

        MyThreadClass(int service_id) {
            this.service_id = service_id;
        }

        public void run() {
            int i = 0;
            synchronized (this) {
                while (DataService.this.dataThread.getName().equals("showNotification")) {
                    DataService.this.getData();
                    try {
                        wait(1000);
                        i++;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}

