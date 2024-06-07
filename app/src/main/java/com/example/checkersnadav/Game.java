package com.example.checkersnadav;

import java.util.Objects;

/**
 * Represents a game of checkers, managing the game board and state.
 */
public class Game
{
    protected Board board;  // The game board containing the pieces and state.
    protected boolean isActive;  // Indicates whether the game is currently active.
    public static final String WHITE_STRING = "WHITE";  // Constant for representing the white player.
    public static final String BLACK_STRING = "BLACK";  // Constant for representing the black player.
    public static final String DRAW_STRING = "DRAW";  // Constant for a draw outcome.
    public static final String NONE_STRING = "NONE";  // Constant for no specific outcome.

    /**
     * Constructs a new Game instance initializing the board and setting the game as active.
     */
    public Game()
    {
        board = new Board();  // Initialize the game board.
        isActive = true;  // Set the game as active.
    }

    /**
     * Retrieves the game board.
     * @return the current state of the board.
     */
    public Board getBoard()
    {
        return board;
    }

    /**
     * Attempts to make a move on the board from a source position to a destination.
     * It validates the move with the board, updates the game status, and toggles player turns.
     *
     * @param xSrc The x-coordinate of the piece to move.
     * @param ySrc The y-coordinate of the piece to move.
     * @param xDst The x-coordinate of the destination.
     * @param yDst The y-coordinate of the destination.
     * @return true if the move was successful, false otherwise.
     */
    public boolean makeMove(int xSrc, int ySrc, int xDst, int yDst)
    {
        if (!isActive)
        {
            return false;  // Return false if the game is no longer active.
        }
        if (board.move(xSrc, ySrc, xDst, yDst))
        {
            // Check if the move has changed the game status.
            if (!Objects.equals(board.checkGameStatus(), Game.NONE_STRING))
            {
                isActive = false;  // Set game as inactive if the status is not NONE.
            }
            return true;  // Move was successful.
        }

        return false;  // Move was not successful.
    }

    /**
     * Checks if the game is currently active.
     * @return true if the game is ongoing, false if it has concluded.
     */
    public boolean isActive()
    {
        return isActive;
    }

    /**
     * Forfeits the game for the player of the given color.
     *
     * @param color The player's color.
     */
    public void forfeitGame(String color)
    {
        board.forfeit(color);
        isActive = false;
    }
}
