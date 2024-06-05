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
 * Activity that displays a draw screen for the game, indicating that the game ended without a winner.
 * It fetches and shows the player's username and provides an option to return to the main menu.
 */
public class DrawScreen extends AppCompatActivity
{

    private TextView usernameTextView; // TextView to display the user's username
    private TextView newEloTextView; // TextView for displaying the user's new elo after the game

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lose_screen); // Note: Uses the lose screen layout, might want to rename or confirm this is intentional.

        usernameTextView = findViewById(R.id.usernameTextView);
        newEloTextView = findViewById(R.id.newEloTextView);
        Button backToMenuButton = findViewById(R.id.backToMenuButton);

        // Retrieve the user ID and username from the passed intent.
        String userId = getIntent().getStringExtra("userId");

        // Reference to the user's data in Firebase.
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    // Display the username on the screen if it exists in the database.
                    usernameTextView.setText(dataSnapshot.child("username").getValue(String.class));
                    newEloTextView.setText("New Elo: " + dataSnapshot.child("stats").child("elo").getValue(Integer.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                // Show error message if there is a failure fetching the username.
                Toast.makeText(DrawScreen.this, "Failed to fetch player's username: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Setup the back button to navigate back to the Menu screen.
        backToMenuButton.setOnClickListener(v ->
        {
            Intent intent = new Intent(DrawScreen.this, MenuActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            finish();
        });
    }
}
