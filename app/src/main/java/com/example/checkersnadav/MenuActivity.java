package com.example.checkersnadav;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Activity representing the main menu of the app where the user can navigate to different game modes
 * and functionalities like PvP, Stats, Tutorial, or Log Out.
 */
public class MenuActivity extends AppCompatActivity
{

    private String userId;  // User ID to maintain session state across activities.

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        TextView tvPlayerNameDisplay = findViewById(R.id.playerName);

        // Retrieving the user's ID from the intent. This could be from the login activity or any other
        // activity that navigates back to the main menu.
        userId = getIntent().getStringExtra("userId");

        // Reference to the Firebase Database users node.
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists()) {
                    // Display the logged-in user's username in the main menu.
                    tvPlayerNameDisplay.setText("Logged in As:\n" + dataSnapshot.child("username").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                // Handle possible errors gracefully.
                Toast.makeText(MenuActivity.this, "Failed to fetch player name", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Navigates to the Local Player vs Player game mode.
     * @param view The view that initiated the call.
     */
    public void goToLocalPvP(View view)
    {
        Intent intent = new Intent(MenuActivity.this, LocalPvPActivity.class);
        intent.putExtra("userId", userId); // Pass the user's ID
        startActivity(intent);
        finish();
    }

    /**
     * Navigates to the room creation or joining screen for online multiplayer.
     * @param view The view that initiated the call.
     */
    public void goToCreateOrJoinRoom(View view)
    {
        Intent intent = new Intent(MenuActivity.this, CreateAndJoinRoomActivity.class);
        intent.putExtra("userId", userId); // Pass the user's ID
        startActivity(intent);
        finish();
    }

    /**
     * Navigates to the tutorial screen.
     * @param view The view that initiated the call.
     */
    public void goToTutorial(View view)
    {
        Intent intent = new Intent(MenuActivity.this, TutorialActivity.class);
        intent.putExtra("userId", userId); // Pass the user's ID
        startActivity(intent);
        finish();
    }

    /**
     * Navigates to the statistics screen.
     * @param view The view that initiated the call.
     */
    public void goToStats(View view)
    {
        Intent intent = new Intent(this, StatsActivity.class);
        intent.putExtra("userId", userId); // Pass the user's ID
        startActivity(intent);
        finish();
    }

    /**
     * Logs out the current user and navigates back to the login screen.
     * @param view The view that initiated the call.
     */
    public void logout(View view)
    {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
