package com.example.checkersnadav;

class Piece {
    private final boolean color;
    private boolean isKing;

    public Piece(boolean color) {
        this.color = color;
        this.isKing = false;
    }

    public boolean isBlack() {
        return color;
    }

    public int getPictureID()
    {
        if (isKing())
        {
            if (isBlack())
            {
                return R.drawable.black_king;
            }

            return R.drawable.white_king;
        }

        if (isBlack())
        {
            return R.drawable.black_piece;
        }

        return R.drawable.white_piece;

    }

    public boolean isKing() {
        return isKing;
    }

    public void setKing(boolean isKing) {
        this.isKing = isKing;
    }
}