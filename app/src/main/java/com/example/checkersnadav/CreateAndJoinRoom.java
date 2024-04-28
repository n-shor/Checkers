package com.example.checkersnadav;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
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
 */
public class CreateAndJoinRoom extends AppCompatActivity {

    private ListView roomListView;
    private Button createRoomButton;
    private Button joinRoomButton;
    private RoomAdapter roomAdapter;
    private List<Room> roomList = new ArrayList<>();

    /**
     * Initializes the activity, setting up the layout and the adapters for displaying rooms.
     * Also sets listeners for the room creation and joining buttons.
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

        roomAdapter = new RoomAdapter(this, roomList);
        roomListView.setAdapter(roomAdapter);

        createRoomButton.setOnClickListener(v -> createRoom());
        joinRoomButton.setOnClickListener(v -> joinRoom(roomListView.getCheckedItemPosition()));
        updateRoomList();
    }

    /**
     * Creates a new game room and adds it to Firebase.
     * The new room is associated with the current user as the first player.
     */
    private void createRoom() {
        DatabaseReference roomsRef = FirebaseDatabase.getInstance().getReference("rooms");
        String roomId = roomsRef.push().getKey();
        String currentUserEmail = getCurrentUserEmail();

        Room newRoom = new Room(roomId, currentUserEmail);
        roomsRef.child(roomId).setValue(newRoom)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(CreateAndJoinRoom.this, "Room created successfully!", Toast.LENGTH_SHORT).show();
                    updateRoomList();  // Refresh the room list to include the new room
                })
                .addOnFailureListener(e -> Toast.makeText(CreateAndJoinRoom.this, "Failed to create room", Toast.LENGTH_SHORT).show());
    }

    /**
     * Fetches the current user's email.
     * This method is a placeholder and should be implemented to return the actual user's email.
     * @return The current user's email address.
     */
    private String getCurrentUserEmail() {
        return "example@example.com"; // Placeholder
    }

    /**
     * Updates the list of rooms displayed to the user.
     * This method listens for changes in the Firebase database and updates the UI accordingly.
     */
    private void updateRoomList() {
        DatabaseReference roomsRef = FirebaseDatabase.getInstance().getReference("rooms");
        roomsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                roomList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Room room = snapshot.getValue(Room.class);
                    roomList.add(room);
                }
                roomAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(CreateAndJoinRoom.this, "Failed to load rooms.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Handles joining a selected room from the list.
     * Validates the room's availability and informs the user if joining is not possible.
     * @param position The index of the selected room in the list.
     */
    private void joinRoom(int position) {
        if (position == ListView.INVALID_POSITION) {
            Toast.makeText(this, "Please select a room first", Toast.LENGTH_SHORT).show();
            return;
        }

        Room room = roomList.get(position);
        if (room.canJoin()) {
            // Join the room
            // Add current user as player2 or handle accordingly
        } else {
            Toast.makeText(this, "Room is full or the game is already ongoing", Toast.LENGTH_SHORT).show();
        }
    }

}
