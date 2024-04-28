package com.example.checkersnadav;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

public class RoomAdapter extends ArrayAdapter<Room> {

    public RoomAdapter(Context context, List<Room> rooms) {
        super(context, 0, rooms);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Room room = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        TextView tvRoom = (TextView) convertView.findViewById(android.R.id.text1);

        if (room != null) {
            tvRoom.setText("Room: " + room.getRoomId());
            if (room.isGameOngoing()) {
                tvRoom.setTextColor(Color.RED); // Ongoing game
            } else if (room.getPlayer2() != null) {
                tvRoom.setTextColor(Color.GRAY); // Full room
            } else {
                tvRoom.setTextColor(Color.BLACK); // Available room
            }
        }

        return convertView;
    }
}
