package com.avpti.cari.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.avpti.cari.R;
import com.avpti.cari.classes.Room;
import com.avpti.cari.holders.PlacesCardViewHolder;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

public class PlacesCardRecyclerViewAdapter extends RecyclerView.Adapter<PlacesCardViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Room item);
    }

    private ArrayList<Room> placesList;
    private final OnItemClickListener listener;

    public PlacesCardRecyclerViewAdapter(ArrayList<Room> placesList,OnItemClickListener listener) {
        this.placesList = placesList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlacesCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.places_card, parent, false);
        return new PlacesCardViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull final PlacesCardViewHolder holder, int position) {
        holder.bind(placesList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return placesList.size();
    }
}