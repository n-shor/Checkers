package com.example.checkersnadav;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class OnlinePvPActivity extends AppCompatActivity {
    private OnlineGame game;
    private GridView gridView;
    private CheckersAdapter adapter;
    private String gameId;
    private String playerColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_pv_pactivity);

        // Retrieving data from intent
        String playerColor = getIntent().getStringExtra("playerColor");
        String player1Email = getIntent().getStringExtra("player1Email");
        String player2Email = getIntent().getStringExtra("player2Email");
        String roomId = getIntent().getStringExtra("roomId");

        // Firebase reference to the games node
        DatabaseReference gameRef = FirebaseDatabase.getInstance().getReference("games").child(roomId);

        if (playerColor.equals("WHITE")) {
            // This device belongs to the room owner (white player)
            initializeGameInFirebase(gameRef, player1Email, player2Email, roomId);
        } else {
            // This device belongs to the black player
            waitForGameToBeInitialized(gameRef);
        }

        gridView = findViewById(R.id.grid_view);
        adapter = new CheckersAdapter(this, null); // Initially, we don't have a game state
        gridView.setAdapter(adapter);

        setupTouchListeners();
    }

    private void initializeGameInFirebase(DatabaseReference gameRef, String player1Email, String player2Email, String gameId) {
        // Initialize game object

        final Player[] player1 = new Player[1];
        final Player[] player2 = new Player[1];

        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        usersRef.orderByChild("email").equalTo(player1Email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    DataSnapshot userSnapshot = dataSnapshot.getChildren().iterator().next();
                    player1[0] = userSnapshot.getValue(Player.class); //Variable 'player1' is accessed from within inner class, needs to be final or effectively final
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(OnlinePvPActivity.this, "Failed to fetch player details", Toast.LENGTH_SHORT).show();
            }
        });

        usersRef.orderByChild("email").equalTo(player2Email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    DataSnapshot userSnapshot = dataSnapshot.getChildren().iterator().next();
                    player2[0] = userSnapshot.getValue(Player.class); //Variable 'player2' is accessed from within inner class, needs to be final or effectively final
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(OnlinePvPActivity.this, "Failed to fetch player details", Toast.LENGTH_SHORT).show();
            }
        });

        OnlineGame game = new OnlineGame(gameId, "WHITE", player1[0], player2[0]);
        gameRef.setValue(game.serialize()); // Serialize and set the initial game state
        setupGameStateListener(gameRef); // Listen for updates to the game state
    }

    private void waitForGameToBeInitialized(DatabaseReference gameRef) {
        // Wait for the game to be initialized by the white player
        gameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    OnlineGame game = OnlineGame.deserialize(dataSnapshot.getValue(String.class));
                    adapter.updateGameState(game.getBoard().getState());
                    gridView.setAdapter(adapter);
                    setupGameStateListener(gameRef); // Once game is initialized, start listening for updates
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(OnlinePvPActivity.this, "Failed to find game: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupGameStateListener(DatabaseReference gameRef) {
        gameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    OnlineGame game = OnlineGame.deserialize(dataSnapshot.getValue(String.class));
                    adapter.updateGameState(game.getBoard().getState());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(OnlinePvPActivity.this, "Error updating game: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

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

                // Adjust for any GUI board flipping like in the local PvP
                int logicalRow = Board.BOARD_SIZE - 1 - row;
                int logicalCol = Board.BOARD_SIZE - 1 - col;

                switch (action)
                {
                    case MotionEvent.ACTION_DOWN:
                        startX = logicalRow;
                        startY = logicalCol;
                        break;
                    case MotionEvent.ACTION_UP:
                        if (game.makeMove(startX, startY, logicalRow, logicalCol))
                        {
                            adapter.notifyDataSetChanged();
                        }
                        else
                        {
                            Toast.makeText(OnlinePvPActivity.this, "Invalid Move, " + (game.isGameActive() ? "(" + startX + ", " + startY + "), (" + logicalRow + ", " + logicalCol + ")" : "Game Over! Winner: " + game.getBoard().getWinner()), Toast.LENGTH_SHORT).show();
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
