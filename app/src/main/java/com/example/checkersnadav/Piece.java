package com.example.checkersnadav;

/**
 * Represents a single checkers piece with attributes to determine its color and whether it's a king.
 */
class Piece
{
    private final boolean color;  // true for black, false for white
    private boolean isKing;       // true if the piece is promoted to a king

    /**
     * Constructs a new Piece with the specified color.
     * Initially, the piece is not a king.
     *
     * @param color true if the piece is black, false if white
     */
    public Piece(boolean color)
    {
        this.color = color;
        this.isKing = false;
    }

    /**
     * Constructs a new Piece with specified color and king status.
     *
     * @param color true if the piece is black, false if white
     * @param isKing true if the piece is a king, otherwise false
     */
    public Piece(boolean color, boolean isKing)
    {
        this.color = color;
        this.isKing = isKing;
    }

    /**
     * Returns whether the piece is black.
     *
     * @return true if the piece is black, false if white
     */
    public boolean isBlack()
    {
        return color;
    }

    /**
     * Retrieves the drawable resource ID for this piece based on its color and king status.
     *
     * @return the drawable resource ID corresponding to this piece's appearance
     */
    public int getPictureID()
    {
        if (isKing())
        {
            return isBlack() ? R.drawable.black_king : R.drawable.white_king;
        }
        return isBlack() ? R.drawable.black_piece : R.drawable.white_piece;
    }

    /**
     * Returns whether this piece is a king.
     *
     * @return true if this piece is a king, otherwise false
     */
    public boolean isKing()
    {
        return isKing;
    }

    /**
     * Sets the king status of this piece.
     *
     * @param isKing true if the piece should be a king, otherwise false
     */
    public void setKing(boolean isKing)
    {
        this.isKing = isKing;
    }
}
