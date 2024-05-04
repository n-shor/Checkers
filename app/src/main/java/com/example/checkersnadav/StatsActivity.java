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
            Intent intent1 = new Intent(StatsActivity.this, MenuActivity.class);
            intent1.putExtra("userId", userId);
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
                            " Player stats of %s:\n\n\n Games Won: %d\n\n Games Lost: %d\n\n Draws: %d\n\n Avg Moves/Game: %d\n\n Most Moves in\n One Game: %d",
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
