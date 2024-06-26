package com.example.checkersnadav;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
 */
public class CreateAndJoinRoomActivity extends AppCompatActivity {
    private ListView roomListView;
    private EditText roomNameEditText;
    private RoomAdapter roomAdapter;
    private final List<Room> roomList = new ArrayList<>();
    private String userId;

    /**
     * Initializes the activity with components for creating and joining rooms.
     * Sets up listeners for UI elements and initializes Firebase references.
     *
     * @param savedInstanceState Bundle containing the activity's previously saved state, if it existed.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_and_join_room);

        roomListView = findViewById(R.id.roomListView);
        Button createRoomButton = findViewById(R.id.createRoomButton);
        Button joinRoomButton = findViewById(R.id.joinRoomButton);
        roomNameEditText = findViewById(R.id.roomNameEditText);
        Button backToMenuButton = findViewById(R.id.backToMenuButton);

        // Retrieve the current user's identifier from the intent extras.
        userId = getIntent().getStringExtra("userId");

        // Setup the room list view with a custom adapter.
        roomAdapter = new RoomAdapter(this, roomList);
        roomListView.setAdapter(roomAdapter);
        roomListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE); // Enable single choice mode.

        // Setup button listeners.
        backToMenuButton.setOnClickListener(v ->
        {
            Intent intent = new Intent(CreateAndJoinRoomActivity.this, MenuActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            finish();
        });

        createRoomButton.setOnClickListener(v -> createRoom());
        joinRoomButton.setOnClickListener(v -> joinRoom(roomListView.getCheckedItemPosition()));

        // Load the list of available rooms.
        updateRoomList();
    }

    /**
     * Creates a new game room using the entered room name, adds it to Firebase, and redirects the user to the room.
     */
    private void createRoom()
    {
        String roomName = roomNameEditText.getText().toString().trim();
        if (roomName.isEmpty())
        {
            Toast.makeText(this, "Please enter a room name", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference roomsRef = FirebaseDatabase.getInstance().getReference("rooms");
        String roomId = roomsRef.push().getKey();
        Room newRoom = new Room(roomId, userId, roomName);
        roomsRef.child(roomId).setValue(newRoom)
                .addOnSuccessListener(aVoid -> Toast.makeText(CreateAndJoinRoomActivity.this, "Room created successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(CreateAndJoinRoomActivity.this, "Failed to create room", Toast.LENGTH_SHORT).show());

        // Navigate to the RoomActivity as the room creator.
        Intent intent = new Intent(CreateAndJoinRoomActivity.this, RoomActivity.class);
        intent.putExtra("player1Id", userId);
        intent.putExtra("roomId", roomId);
        startActivity(intent);
        finish();
    }

    /**
     * Updates the list of available rooms by querying the Firebase database.
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
                Toast.makeText(CreateAndJoinRoomActivity.this, "Failed to load rooms.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Handles the action of a user attempting to join a selected room from the list.
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
            Intent intent = new Intent(CreateAndJoinRoomActivity.this, RoomActivity.class);
            intent.putExtra("player2Id", userId);
            intent.putExtra("roomId", room.getRoomId());
            startActivity(intent);
            finish();
        }
        else
        {
            Toast.makeText(this, "Room is full or the game is already ongoing", Toast.LENGTH_SHORT).show();
        }
    }
}
