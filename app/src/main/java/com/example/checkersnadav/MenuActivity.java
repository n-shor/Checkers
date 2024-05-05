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

public class MenuActivity extends AppCompatActivity {

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        TextView tvPlayNameDisplay = findViewById(R.id.playerName);

        // Retrieving the user's email from the intent, this could be the login activity or any other activity that goes back to the main menu
        userId = getIntent().getStringExtra("userId");

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    tvPlayNameDisplay.setText("Logged As:\n" + dataSnapshot.child("username").getValue(String.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(MenuActivity.this, "Failed to fetch player name", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void goToLocalPvP(View view) {
        Intent intent = new Intent(MenuActivity.this, LocalPvPActivity.class);
        intent.putExtra("userId", userId); // Pass the user's ID
        startActivity(intent);
        finish();
    }

    public void goToCreateOrJoinRoom(View view) {
        Intent intent = new Intent(MenuActivity.this, CreateAndJoinRoomActivity.class);
        intent.putExtra("userId", userId); // Pass the user's ID
        startActivity(intent);
        finish();
    }

    public void goToTutorial(View view) {
        Intent intent = new Intent(MenuActivity.this, TutorialActivity.class);
        intent.putExtra("userId", userId); // Pass the user's ID
        startActivity(intent);
        finish();
    }


    public void goToStats(View view) {
        Intent intent = new Intent(this, StatsActivity.class);
        intent.putExtra("userId", userId); // Pass the user's ID
        startActivity(intent);
        finish();
    }

    public void logout(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
