package com.example.checkersnadav;

import android.util.Log;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

/**
 * Extends the Game class to add online multiplayer capabilities using Firebase.
 * This class handles synchronization of the game state with a Firebase backend,
 * ensuring that all moves made by players are updated in real-time across different sessions.
 */
public class OnlineGame extends Game
{
    private String whiteEmail;
    private String blackEmail;
    private DatabaseReference gameRef;
    private final String playerColor; // "WHITE" or "BLACK"
    private CheckersAdapter adapter;

    /**
     * Constructor for OnlineGame. Initializes the game board and sets up Firebase
     * synchronization based on the provided game ID and the player's assigned color.
     *
     * @param gameId The unique identifier for the game session on Firebase.
     * @param playerColor The color assigned to the player ("WHITE" or "BLACK").
     * @param whiteEmail The email of the player that is using the white pieces.
     * @param blackEmail The email of the player that is using the black pieces.
     */
    public OnlineGame(String gameId, String playerColor, String whiteEmail, String blackEmail)
    {
        super(); // Initializes the board and sets the game to active
        this.whiteEmail = whiteEmail;
        this.blackEmail = blackEmail;
        this.playerColor = playerColor;
        setupFirebase(gameId);
    }

    /**
     * Sets up the Firebase database reference and establishes a listener to handle
     * updates to the game state from the database, reflecting changes made by the other player.
     *
     * @param gameId The unique identifier for the game session on Firebase.
     */
    private void setupFirebase(String gameId)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        gameRef = database.getReference("games").child(gameId);

        // Listen for changes in the database, which indicate the other player's move
        gameRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                String boardState = dataSnapshot.child("boardState").getValue(String.class);
                String currentTurn = dataSnapshot.child("currentTurn").getValue(String.class);
                board.setTurn(!Objects.equals(currentTurn, Game.WHITE_STRING)); // White is false

                // Making sure the integers are initialized in Firebase before reading them
                if (dataSnapshot.child("lastMoveX").getValue() != null
                        && dataSnapshot.child("lastMoveY").getValue() != null
                        && dataSnapshot.child("movesSinceCaptureOrKing").getValue() != null)
                {
                    board.setLastMoveX(dataSnapshot.child("lastMoveX").getValue(Integer.class));
                    board.setLastMoveY(dataSnapshot.child("lastMoveY").getValue(Integer.class));
                    board.setMovesSinceCaptureOrKing(dataSnapshot.child("movesSinceCaptureOrKing").getValue(Integer.class));
                }

                // Making sure the board is already initialized in Firebase, if not we'll just update it next time
                if (boardState != null)
                {
                    updateLocalBoardState(boardState);
                    adapter.updateGameState(board.getState());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.e("OnlineGame", "Failed to read game data: " + databaseError.toException());
            }
        });

        // Making sure that Firebase will only get initialized once
        if (Objects.equals(playerColor, Game.WHITE_STRING))
        {
            // Initialize the board
            gameRef.child("whiteEmail").setValue(whiteEmail);
            gameRef.child("blackEmail").setValue(blackEmail);
            updateGameStateInFirebase();
        }
    }

    /**
     * Attempts to make a move on the board. Validates if the move is by the correct player
     * and updates the board state in Firebase if the move is valid.
     *
     * @param xSrc Source x-coordinate.
     * @param ySrc Source y-coordinate.
     * @param xDst Destination x-coordinate.
     * @param yDst Destination y-coordinate.
     * @return true if the move was successful, false otherwise.
     */
    @Override
    public boolean makeMove(int xSrc, int ySrc, int xDst, int yDst)
    {
        if (!isValidPlayerMove(xSrc, ySrc))
        {
            return false;
        }

        boolean success = super.makeMove(xSrc, ySrc, xDst, yDst);
        if (success)
        {
            updateGameStateInFirebase();
        }
        return success;
    }

    /**
     * Checks if the move is valid for the current player, ensuring they are moving their own pieces.
     *
     * @param xSrc Source x-coordinate.
     * @param ySrc Source y-coordinate.
     * @return true if the player is moving their own piece, false otherwise.
     */
    private boolean isValidPlayerMove(int xSrc, int ySrc)
    {
        Piece piece = board.getState()[xSrc][ySrc];
        if (piece == null)
        {
            return false;
        }
        return (piece.isBlack() && !playerColor.equals(Game.WHITE_STRING)) || (!piece.isBlack() && playerColor.equals(Game.WHITE_STRING));
    }

    /**
     * Updates the Firebase database with the current state of the game board.
     */
    private void updateGameStateInFirebase()
    {
        gameRef.child("boardState").setValue(serializeBoardState());
        gameRef.child("currentTurn").setValue(board.getTurn() ? "BLACK" : Game.WHITE_STRING);
        gameRef.child("lastMoveX").setValue(board.getLastMoveX());
        gameRef.child("lastMoveY").setValue(board.getLastMoveY());
        gameRef.child("movesSinceCaptureOrKing").setValue(board.getMovesSinceCaptureOrKing());
    }

    /**
     * Updates the local board state based on the serialized state from Firebase.
     *
     * @param boardState The serialized string representation of the game board.
     */
    private void updateLocalBoardState(String boardState)
    {
        deserializeBoardState(boardState);
    }

    /**
     * Serializes the current state of the game board into a string format for storage in Firebase.
     *
     * @return A string representation of the game board.
     */
    private String serializeBoardState()
    {
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < Board.BOARD_SIZE; y++)
        {
            for (int x = 0; x < Board.BOARD_SIZE; x++)
            {
                Piece piece = board.getState()[x][y];
                if (piece == null)
                {
                    sb.append('_');
                }
                else
                {
                    if (piece.isBlack())
                    {
                        sb.append(piece.isKing() ? 'K' : 'P');
                    }
                    else
                    {
                        sb.append(piece.isKing() ? 'k' : 'p');
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * Deserializes the board state from a string representation and applies it to the local game board.
     *
     * @param boardState The serialized string representation of the game board.
     */
    public void deserializeBoardState(String boardState)
    {
        int index = 0;
        for (int y = 0; y < Board.BOARD_SIZE; y++)
        {
            for (int x = 0; x < Board.BOARD_SIZE; x++)
            {
                char ch = boardState.charAt(index++);
                switch (ch)
                {
                    case '_':
                        board.getState()[x][y] = null;
                        break;
                    case 'p':
                        board.getState()[x][y] = new Piece(false); // White piece
                        break;
                    case 'k':
                        board.getState()[x][y] = new Piece(false, true); // White king
                        break;
                    case 'P':
                        board.getState()[x][y] = new Piece(true); // Black piece
                        break;
                    case 'K':
                        board.getState()[x][y] = new Piece(true, true); // Black king
                        break;
                    default:
                        throw new IllegalArgumentException("Unexpected character in board state: " + ch);
                }
            }
        }
    }


    public void setAdapter(CheckersAdapter adapter) {
        this.adapter = adapter;
    }
}
