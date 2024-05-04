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


public class LoseScreen extends AppCompatActivity {

    private TextView usernameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lose_screen);

        usernameTextView = findViewById(R.id.usernameTextView);
        Button backToMenuButton = findViewById(R.id.backToMenuButton);

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
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(LoseScreen.this,"Failed to fetch player's username: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        // Set up the back button
        backToMenuButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoseScreen.this, Menu.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            finish();
        });
    }
}
