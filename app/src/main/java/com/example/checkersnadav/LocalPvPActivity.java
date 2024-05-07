package com.example.checkersnadav;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity to manage local player vs. player games of checkers.
 * It sets up the game board, handles touch interactions for making moves, and updates the game state accordingly.
 */
public class LocalPvPActivity extends AppCompatActivity
{
    private Game game; // The game logic handler.
    private GridView gridView; // The grid view that displays the checkers board.
    private CheckersAdapter adapter; // Adapter to manage the interaction between the GridView and the game data.

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_pvp);

        String userId = getIntent().getStringExtra("userId");

        game = new Game();
        gridView = findViewById(R.id.grid_view);
        adapter = new CheckersAdapter(this, game.getBoard().getState(), Board.WHITE); // Set the board perspective to white for local PvP.
        gridView.setAdapter(adapter);

        // Handle touch events on the grid view for move operations.
        gridView.setOnTouchListener(new View.OnTouchListener()
        {
            private int startX = -1; // Start row for move.
            private int startY = -1; // Start column for move.

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                int action = event.getActionMasked();
                int position = gridView.pointToPosition((int)event.getX(), (int)event.getY());

                if (position == GridView.INVALID_POSITION)
                {
                    return false; // Invalid touch position, outside of grid bounds.
                }

                int row = position / Board.BOARD_SIZE;
                int col = position % Board.BOARD_SIZE;

                // Adjust for flipped board orientation in GUI.
                int flippedRow = Board.BOARD_SIZE - 1 - row;
                int flippedCol = Board.BOARD_SIZE - 1 - col;

                switch (action)
                {
                    case MotionEvent.ACTION_DOWN:
                        // Record the start position of a drag (potential move).
                        startX = flippedRow;
                        startY = flippedCol;
                        break;
                    case MotionEvent.ACTION_UP:
                        // Attempt to make a move from the start position to the end position.
                        if (game.makeMove(startX, startY, flippedRow, flippedCol))
                        {
                            adapter.notifyDataSetChanged(); // Update the board if move was successful.
                        }
                        else
                        {
                            Toast.makeText(LocalPvPActivity.this, "Invalid Move", Toast.LENGTH_SHORT).show();
                        }

                        // Reset the start positions for the next move.
                        startX = -1;
                        startY = -1;

                        // Check if the game has ended and move to the end screen if so.
                        if (!game.isActive())
                        {
                            Intent intent = new Intent(LocalPvPActivity.this, OfflineEndScreenActivity.class);
                            intent.putExtra("winner", game.getBoard().checkGameStatus()); // Pass the game result.
                            intent.putExtra("userId", userId);
                            startActivity(intent);
                            finish();
                        }
                        break;
                }
                return true; // Touch event was handled.
            }
        });
    }
}
