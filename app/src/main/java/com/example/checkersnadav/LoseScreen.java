package com.example.checkersnadav;

import android.content.Intent;
import android.os.Bundle;
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
 * This activity represents the lose screen displayed when a player loses a game.
 * It fetches the user's username from Firebase and provides an option to return to the main menu.
 */
public class LoseScreen extends AppCompatActivity
{

    private TextView usernameTextView;  // TextView to display the player's username.

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lose_screen);

        usernameTextView = findViewById(R.id.usernameTextView);
        Button backToMenuButton = findViewById(R.id.backToMenuButton);

        // Retrieve the userId passed from the previous activity.
        String userId = getIntent().getStringExtra("userId");

        // Reference to the Firebase database for user details.
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);

        // Single value event listener to fetch the username only once.
        userRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    // Set the username in the TextView.
                    usernameTextView.setText(dataSnapshot.child("username").getValue(String.class));
                }
                else
                {
                    Toast.makeText(LoseScreen.this, "User data not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                // Handle possible errors with Firebase database operations.
                Toast.makeText(LoseScreen.this, "Failed to fetch player's username: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Set up the back button to return to the main menu.
        backToMenuButton.setOnClickListener(v ->
        {
            Intent intent = new Intent(LoseScreen.this, MenuActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            finish();
        });
    }
}
