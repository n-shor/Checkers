package com.example.checkersnadav;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Menu extends AppCompatActivity {

    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Retrieving the user's email from the intent
        userEmail = getIntent().getStringExtra("userEmail");

    }

    public void goToCreateOrJoinRoom(View view) {
        Intent intent = new Intent(this, LocalPvPActivity.class);
        intent.putExtra("userEmail", userEmail); // Pass the user's email
        startActivity(intent);
        finish();
    }

    public void goToOnlinePvP(View view) {
        Intent intent = new Intent(this, CreateAndJoinRoom.class);
        intent.putExtra("userEmail", userEmail); // Pass the user's email
        startActivity(intent);
        finish();
    }

    public void goToStats(View view) {
        Intent intent = new Intent(this, StatsActivity.class);
        intent.putExtra("userEmail", userEmail); // Pass the user's email
        startActivity(intent);
        finish();
    }

    public void logout(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
