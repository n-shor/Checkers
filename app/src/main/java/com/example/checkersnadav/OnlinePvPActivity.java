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
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

/**
 * This activity manages an online Player vs. Player game of checkers.
 * It initializes the game board, sets up Firebase listeners to synchronize game state across devices,
 * and updates the UI based on game progress.
 */
public class OnlinePvPActivity extends AppCompatActivity
{
    private OnlineGame game;
    private GridView gridView;
    private CheckersAdapter adapter;
    private String playerColor;
    private String player1Id;
    private String player2Id;
    private TextView tvTop;
    private TextView tvBottom;
    private TextView turnIndicator;

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
        setContentView(R.layout.activity_online_pvp);

        // Retrieve player information and game ID from intent
        playerColor = getIntent().getStringExtra("playerColor");
        player1Id = getIntent().getStringExtra("player1Id");
        player2Id = getIntent().getStringExtra("player2Id");
        String gameId = getIntent().getStringExtra("gameId");

        // Setup the game environment
        game = new OnlineGame(gameId, playerColor, player1Id, player2Id);

        // Setup TextViews for displaying player names and current turn
        tvTop = findViewById(R.id.tv_top);
        tvBottom = findViewById(R.id.tv_bottom);
        turnIndicator = findViewById(R.id.turn_indicator);

        turnIndicator.setText("Current Turn: White");

        // Firebase reference to user details
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    String opponentName = dataSnapshot.child(!playerColor.equals(Game.WHITE_STRING) ? player1Id : player2Id).child("username").getValue(String.class);
                    String playerName = dataSnapshot.child(playerColor.equals(Game.WHITE_STRING) ? player1Id : player2Id).child("username").getValue(String.class);

                    int opponentElo = dataSnapshot.child(!playerColor.equals(Game.WHITE_STRING) ? player1Id : player2Id).child("stats").child("elo").getValue(Integer.class);
                    int playerElo = dataSnapshot.child(playerColor.equals(Game.WHITE_STRING) ? player1Id : player2Id).child("stats").child("elo").getValue(Integer.class);

                    tvTop.setText(opponentName + " (" + opponentElo + ")"); // Display opponent name and elo at the top
                    tvBottom.setText(playerName + " (" + playerElo + ")"); // Display player name and elo at the bottom
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Toast.makeText(OnlinePvPActivity.this, "Failed to fetch players' names: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Setup the game board view
        gridView = findViewById(R.id.grid_view);
        adapter = new CheckersAdapter(this, game.getBoard().getState(), !playerColor.equals(Game.WHITE_STRING));
        gridView.setAdapter(adapter);
        game.setAdapter(adapter);

        // Setup touch listeners for making moves
        setupTouchListeners();

        // Monitor game status changes
        DatabaseReference gamesRef = FirebaseDatabase.getInstance().getReference("games");
        gamesRef.child(gameId).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    // Update the current turn on the board based on the Firebase data
                    String turnText = "Current Turn: " + (Game.WHITE_STRING.equals(dataSnapshot.child("currentTurn").getValue(String.class)) ? "White" : "Black");
                    turnIndicator.setText(turnText);

                    Boolean isGameActive = dataSnapshot.child("isActive").getValue(Boolean.class);

                    // Check if the game has ended
                    if (isGameActive != null && !isGameActive)
                    {
                        moveToResultsScreen();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Toast.makeText(OnlinePvPActivity.this, "Failed to fetch player's statistics: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Set up the forfeit button
        Button forfeitButton = findViewById(R.id.forfeitButton);
        forfeitButton.setOnClickListener(view -> showForfeitConfirmationDialog());
    }

    /**
     * Shows a confirmation dialog to confirm if the user wants to forfeit the game.
     */
    private void showForfeitConfirmationDialog()
    {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Confirm")
                .setMessage("Are you sure you want to forfeit the game?")
                .setPositiveButton(android.R.string.yes, (dialogInterface, which) -> forfeitGame())
                .setNeutralButton(android.R.string.no, null)
                .setIcon(R.drawable.logo) // Set the icon to the app's logo
                .show();

        // Set the text color of the dialog buttons to black
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setTextColor(Color.BLACK);
    }

    /**
     * Forfeits the game.
     */
    private void forfeitGame()
    {
        game.forfeitGame(playerColor);
        moveToResultsScreen();
    }

    /**
     * Moves to the relevant result screen based on the player's result in the game that just ended.
     */
    private void moveToResultsScreen()
    {
        Intent intent;
        if (Objects.equals(game.getBoard().checkGameStatus(), playerColor))
        {
            intent = new Intent(OnlinePvPActivity.this, WinScreen.class);
        }
        else if (Objects.equals(game.getBoard().checkGameStatus(), Game.DRAW_STRING))
        {
            intent = new Intent(OnlinePvPActivity.this, DrawScreen.class);
        }
        else
        {
            intent = new Intent(OnlinePvPActivity.this, LoseScreen.class);
        }

        intent.putExtra("userId", playerColor.equals(Game.WHITE_STRING) ? player1Id : player2Id);
        startActivity(intent);
        finish();
    }

    /**
     * Sets up touch listeners on the grid view to detect player moves.
     */
    private void setupTouchListeners()
    {
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
                        game.getBoard().setPieceInPosition(heldPiece, playerColor.equals(Game.WHITE_STRING) ? heldPosition : Board.BOARD_SIZE * Board.BOARD_SIZE - 1 - heldPosition); // Put the held piece back in
                        adapter.notifyDataSetChanged();
                    }
                    adapter.setHighlightedPosition(-1);
                    return false; // Invalid touch position, outside of grid bounds.
                }

                int row = position / Board.BOARD_SIZE;
                int col = position % Board.BOARD_SIZE;

                // Adjust for GUI board flipping for the white player
                if (playerColor.equals(Game.WHITE_STRING))
                {
                    row = Board.BOARD_SIZE - 1 - row;
                    col = Board.BOARD_SIZE - 1 - col;
                }

                switch (action)
                {
                    case MotionEvent.ACTION_DOWN:
                        // Record the start position of a drag (potential move).
                        startX = row;
                        startY = col;
                        heldPosition = Board.BOARD_SIZE * Board.BOARD_SIZE - 1 - position;
                        adapter.setDraggingPosition(playerColor.equals(Game.WHITE_STRING) ? heldPosition : Board.BOARD_SIZE * Board.BOARD_SIZE - 1 - heldPosition); // Reversing the position (if needed) to account for the reversed board
                        heldPiece = (Piece) adapter.getItem(playerColor.equals(Game.WHITE_STRING) ? heldPosition : Board.BOARD_SIZE * Board.BOARD_SIZE - 1 - heldPosition);
                        if (heldPiece != null)
                        {
                            originalView = gridView.getChildAt(position);
                            if (originalView != null && originalView instanceof ImageView)
                            {
                                ((ImageView) originalView).setImageDrawable(null); // Remove the image from the original view
                            }
                            draggedPiece = new ImageView(OnlinePvPActivity.this);
                            draggedPiece.setImageResource(heldPiece.getPictureID()); // Get the correct image for the piece
                            draggedPiece.setLayoutParams(new GridView.LayoutParams(140, 140));
                            draggedPiece.setVisibility(View.INVISIBLE);
                            ((ViewGroup) gridView.getParent()).addView(draggedPiece);
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        adapter.setHighlightedPosition(position);
                        game.getBoard().setPieceInPosition(null, playerColor.equals(Game.WHITE_STRING) ? heldPosition : Board.BOARD_SIZE * Board.BOARD_SIZE - 1 - heldPosition);
                        adapter.notifyDataSetChanged(); // Update the board if move was successful.

                        if (draggedPiece != null)
                        {
                            draggedPiece.setVisibility(View.VISIBLE);
                            draggedPiece.setX(event.getRawX() - draggedPiece.getWidth() / 2);
                            draggedPiece.setY(event.getRawY() - (int)(draggedPiece.getHeight()));
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        // Attempt to make a move from the start position to the end position.
                        adapter.setDraggingPosition(-1);
                        adapter.setHighlightedPosition(-1);
                        game.getBoard().setPieceInPosition(heldPiece, playerColor.equals(Game.WHITE_STRING) ? heldPosition : Board.BOARD_SIZE * Board.BOARD_SIZE - 1 - heldPosition); // Put the held piece back in for board evaluations
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
                        if (game.makeMove(startX, startY, row, col))
                        {
                            adapter.notifyDataSetChanged(); // Update the board if move was successful.
                            String turnText = "Current Turn: " + (game.getBoard().getTurn() ? "Black" : "White");
                            turnIndicator.setText(turnText);
                        }

                        // Reset the start positions for the next move.
                        startX = -1;
                        startY = -1;

                        break;
                }
                return true;
            }
        });
    }
}
