package com.example.checkersnadav;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class OfflineEndScreenActivity extends AppCompatActivity {

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_end_screen);

        userId = getIntent().getStringExtra("userId");
        String winner = getIntent().getStringExtra("winner");

        TextView tvWinner = findViewById(R.id.tvWinner);

        if (Game.DRAW_STRING.equals(winner))
        {
            tvWinner.setText("DRAW!");
        }
        else
        {
            tvWinner.setText("Winner:\n" + winner + "!");
        }

    }


    public void goToMainMenu(View view) {
        Intent intent = new Intent(OfflineEndScreenActivity.this, MenuActivity.class);
        intent.putExtra("userId", userId); // Pass the user's ID
        startActivity(intent);
        finish();
    }

}