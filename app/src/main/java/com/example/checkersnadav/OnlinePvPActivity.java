package com.example.checkersnadav;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

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

                    tvTop.setText(opponentName); // Display opponent name at the top
                    tvBottom.setText(playerName); // Display player name at the bottom
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
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Toast.makeText(OnlinePvPActivity.this, "Failed to fetch player's statistics: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Sets up touch listeners on the grid view to detect player moves.
     */
    private void setupTouchListeners()
    {
        gridView.setOnTouchListener(new View.OnTouchListener()
        {
            // Starting in an invalid position to avoid bugs, just to be safe
            private int startX = -1;
            private int startY = -1;

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                int action = event.getActionMasked();
                int position = gridView.pointToPosition((int) event.getX(), (int) event.getY());

                if (position == GridView.INVALID_POSITION)
                {
                    return false;
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
                        startX = row;
                        startY = col;
                        break;
                    case MotionEvent.ACTION_UP:
                        if (game.makeMove(startX, startY, row, col))
                        {
                            // Update the GUI of the board
                            adapter.notifyDataSetChanged();
                            // Updating the turn indicator based on the local state of the game
                            String turnText = "Current Turn: " + (game.getBoard().getTurn() ? "Black" : "White");
                            turnIndicator.setText(turnText);
                        }
                        else
                        {
                            Toast.makeText(OnlinePvPActivity.this, "Invalid Move", Toast.LENGTH_SHORT).show();
                        }
                        // Resetting the move
                        startX = -1;
                        startY = -1;
                        break;
                }
                return true;
            }
        });
    }
}
