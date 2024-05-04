package com.example.checkersnadav;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class RoomAdapter extends ArrayAdapter<Room>
{

    public RoomAdapter(Context context, List<Room> rooms) {
        super(context, 0, rooms);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        Room room = getItem(position);

        if (convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        TextView tvRoom = convertView.findViewById(android.R.id.text1);

        if (room != null)
        {
            DatabaseReference roomOwnerRef = FirebaseDatabase.getInstance().getReference("users").child(room.getRoomOwnerId());
            roomOwnerRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    tvRoom.setText("Room Name: " + room.getRoomName() + "\nRoom Owner Username: " + dataSnapshot.child("username").getValue(String.class));

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.d("Database Error while trying to retrieve room owner username", databaseError.getMessage());
                }
            });

            if (room.isGameOngoing())
            {
                tvRoom.setTextColor(Color.RED); // Ongoing game
            }
            else if (room.getPlayer2Id() != null)
            {
                tvRoom.setTextColor(Color.GRAY); // Full room
            }
            else
            {
                tvRoom.setTextColor(Color.BLACK); // Available room
            }
        }

        return convertView;
    }
}
