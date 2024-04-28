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
    private String player2Email;
    private String roomOwnerEmail;
    private String roomId;
    private TextView txtRoomOwner, txtOtherPlayer;
    private DatabaseReference roomRef;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        txtRoomOwner = findViewById(R.id.txtRoomOwner);
        txtOtherPlayer = findViewById(R.id.txtOtherPlayer);

        btnStartGame = findViewById(R.id.btnStartGame);
        btnCloseRoom = findViewById(R.id.btnCloseRoom);
        btnLeaveRoom = findViewById(R.id.btnLeaveRoom);

        roomOwnerEmail = getIntent().getStringExtra("player1Email");
        player2Email = getIntent().getStringExtra("player2Email");
        roomId = getIntent().getStringExtra("roomId");

        roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomId);
        setupRoomListener();

        if (player2Email != null)
        {
            fetchPlayerDetails(player2Email, txtOtherPlayer, "Other Player: ");
        }

        fetchPlayerDetails(roomOwnerEmail, txtRoomOwner, "Room Owner: ");

        // Determine button visibility based on player role
        setButtonVisibility(!roomOwnerEmail.equals(player2Email));

        // Set onClickListeners
        btnStartGame.setOnClickListener(v -> startGame());
        btnCloseRoom.setOnClickListener(v -> closeRoom());
        btnLeaveRoom.setOnClickListener(v -> leaveRoom());
    }

    private void setupRoomListener()
    {
        roomRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (!dataSnapshot.exists()) {
                    // If the room is deleted, exit activity
                    Toast.makeText(RoomActivity.this, "Room closed", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Room room = dataSnapshot.getValue(Room.class);
                    if (room != null && room.getPlayer2Email() == null)
                    {
                        txtOtherPlayer.setText("Other Player: Waiting...");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(RoomActivity.this, "Failed to monitor room state.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchPlayerDetails(String email, TextView textView, String prefix) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Player player = snapshot.getValue(Player.class);
                        if (player != null) {
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

    private void setButtonVisibility(boolean isNotOwner) {
        if (isNotOwner) {
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
        // Code to start the game
        roomRef.child("gameOngoing").setValue(true);
    }

    private void closeRoom() {
        roomRef.removeValue();
    }

    private void leaveRoom() {
        if (!roomOwnerEmail.equals(player2Email)) { // if it's not the owner
            roomRef.child("player2Email").setValue(null);
        } else {
            closeRoom(); // If owner leaves, close the room
        }
    }
}
