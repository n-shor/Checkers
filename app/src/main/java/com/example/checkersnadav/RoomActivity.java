package com.example.checkersnadav;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RoomActivity extends AppCompatActivity {

    private Button btnStartGame;
    private Button btnCloseRoom;
    private Button btnLeaveRoom;
    private String roomOwnerEmail;
    private String player2Email;
    private String roomId;
    private TextView txtRoomOwner, txtOtherPlayer;
    private DatabaseReference roomRef;
    private String playerColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        txtRoomOwner = findViewById(R.id.txtRoomOwner);
        txtOtherPlayer = findViewById(R.id.txtOtherPlayer);

        btnStartGame = findViewById(R.id.btnStartGame);
        btnCloseRoom = findViewById(R.id.btnCloseRoom);
        btnLeaveRoom = findViewById(R.id.btnLeaveRoom);

        roomId = getIntent().getStringExtra("roomId");
        roomOwnerEmail = getIntent().getStringExtra("player1Email");
        player2Email = getIntent().getStringExtra("player2Email");

        roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomId);

        setupRoomListener();

        if (player2Email == null) {
            // Assume this device belongs to the room owner
            playerColor = "WHITE"; // Room owner plays white
            fetchPlayerDetails(roomOwnerEmail, txtRoomOwner, "Room Owner: ");
        } else {
            // Assume this device belongs to the second player
            // Update Firebase with the second player's email
            playerColor = "BLACK"; // Second player plays black
            roomRef.child("player2Email").setValue(player2Email);
            fetchPlayerDetails(player2Email, txtOtherPlayer, "Other Player: ");
        }

        setButtonVisibility(player2Email == null);

        btnStartGame.setOnClickListener(v -> startGame());
        btnCloseRoom.setOnClickListener(v -> closeRoom());
        btnLeaveRoom.setOnClickListener(v -> leaveRoom());
    }


    private void setupRoomListener() {
        roomRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Toast.makeText(RoomActivity.this, "Room closed", Toast.LENGTH_SHORT).show();
                    finish(); // Exit activity if room is closed
                } else {
                    Room room = dataSnapshot.getValue(Room.class);
                    if (room != null) {
                        updateUI(room);
                    }
                    if (room.isGameOngoing()) {
                        startOnlinePvPActivity(); // Move both players to game activity
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(RoomActivity.this, "Failed to monitor room state.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(Room room) {
        // Update room owner name if available, otherwise indicate waiting for room owner
        if (room.getRoomOwnerEmail() != null && !room.getRoomOwnerEmail().isEmpty()) {
            // Fetch and display the room owner's username based on their email
            fetchPlayerDetails(room.getRoomOwnerEmail(), txtRoomOwner, "Room Owner: ");
        } else {
            txtRoomOwner.setText("Room Owner: Waiting...");
        }

        // Update second player's name if available, otherwise indicate that the player slot is open
        if (room.getPlayer2Email() != null && !room.getPlayer2Email().isEmpty()) {
            // Fetch and display the second player's username based on their email
            fetchPlayerDetails(room.getPlayer2Email(), txtOtherPlayer, "Other Player: ");
            roomRef.child("isJoinable").setValue(false); // Set the room to unjoinable if second player is present
        } else {
            txtOtherPlayer.setText("Other Player: Waiting for player...");
            roomRef.child("isJoinable").setValue(true); // Set the room to joinable if no second player
        }

    }


    private void fetchPlayerDetails(String email, TextView textView, String prefix) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Player player = snapshot.getValue(Player.class);
                        if (player != null && player.getUsername() != null) {
                            textView.setText(prefix + player.getUsername());
                        }
                    }
                } else {
                    textView.setText(prefix + "[Name not found]");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(RoomActivity.this, "Failed to fetch player details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setButtonVisibility(boolean isOwner) {
        if (!isOwner) {
            btnStartGame.setVisibility(View.GONE);
            btnCloseRoom.setVisibility(View.GONE);
            btnLeaveRoom.setVisibility(View.VISIBLE);
        } else {
            btnStartGame.setVisibility(View.VISIBLE);
            btnCloseRoom.setVisibility(View.VISIBLE);
            btnLeaveRoom.setVisibility(View.GONE);
        }
    }

    private void startGame() {
        roomRef.child("isJoinable").get().addOnSuccessListener(snapshot -> {
            // Check if the room is not joinable
            if (!Boolean.TRUE.equals(snapshot.getValue(Boolean.class))) {
                // Set "gameOngoing" to true
                roomRef.child("gameOngoing").setValue(true)
                        .addOnSuccessListener(aVoid -> {
                            // Game has started successfully
                            Toast.makeText(getApplicationContext(), "Game started, players are in.", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            // Handle failure
                            Toast.makeText(getApplicationContext(), "Failed to start the game: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }
            else
            {
                // Inform the room leader that the room is not full
                Toast.makeText(getApplicationContext(), "Please wait for the second player to enter the room before starting the game!", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(e -> {
            // Handle failure in getting the value
            Toast.makeText(getApplicationContext(), "Failed to check if room is joinable: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void setupDisconnectHandling() {
        if (player2Email != null) {
            // Set the player2Email to null on disconnect only if the current user is the second player
            DatabaseReference player2Ref = roomRef.child("player2Email");
            player2Ref.onDisconnect().setValue(null);
        }
    }

    private void leaveRoom() {
        // The current user is not the room owner, so simply clear their field in the database
        roomRef.child("player2Email").setValue(null).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(RoomActivity.this, "You have successfully left the room.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(RoomActivity.this, "Failed to leave the room.", Toast.LENGTH_SHORT).show();
            }
        });
        finish(); // Return to previous activity
    }


    private void closeRoom() {
        // Remove the room from Firebase, which triggers the onDataChange in the room listener to finish the activity
        roomRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(RoomActivity.this, "Room closed successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(RoomActivity.this, "Failed to close room", Toast.LENGTH_SHORT).show();
            }
        });
        finish(); // Return to previous activity
    }

    private void startOnlinePvPActivity() {
        roomRef.child("roomOwnerEmail").get().addOnSuccessListener(ownerSnapshot -> {
            String roomOwnerEmail = ownerSnapshot.getValue(String.class);

            roomRef.child("player2Email").get().addOnSuccessListener(player2Snapshot -> {
                String player2Email = player2Snapshot.getValue(String.class);

                Intent intent = new Intent(RoomActivity.this, OnlinePvPActivity.class);
                intent.putExtra("gameId", roomId);
                intent.putExtra("playerColor", playerColor);
                intent.putExtra("player1Email", roomOwnerEmail);
                intent.putExtra("player2Email", player2Email);

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Why is this necessary?

                startActivity(intent);

            }).addOnFailureListener(e -> {
                Toast.makeText(RoomActivity.this, "Failed to retrieve player 2's email: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });

        }).addOnFailureListener(e -> {
            Toast.makeText(RoomActivity.this, "Failed to retrieve room owner's email: " + e.getMessage(), Toast.LENGTH_LONG).show();
        });
    }
}