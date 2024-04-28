package com.example.checkersnadav;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity to create and join rooms for the game.
 * Users can create a new room or join an existing room through the UI provided by this activity.
 * This activity also allows users to refresh the list of available rooms.
 */
public class CreateAndJoinRoom extends AppCompatActivity {

    private ListView roomListView;
    private Button createRoomButton, joinRoomButton, refreshRoomsButton;
    private EditText roomNameEditText;
    private RoomAdapter roomAdapter;
    private List<Room> roomList = new ArrayList<>();
    private String currentUserEmail;

    /**
     * Called when the activity is starting. This is where most initialization should go: calling setContentView(int)
     * to inflate the activity's UI, using findViewById to programmatically interact with widgets in the UI,
     * registering listeners, etc.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     *                           Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_and_join_room);

        roomListView = findViewById(R.id.roomListView);
        createRoomButton = findViewById(R.id.createRoomButton);
        joinRoomButton = findViewById(R.id.joinRoomButton);
        refreshRoomsButton = findViewById(R.id.refreshRoomsButton);
        roomNameEditText = findViewById(R.id.roomNameEditText);

        Intent intent = getIntent();
        currentUserEmail = intent.getStringExtra("userEmail");

        roomAdapter = new RoomAdapter(this, roomList);
        roomListView.setAdapter(roomAdapter);

        createRoomButton.setOnClickListener(v -> createRoom());
        joinRoomButton.setOnClickListener(v -> joinRoom(roomListView.getCheckedItemPosition()));
        refreshRoomsButton.setOnClickListener(v -> updateRoomList());

        // Show the rooms in their initial state when entering the room
        updateRoomList();
    }

    /**
     * Creates a new game room and adds it to the Firebase database.
     * The new room is associated with the current user as the first player.
     */
    private void createRoom() {
        String roomName = roomNameEditText.getText().toString().trim();
        if (roomName.isEmpty()) {
            Toast.makeText(this, "Please enter a room name", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference roomsRef = FirebaseDatabase.getInstance().getReference("rooms");
        String roomId = roomsRef.push().getKey();
        Room newRoom = new Room(roomId, currentUserEmail, roomName);
        roomsRef.child(roomId).setValue(newRoom)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CreateAndJoinRoom.this, "Room created successfully!", Toast.LENGTH_SHORT).show();
                    updateRoomList();  // Refresh the room list to include the new room
                })
                .addOnFailureListener(e -> Toast.makeText(CreateAndJoinRoom.this, "Failed to create room", Toast.LENGTH_SHORT).show());

        Intent intent = new Intent(CreateAndJoinRoom.this, RoomActivity.class);
        intent.putExtra("player1Email", currentUserEmail);
        intent.putExtra("roomId", roomId);
        startActivity(intent);
    }


    /**
     * Updates the list of rooms displayed to the user by querying the Firebase database.
     * This method listens for changes in the database and updates the UI accordingly.
     */
    private void updateRoomList()
    {
        DatabaseReference roomsRef = FirebaseDatabase.getInstance().getReference("rooms");
        roomsRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                roomList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    Room room = snapshot.getValue(Room.class);
                    roomList.add(room);
                }
                roomAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Toast.makeText(CreateAndJoinRoom.this, "Failed to load rooms.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Handles the action of a user attempting to join a selected room from the list.
     * It checks if the room is available to join and provides feedback accordingly.
     *
     * @param position The index of the selected room in the ListView.
     */
    private void joinRoom(int position)
    {
        if (position == ListView.INVALID_POSITION)
        {
            Toast.makeText(this, "Please select a room first", Toast.LENGTH_SHORT).show();
            return;
        }

        Room room = roomList.get(position);
        if (room.canJoin())
        {
            Intent intent = new Intent(CreateAndJoinRoom.this, RoomActivity.class);  // Add current user as player2 or handle accordingly
            intent.putExtra("player2Email", currentUserEmail);
            intent.putExtra("roomId", room.getRoomId());
            startActivity(intent);
        }
        else
        {
            Toast.makeText(this, "Room is full or the game is already ongoing", Toast.LENGTH_SHORT).show();
        }
    }
}
