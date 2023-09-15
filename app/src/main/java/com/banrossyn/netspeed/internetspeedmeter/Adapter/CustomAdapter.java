package com.banrossyn.netspeed.internetspeedmeter.Adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.banrossyn.netspeed.internetspeedmeter.R;

import java.util.ArrayList;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> {

    private Context context;
    private Activity activity;
    private ArrayList time, type, ping, download,upload;

    public CustomAdapter(Activity activity, Context context, ArrayList time, ArrayList type, ArrayList ping,
                         ArrayList download, ArrayList upload){
        this.activity = activity;
        this.context = context;
        this.time = time;
        this.type = type;
        this.ping = ping;
        this.download = download;
        this.upload = upload;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.row, parent, false);
        return new MyViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        holder.time_txt.setText(String.valueOf(time.get(position)));
        holder.type_txt.setText(String.valueOf(type.get(position)));
        holder.ping_txt.setText(String.valueOf(ping.get(position)));
        holder.download_txt.setText(String.valueOf(download.get(position)));
        holder.upload_txt.setText(String.valueOf(upload.get(position)));



    }

    @Override
    public int getItemCount() {
        return time.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView time_txt, type_txt, ping_txt, download_txt,upload_txt;
        LinearLayout mainLayout;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            time_txt = itemView.findViewById(R.id.m_time);
            type_txt = itemView.findViewById(R.id.m_type);
            ping_txt = itemView.findViewById(R.id.m_ping);
            download_txt = itemView.findViewById(R.id.m_download);
            upload_txt = itemView.findViewById(R.id.m_upload);
            mainLayout = itemView.findViewById(R.id.mainLayout);
        }

    }

}
