package com.example.checkersnadav;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class WinScreen extends AppCompatActivity {

    private TextView usernameTextView;
    private TextView bonusActivatedTextView;
    private Button backToMenuButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_win_screen);

        usernameTextView = findViewById(R.id.usernameTextView);
        bonusActivatedTextView = findViewById(R.id.bonusActivatedTextView);
        backToMenuButton = findViewById(R.id.backToMenuButton);

        bonusActivatedTextView.setVisibility(View.GONE);

        // Get the userId and username from the intent
        String userId = getIntent().getStringExtra("userId");

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists())
                {
                    // Set the username in the TextView
                    usernameTextView.setText(dataSnapshot.child("username").getValue(String.class));

                    // Get the date in Israel right now
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", new Locale("he", "IL"));
                    sdf.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));  // Set to Israel's time zone
                    String todayInIsrael = sdf.format(new Date());

                    // Check if the user has a first win of the day bonus
                    if (!todayInIsrael.equals(dataSnapshot.child("lastWinDate").getValue(String.class)))
                    {
                        bonusActivatedTextView.setVisibility(View.VISIBLE);
                    }

                    // Update the user's last win date, so their daily bonus will be removed
                    userRef.child("lastWinDate").setValue(todayInIsrael);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Failed to fetch player's username: " + databaseError.getMessage());
            }
        });


        // Set up the back button
        backToMenuButton.setOnClickListener(v -> {
            Intent intent = new Intent(WinScreen.this, Menu.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            finish();
        });
    }
}
