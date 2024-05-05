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

/**
 * Adapter for displaying room information in a ListView.
 * Each room displays its name, the username of the room owner, and updates the display based on the room's status.
 */
public class RoomAdapter extends ArrayAdapter<Room>
{
    /**
     * Constructor for the RoomAdapter.
     *
     * @param context The current context.
     * @param rooms   The list of rooms to be displayed.
     */
    public RoomAdapter(Context context, List<Room> rooms)
    {
        super(context, 0, rooms);
    }

    /**
     * Provides a view for an AdapterView (ListView, GridView, etc.).
     *
     * @param position    The position in the list of data that should be displayed in the list item view.
     * @param convertView The old view to reuse, if possible. If it is not possible to reuse, a new view will be created.
     * @param parent      The parent that this view will eventually be attached to.
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        Room room = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null)
        {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        TextView tvRoom = convertView.findViewById(android.R.id.text1);

        if (room != null)
        {
            // Reference to the Firebase database to retrieve the room owner's username
            DatabaseReference roomOwnerRef = FirebaseDatabase.getInstance().getReference("users").child(room.getRoomOwnerId());
            roomOwnerRef.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    // Setting the text of the TextView to include the room name and owner's username
                    tvRoom.setText("Room Name: " + room.getRoomName() + "\nRoom Owner Username: " + dataSnapshot.child("username").getValue(String.class));
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {
                    // Logging the error to Android's logging system
                    Log.e("Database Error while trying to retrieve room owner username", databaseError.getMessage());
                }
            });

            // Setting a shadow layer on the text for better readability
            tvRoom.setShadowLayer(6, -2, -2, Color.BLACK);

            // Changing text color based on the status of the room
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
                tvRoom.setTextColor(Color.WHITE); // Available room
            }
        }

        return convertView;
    }
}
