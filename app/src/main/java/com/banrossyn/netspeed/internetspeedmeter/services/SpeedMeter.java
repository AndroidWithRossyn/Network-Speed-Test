package com.banrossyn.netspeed.internetspeedmeter.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;

import androidx.core.app.NotificationCompat;

import android.telephony.TelephonyManager;
import android.util.Log;

import com.banrossyn.netspeed.internetspeedmeter.Notifaction.Data;
import com.banrossyn.netspeed.internetspeedmeter.Notifaction.SensorRestarterBroadcastReceiver;
import com.banrossyn.netspeed.internetspeedmeter.NotificationActivity;
import com.banrossyn.netspeed.internetspeedmeter.R;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class SpeedMeter extends Service {
    boolean isFirst = true;
    long exRX, exTX;
    long nowRX, nowTX;
    double rxBPS, txBPS;
    private boolean firstTime = true;
    private NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
            this);

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public void onDestroy() {
        Data.sflag = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        } else {
            stopSelf();
        }
        Log.i("Internet Speed Meter","Service Force Restart");
        //todo it should be back
        Intent broadcastIntent = new Intent(getApplicationContext(), SensorRestarterBroadcastReceiver.class);
        sendBroadcast(broadcastIntent);
        super.onDestroy();
    }

    @Override
    public void onCreate() {

        Data.sflag = true;
        Data.dailyDataUsage = new Date();
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
        }
        getTrans();
        doTest();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        return Service.START_STICKY;
    }

    public void doTest() {
        TimerTask task = new TimerTask() {

            @Override
            public void run() {

                if (autoHide()) {
                    String s, d, u;
                    double rxDiff = 0, txDiff = 0;
                    if (exRX == 0 || exTX == 0) {
                        exTX = TrafficStats.getTotalTxBytes();
                        exRX = TrafficStats.getTotalRxBytes();
                    }
                    nowTX = TrafficStats.getTotalTxBytes();
                    nowRX = TrafficStats.getTotalRxBytes();
                    rxDiff = nowRX - exRX;
                    txDiff = nowTX - exTX;

                    rxBPS = (rxDiff / (1000 / 1000));
                    txBPS = (txDiff / (1000 / 1000));


                    exRX = nowRX;
                    exTX = nowTX;

                    s = calculateData(txBPS + rxBPS);
                    d = calculateData(rxBPS);
                    u = calculateData(txBPS);
                    Data.setData(s, d, u, txBPS + rxBPS, rxBPS, txBPS);

                    if (Data.flag == false) {
                        showNotification(
                                getResources().getString(R.string.speed) + ": "
                                        + s,
                                getResources().getString(R.string.download)
                                        + ": "
                                        + d
                                        + "  "
                                        + getResources().getString(
                                        R.string.upload) + ": " + u);

                    } else {
                        disappearNotification();
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        mNotificationManager.deleteNotificationChannel(channelId);
                    }else {
                        disappearNotification();
                    }

                }
            }

        };
        Timer timer = new Timer();
        timer.schedule(task, 0, 999);

    }




    private void disappearNotification() {
        stopForeground(false);
        mNotificationManager.cancel(3128);




    }

    private String setTrance(double a, double b) {
        double c = (a - b);

        return calculateData2((int) c);

    }

    private String calculateData2(int a) {
        String res;

        if (a / 1024 >= 1) {
            if ((a / 1000) / 1048576 >= 1) {
                res = Math.abs((int) (a / 1024) / 1048576) + " GB";
            } else {
                if ((a / 1024) / 1024 >= 1) {
                    res = Math.abs((int) (a / 1024) / 1024) + " MB";
                } else {
                    res = Math.abs((int) a / 1024) + " KB";
                }
            }

        } else {
            res = Math.abs((int) a) + getResources().getString(R.string.download);
        }
        return res;

    }

    private NotificationManager mNotificationManager;
    private NotificationChannel mChannel;
    private String channelId = "channel-01";
    private String channelName = "Internet Speed Meter";

    private void showNotification(String head, String sub) {
        try {

            if (hide()) {

                dailyUsageControler();
                if (showDailyDataUsage()) {
                    if (isWifiConnected()) {
                        sub = getResources().getString(R.string.wifi) + ": " + setTrance(
                                (TrafficStats.getTotalTxBytes() + TrafficStats.getTotalRxBytes())
                                        - (TrafficStats.getMobileTxBytes() + TrafficStats.getMobileRxBytes()), Data.wifirec + Data.wifisend);
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                            sub += "  " + getWifiName();
                        }
                    } else {
                        sub = getResources().getString(R.string.mobile) + ": " + setTrance(TrafficStats.getMobileTxBytes() + TrafficStats.getMobileRxBytes(), Data.mobilerec + Data.mobilesend);
                        sub += "  " + carrierName();
                    }
                }


                int importance = NotificationManager.IMPORTANCE_LOW;
                if (mNotificationManager == null) {
                    mNotificationManager = (NotificationManager) getSystemService(getApplicationContext().NOTIFICATION_SERVICE);
                }
                if (firstTime) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        mChannel = mNotificationManager.getNotificationChannel(channelId);
                        if (mChannel == null) {
                            mChannel = new NotificationChannel(
                                    channelId, channelName, importance);
                            mNotificationManager.createNotificationChannel(mChannel);
                        }
                    }
                    mBuilder.setContentTitle(head)
                            .setContentText(sub)
                            .setSmallIcon(
                                    getResources().getIdentifier(
                                            calculateIcon(txBPS + rxBPS), "drawable",
                                            SpeedMeter.this.getPackageName()))
                            .setShowWhen(false).setAutoCancel(false).setOngoing(true);
                    if (showExteraInfo()) {
                        mBuilder.setContentText(sub);
                    } else {
                        mBuilder.setContentText("");
                    }
                    if(lockScreen()){

                        mBuilder.setVisibility(NotificationCompat.VISIBILITY_SECRET);

                    }else {
                        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
                    }
                    Intent myIntent = new Intent(getBaseContext(), NotificationActivity.class);

                    PendingIntent intent2 = PendingIntent.getActivity(getBaseContext(),
                            3128, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                    mBuilder.setContentIntent(intent2);
                    mBuilder.build();
                    firstTime = false;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel mChannel = new NotificationChannel(
                            channelId, channelName, importance);
                    mNotificationManager.createNotificationChannel(mChannel);
                }
                mBuilder.setContentTitle(head)

                        .setShowWhen(false)
                        .setChannelId(channelId)
                        .setAutoCancel(true)
                        .setSmallIcon(
                                getResources().getIdentifier(
                                        calculateIcon(txBPS + rxBPS), "drawable",
                                        SpeedMeter.this.getPackageName()));
                if (showExteraInfo()) {
                    mBuilder.setContentText(sub);
                } else {
                    mBuilder.setContentText("");
                }

                startForeground(3128, mBuilder.build());
            //    mNotificationManager.notify(3128 /* Request Code */, mBuilder.build());

            }
        } catch (Exception e) {
            firstTime =true;
        }
    }


    private String calculateData(double a) {
        String res;

        if (a / 1000 >= 1) {
            if ((a / 1000) / 1000000 >= 1) {
                res = Math.abs((int) (a / 1000) / 1000000) + " GB/s";
            } else {
                if ((a / 1000) / 1000 >= 1) {
                    res = Math.abs((int) (a / 1000) / 1000) + " " + getResources().getString(R.string.mb);
                } else {
                    res = Math.abs((int) a / 1000) + " " + getResources().getString(R.string.kb);
                }
            }

        } else {
            res = Math.abs((int) a) + " " + getResources().getString(R.string.b);
        }
        return res;

    }

    private String calculateIcon(double a) {
        try {
            String res = null;
            if (a / 1000 >= 1) {
                if ((a / 1000) / 1000 >= 1) {
                    int alpha = (int) ((a / 1000) / 1000);
                    switch (alpha) {
                        case 1:
                            res = "mb1_" + (int) (a / 1000) / 1000;
                            break;
                        case 2:
                            res = "mb2_" + (int) (a / 1000) / 1000;
                            break;
                        case 3:
                            res = "mb3_" + (int) (a / 1000) / 1000;
                            break;
                        case 4:
                            res = "mb4_" + (int) (a / 1000) / 1000;
                            break;
                        case 5:
                            res = "mb5_" + (int) (a / 1000) / 1000;
                            break;
                        case 6:
                            res = "mb6_" + (int) (a / 1000) / 1000;
                            break;
                        case 7:
                            res = "mb7_" + (int) (a / 1000) / 1000;
                            break;
                        case 8:
                            res = "mb8_" + (int) (a / 1000) / 1000;
                            break;
                        case 9:
                            res = "mb9_" + (int) (a / 1000) / 1000;
                            break;
                        case 10:
                            res = "mb10";
                            break;
                        case 11:
                            res = "mb11";
                            break;
                        case 12:
                            res = "mb12";
                            break;
                        case 13:
                            res = "mb13";
                            break;
                        case 14:
                            res = "mb14";
                            break;
                        case 15:
                            res = "mb15";
                            break;
                        case 16:
                            res = "mb16";
                            break;
                        case 17:
                            res = "mb17";
                            break;
                        case 18:
                            res = "mb18";
                            break;
                        case 19:
                            res = "mb19";
                            break;
                        case 20:
                            res = "mb20";
                            break;
                        case 21:
                            res = "mb21";
                            break;
                        case 22:
                            res = "mb22";
                            break;
                        case 23:
                            res = "mb23";
                            break;
                        case 24:
                            res = "mb24";
                            break;
                        case 25:
                            res = "mb25";
                            break;
                        case 26:
                            res = "mb26";
                            break;
                        case 27:
                            res = "mb27";
                            break;
                        case 28:
                            res = "mb28";
                            break;
                        case 29:
                            res = "mb29";
                            break;
                        case 30:
                            res = "mb30";
                            break;
                        case 31:
                            res = "mb31";
                            break;
                        case 32:
                            res = "mb32";
                            break;
                        case 33:
                            res = "mb33";
                            break;
                        case 34:
                            res = "mb34";
                            break;
                        case 35:
                            res = "mb35";
                            break;
                        case 36:
                            res = "mb36";
                            break;
                        case 37:
                            res = "mb37";
                            break;
                        case 38:
                            res = "mb38";
                            break;
                        case 39:
                            res = "mb39";
                            break;
                        case 40:
                            res = "mb40";
                            break;
                        case 41:
                            res = "mb41";
                            break;
                        case 42:
                            res = "mb42";
                            break;
                        case 43:
                            res = "mb43";
                            break;
                        case 44:
                            res = "mb44";
                            break;
                        case 45:
                            res = "mb45";
                            break;
                        case 46:
                            res = "mb46";
                            break;
                        case 47:
                            res = "mb47";
                            break;
                        case 48:
                            res = "mb48";
                            break;
                        case 49:
                            res = "mb49";
                            break;
                        case 50:
                            res = "mb50";
                            break;
                        case 51:
                            res = "mb51";
                            break;
                        case 52:
                            res = "mb52";
                            break;
                        case 53:
                            res = "mb54";
                            break;
                        case 55:
                            res = "mb55";
                            break;
                        case 56:
                            res = "mb56";
                            break;
                        case 57:
                            res = "mb57";
                            break;
                        case 58:
                            res = "mb58";
                            break;
                        case 59:
                            res = "mb59";
                            break;
                        case 60:
                            res = "mb60";
                            break;
                        case 61:
                            res = "mb61";
                            break;
                        case 62:
                            res = "mb62";
                            break;
                        case 63:
                            res = "mb63";
                            break;
                        case 64:
                            res = "mb64";
                            break;
                        case 65:
                            res = "mb65";
                            break;
                        case 66:
                            res = "mb66";
                            break;
                        case 67:
                            res = "mb67";
                            break;
                        case 68:
                            res = "mb68";
                            break;
                        case 69:
                            res = "mb69";
                            break;
                        case 70:
                            res = "mb70";
                            break;
                        case 71:
                            res = "mb71";
                            break;
                        case 72:
                            res = "mb72";
                            break;
                        case 73:
                            res = "mb73";
                            break;
                        case 74:
                            res = "mb74";
                            break;
                        case 75:
                            res = "mb75";
                            break;
                        case 76:
                            res = "mb76";
                            break;
                        case 77:
                            res = "mb77";
                            break;
                        case 78:
                            res = "mb78";
                            break;
                        case 79:
                            res = "mb79";
                            break;
                        case 80:
                            res = "mb80";
                            break;
                        case 81:
                            res = "mb81";
                            break;
                        case 82:
                            res = "mb82";
                            break;
                        case 83:
                            res = "mb83";
                            break;
                        case 84:
                            res = "mb84";
                            break;
                        case 85:
                            res = "mb85";
                            break;
                        case 86:
                            res = "mb86";
                            break;
                        case 87:
                            res = "mb88";
                            break;
                        case 88:
                            res = "mb88";
                            break;
                        case 89:
                            res = "mb89";
                            break;
                        case 90:
                            res = "mb90";
                            break;
                        case 91:
                            res = "mb91";
                            break;
                        case 92:
                            res = "mb92";
                            break;
                        case 93:
                            res = "mb93";
                            break;
                        case 94:
                            res = "mb94";
                            break;
                        case 95:
                            res = "mb95";
                            break;
                        case 96:
                            res = "mb96";
                            break;
                        case 97:
                            res = "mb97";
                            break;
                        case 98:
                            res = "mb98";
                            break;
                        case 99:
                            res = "mb99";
                            break;
                        default:
                            res = "wkb0";
                            break;


                    }
                } else {
                    res = "wkb" + (int) a / 1000;
                }

            } else {
                res = "wkb0";
            }


            return res;
        } catch (Exception e) {
            Log.e("Service Exception", e.getMessage());
            return "wkb0";
        }


    }

    protected void dailyUsageControler() {
        Date now = new Date();
        if (Data.dailyDataUsage.getDate() < now.getDate()) {
            getTrans();
            Data.dailyDataUsage = now;
        }

    }

    private void getTrans() {
        Data.dailyTotlSend = TrafficStats.getTotalTxBytes();
        Data.dailyTotalRecive = TrafficStats.getTotalRxBytes();
        Data.totalsend = TrafficStats.getTotalTxBytes();
        Data.totalrec = TrafficStats.getTotalRxBytes();
        Data.mobilesend = TrafficStats.getMobileTxBytes();
        Data.mobilerec = TrafficStats.getMobileRxBytes();
        Data.wifirec = TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes();
        Data.wifisend = TrafficStats.getTotalTxBytes() - TrafficStats.getMobileTxBytes();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager
                .getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    protected boolean hide() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return prefs.getBoolean("notificationSetting", true);
    }

    protected boolean autoHide() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if (prefs.getBoolean("autoHideSetting", true)) {
            return isNetworkAvailable();
        } else {
            return true;
        }
    }

    protected boolean lockScreen() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return prefs.getBoolean("hideOnLockSetting", true);
    }

    protected boolean showExteraInfo() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return prefs.getBoolean("hideExtera", true);
    }



    protected boolean showDailyDataUsage() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return prefs.getBoolean("showDataUsage", false);
    }



    protected String getWifiName() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo;
        wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
            return wifiInfo.getSSID();
        }
        return null;
    }


    protected boolean isWifiConnected() {
        ConnectivityManager cm =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;


    }

    protected String carrierName() {
        TelephonyManager manager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        return manager.getNetworkOperatorName();
    }


}
