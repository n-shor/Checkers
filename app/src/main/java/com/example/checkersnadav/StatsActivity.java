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
        String userId = intent.getStringExtra("userId");

        // Fetch user's statistics from Firebase
        fetchUserStats(userId);

        // Set up back button to return to MenuActivity
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Intent intent1 = new Intent(StatsActivity.this, Menu.class);
            startActivity(intent1);
            finish();
        });
    }

    private void fetchUserStats(String userId) {
        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference();

        databaseRef.child("users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Statistics stats = dataSnapshot.child("stats").getValue(Statistics.class);

                    // Display the statistics
                    String userStats = String.format(
                            "Statistics for %s:\nGames Won: %d\nGames Lost: %d\nDraws: %d\nAvg Moves/Game: %d\nMost Moves in One Game: %d",
                            dataSnapshot.child("username").getValue(), stats.getWins(), stats.getLosses(), stats.getDraws(),
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
