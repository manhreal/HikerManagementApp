package com.example.hikermanagementapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ObservationAdapter extends RecyclerView.Adapter<ObservationAdapter.ObservationViewHolder> {

    private Context context;
    private List<Observation> observationList;

    public ObservationAdapter(Context context, List<Observation> observationList) {
        this.context = context;
        this.observationList = observationList;
    }

    @NonNull
    @Override
    public ObservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_observation, parent, false);
        return new ObservationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ObservationViewHolder holder, int position) {
        Observation observation = observationList.get(position);

        holder.tvType.setText(observation.getObservationType());
        holder.tvTime.setText(observation.getObservationTime());
        holder.tvComment.setText(observation.getNote());
    }

    @Override
    public int getItemCount() {
        return observationList.size();
    }

    public void updateList(List<Observation> newList) {
        this.observationList = newList;
        notifyDataSetChanged();
    }

    public static class ObservationViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvTime, tvComment;

        public ObservationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvObservationType);
            tvTime = itemView.findViewById(R.id.tvObservationTime);
            tvComment = itemView.findViewById(R.id.tvObservationComment);
        }
    }
}