package com.example.checkersnadav;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
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
    private TextView turnIndicator; // TextView to display the current turn.
    private TextView turnIndicator2; // Flipped turn indicator for the second player to look at.

    private int startX = -1; // Start row for move.
    private int startY = -1; // Start column for move.
    private ImageView draggedPiece; // ImageView to represent the dragged piece
    private View originalView; // Original view of the dragged piece
    private int heldPosition; // The position of the currently held piece
    private Piece heldPiece; // The currently held piece

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_pvp);

        String userId = getIntent().getStringExtra("userId");

        game = new Game();
        gridView = findViewById(R.id.grid_view);
        turnIndicator = findViewById(R.id.turn_indicator);
        turnIndicator2 = findViewById(R.id.turn_indicator2);
        adapter = new CheckersAdapter(this, game.getBoard().getState(), Board.WHITE); // Set the board perspective to white for local PvP.
        gridView.setAdapter(adapter);

        updateTurnIndicators();

        gridView.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                int action = event.getActionMasked();
                int position = gridView.pointToPosition((int) event.getX(), (int) event.getY());

                if (position == GridView.INVALID_POSITION)
                {
                    if (draggedPiece != null)
                    {
                        // Remove dragged piece and highlighting if dropped outside the board
                        ((ViewGroup) gridView.getParent()).removeView(draggedPiece);
                        draggedPiece = null;
                        if (originalView != null)
                        {
                            originalView.setVisibility(View.VISIBLE);
                        }
                        adapter.setDraggingPosition(-1);
                        game.getBoard().setPieceInPosition(heldPiece, heldPosition); // Put the held piece back in
                        adapter.notifyDataSetChanged();
                    }
                    adapter.setHighlightedPosition(-1);
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
                        heldPosition = Board.BOARD_SIZE * Board.BOARD_SIZE - 1 - position;
                        adapter.setDraggingPosition(heldPosition); // Reversing the position to account for the reversed board
                        heldPiece = (Piece) adapter.getItem(heldPosition);
                        if (heldPiece != null)
                        {
                            originalView = gridView.getChildAt(position);
                            if (originalView != null && originalView instanceof ImageView)
                            {
                                ((ImageView) originalView).setImageDrawable(null); // Remove the image from the original view
                            }
                            draggedPiece = new ImageView(LocalPvPActivity.this);
                            draggedPiece.setImageResource(heldPiece.getPictureID()); // Get the correct image for the piece
                            draggedPiece.setLayoutParams(new GridView.LayoutParams(140, 140));
                            draggedPiece.setVisibility(View.INVISIBLE);
                            ((ViewGroup) gridView.getParent()).addView(draggedPiece);
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        adapter.setHighlightedPosition(position);
                        game.getBoard().setPieceInPosition(null, heldPosition);
                        adapter.notifyDataSetChanged(); // Update the board if move was successful.

                        if (draggedPiece != null)
                        {
                            draggedPiece.setVisibility(View.VISIBLE);
                            draggedPiece.setX(event.getRawX() - draggedPiece.getWidth() / 2);
                            draggedPiece.setY(event.getRawY() - (draggedPiece.getHeight()));
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        // Attempt to make a move from the start position to the end position.
                        adapter.setDraggingPosition(-1);
                        adapter.setHighlightedPosition(-1);
                        game.getBoard().setPieceInPosition(heldPiece, heldPosition); // Put the held piece back in for board evaluations
                        adapter.notifyDataSetChanged();

                        if (draggedPiece != null)
                        {
                            ((ViewGroup) gridView.getParent()).removeView(draggedPiece);
                            draggedPiece = null;
                        }
                        if (originalView != null && originalView instanceof ImageView)
                        {
                            Piece originalPiece = (Piece) adapter.getItem(position);
                            if (originalPiece != null)
                            {
                                ((ImageView) originalView).setImageResource(originalPiece.getPictureID()); // Restore the image
                            }
                            originalView = null;
                        }
                        if (game.makeMove(startX, startY, flippedRow, flippedCol))
                        {
                            adapter.notifyDataSetChanged(); // Update the board if move was successful.
                            updateTurnIndicators(); // Update turn indicator after a successful move.
                        }

                        // Reset the start positions for the next move.
                        startX = -1;
                        startY = -1;

                        // Check if the game has ended and move to the end screen if so.
                        if (!game.isActive())
                        {
                            Intent intent = new Intent(LocalPvPActivity.this, LocalEndScreenActivity.class);
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

        // Set up the forfeit buttons
        Button buttonBackReversed = findViewById(R.id.forfeitButtonReversed);
        buttonBackReversed.setOnClickListener(view -> showBackConfirmationDialog(userId, Game.BLACK_STRING));

        Button buttonBack = findViewById(R.id.forfeitButton);
        buttonBack.setOnClickListener(view -> showBackConfirmationDialog(userId, Game.WHITE_STRING));
    }

    /**
     * Shows a confirmation dialog to confirm if the user wants to forfeit.
     *
     * @param userId The user ID to be passed back to the main menu.
     * @param color The color of the forfeiting user.
     */
    private void showBackConfirmationDialog(String userId, String color) {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Confirm")
                .setMessage("Are you sure you want to forfeit?")
                .setPositiveButton(android.R.string.yes, (dialogInterface, which) -> forfeit(userId, color))
                .setNeutralButton(android.R.string.no, null)
                .setIcon(R.drawable.logo)
                .show();

        // Set the text color of the dialog buttons to black
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.BLACK);
    }

    /**
     * Navigates back to the main menu, passing the user ID via SharedPreferences.
     *
     * @param userId The user ID to be passed back to the main menu.
     */
    private void forfeit(String userId, String color)
    {
        game.forfeitGame(color);
        Intent intent = new Intent(LocalPvPActivity.this, LocalEndScreenActivity.class);
        intent.putExtra("winner", game.getBoard().checkGameStatus()); // Pass the game result.
        intent.putExtra("userId", userId);
        startActivity(intent);
        finish();
    }

    /**
     * Updates the turn indicators based on the current player's turn.
     */
    private void updateTurnIndicators()
    {
        String turnText = "Current Turn: " + (game.getBoard().getTurn() ? "Black" : "White");
        turnIndicator.setText(turnText);
        turnIndicator2.setText(turnText);
    }
}
