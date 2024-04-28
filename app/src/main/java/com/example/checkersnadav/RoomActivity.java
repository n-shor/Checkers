package com.example.checkersnadav;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RoomActivity extends AppCompatActivity {

    private Button btnStartGame;
    private Button btnCloseRoom;
    private Button btnLeaveRoom;
    private String player2Email;
    private String roomOwnerEmail;
    private String roomId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        btnStartGame = findViewById(R.id.btnStartGame);
        btnCloseRoom = findViewById(R.id.btnCloseRoom);
        btnLeaveRoom = findViewById(R.id.btnLeaveRoom);

        // Retrieve the current user's email and room owner's email from the intent
        player2Email = getIntent().getStringExtra("player2Email");
        roomOwnerEmail = getIntent().getStringExtra("player1Email");
        roomId = getIntent().getStringExtra("roomId");

        // Check if the current user is the room owner
        if (!player2Email.equals(roomOwnerEmail))
        {
            // Hide the start and close buttons if not the owner
            btnStartGame.setVisibility(View.GONE);
            btnCloseRoom.setVisibility(View.GONE);
            btnLeaveRoom.setVisibility(View.VISIBLE);
        }
        else
        {
            // Show start and close buttons for the owner
            btnStartGame.setVisibility(View.VISIBLE);
            btnCloseRoom.setVisibility(View.VISIBLE);
            btnLeaveRoom.setVisibility(View.GONE);
        }

        // Set onClickListeners
        btnStartGame.setOnClickListener(v -> startGame());
        btnCloseRoom.setOnClickListener(v -> closeRoom());
        btnLeaveRoom.setOnClickListener(v -> leaveRoom());
    }

    private void startGame()
    {
        // Code to start the game
    }

    private void closeRoom()
    {
        DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomId);
        roomRef.removeValue()  // Remove the room from the database
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        Toast.makeText(RoomActivity.this, "Room closed successfully", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(RoomActivity.this, "Failed to close room", Toast.LENGTH_SHORT).show();
                    }
                    finish();  // Go back to the previous activity
                });
    }

    private void leaveRoom() {
        DatabaseReference roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomId);
        roomRef.child("player2Email").setValue(null)  // Set player2Email to null to indicate the spot is available
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        Toast.makeText(RoomActivity.this, "You have left the room", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(RoomActivity.this, "Failed to leave the room", Toast.LENGTH_SHORT).show();
                    }
                    finish();  // Go back to the previous activity
                });
    }
}
