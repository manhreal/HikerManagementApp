package com.example.hikermanagementapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
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

        // Load and display image if exists
        String imagePath = observation.getImage();
        if (imagePath != null && !imagePath.isEmpty()) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                holder.imgObservation.setImageBitmap(bitmap);
                holder.imgObservation.setVisibility(View.VISIBLE);
            } else {
                holder.imgObservation.setVisibility(View.GONE);
            }
        } else {
            holder.imgObservation.setVisibility(View.GONE);
        }
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
        ImageView imgObservation;

        public ObservationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tvObservationType);
            tvTime = itemView.findViewById(R.id.tvObservationTime);
            tvComment = itemView.findViewById(R.id.tvObservationComment);
            imgObservation = itemView.findViewById(R.id.imgObservation);
        }
    }
}