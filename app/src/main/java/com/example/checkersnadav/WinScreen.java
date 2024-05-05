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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Activity displayed when a user wins a game. It shows the user's username and indicates whether a daily bonus is activated.
 */
public class WinScreen extends AppCompatActivity
{

    private TextView usernameTextView; // TextView for displaying the user's username
    private TextView bonusActivatedTextView; // TextView for indicating the daily bonus activation

    /**
     * Initializes the win screen with user details and bonus status.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     *                           Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_win_screen);

        usernameTextView = findViewById(R.id.usernameTextView);
        bonusActivatedTextView = findViewById(R.id.bonusActivatedTextView);
        Button backToMenuButton = findViewById(R.id.backToMenuButton);

        bonusActivatedTextView.setVisibility(View.GONE);

        // Retrieve the userId from the intent used to start this activity
        String userId = getIntent().getStringExtra("userId");

        // Reference to the Firebase node where user information is stored
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    // Display the username in the username TextView
                    usernameTextView.setText(dataSnapshot.child("username").getValue(String.class));

                    // Format the current date to check against the last win date
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", new Locale("he", "IL"));
                    sdf.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));  // Set timezone to Israel
                    String todayInIsrael = sdf.format(new Date());

                    // Check and display if the user has activated the daily bonus
                    if (!todayInIsrael.equals(dataSnapshot.child("lastWinDate").getValue(String.class)))
                    {
                        bonusActivatedTextView.setVisibility(View.VISIBLE);
                    }

                    // Update the user's last win date to today
                    userRef.child("lastWinDate").setValue(todayInIsrael);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                // Handle errors in fetching data from Firebase
                Toast.makeText(WinScreen.this, "Failed to fetch player's username: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Set up the button to go back to the main menu
        backToMenuButton.setOnClickListener(v ->
        {
            Intent intent = new Intent(WinScreen.this, MenuActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            finish();
        });
    }
}
