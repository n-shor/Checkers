package com.example.checkersnadav;

/**
 * Represents the game board for a checkers game.
 * This class manages the game state, including the positions of pieces,
 * whose turn it is, and the rules for moving and capturing pieces.
 */
public class Board {
    public static final boolean WHITE = false;
    public static final boolean BLACK = !WHITE;
    public static final int BOARD_SIZE = 8;
    private Piece[][] state;
    private boolean turn;
    private int movesSinceCaptureOrKing = 0;
    private int lastMoveX = BOARD_SIZE - 1; // Making sure these are on a black square initially so the logic won't think that white needs to do a capture chain
    private int lastMoveY = BOARD_SIZE - 2;


    /**
     * Constructs a new Board with pieces in their initial positions for a standard game of checkers.
     */
    public Board() {
        state = new Piece[BOARD_SIZE][BOARD_SIZE];
        turn = WHITE; // White starts in checkers

        // Initialize the board with alternating empty and filled squares
        for (int i = 0; i < BOARD_SIZE; i++)
        {
            for (int j = 0; j < BOARD_SIZE; j++)
            {
                if ((i + j) % 2 != 0)
                {
                    if (i <= 2)
                    {
                        state[i][j] = new Piece(WHITE);
                    }
                    else if (i >= 5)
                    {
                        state[i][j] = new Piece(BLACK);
                    }
                    else
                    {
                        state[i][j] = null;
                    }
                }
                else
                {
                    state[i][j] = null;
                }
            }
        }

    }


    /**
     * Attempts to move a piece from the source to the destination coordinates.
     * It checks if the move is valid, performs the move, captures if applicable,
     * and switches turns if no further captures are possible.
     *
     * @param xSrc Source x-coordinate
     * @param ySrc Source y-coordinate
     * @param xDst Destination x-coordinate
     * @param yDst Destination y-coordinate
     * @return true if the move is successful, false otherwise
     */
    public boolean move(int xSrc, int ySrc, int xDst, int yDst)
    {
        if (!isValidMove(xSrc, ySrc, xDst, yDst))
        {
            return false; // Move is invalid
        }

        Piece piece = state[xSrc][ySrc];
        state[xDst][yDst] = piece;
        state[xSrc][ySrc] = null;

        movesSinceCaptureOrKing++;

        // Handle captures
        // Piece capture
        if (!piece.isKing() && Math.abs(xDst - xSrc) == 2)
        {
            performPieceCapture(xSrc, ySrc, xDst, yDst);

            // Determine if further captures are possible for multi-jump, using the current location of the piece, which is the move's destination
            if (pieceHasMandatoryCapture(xDst, yDst))
            {
                turn = !turn; // Reverse the incoming turn switch
            }

            movesSinceCaptureOrKing = 0; // Reset on capture
        }
        // King capture
        // making sure the path is not clear - meaning there is something to capture
        // (we already know the move is valid so we don't have to worry about isPathClear() not being specific enough)
        // also, it doesn't matter that the piece has moved already because we only check the squares that are in between the source and the destination squares
        else if (!isPathClear(xSrc, ySrc, xDst, yDst))
        {
            performKingCapture(xSrc, ySrc, xDst, yDst);

            // Determine if further captures are possible for multi-jump, using the current location of the king, which is the move's destination
            if (kingHasMandatoryCapture(xDst, yDst))
            {
                turn = !turn; // Reverse the incoming turn switch
            }

            movesSinceCaptureOrKing = 0; // Reset on capture
        }

        // King promotion
        if (!piece.isKing() && ((xDst == 0 && piece.isBlack() == BLACK) ||
                (xDst == BOARD_SIZE - 1 && piece.isBlack() == WHITE)))
        // xDst and not yDst because poor planning led into x coordinates corresponding to rows and not columns
        {
            piece.setKing(true);
            movesSinceCaptureOrKing = 0; // Reset on kinging
        }

        turn = !turn; // Turn switch

        // Updating the last move
        lastMoveX = xDst;
        lastMoveY = yDst;

        return true;
    }


    /**
     * Checks if the specified piece at the given location can make any legal move.
     * @param x The x-coordinate of the piece on the board.
     * @param y The y-coordinate of the piece on the board.
     * @return true if there is at least one valid move available, false otherwise.
     */
    private boolean canMove(int x, int y) {
        Piece piece = state[x][y];

        if (piece == null) {
            return false;
        }

        // Possible directions to check for moves or captures
        int[] possibleMoves = {1, -1};
        for (int dx : possibleMoves) {
            for (int dy : possibleMoves) {
                int newX = x + dx;
                int newY = y + dy;

                // Check simple move
                if (isValidMove(x, y, newX, newY))
                {
                    return true;
                }

                // Check capture
                newX = x + 2 * dx;
                newY = y + 2 * dy;

                if (isValidMove(x, y, newX, newY))
                {
                    return true;
                }

                // Check more moves if the piece is a king
                if (piece.isKing())
                {
                    for (int i = 2; i < BOARD_SIZE; i++)
                    {
                        newX = x + i * dx;
                        newY = y + i * dy;

                        if (isValidMove(x, y, newX, newY))
                        {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }


    /**
     * Determines the winner of the game based on the remaining pieces and possible moves.
     * @return 'BLACK' if black wins, 'WHITE' if white wins, 'DRAW' if the game is a draw, 'NONE' if the game is not over.
     */
    public String getWinner()
    {
        boolean whiteHasPieces = false;
        boolean blackHasPieces = false;
        boolean whiteHasMoves = false;
        boolean blackHasMoves = false;

        // Scan the board to check for pieces and possible moves
        for (int i = 0; i < BOARD_SIZE; i++)
        {
            for (int j = 0; j < BOARD_SIZE; j++)
            {
                Piece piece = state[i][j];
                if (piece != null)
                {
                    if (piece.isBlack())
                    {
                        blackHasPieces = true;
                        if (!blackHasMoves)
                        { // Only check for moves if not already found
                            blackHasMoves = canMove(i, j);
                        }
                    }
                    else
                    {
                        whiteHasPieces = true;
                        if (!whiteHasMoves)
                        { // Only check for moves if not already found
                            whiteHasMoves = canMove(i, j);
                        }
                    }
                }
            }
        }

        // Determine winner based on pieces and moves left
        if (!whiteHasPieces || (!whiteHasMoves && turn == WHITE))
        {
            return "BLACK"; // Black wins if white has no pieces or moves
        }
        if (!blackHasPieces || (!blackHasMoves && turn == BLACK))
        {
            return Game.WHITE_STRING; // White wins if black has no pieces or moves
        }
        return "NONE"; // No winner yet
    }


    /**
     * Checks if the game is over due to a win or a draw.
     * @return 'BLACK' if black wins, 'WHITE' if white wins, 'DRAW' if the game is a draw, 'NONE' if the game is not over.
     */
    public String checkGameStatus()
    {
        String winner = getWinner();
        if (!winner.equals("NONE"))
        {
            return winner;
        }
        if (movesSinceCaptureOrKing >= 80)
        { // 40 moves by each player without capture or kinging
            return "DRAW";
        }
        return "NONE";
    }


    public Piece[][] getState()
    {
        return state;
    }


    /**
     * Checks if the specified move is valid according to the rules of checkers.
     * This includes bounds checking, ensuring the move is to a legal board square,
     * and that the movement is consistent with checkers rules for simple moves and captures.
     *
     * @param xSrc Source x-coordinate
     * @param ySrc Source y-coordinate
     * @param xDst Destination x-coordinate
     * @param yDst Destination y-coordinate
     * @return true if the move is valid, false otherwise
     */
    public boolean isValidMove(int xSrc, int ySrc, int xDst, int yDst)
    {
        // Make sure the move is in the bounds of the board
        if (xDst < 0 || xDst >= BOARD_SIZE || yDst < 0 || yDst >= BOARD_SIZE)
        {
            return false;
        }

        // Make sure the destination of the move is a black square, and that it does not contain another piece
        if ((xDst + yDst) % 2 == 0 || state[xDst][yDst] != null)
        {
            return false;
        }

        Piece piece = state[xSrc][ySrc];

        // Make sure the square that we are attempting to move a piece from contains a piece of the correct color
        if (piece == null || piece.isBlack() != turn)
        {
            return false;
        }

        // If the first half of the condition is true, it means we are in a situation of a chain capture and that only that piece can move
        if (state[lastMoveX][lastMoveY].isBlack() == turn && (lastMoveX != xSrc || lastMoveY != ySrc))
        {
            return false;
        }

        // King logic
        if (piece.isKing())
        {
            // Make sure the king is moving in a diagonal
            if (Math.abs(xDst - xSrc) != Math.abs(yDst - ySrc))
            {
                return false;
            }

            // Check if it's a normal king move
            if (isPathClear(xSrc, ySrc, xDst, yDst))
            {
                // The player must capture if possible
                return !playerHasMandatoryCapture();
            }
            else
            {
                // Check if it's a capture or an invalid move
                return hasOpponentPieceInBetween(xSrc, ySrc, xDst, yDst);
            }
        }
        // Non-king logic
        else
        {
            int dx = Math.abs(xDst - xSrc);
            int dy = Math.abs(yDst - ySrc);

            // Normal move
            if (dx == 1 && dy == 1)
            {
                // The player must capture if possible
                if (playerHasMandatoryCapture())
                {
                    return false;
                }

                // Make sure the piece is going in the right direction
                return (piece.isBlack() && xDst - xSrc == -1) || (!piece.isBlack() && xDst - xSrc == 1);
            }
            // Capture
            else if (dx == 2 && dy == 2)
            {
                // Make sure the capture is possible
                return hasOpponentPieceInBetween(xSrc, ySrc, xDst, yDst);
            }
        }

        return false;
    }


    /**
     * Performs the capture of an opponent's piece located between the source and destination squares.
     * The captured piece is removed from the board.
     */
    private void performPieceCapture(int xSrc, int ySrc, int xDst, int yDst)
    {
        int capturedX = (xSrc + xDst) / 2;
        int capturedY = (ySrc + yDst) / 2;
        state[capturedX][capturedY] = null;

        movesSinceCaptureOrKing = 0;
    }


    /**
     * Performs a capture by a king, removing the first encountered opponent's piece from the board.
     * This function assumes that the path to the destination is already verified to be clear.
     *
     * @param xSrc Source x-coordinate
     * @param ySrc Source y-coordinate
     * @param xDst Destination x-coordinate
     * @param yDst Destination y-coordinate
     */
    private void performKingCapture(int xSrc, int ySrc, int xDst, int yDst)
    {
        int stepX = Integer.signum(xDst - xSrc);
        int stepY = Integer.signum(yDst - ySrc);
        int curX = xSrc + stepX;
        int curY = ySrc + stepY;
        boolean capturePerformed = false;

        // Move along the diagonal until the destination or an opponent's piece is found
        while (curX != xDst && curY != yDst && !capturePerformed)
        {
            if (state[curX][curY] != null && state[curX][curY].isBlack() != turn)
            {
                // Remove the first encountered opponent piece
                state[curX][curY] = null;
                capturePerformed = true;
                // continue to update coordinates but stop checking for captures
                curX += stepX;
                curY += stepY;
                continue;
            }
            curX += stepX;
            curY += stepY;
        }

        movesSinceCaptureOrKing = 0;
    }


    /**
     * Checks if the path between the source and destination is clear, which is necessary for king moves.
     * This method verifies that all intermediate squares between the starting and ending position are empty.
     */
    private boolean isPathClear(int xSrc, int ySrc, int xDst, int yDst)
    {
        int stepX = Integer.signum(xDst - xSrc);
        int stepY = Integer.signum(yDst - ySrc);
        int curX = xSrc + stepX, curY = ySrc + stepY;
        while (curX != xDst && curY != yDst)
        {
            if (state[curX][curY] != null)
            {
                return false;
            }

            curX += stepX;
            curY += stepY;
        }
        return true;
    }


    /**
     * Checks if there is exactly one of the opponent's pieces between the source and destination and nothing else.
     * Assumes the move is along a diagonal.
     *
     * @param xSrc Source x-coordinate
     * @param ySrc Source y-coordinate
     * @param xDst Destination x-coordinate
     * @param yDst Destination y-coordinate
     * @return true if exactly one opponent's piece is between the source and destination and nothing else, false otherwise.
     */
    private boolean hasOpponentPieceInBetween(int xSrc, int ySrc, int xDst, int yDst)
    {
        int stepX = Integer.signum(xDst - xSrc);
        int stepY = Integer.signum(yDst - ySrc);
        int opponentCount = 0;

        int curX = xSrc + stepX;
        int curY = ySrc + stepY;

        while (curX != xDst && curY != yDst)
        {
            if (state[curX][curY] != null)
            {
                if (state[curX][curY].isBlack() != turn && opponentCount == 0)
                {
                    // First opponent piece encountered
                    opponentCount++;
                }
                else
                {
                    // More than one piece or encountering a non-opponent piece
                    return false;
                }
            }
            curX += stepX;
            curY += stepY;
        }

        // Return true only if an opponent piece was found (because we can get through the for loop without finding anything)
        return opponentCount == 1;
    }


    /**
     * Checks if there is a mandatory capture from the given position - only for non-king pieces.
     */
    private boolean pieceHasMandatoryCapture(int x, int y)
    {
        int[][] directionVectors = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
        for (int[] dir : directionVectors)
        {
            int jumpX = x + 2 * dir[0];
            int jumpY = y + 2 * dir[1];
            if (jumpX >= 0 && jumpX < BOARD_SIZE && jumpY >= 0 && jumpY < BOARD_SIZE &&
                    state[x + dir[0]][y + dir[1]] != null &&
                    state[x + dir[0]][y + dir[1]].isBlack() != turn &&
                    state[jumpX][jumpY] == null)
            {
                return true;
            }
        }
        return false;
    }


    /**
     * Checks if there is a mandatory capture available from the given position for a king.
     * Kings can capture in any diagonal direction over multiple squares.
     *
     * @param x The x-coordinate of the king on the board.
     * @param y The y-coordinate of the king on the board.
     * @return true if a capture is mandatory, false otherwise.
     */
    private boolean kingHasMandatoryCapture(int x, int y)
    {
        // Directions vectors for diagonal movement
        int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

        for (int[] dir : directions)
        {
            int curX = x + dir[0];
            int curY = y + dir[1];

            boolean foundOpponent = false;

            while (curX >= 0 && curX < BOARD_SIZE && curY >= 0 && curY < BOARD_SIZE)
            {
                if (state[curX][curY] != null)
                {
                    if (foundOpponent)
                    {
                        break; // Found two pieces in a row - the capture would be illegal
                    }

                    if (state[curX][curY].isBlack() != turn)
                    {
                        foundOpponent = true; // Found an opponent's piece
                    }
                    else
                    {
                        break; // Blocked by a same-color piece
                    }
                }
                else if (foundOpponent)
                {
                    // Empty space after an opponent's piece - valid capture
                    return true;
                }
                curX += dir[0];
                curY += dir[1];
            }
        }
        return false;
    }


    /**
     * Checks if there is a mandatory capture for any piece of the current player in the given position.
     */
    private boolean playerHasMandatoryCapture()
    {
        for (int i = 0; i < BOARD_SIZE; i++)
        {
            for (int j = 0; j < BOARD_SIZE; j++)
            {
                if (state[i][j] != null && state[i][j].isBlack() == turn)
                {
                    if (state[i][j].isKing() && kingHasMandatoryCapture(i, j) || !state[i][j].isKing() && pieceHasMandatoryCapture(i, j))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }


    // Required for online play - lets the turn be switched on the other player's device if needed.
    public void setTurn(boolean turn) {
        this.turn = turn;
    }

    // All of these are also required in online play in order to sync up the boards.
    public boolean getTurn()
    {
        return turn;
    }
    public int getLastMoveX() {
        return lastMoveX;
    }

    public void setLastMoveX(int lastMoveX) {
        this.lastMoveX = lastMoveX;
    }

    public int getLastMoveY() {
        return lastMoveY;
    }

    public void setLastMoveY(int lastMoveY) {
        this.lastMoveY = lastMoveY;
    }

    public int getMovesSinceCaptureOrKing()
    {
        return movesSinceCaptureOrKing;
    }

    public void setMovesSinceCaptureOrKing(int movesSinceCaptureOrKing) {
        this.movesSinceCaptureOrKing = movesSinceCaptureOrKing;
    }
}
