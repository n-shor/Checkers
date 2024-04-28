package com.example.checkersnadav;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

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

        // Assume gameId and playerColor are retrieved from an intent or similar
        gameId = getIntent().getStringExtra("GAME_ID");
        playerColor = getIntent().getStringExtra("PLAYER_COLOR");

        game = new OnlineGame(gameId, playerColor);
        gridView = findViewById(R.id.grid_view);
        adapter = new CheckersAdapter(this, game.getBoard().getState());
        gridView.setAdapter(adapter);

        setupTouchListeners();
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
