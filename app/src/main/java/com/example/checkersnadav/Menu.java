package com.example.checkersnadav;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class Menu extends AppCompatActivity {

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        // Retrieving the user's email from the intent
        userId = getIntent().getStringExtra("userId");

    }

    public void goToLocalPvP(View view) {
        Intent intent = new Intent(this, LocalPvPActivity.class);
        startActivity(intent);
        finish();
    }

    public void goToCreateOrJoinRoom(View view) {
        Intent intent = new Intent(this, CreateAndJoinRoom.class);
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
