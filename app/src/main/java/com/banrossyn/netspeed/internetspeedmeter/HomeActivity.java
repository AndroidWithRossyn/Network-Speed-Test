package com.banrossyn.netspeed.internetspeedmeter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.androidsx.rateme.OnRatingListener;
import com.androidsx.rateme.RateMeDialog;
import com.aries.ui.widget.menu.MenuItemEntity;
import com.aries.ui.widget.menu.UIPopupMenu;
import com.banrossyn.netspeed.internetspeedmeter.Adapter.MyDatabaseHelper;
import com.banrossyn.netspeed.internetspeedmeter.Speed.HttpUploadTest;
import com.banrossyn.netspeed.internetspeedmeter.Speed.PingTest;
import com.banrossyn.netspeed.internetspeedmeter.Speed.RossynDownloadTest;
import com.banrossyn.netspeed.internetspeedmeter.htmldialog.HtmlDialog;
import com.banrossyn.netspeed.internetspeedmeter.htmldialog.HtmlDialogListener;
import com.banrossyn.netspeed.internetspeedmeter.utils.GetSpeedTestHostsHandler;
import com.banrossyn.netspeed.internetspeedmeter.utils.NetworkUtilHome;
import com.banrossyn.netspeed.internetspeedmeter.utils.RetrieveDataHome;
import com.gun0912.tedpermission.PermissionListener;
import com.jtv7.rippleswitchlib.RippleSwitch;
import com.sdsmdg.tastytoast.TastyToast;

import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;


public class HomeActivity extends AppCompatActivity implements RippleSwitch.OnCheckedChangeListener {
    static int position = 0;
    static int lastPosition = 0;
    GetSpeedTestHostsHandler getSpeedTestHostsHandler = null;
    HashSet<String> tempBlackList;
    private TextView hostname, locationname, type_of_network;
    private TextView fhost, fping, flocation, fdownload, fupload, mhead, mdatametartype;
    private Typeface type;
    private TextView pingTextView, downloadTextView, uploadTextView;
    private Button startButton, ratingme;
    private RippleSwitch rs;
    private ImageView speedface;
    private Bitmap bitmap;
    private static final int APP_PERMISSION_REQUEST = 102;

    private DecimalFormat dec;
    private DecimalFormat decmbs;
    boolean doubleBackToExitPressedOnce = false;
    int timer = 0;
    private UIPopupMenu uiPopupMenu;
    List<MenuItemEntity> listMenus = new ArrayList<>();


    private Handler mHandler = new Handler();
    private Thread dataThread;
    private TextView textViewdownload, textViewUpload, textViewInfo;

    @Override
    public void onResume() {
        super.onResume();

        getSpeedTestHostsHandler = new GetSpeedTestHostsHandler();
        getSpeedTestHostsHandler.start();
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        SharedPreferences prefs = HomeActivity.this.getSharedPreferences("MyPrefsFile", MODE_PRIVATE);
        String Privacy = prefs.getString("Policy", "1st");
        if (Privacy.equals("1st")) {
            ShowPolicy();
        } else {

        }


        startButton = (Button) findViewById(R.id.startButton);
        ratingme = findViewById(R.id.thisapprating);


        /**Rate Button Animation*/
        ObjectAnimator fadeOut = ObjectAnimator.ofFloat(ratingme, "alpha", 1f, .3f);
        fadeOut.setDuration(1600);
        ObjectAnimator fadeIn = ObjectAnimator.ofFloat(ratingme, "alpha", .3f, 1f);
        fadeIn.setDuration(1600);
        final AnimatorSet mAnimationSet = new AnimatorSet();
        mAnimationSet.play(fadeIn).after(fadeOut);
        mAnimationSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mAnimationSet.start();
                timer++;
            }
        });
        mAnimationSet.start();


        /**speed view image*/
        speedface = findViewById(R.id.imageView);


        dec = new DecimalFormat("#.##");
        decmbs = new DecimalFormat("#.###");

        tempBlackList = new HashSet<>();
        getSpeedTestHostsHandler = new GetSpeedTestHostsHandler();
        getSpeedTestHostsHandler.start();


        /**host , location , ping , textview*/
        hostname = findViewById(R.id.host);
        locationname = findViewById(R.id.location);
        pingTextView = (TextView) findViewById(R.id.pingTextView);


        /**network type view , main head, datametar type   textview*/
        type_of_network = findViewById(R.id.type);
        mhead = findViewById(R.id.heder);
        mdatametartype = findViewById(R.id.datametartype);


        /**upload , download textView/**/
        downloadTextView = (TextView) findViewById(R.id.downloadTextView);
        uploadTextView = (TextView) findViewById(R.id.uploadTextView);


        /**set custom font id's*/
        fhost = findViewById(R.id.fhost);
        fping = findViewById(R.id.fping);
        flocation = findViewById(R.id.flocation);
        fdownload = findViewById(R.id.textView2);
        fupload = findViewById(R.id.textView3);


        /** Custom type face install */
        type = Typeface.createFromAsset(getAssets(), "fonts/Sans.ttf");


        fhost.setTypeface(type);
        fping.setTypeface(type);
        flocation.setTypeface(type);
        fdownload.setTypeface(type);
        fupload.setTypeface(type);

        type_of_network.setTypeface(type);
        mhead.setTypeface(type);
        mdatametartype.setTypeface(type);

        downloadTextView.setTypeface(type);
        uploadTextView.setTypeface(type);

        hostname.setTypeface(type);
        locationname.setTypeface(type);
        pingTextView.setTypeface(type);

        startButton.setTypeface(type);


        /**rippleSwitch*/
        rs = findViewById(R.id.rippleSwitch);
        rs.setOnCheckedChangeListener(HomeActivity.this);


        ratingme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /**open menu*/
                uiPopupMenu = new UIPopupMenu(HomeActivity.this);
                listMenus.clear();
                listMenus.add(new MenuItemEntity(R.drawable.like, "Rate Me!"));
                listMenus.add(new MenuItemEntity(R.drawable.ic_police_man, "Privacy Policy"));
                listMenus.add(new MenuItemEntity(R.drawable.ic_baseline_history, "NetCheakHistory"));
                listMenus.add(new MenuItemEntity(R.drawable.ic_outline_notifications24, "Notification Setting"));
                listMenus.add(new MenuItemEntity(R.drawable.feedbacks, "Feedback"));
                listMenus.add(new MenuItemEntity(R.drawable.more, "More Apps"));
                uiPopupMenu
                        .setAlpha(0.5f)
                        .setMargin(0, 0, 0, 0)
                        .setMenuItems(listMenus)
                        .setOnMenuItemClickListener(new UIPopupMenu.OnMenuItemClickListener() {
                            @Override
                            public void onMenuItemClick(int position) {
                                if (position == 0) {
                                    Apprating();
                                } else if (position == 1) {
                                    ShowPolicy();
                                } else if (position == 2) {
                                    Intent next = new Intent(HomeActivity.this, NetCheakHistory.class);
                                    HomeActivity.this.startActivity(next);
                                } else if (position == 3) {
                                    Intent next = new Intent(HomeActivity.this, NotificationActivity.class);
                                    HomeActivity.this.startActivity(next);
                                } else if (position == 4) {
//                                      feedback
                                    feedbacks();
                                } else if (position == 5) {
//                                        moreapps
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.moregamesurl))));
                                }
                            }
                        })
                        .setAnimationEnable(true)
                        .showAsDropDown(ratingme, -200, 0);

            }
        });


        /**get network type*/
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                type_of_network.setText("WIFI");
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                type_of_network.setText("MOBILE");
            } else {
                type_of_network.setText("OFF");
            }
        }


        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startButton.setText("Running");
                mainrun();
            }
        });

        ImageView datause = findViewById(R.id.datauses);
        datause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this, DataHistoryActivity.class));
            }
        });


        textViewdownload = findViewById(R.id.download);
        textViewUpload = findViewById(R.id.upload);
        textViewInfo = findViewById(R.id.textViewInfo);

        this.dataThread = new Thread(new MyThreadClass());
        this.dataThread.setName("showSpeed");
        this.dataThread.start();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packagename = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packagename)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packagename));
                startActivity(intent);
            }

        }
    }

    private final class MyThreadClass implements Runnable {
        public void run() {
//            int noOfLoop = 0;

            synchronized (this) {
                while (HomeActivity.this.dataThread.getName().equals("showSpeed")) {
//                    Log.d("MyTAG", String.valueOf(noOfLoop));
                    getData();
                    try {
                        wait(1000);
//                        noOfLoop++;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void getData() {
        String network_status = NetworkUtilHome.getConnectivityStatusString(getApplicationContext());
        Log.d("MyTAG", network_status);

        List<Long> allData = RetrieveDataHome.findData();
        Long mDownload = allData.get(0);
        Long mUpload = allData.get(1);

        if (network_status != "no_connection") {
            showSpeed(mDownload, mUpload);
        } else {
            Log.d("MyTAG", "NO NETWORK");
        }
    }

    public void showSpeed(long mDownload, long mUpload) {
        List<String> connStatus = NetworkUtilHome.getConnectivityInfo(getApplicationContext());

        final String network_name;

        if ((connStatus.get(0)).equals("wifi_enabled")) {
            // (connStatus(1)) is network name and (connStatus(2)) is the ssid
            network_name = "WIFI strength " + (connStatus.get(2));
        } else if ((connStatus.get(0)).equals("mobile_enabled")) {
            network_name = connStatus.get(1);
        } else {
            network_name = "";
        }

        DecimalFormat df = new DecimalFormat("#.##");
        final String downSpeed, upSpeed;


        if (mUpload < 128) {
            upSpeed = ((int) mUpload) + " B/s";
        } else if (mUpload < 1048576) {
            upSpeed = (((int) mUpload) / 1024) + " KB/s";
        } else {
            upSpeed = df.format(((double) mUpload) / 1048576.0d) + " MB/s";
        }

        if (mDownload < 128) {
            downSpeed = ((int) mDownload) + " B/s";
        } else if (mDownload < 1048576) {
            downSpeed = (((int) mDownload) / 1024) + " KB/s";
        } else {
            downSpeed = df.format(((double) mDownload) / 1048576.0d) + " MB/s";
        }

        Log.d("MyTAG", String.format("%s %s %s", downSpeed, upSpeed, network_name));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewdownload.setText(downSpeed);
                textViewUpload.setText(upSpeed);
                textViewInfo.setText(network_name);
            }
        });
    }

    public void mainrun() {
        startButton.setEnabled(false);

        if (getSpeedTestHostsHandler == null) {
            getSpeedTestHostsHandler = new GetSpeedTestHostsHandler();
            getSpeedTestHostsHandler.start();
        }

        new Thread(new Runnable() {
            RotateAnimation rotate;
            ImageView barImageView = (ImageView) findViewById(R.id.barImageView);


            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mhead.setText("Selecting best server");
                    }
                });

                //Get egcodes.speedtest hosts
                int timeCount = 600; //1min
                while (!getSpeedTestHostsHandler.isFinished()) {
                    timeCount--;
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                    if (timeCount <= 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "No Connection...", Toast.LENGTH_LONG).show();
                                startButton.setEnabled(true);
                                startButton.setText("Restart Test");
                                try {
                                    mhead.setText(R.string.app_name);
                                } catch (Exception O) {
                                }
                            }
                        });
                        getSpeedTestHostsHandler = null;
                        return;
                    }
                }

                //Find closest server
                HashMap<Integer, String> mapKey = getSpeedTestHostsHandler.getMapKey();
                HashMap<Integer, List<String>> mapValue = getSpeedTestHostsHandler.getMapValue();
                double selfLat = getSpeedTestHostsHandler.getSelfLat();
                double selfLon = getSpeedTestHostsHandler.getSelfLon();
                double tmp = 19349458;
                double dist = 0.0;
                int findServerIndex = 0;
                for (int index : mapKey.keySet()) {
                    if (tempBlackList.contains(mapValue.get(index).get(5))) {
                        continue;
                    }

                    Location source = new Location("Source");
                    source.setLatitude(selfLat);
                    source.setLongitude(selfLon);

                    List<String> ls = mapValue.get(index);
                    Location dest = new Location("Dest");
                    dest.setLatitude(Double.parseDouble(ls.get(0)));
                    dest.setLongitude(Double.parseDouble(ls.get(1)));

                    double distance = source.distanceTo(dest);
                    if (tmp > distance) {
                        tmp = distance;
                        dist = distance;
                        findServerIndex = index;
                    }
                }
                String testAddr = mapKey.get(findServerIndex).replace("http://", "https://");
                final List<String> info = mapValue.get(findServerIndex);
                final double distance = dist;

                if (info == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mhead.setText("There was a problem");
                        }
                    });
                    return;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hostname.setText(info.get(5));
                        locationname.setText(info.get(3));
                    }
                });


                //Reset value, graphics
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pingTextView.setText("0 ms");
                        if (rs.isChecked()) {
                            downloadTextView.setText("0 MB/s");
                            uploadTextView.setText("0 MB/s");
                        } else {
                            downloadTextView.setText("0 Mbps");
                            uploadTextView.setText("0 Mbps");
                        }

                    }
                });
                final List<Double> pingRateList = new ArrayList<>();
                final List<Double> downloadRateList = new ArrayList<>();
                final List<Double> uploadRateList = new ArrayList<>();
                Boolean pingTestStarted = false;
                Boolean pingTestFinished = false;
                Boolean downloadTestStarted = false;
                Boolean downloadTestFinished = false;
                Boolean uploadTestStarted = false;
                Boolean uploadTestFinished = false;

                //Init Test
                final PingTest pingTest = new PingTest(info.get(6).replace(":8080", ""), 3);
                final RossynDownloadTest downloadTest = new RossynDownloadTest(testAddr.replace(testAddr.split("/")[testAddr.split("/").length - 1], ""));
                final HttpUploadTest uploadTest = new HttpUploadTest(testAddr);


                //Tests
                while (true) {
                    if (!pingTestStarted) {
                        pingTest.start();
                        pingTestStarted = true;
                    }
                    if (pingTestFinished && !downloadTestStarted) {
                        downloadTest.start();
                        downloadTestStarted = true;
                    }
                    if (downloadTestFinished && !uploadTestStarted) {
                        uploadTest.start();
                        uploadTestStarted = true;
                    }


                    //Ping Test
                    if (pingTestFinished) {
                        //Failure
                        if (pingTest.getAvgRtt() == 0) {
                            System.out.println("Ping error...");
                        } else {
                            //Success
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    pingTextView.setText(dec.format(pingTest.getAvgRtt()) + " ms");
                                    try {
                                        mhead.setText("PING");
                                    } catch (Exception O) {
                                    }
                                }
                            });
                        }
                    } else {
                        pingRateList.add(pingTest.getInstantRtt());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pingTextView.setText(dec.format(pingTest.getInstantRtt()) + " ms");
                                try {
                                    mhead.setText("PING");
                                } catch (Exception O) {
                                }
                            }
                        });

                        //Update chart
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Creating an  XYSeries for Income
                                XYSeries pingSeries = new XYSeries("");
                                pingSeries.setTitle("");

                                int count = 0;
                                List<Double> tmpLs = new ArrayList<>(pingRateList);
                                for (Double val : tmpLs) {
                                    pingSeries.add(count++, val);
                                }

                                XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
                                dataset.addSeries(pingSeries);


                            }
                        });
                    }


                    //Download Test
                    if (pingTestFinished) {
                        if (downloadTestFinished) {
                            //Failure
                            if (downloadTest.getFinalDownloadRate() == 0) {
                                System.out.println("Download error...");
                            } else {
                                //Success
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (rs.isChecked()) {
                                            downloadTextView.setText(decmbs.format(downloadTest.getFinalDownloadRate() / 8) + " MB/s");
                                        } else {
                                            downloadTextView.setText(dec.format(downloadTest.getFinalDownloadRate()) + " Mbps");
                                        }

                                        try {
                                            mhead.setText("DOWNLOAD");
                                        } catch (Exception O) {
                                        }
                                    }
                                });
                            }
                        } else {
                            //Calc position
                            double downloadRate = downloadTest.getInstantDownloadRate();
                            downloadRateList.add(downloadRate);
                            if (rs.isChecked()) {
                                position = getPositionByRate(downloadRate / 8);
                            } else {
                                position = getPositionByRate(downloadRate);
                            }
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    rotate = new RotateAnimation(lastPosition, position, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                    rotate.setInterpolator(new LinearInterpolator());
                                    rotate.setDuration(100);
                                    barImageView.startAnimation(rotate);
                                    if (rs.isChecked()) {
                                        downloadTextView.setText(decmbs.format(downloadTest.getInstantDownloadRate() / 8) + " MB/s");
                                    } else {
                                        downloadTextView.setText(dec.format(downloadTest.getInstantDownloadRate()) + " Mbps");
                                    }
                                    try {
                                        mhead.setText("DOWNLOAD");
                                    } catch (Exception O) {
                                    }

                                }

                            });
                            lastPosition = position;

                            /**Update chart*/
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // Creating an  XYSeries for Income
                                    XYSeries downloadSeries = new XYSeries("");
                                    downloadSeries.setTitle("");
                                    List<Double> tmpLs = new ArrayList<>(downloadRateList);
                                    int count = 0;
                                    for (Double val : tmpLs) {
                                        downloadSeries.add(count++, val);
                                    }
                                    XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
                                    dataset.addSeries(downloadSeries);
                                }
                            });

                        }
                    }


                    //Upload Test
                    if (downloadTestFinished) {
                        if (uploadTestFinished) {
                            //Failure
                            if (uploadTest.getFinalUploadRate() == 0) {
                                System.out.println("Upload error...");
                            } else {
                                //Success
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (rs.isChecked()) {
                                            uploadTextView.setText(decmbs.format(uploadTest.getFinalUploadRate() / 8) + " MM/s");
                                        } else {
                                            uploadTextView.setText(dec.format(uploadTest.getFinalUploadRate()) + " Mbps");
                                        }
                                        try {
                                            mhead.setText("UPLOAD");
                                        } catch (Exception O) {
                                        }
                                    }
                                });
                            }
                        } else {
                            //Calc position
                            double uploadRate = uploadTest.getInstantUploadRate();
                            uploadRateList.add(uploadRate);
                            if (rs.isChecked()) {
                                position = getPositionByRate(uploadRate / 8);
                            } else {
                                position = getPositionByRate(uploadRate);
                            }

                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    rotate = new RotateAnimation(lastPosition, position, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                    rotate.setInterpolator(new LinearInterpolator());
                                    rotate.setDuration(100);
                                    barImageView.startAnimation(rotate);
                                    if (rs.isChecked()) {
                                        uploadTextView.setText(decmbs.format(uploadTest.getInstantUploadRate() / 8) + " MB/s");
                                    } else {
                                        uploadTextView.setText(dec.format(uploadTest.getInstantUploadRate()) + " Mbps");
                                    }

                                    try {
                                        mhead.setText("UPLOAD");
                                    } catch (Exception O) {
                                    }

                                }

                            });
                            lastPosition = position;

                            //Update chart
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // Creating an  XYSeries for Income
                                    XYSeries uploadSeries = new XYSeries("");
                                    uploadSeries.setTitle("");

                                    int count = 0;
                                    List<Double> tmpLs = new ArrayList<>(uploadRateList);
                                    for (Double val : tmpLs) {
                                        if (count == 0) {
                                            val = 0.0;
                                        }
                                        uploadSeries.add(count++, val);
                                    }

                                    XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
                                    dataset.addSeries(uploadSeries);
                                }
                            });

                        }
                    }

                    //Test bitti
                    if (pingTestFinished && downloadTestFinished && uploadTest.isFinished()) {
                        break;
                    }

                    if (pingTest.isFinished()) {
                        pingTestFinished = true;
                    }
                    if (downloadTest.isFinished()) {
                        downloadTestFinished = true;
                    }
                    if (uploadTest.isFinished()) {
                        uploadTestFinished = true;
                    }

                    if (pingTestStarted && !pingTestFinished) {
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                        }
                    } else {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                        }
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startButton.setEnabled(true);
                        startButton.setText("Restart Test");

                        try {
                            mhead.setText(R.string.app_name);
                        } catch (Exception O) {
                        }
                        sharing();


                        /**
                         * add data base
                         */
                        String currentTime = new SimpleDateFormat("HH:mm a", Locale.getDefault()).format(new Date());
                        MyDatabaseHelper myDB = new MyDatabaseHelper(HomeActivity.this);
                        myDB.addBook(
                                currentTime.trim(),
                                type_of_network.getText().toString().trim(),
                                pingTextView.getText().toString().trim(),
                                downloadTextView.getText().toString().trim(),
                                uploadTextView.getText().toString().trim());

                    }
                });


            }
        }).start();
    }


    public int getPositionByRate(double rate) {
        if (rate <= 1) {
            return (int) (rate * 30);

        } else if (rate <= 10) {
            return (int) (rate * 6) + 30;

        } else if (rate <= 30) {
            return (int) ((rate - 10) * 3) + 90;

        } else if (rate <= 50) {
            return (int) ((rate - 30) * 1.5) + 150;

        } else if (rate <= 100) {
            return (int) ((rate - 50) * 1.2) + 180;
        }

        return 0;
    }

    @Override
    public void onCheckChanged(boolean b) {

        if (rs.isChecked()) {
            mdatametartype.setText("MEGABYTE PER SECOND");
            downloadTextView.setText("0 MB/s");
            uploadTextView.setText("0 MB/s");
            speedface.setImageResource(R.drawable.megabyteps);
        } else {
            mdatametartype.setText("MEGABIT PER SECOND");
            downloadTextView.setText("0 Mbps");
            uploadTextView.setText("0 Mbps");
            speedface.setImageResource(R.drawable.megabitps);
        }

    }

    public void sharing() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(HomeActivity.this, R.style.CustomDialog);
        View mView = getLayoutInflater().inflate(R.layout.share_activity, null);

        /**linelyout install*/
        LinearLayout resultview = (LinearLayout) mView.findViewById(R.id.result);


        /**share windows settext textview install*/
        TextView rping = (TextView) mView.findViewById(R.id.rping);//
        TextView rdownload = (TextView) mView.findViewById(R.id.rdownload);//
        TextView rupload = (TextView) mView.findViewById(R.id.rupload);//
        TextView tping = (TextView) mView.findViewById(R.id.tping);//
        TextView tdownload = (TextView) mView.findViewById(R.id.tdownload);//
        TextView tupload = (TextView) mView.findViewById(R.id.tupload);//
        /**share windows settext textview install*/


//        /**closed button*/
//        Button share = (Button) mView.findViewById(R.id.closed);
        /**closed button*/

        /**share result textview*/
        TextView sheader = (TextView) mView.findViewById(R.id.sharehead);
        TextView subhead = (TextView) mView.findViewById(R.id.subhead);
        /**share result textview*/

        /**time and date textview install*/
        TextView times = (TextView) mView.findViewById(R.id.timeview);
        TextView dates = (TextView) mView.findViewById(R.id.dateview);
        /**set time and date*/
        String currentTime = new SimpleDateFormat("HH:mm:ss a", Locale.getDefault()).format(new Date());
        times.setText(currentTime);
        //////////////////////////////
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        dates.setText(currentDate);
        /**set time and date*/
        /**time and date textview install*/


        /**host and location text install*/
        TextView sharhost = (TextView) mView.findViewById(R.id.thostingname);
        TextView sharlocation = (TextView) mView.findViewById(R.id.tlocationname);
        /**host and location text install*/


        /**set custom font typeface all textview sharewindows*/
        rping.setTypeface(type);
        rdownload.setTypeface(type);
        rupload.setTypeface(type);
        tping.setTypeface(type);
        tdownload.setTypeface(type);
        tupload.setTypeface(type);
        sheader.setTypeface(type);
        subhead.setTypeface(type);
        sharhost.setTypeface(type);
        sharlocation.setTypeface(type);
        /**set custom font typeface all textview sharewindows*/


        /**set text*/
        rping.setText(pingTextView.getText().toString());
        rdownload.setText(downloadTextView.getText().toString());
        rupload.setText(uploadTextView.getText().toString());
        sharhost.setText("SERVER : " + hostname.getText().toString());
        sharlocation.setText("LOCATION : " + locationname.getText().toString());
        /**set text*/

        alert.setView(mView);
        final AlertDialog alertDialog = alert.create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_PERMISSION_REQUEST && resultCode == RESULT_OK) {
            Toast.makeText(this, "Draw over other app permission enable.", Toast.LENGTH_SHORT).show();
        } else {

        }
    }

    PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {

        }

        @Override
        public void onPermissionDenied(List<String> deniedPermissions) {
            Toast.makeText(HomeActivity.this, "These permissions are required to proceed. Please try again\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            finish();
        }
    };


    public void Apprating() {
        new RateMeDialog.Builder(getPackageName(), getString(R.string.app_name))
                .setHeaderBackgroundColor(getResources().getColor(R.color.ratedialog))
                .setBodyBackgroundColor(getResources().getColor(R.color.ratedialogbackgraun))
                .setBodyTextColor(getResources().getColor(R.color.newbg))
                .showAppIcon(R.mipmap.ic_launcher)
                .setShowShareButton(true)
                .setRateButtonBackgroundColor(getResources().getColor(R.color.ratedialog))
                .setRateButtonPressedBackgroundColor(getResources().getColor(R.color.subtext))
                .setOnRatingListener(new OnRatingListener() {
                    @Override
                    public void onRating(OnRatingListener.RatingAction action, float rating) {

                    }

                    @Override
                    public int describeContents() {

                        return 0;
                    }

                    @Override
                    public void writeToParcel(Parcel dest, int flags) {
                        // Nothing to write
                    }
                })
                .build()
                .show(getFragmentManager(), "custom-dialog");
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        TastyToast.makeText(getApplicationContext(), "Please click BACK again to exit", TastyToast.LENGTH_LONG, TastyToast.WARNING);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);

    }

    /**
     * Show Policy lestner
     */
    public void ShowPolicy() {
        new HtmlDialog.Builder(getSupportFragmentManager())
                .setListener(listener)
                .setHtmlResId(R.raw.licenses)
                .setTitle(getResources().getString(R.string.title))
                .setShowPositiveButton(true)
                .setPositiveButtonText(getResources().getString(R.string.ok))
                .build()
                .show();
    }


    private void feedbacks() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        String[] recipients = {"banrossyn@gmail.com"};
        intent.putExtra(Intent.EXTRA_EMAIL, recipients);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Network Speed Test Feedback");
        intent.putExtra(Intent.EXTRA_CC, "banrossyn@gmail.com");
        intent.setType("text/html");
        intent.setPackage("com.google.android.gm");
        startActivity(Intent.createChooser(intent, "Send mail"));
    }

    /**
     * Policy lestner
     */
    private HtmlDialogListener listener = new HtmlDialogListener() {
        @Override
        public void onNegativeButtonPressed() {
        }

        @Override
        public void onPositiveButtonPressed() {
            SharedPreferences.Editor editor = getSharedPreferences("MyPrefsFile", MODE_PRIVATE).edit();
            editor.putString("Policy", "2nd");
            editor.apply();
        }
    };


}
