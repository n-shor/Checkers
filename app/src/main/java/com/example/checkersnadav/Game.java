package com.example.checkersnadav;


import java.util.Objects;

public class Game {
    protected Board board;
    protected boolean isActive;
    public static final String WHITE_STRING = "WHITE";
    public static final String BLACK_STRING = "BLACK";
    public static final String DRAW_STRING = "DRAW";
    public static final String NONE_STRING = "NONE";


    public Game() {
        board = new Board();
        isActive = true;
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
        if (!isActive)
        {
            return false;
        }
        if (board.move(xSrc, ySrc, xDst, yDst))
        {
            if (!Objects.equals(board.checkGameStatus(), Game.NONE_STRING))
            {
                isActive = false;
            }
            return true;
        }

        return false;
    }



    /**
     * Determines if the game is still active.
     * @return true if the game is active, false if it has ended.
     */
    public boolean isActive() {
        return isActive;
    }

}
