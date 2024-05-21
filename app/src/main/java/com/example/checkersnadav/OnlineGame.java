package com.example.checkersnadav;

import static com.example.checkersnadav.Statistics.Outcomes.DRAW;
import static com.example.checkersnadav.Statistics.Outcomes.LOSS;
import static com.example.checkersnadav.Statistics.Outcomes.WIN;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Extends the Game class to add online multiplayer capabilities using Firebase.
 * This class handles synchronization of the game state with a Firebase backend,
 * ensuring that all moves made by players are updated in real-time across different sessions.
 */
public class OnlineGame extends Game {
    private final String whiteId; // Firebase user ID for the player using white pieces.
    private final String blackId; // Firebase user ID for the player using black pieces.
    private final String gameId; // Unique game identifier for the Firebase database.
    private DatabaseReference gameRef; // Firebase database reference for this game.
    private final String playerColor; // "WHITE" or "BLACK", indicates the player's color.
    private int playerMoves; // Counter for the player's moves.
    private CheckersAdapter adapter; // Adapter to interact with the board view.

    /**
     * Constructor for OnlineGame. Initializes the game board and sets up Firebase
     * synchronization based on the provided game ID and the player's assigned color.
     *
     * @param gameId The unique identifier for the game session on Firebase.
     * @param playerColor The color assigned to the player ("WHITE" or "BLACK").
     * @param whiteId The email of the player that is using the white pieces.
     * @param blackId The email of the player that is using the black pieces.
     */
    public OnlineGame(String gameId, String playerColor, String whiteId, String blackId)
    {
        super(); // Initializes the board and sets the game to active
        this.whiteId = whiteId;
        this.blackId = blackId;
        this.playerColor = playerColor;
        this.gameId = gameId;
        playerMoves = 0;
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
                if (dataSnapshot.exists())
                {
                    // Sync the board state based on Firebase
                    String boardState = dataSnapshot.child("boardState").getValue(String.class);
                    // Sync the game turn based on Firebase
                    String currentTurn = dataSnapshot.child("currentTurn").getValue(String.class);
                    board.setTurn(!Objects.equals(currentTurn, Game.WHITE_STRING)); // White is false

                    if (dataSnapshot.hasChild("lastMoveX") && dataSnapshot.hasChild("lastMoveY") &&
                            dataSnapshot.hasChild("movesSinceCaptureOrKing") && dataSnapshot.hasChild("isActive") &&
                            boardState != null)
                    {
                        // Update internal state tracking based on Firebase data
                        board.setLastMoveX(dataSnapshot.child("lastMoveX").getValue(Integer.class));
                        board.setLastMoveY(dataSnapshot.child("lastMoveY").getValue(Integer.class));
                        board.setMovesSinceCaptureOrKing(dataSnapshot.child("movesSinceCaptureOrKing").getValue(Integer.class));
                        updateLocalBoardState(boardState);
                        adapter.updateGameState(board.getState());

                        if (!dataSnapshot.child("isActive").getValue(Boolean.class))
                        {
                            isActive = false;
                        }
                    }

                    // Handle game ending
                    if (!isActive)
                    {
                        finishGame();
                    }
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
            gameRef.child("whiteId").setValue(whiteId);
            gameRef.child("blackId").setValue(blackId);
            gameRef.child("isActive").setValue(true);
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
            playerMoves++;
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
        gameRef.child("currentTurn").setValue(board.getTurn() ? Game.BLACK_STRING : Game.WHITE_STRING);
        gameRef.child("lastMoveX").setValue(board.getLastMoveX());
        gameRef.child("lastMoveY").setValue(board.getLastMoveY());
        gameRef.child("movesSinceCaptureOrKing").setValue(board.getMovesSinceCaptureOrKing());
        gameRef.child("isActive").setValue(isActive);
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
     * Uppercase letters signify black pieces, while lowercase letters signify white pieces.
     * The letter 'p' signifies a normal piece, and the letter 'k' signifies a king.
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
     * Uppercase letters signify black pieces, while lowercase letters signify white pieces.
     * The letter 'p' signifies a normal piece, and the letter 'k' signifies a king.
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

    /**
     * Determines the outcome of the game based on the board's status.
     * @return Outcome of the game (WIN, LOSS, DRAW).
     */
    private Statistics.Outcomes determineOutcome()
    {
        if (Objects.equals(board.checkGameStatus(), playerColor))
        {
            return WIN;
        }
        else if (Objects.equals(board.checkGameStatus(), Game.DRAW_STRING))
        {
            return DRAW;
        }
        else
        {
            return LOSS;
        }
    }

    /**
     * Set the adapter for updating the UI based on game state changes.
     * @param adapter CheckersAdapter for the game board UI.
     */
    public void setAdapter(CheckersAdapter adapter)
    {
        this.adapter = adapter;
    }

    /**
     * Concludes the game by deleting Firebase records and updating player statistics.
     * It also ensures the final game state is consistent across devices.
     */
    public void finishGame()
    {
        String playerId = playerColor.equals(Game.WHITE_STRING) ? whiteId : blackId;

        // References to the games and rooms in Firebase
        DatabaseReference gamesRef = FirebaseDatabase.getInstance().getReference("games");
        DatabaseReference roomsRef = FirebaseDatabase.getInstance().getReference("rooms");

        // Delete the game and room from Firebase, using game ID
        gamesRef.child(gameId).removeValue();
        roomsRef.child(gameId).removeValue();

        // Reference to the users in Firebase
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        // Fetch player details and update statistics
        usersRef.child(playerId).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    // Extract and update player statistics
                    Statistics stats = dataSnapshot.child("stats").getValue(Statistics.class);
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", new Locale("he", "IL"));
                    sdf.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));
                    String todayInIsrael = sdf.format(new Date());

                    // Determine if the player earns a daily bonus
                    boolean hasDailyBonus = !todayInIsrael.equals(dataSnapshot.child("lastWinDate").getValue(String.class));
                    Statistics.Outcomes outcome = determineOutcome();

                    // Update statistics based on game outcome
                    stats.updateStatistics(outcome, playerMoves, hasDailyBonus);
                    usersRef.child(playerId).child("stats").setValue(stats);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Log.e("Failed to fetch player info", databaseError.getMessage());
            }
        });
    }
}
