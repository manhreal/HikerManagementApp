package com.example.hikermanagementapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.helper.widget.Layer;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class TripAdapter extends RecyclerView.Adapter<TripAdapter.TripViewHolder> {
    private Context ct;
    private List<Trip> trip_list;

    public TripAdapter(Context ct, List<Trip> trip_list) {
        this.ct = ct;
        this.trip_list = trip_list;
    }

    @NonNull
    @Override
    public TripViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int view_type){
        View view = LayoutInflater.from(ct).inflate(R.layout.item_trip, parent, false);
        return new TripViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TripViewHolder holder, int position) {
        Trip trip = trip_list.get(position);

        holder.tv_i_trip_name.setText(trip.getTripName());
        holder.tv_i_trip_destination.setText(trip.getDestination());
        holder.tv_i_trip_date.setText(trip.getDate());
        holder.tv_i_trip_length.setText(trip.getLength() + " km");
        holder.tv_i_trip_difficulty.setText(trip.getDifficulty());
        holder.tv_i_trip_require_parking.setText(trip.isRequireParking() ? "Yes" : "No");

        // change difficulty color
        switch (trip.getDifficulty().toLowerCase()) {
            case "Easy":
                holder.tv_i_trip_difficulty.setTextColor(ct.getResources().getColor(android.R.color.holo_green_dark));
                break;
            case "Medium":
                holder.tv_i_trip_difficulty.setTextColor(ct.getResources().getColor(android.R.color.holo_orange_dark));
                break;
            case "Hard":
                holder.tv_i_trip_difficulty.setTextColor(ct.getResources().getColor(android.R.color.holo_red_dark));
                break;
        }

        // view detail
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ct, TripDetailActivity.class);
                intent.putExtra("trip_id", trip.getTripId());
                ct.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return trip_list.size();
    }

    // update list
    public void updateList(List<Trip> newList) {
        this.trip_list = newList;
        notifyDataSetChanged();
    }

    public static class TripViewHolder extends RecyclerView.ViewHolder {
        TextView tv_i_trip_name, tv_i_trip_destination, tv_i_trip_date,
                tv_i_trip_length, tv_i_trip_difficulty, tv_i_trip_require_parking;

        public TripViewHolder(@NonNull View itemView) {
            super(itemView);
            tv_i_trip_name = itemView.findViewById(R.id.tv_i_trip_name);
            tv_i_trip_destination = itemView.findViewById(R.id.tv_i_trip_destination);
            tv_i_trip_date = itemView.findViewById(R.id.tv_i_trip_date);
            tv_i_trip_length = itemView.findViewById(R.id.
                    tv_i_trip_length);
            tv_i_trip_difficulty = itemView.findViewById(R.id.tv_i_trip_difficulty);
            tv_i_trip_require_parking = itemView.findViewById(R.id.tv_i_trip_require_parking);
        }
    }
}
