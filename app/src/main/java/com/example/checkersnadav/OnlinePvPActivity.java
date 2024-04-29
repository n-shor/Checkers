package com.example.checkersnadav;

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
import java.util.concurrent.CountDownLatch;


public class OnlinePvPActivity extends AppCompatActivity {
    private OnlineGame game;
    private GridView gridView;
    private CheckersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_pv_pactivity);

        // Retrieving data from intent
        String playerColor = getIntent().getStringExtra("playerColor");
        String player1Email = getIntent().getStringExtra("player1Email");
        String player2Email = getIntent().getStringExtra("player2Email");
        String gameId = getIntent().getStringExtra("gameId");

        initializeGame(player1Email, player2Email, gameId);

        gridView = findViewById(R.id.grid_view);
        adapter = new CheckersAdapter(this, game.getBoard().getState());
        gridView.setAdapter(adapter);

        setupTouchListeners();
    }


    private void initializeGame(String player1Email, String player2Email, String gameId)
    {
        // Initialize game object
        game = new OnlineGame(gameId, "WHITE", player1Email, player2Email); // The initializer is always WHITE
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
