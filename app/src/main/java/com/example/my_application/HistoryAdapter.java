package com.example.my_application;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<UserData> dataList;

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<UserData> dataList) {
        Log.d("HistoryAdapter", "setData called with dataList size: " + dataList.size());
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("HistoryAdapter", "onCreateViewHolder called");
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        Log.d("HistoryAdapter", "onBindViewHolder called");
        UserData userData = dataList.get(position);
        holder.bind(userData);
    }

    @Override
    public int getItemCount() {
        return dataList != null ? dataList.size() : 0;
    }

    public static class HistoryViewHolder extends RecyclerView.ViewHolder {
        final private TextView timeTextView;
        final private TextView activityTextView;
        final private TextView descriptionTextView;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            activityTextView = itemView.findViewById(R.id.activityTextView);
            descriptionTextView = itemView.findViewById(R.id.descriptionTextView);
        }

        public void bind(UserData userData) {
            timeTextView.setText(userData.getTime());
            activityTextView.setText(userData.getActivity());
            descriptionTextView.setText(userData.getDescription());
        }
    }
}

