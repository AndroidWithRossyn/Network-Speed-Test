package com.banrossyn.netspeed.internetspeedmeter.Adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.banrossyn.netspeed.internetspeedmeter.R;
import com.banrossyn.netspeed.internetspeedmeter.utils.DataInfo;

import java.util.List;

public class DataAdapter extends RecyclerView.Adapter<DataAdapter.DataViewHolder> {
    private FragmentActivity activity;
    public List<DataInfo> dataList;

    class DataViewHolder extends RecyclerView.ViewHolder {
        LinearLayout linearCard;
        TextView vDate;
        TextView vMobile;
        TextView vTotal;
        TextView vWifi;

        DataViewHolder(View itemView) {
            super(itemView);
            this.vDate = itemView.findViewById(R.id.id_date);
            this.vWifi = itemView.findViewById(R.id.id_wifi);
            this.vMobile =  itemView.findViewById(R.id.mobile);
            this.vTotal = itemView.findViewById(R.id.total);
            this.linearCard =  itemView.findViewById(R.id.linearCard);
        }
    }

    public DataAdapter(FragmentActivity activity, List<DataInfo> dataList) {
        this.activity = activity;
        this.dataList = dataList;
    }

    public DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DataViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout, parent, false));
    }

    public void onBindViewHolder(DataViewHolder holder, int position) {
        DataInfo di = dataList.get(position);
        holder.vDate.setText(di.date);
        holder.vWifi.setText(di.wifi);
        holder.vMobile.setText(di.mobile);
        holder.vTotal.setText(di.total);
        if (position % 2 == 0) {
            holder.linearCard.setBackgroundColor(ContextCompat.getColor(this.activity, R.color.down));
        } else {
            holder.linearCard.setBackgroundColor(ContextCompat.getColor(this.activity, R.color.up));
        }
    }

    public void updateData(List<DataInfo> temp) {
        this.dataList = temp;
        notifyDataSetChanged();
    }

    public int getItemCount() {
        return this.dataList.size();
    }
}