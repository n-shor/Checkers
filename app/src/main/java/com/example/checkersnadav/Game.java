package com.example.checkersnadav;

public class Game {
    protected Player white;
    protected Player black;
    protected Board board;
    private boolean gameActive;
    Player currentPlayer;

    public Game() {
        board = new Board();
        gameActive = true;
        currentPlayer = white; // White starts in checkers
    }

    public Board getBoard() {
        return board;
    }

    /**
     * Executes a move if it is valid, and switches turns.
     * @param xSrc The x-coordinate of the piece to move.
     * @param ySrc The y-coordinate of the piece to move.
     * @param xDst The x-coordinate of the destination.
     * @param yDst The y-coordinate of the destination.
     * @return true if the move was successful, false otherwise.
     */
    public boolean makeMove(int xSrc, int ySrc, int xDst, int yDst)
    {
        if (!gameActive)
        {
            return false;
        }
        if (board.move(xSrc, ySrc, xDst, yDst))
        {
            switchTurns();
            if (board.checkGameStatus() != "NONE")
            {
                gameActive = false;
            }
            return true;
        }

        return false;
    }

    /**
     * Switches the current player after a move is made.
     */
    private void switchTurns() {
        currentPlayer = (currentPlayer == white) ? black : white;
    }

    /**
     * Determines if the game is still active.
     * @return true if the game is active, false if it has ended.
     */
    public boolean isGameActive() {
        return gameActive;
    }

}
