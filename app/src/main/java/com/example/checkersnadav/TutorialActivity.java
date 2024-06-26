package com.example.checkersnadav;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity class that provides a tutorial for the Checkers game.
 * Displays game rules and instructions formatted in HTML to enhance readability and user experience.
 */
public class TutorialActivity extends AppCompatActivity
{

    /**
     * Called when the activity is starting.
     * Initializes the UI, sets up the tutorial text, and provides navigation options.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     *                           Note: Otherwise it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        // Retrieve the user's ID from the intent used to start this activity
        String userId = getIntent().getStringExtra("userId");

        // Find the TextView and prepare the HTML content for the tutorial
        TextView textViewRules = findViewById(R.id.textViewRules);
        String rulesHtml = "<h1>Welcome to my Checkers game!</h1><br>" +
                "<h2>Basic Rules of Checkers:</h2>" +
                "<p>- The game is played on an 8x8 board with 12 pieces per player, positioned on the dark squares.<br>" +
                "- Pieces move diagonally, and only forward, to an adjacent unoccupied square.<br>" +
                "- If an opponent's piece can be captured by jumping over it to a vacant square immediately beyond it, the jump must be made.<br>" +
                "- If after capturing your moved piece would be able to capture if it were your turn, the turn will not transfer to the other player, and you will have to capture again with the same piece. This is called a chain capture.<br>" +
                "- When a piece reaches the furthest row from the player (the king row), it becomes a king. Kings can move forward and backward diagonally, and their move distance is unlimited, as long as they don't jump over more than one piece or just one allied piece. After capturing a piece, kings may choose to land at any square that is behind the captured piece, as long as the path to said square is not occupied.<br>" +
                "- When a player runs out of pieces or has no possible moves on their turn, they lose, and the other play consequentially wins. If 80 moves (in total, both players' moves count) pass without a capture and without a new king being crowned, the game ends in a draw.</p><br>" +
                "<h2>Custom Modifications:</h2>" +
                "<p>- <strong>All pieces can capture backwards, not just kings.</strong> This allows normal pieces to jump backwards during a capture move.<br>" +
                "- <strong>While capturing is mandatory, you are not required to choose the capture path that results in the most chain captures.</strong> This allows for strategic flexibility in your capturing decisions.</p><br>" +
                "<h2>Gameplay Mechanics:</h2>" +
                "<p>- Players move their pieces by dragging and dropping them on the board.<br>" +
                "- If an illegal move is played, a message will display, informing the player that the move is illegal.</p><br>" +
                "<h2>Game Modes:</h2>" +
                "<p>- Local PvP: Play offline against a friend on the same device.<br>" +
                "- Online PvP: Create a room or join an existing one to play against other players online. This game mode features an ELO based rating system, where each player has a rating that changes based on the results of their games.</p><br>" +
                "<h2>Statistics:</h2>" +
                "<p>- Visit the Stats page to view your game statistics and performance. Note that statistics only apply to online games.</p><br>" +
                "<p><h2>Daily Bonuses:</h2>" +
                "<p>- Every day, your first online win will award you 3 wins, and will triple the points added to your ELO rating that game (the wins will be added to your user statistics)!</p><br>" +
                "<p><strong>Enjoy the game, and may the best strategist win!</strong></p>";

        // Apply the HTML formatted text to the TextView
        textViewRules.setText(Html.fromHtml(rulesHtml, Html.FROM_HTML_MODE_COMPACT));
        // Customize line spacing for better readability
        textViewRules.setLineSpacing(1.2f, 1.5f);

        // Set up a back button that navigates to the MenuActivity
        findViewById(R.id.buttonBack).setOnClickListener(v ->
        {
            Intent intent = new Intent(TutorialActivity.this, MenuActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            finish();
        });
    }
}
