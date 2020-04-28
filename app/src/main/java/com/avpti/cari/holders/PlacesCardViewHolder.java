package com.avpti.cari.holders;

import android.app.Activity;
import android.content.Intent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.avpti.cari.DeleteRoom;
import com.avpti.cari.R;
import com.avpti.cari.UpdateRoomActivity;
import com.avpti.cari.adapters.PlacesCardRecyclerViewAdapter;
import com.avpti.cari.classes.Room;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

public class PlacesCardViewHolder extends RecyclerView.ViewHolder {

    public TextView textRoomId;
    public TextView textRoomName,textRoomNo;
    public ImageView imageRoomType;
    public ImageButton btnMore;

    public PlacesCardViewHolder(@NonNull View itemView) {
        super(itemView);
        textRoomId = itemView.findViewById(R.id.text_card_room_id);
        textRoomName = itemView.findViewById(R.id.text_card_room_name);
        textRoomNo = itemView.findViewById(R.id.text_card_room_no);
        imageRoomType = itemView.findViewById(R.id.image_room_type);
        btnMore = itemView.findViewById(R.id.cari_places_card_more);
    }

    public void bind(final Room room, final PlacesCardRecyclerViewAdapter.OnItemClickListener listener) {
        if (room.getRoom_type().equals("kitchen")) {
            imageRoomType.setImageResource(R.drawable.cari_places_kitchen);
        } else if (room.getRoom_type().equals("bed")) {
            imageRoomType.setImageResource(R.drawable.cari_places_bed);
        } else if (room.getRoom_type().equals("living")) {
            imageRoomType.setImageResource(R.drawable.ic_living_room);
        }

        textRoomId.setText(String.valueOf(room.getRoom_id()));
        textRoomName.setText(room.getRoom_name());
        textRoomNo.setText(room.getRoom_no());
        btnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(itemView.getContext(), btnMore);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.cari_places_card_options_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.option_edit:
                                Intent intent = new Intent(itemView.getContext(), UpdateRoomActivity.class);
                                intent.putExtra("room", room);
                                itemView.getContext().startActivity(intent);
                                break;
                            case R.id.option_delete:
                                DeleteRoom deleteRoom = new DeleteRoom(itemView.getContext(),room.getRoom_id());
                                deleteRoom.delete();
                                break;
                        }

                        return true;
                    }
                });

                popup.show();
            }
        });

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClick(room);
            }
        });

    }
}
