package com.banrossyn.netspeed.internetspeedmeter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.banrossyn.netspeed.internetspeedmeter.Adapter.CustomAdapter;
import com.banrossyn.netspeed.internetspeedmeter.Adapter.MyDatabaseHelper;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class NetCheakHistory extends AppCompatActivity {
    RecyclerView recyclerView;

    MyDatabaseHelper myDB;
    ArrayList<String> time, type, ping, download,upload;
    private CustomAdapter customAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cheaknethistory);

        recyclerView = findViewById(R.id.recyclerView);

        myDB = new MyDatabaseHelper(NetCheakHistory.this);

        time = new ArrayList<>();
        type = new ArrayList<>();
        ping = new ArrayList<>();
        download = new ArrayList<>();
        upload = new ArrayList<>();

        storeDataInArrays();

        customAdapter = new CustomAdapter(NetCheakHistory.this,this ,time, type, ping , download , upload);
        recyclerView.setAdapter(customAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(NetCheakHistory.this));

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            recreate();
        }
    }

    void storeDataInArrays(){
        Cursor res = myDB.readAllData();
        if(res.getCount() == 0){
        }else{
            while (res.moveToNext()){
                time.add(res.getString(1));
                type.add(res.getString(2));
                ping.add(res.getString(3));
                download.add(res.getString(4));
                upload.add(res.getString(5));
            }
        }
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
                AlertDialog.Builder builder = new AlertDialog.Builder(NetCheakHistory.this);
                builder.setMessage("All Data Will Be Clear!").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @SuppressLint("WrongConstant")
                    public void onClick(DialogInterface dialog, int id) {

                        try{
                            myDB.deleteAllData();
                        }catch(Exception e){
                            //no need
                        }finally{
                            Intent next = new Intent(NetCheakHistory.this, HomeActivity.class);
                            NetCheakHistory.this.startActivity(next);
                            NetCheakHistory.this.finish();
                        }
                    }
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                AlertDialog alert = builder.create();
                alert.setTitle("Do You Want To Reset Data?");
                alert.show();

            }



        }
        return super.onOptionsItemSelected(item);
    }

}