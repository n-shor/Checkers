package com.example.checkersnadav;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class StatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        // Get user's email from intent
        Intent intent = getIntent();
        String userEmail = intent.getStringExtra("USER_EMAIL");

        // Fetch user's statistics from Firebase
        fetchUserStats(userEmail);

        // Set up back button to return to MenuActivity
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // or explicitly create an intent for MenuActivity if needed
            }
        });
    }

    private void fetchUserStats(String email) {
        // Reference to Firebase database
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users").child(email).child("statistics");

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Statistics stats = dataSnapshot.getValue(Statistics.class);

                    // Display the statistics
                    String userStats = String.format(
                            "Statistics for %s:\nGames Won: %d\nGames Lost: %d\nDraws: %d\nAvg Moves/Game: %d\nTop Moves: %d",
                            email, stats.getWins(), stats.getLosses(), stats.getDraws(),
                            stats.getAverageMovesPerGame(), stats.getTopMoves()
                    );

                    // Update the UI with the stats
                    TextView statsTextView = findViewById(R.id.statsTextView);
                    statsTextView.setText(userStats);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle any errors that occurred while fetching data
                Toast.makeText(StatsActivity.this, "Failed to fetch stats: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
