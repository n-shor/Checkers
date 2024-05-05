package com.example.checkersnadav;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * This activity represents the end screen for offline games, displaying the game outcome and
 * providing a navigation option to return to the main menu.
 */
public class OfflineEndScreenActivity extends AppCompatActivity
{

    private String userId;  // Identifier for the user, used for passing between activities.

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_end_screen);

        // Retrieve user ID and the game outcome from the intent.
        userId = getIntent().getStringExtra("userId");
        String winner = getIntent().getStringExtra("winner");

        TextView tvWinner = findViewById(R.id.tvWinner);

        // Display the game result based on the 'winner' string received.
        if (Game.DRAW_STRING.equals(winner))
        {
            tvWinner.setText("DRAW!");
        }
        else
        {
            tvWinner.setText("Winner:\n" + winner + "!");
        }
    }

    /**
     * Navigates back to the main menu when the user chooses to leave the end screen.
     * @param view The view (button) that was clicked to trigger this method.
     */
    public void goToMainMenu(View view)
    {
        Intent intent = new Intent(OfflineEndScreenActivity.this, MenuActivity.class);
        intent.putExtra("userId", userId); // Ensure the user ID is carried forward to maintain session state.
        startActivity(intent);
        finish(); // Finish this activity to remove it from the back stack.
    }
}
