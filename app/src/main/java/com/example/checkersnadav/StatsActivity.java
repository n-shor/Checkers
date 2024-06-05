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
 * Activity class that displays statistics for a specific user.
 * Fetches user's game statistics from Firebase and displays them in a TextView.
 */
public class StatsActivity extends AppCompatActivity
{
    private TextView titleTextView; // TextView for displaying the title of the screen
    private TextView statsTextView; // TextView for displaying the user's statistics

    /**
     * Called when the activity is starting.
     * Initializes the UI and sets up interactions.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     *                           Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        // Retrieve the user's ID from the intent passed to this activity
        Intent intent = getIntent();
        String userId = intent.getStringExtra("userId");

        titleTextView = findViewById(R.id.titleTextView);
        statsTextView = findViewById(R.id.statsTextView);

        // Fetch and display user's statistics
        fetchUserStats(userId);

        // Set up a button that allows the user to return to the main menu
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v ->
        {
            Intent returnIntent = new Intent(StatsActivity.this, MenuActivity.class);
            returnIntent.putExtra("userId", userId);
            startActivity(returnIntent);
            finish();
        });
    }

    /**
     * Fetches and displays the statistics for a given user.
     * Connects to Firebase to retrieve user data based on the user ID.
     *
     * @param userId The ID of the user whose statistics are to be fetched and displayed.
     */
    private void fetchUserStats(String userId)
    {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();

        // Listen for data changes at the user's node
        databaseRef.child("users").child(userId).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    Statistics stats = dataSnapshot.child("stats").getValue(Statistics.class);

                    titleTextView.setText(String.format("Player stats of %s:", dataSnapshot.child("username").getValue()));

                    // Construct a string that represents the user statistics
                    String userStats = String.format(
                            "Elo: %d\n\nGames Won: %d\n\nGames Lost: %d\n\nDraws: %d\n\nAvg Moves/Game: %d\n\nMost Moves in One Game: %d",
                            stats.getElo(), stats.getWins(), stats.getLosses(), stats.getDraws(),
                            stats.getAverageMovesPerGame(), stats.getTopMoves()
                    );

                    // Update the TextView in the UI to display the statistics
                    statsTextView.setText(userStats);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                // Display an error message if the fetch operation fails
                Toast.makeText(StatsActivity.this, "Failed to fetch stats: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
