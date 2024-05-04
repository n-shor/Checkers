package com.example.checkersnadav;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;


public class OnlinePvPActivity extends AppCompatActivity {
    private OnlineGame game;
    private GridView gridView;
    private CheckersAdapter adapter;
    private String playerColor;
    private String player1Id;
    private String player2Id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_pvp);

        // Retrieving data from intent
        playerColor = getIntent().getStringExtra("playerColor");
        player1Id = getIntent().getStringExtra("player1Id");
        player2Id = getIntent().getStringExtra("player2Id");
        String gameId = getIntent().getStringExtra("gameId");

        // Initialize game object
        game = new OnlineGame(gameId, playerColor, player1Id, player2Id);

        gridView = findViewById(R.id.grid_view);
        adapter = new CheckersAdapter(this, game.getBoard().getState(), !playerColor.equals(Game.WHITE_STRING));
        gridView.setAdapter(adapter);

        game.setAdapter(adapter);

        setupTouchListeners();

        DatabaseReference gameRef = FirebaseDatabase.getInstance().getReference("games");

        gameRef.child(gameId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Extract player's statistics
                    Boolean isGameActive = dataSnapshot.child("isActive").getValue(Boolean.class);

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
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(OnlinePvPActivity.this,"Failed to fetch player's statistics: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    @SuppressLint("ClickableViewAccessibility")
    private void setupTouchListeners() {
        gridView.setOnTouchListener(new View.OnTouchListener()
        {
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

                // Adjust for any GUI board flipping like in the local PvP, but only for the white player
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
                            adapter.notifyDataSetChanged();
                        }
                        else
                        {
                            Toast.makeText(OnlinePvPActivity.this, "Invalid Move, " + (game.isActive() ? "(" + startX + ", " + startY + "), (" + row + ", " + col + ")" : "Game Over! Winner: " + game.getBoard().getWinner()), Toast.LENGTH_SHORT).show();
                        }
                        startX = -1;
                        startY = -1;

                        break;
                }
                return true;
            }
        });
    }

}
