package com.example.checkersnadav;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Activity to manage a specific game room for the online PvP feature of the checkers game.
 * It sets up the room's components and shows helpful information,
 * and allows players to continue to the online game, or to go back to the room selection screen.
 */
public class RoomActivity extends AppCompatActivity
{

    private Button btnStartGame;
    private Button btnCloseRoom;
    private Button btnLeaveRoom;
    private String roomOwnerId;
    private String player2Id;
    private String roomId;
    private TextView txtRoomOwner, txtOtherPlayer;
    private DatabaseReference roomRef;
    private String playerColor;

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

        roomId = getIntent().getStringExtra("roomId");
        roomOwnerId = getIntent().getStringExtra("player1Id");
        player2Id = getIntent().getStringExtra("player2Id");

        roomRef = FirebaseDatabase.getInstance().getReference("rooms").child(roomId);

        setupRoomListener();

        if (player2Id == null)
        {
            // Assume this device belongs to the room owner
            playerColor = Game.WHITE_STRING; // Room owner plays white
            fetchPlayerDetails(roomOwnerId, txtRoomOwner, "Room Owner: ");
        }
        else
        {
            // Assume this device belongs to the second player
            // Update Firebase with the second player's email
            playerColor = Game.BLACK_STRING; // Second player plays black, name technically doesn't matter because we only check if it's different than WHITE
            roomRef.child("player2Id").setValue(player2Id);
            fetchPlayerDetails(player2Id, txtOtherPlayer, "Other Player: ");
        }

        setButtonVisibility(player2Id == null);

        btnStartGame.setOnClickListener(v -> startGame());
        btnCloseRoom.setOnClickListener(v -> closeRoom());
        btnLeaveRoom.setOnClickListener(v -> leaveRoom());
    }

    /**
     * Sets up a ValueEventListener that will listen to changes in the room in the database and update its local state accordingly.
     */
    private void setupRoomListener()
    {
        final boolean[] gameStarted = {false};
        roomRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (!dataSnapshot.exists()) {

                    // Exit activity if room is closed and game has not started yet (second condition prevents code from triggering after a game is over)
                    if (!gameStarted[0])
                    {
                        Toast.makeText(RoomActivity.this, "Room closed", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RoomActivity.this, CreateAndJoinRoomActivity.class);
                        intent.putExtra("userId", playerColor.equals(Game.WHITE_STRING) ? roomOwnerId : player2Id);
                        startActivity(intent);
                        finish();
                    }

                }
                else
                {
                    Room room = dataSnapshot.getValue(Room.class);
                    if (room != null)
                    {
                        updateUI(room);
                    }
                    if (room.isGameOngoing())
                    {
                        gameStarted[0] = true;
                        startOnlinePvPActivity(); // Move both players to game activity
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Toast.makeText(RoomActivity.this, "Failed to monitor room state.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Updates the activity's UI based on the information stored in the room object that it receives as input.
     * @param room The said room object.
     */
    private void updateUI(Room room)
    {
        // Update room owner name if available, otherwise indicate waiting for room owner
        if (room.getRoomOwnerId() != null)
        {
            // Fetch and display the room owner's username based on their email
            fetchPlayerDetails(room.getRoomOwnerId(), txtRoomOwner, "Room Owner: ");
        }
        else
        {
            txtRoomOwner.setText("Room Owner: Waiting...");
        }

        // Update second player's name if available, otherwise indicate that the player slot is open
        if (room.getPlayer2Id() != null)
        {
            // Fetch and display the second player's username based on their email
            fetchPlayerDetails(room.getPlayer2Id(), txtOtherPlayer, "Other Player: ");
            roomRef.child("isJoinable").setValue(false); // Set the room to unjoinable if second player is present
        }
        else
        {
            txtOtherPlayer.setText("Other Player: Waiting for player...");
            roomRef.child("isJoinable").setValue(true); // Set the room to joinable if no second player
        }

    }

    /**
     * Updates a TextView components' text to a certain prefix and right after it the name of a player whose ID is passed as an argument to the method.
     * @param userId The ID of said user.
     * @param textView The TextView component that would get updated.
     * @param prefix The prefix that would come before the player's name.
     */
    private void fetchPlayerDetails(String userId, TextView textView, String prefix)
    {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    Player player = dataSnapshot.getValue(Player.class);
                    if (player != null && player.getUsername() != null)
                    {
                        textView.setText(prefix + player.getUsername());
                    }
                }
                else
                {
                    textView.setText(prefix + "[Name not found]");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Toast.makeText(RoomActivity.this, "Failed to fetch player details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Changes the visibility of buttons in the activity based on the player's identity.
     * @param isOwner The player's room ownership status.
     */
    private void setButtonVisibility(boolean isOwner)
    {
        if (!isOwner)
        {
            // If the player doesn't own the room, only let them leave the room
            btnStartGame.setVisibility(View.GONE);
            btnCloseRoom.setVisibility(View.GONE);
            btnLeaveRoom.setVisibility(View.VISIBLE);
        }
        else
        {
            // If the player owns the room, only let them start the game and close the room
            btnStartGame.setVisibility(View.VISIBLE);
            btnCloseRoom.setVisibility(View.VISIBLE);
            btnLeaveRoom.setVisibility(View.GONE);
        }
    }

    /**
     * Changes the game's status in the database to 'ongoing' if it is eligible to start (both players are in the room).
     */
    private void startGame()
    {
        roomRef.child("isJoinable").get().addOnSuccessListener(snapshot ->
        {
            // Check if the room is not joinable
            if (!Boolean.TRUE.equals(snapshot.getValue(Boolean.class)))
            {
                // Set "gameOngoing" to true
                roomRef.child("gameOngoing").setValue(true)
                        .addOnSuccessListener(aVoid ->
                        {
                            // Game has started successfully
                        })
                        .addOnFailureListener(e ->
                        {
                            // Handle failure
                            Toast.makeText(getApplicationContext(), "Failed to start the game: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
            else
            {
                // Inform the room leader that the room is not full
                Toast.makeText(getApplicationContext(), "Please wait for the second player before starting the game!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e ->
        {
            // Handle failure in getting the value
            Toast.makeText(getApplicationContext(), "Failed to check if room is joinable: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Removes this player from the room entry in the database and goes back to the room selection and/or creation activity.
     */
    private void leaveRoom()
    {
        // The current user is not the room owner, so simply clear their field in the database
        roomRef.child("player2Id").setValue(null).addOnCompleteListener(task ->
        {
            if (task.isSuccessful())
            {
                Toast.makeText(RoomActivity.this, "You have successfully left the room.", Toast.LENGTH_SHORT).show();
            }
            else
            {
                Toast.makeText(RoomActivity.this, "Failed to leave the room.", Toast.LENGTH_SHORT).show();
            }
        });

        Intent intent = new Intent(RoomActivity.this, CreateAndJoinRoomActivity.class);
        intent.putExtra("userId", player2Id);
        startActivity(intent);
        finish();
    }

    /**
     * Deletes this room from the database and sends both players back to the room selection and/or creation activity.
     */
    private void closeRoom()
    {
        // Remove the room from Firebase, which triggers the onDataChange in the room listener to finish the activity
        roomRef.removeValue().addOnCompleteListener(task ->
        {
            if (!task.isSuccessful())
            {
                Toast.makeText(RoomActivity.this, "Failed to close room", Toast.LENGTH_SHORT).show();
            }
        });
        Intent intent = new Intent(RoomActivity.this, CreateAndJoinRoomActivity.class);
        intent.putExtra("userId", roomOwnerId);
        startActivity(intent);
        finish();
    }

    /**
     * Sends the player into the OnlinePvP activity and sends necessary information along the way.
     */
    private void startOnlinePvPActivity()
    {
        roomRef.child("roomOwnerId").get().addOnSuccessListener(ownerSnapshot ->
        {
            String roomOwnerId = ownerSnapshot.getValue(String.class);

            roomRef.child("player2Id").get().addOnSuccessListener(player2Snapshot ->
            {
                String player2Id = player2Snapshot.getValue(String.class);
                // Add necessary information for the online game to function
                Intent intent = new Intent(RoomActivity.this, OnlinePvPActivity.class);
                intent.putExtra("gameId", roomId);
                intent.putExtra("playerColor", playerColor);
                intent.putExtra("player1Id", roomOwnerId);
                intent.putExtra("player2Id", player2Id);

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                Toast.makeText(getApplicationContext(), "Game started, players are in.", Toast.LENGTH_SHORT).show();
                startActivity(intent);
                finish();

            }).addOnFailureListener(e -> Toast.makeText(RoomActivity.this, "Failed to retrieve player 2's email: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        }).addOnFailureListener(e -> Toast.makeText(RoomActivity.this, "Failed to retrieve room owner's email: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}