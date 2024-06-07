package com.example.checkersnadav;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * Adapter class for managing the display of the checkers board in a GridView.
 * This adapter handles the creation and updating of view elements representing
 * each cell on the board, including setting the appropriate background,
 * piece images, and king images based on the game state.
 */
public class CheckersAdapter extends BaseAdapter
{
    private final Context context; // Context in which the adapter is running
    private Piece[][] boardState; // Current state of the board
    private final boolean color; // Required for online play, so we know how to display the board
    private int highlightedPosition = -1; // Position currently highlighted

    /**
     * Constructs a new CheckersAdapter.
     * @param context The current context.
     * @param boardState The 2D array representing the state of the checkers board.
     */
    public CheckersAdapter(Context context, Piece[][] boardState, boolean color)
    {
        this.context = context;
        this.boardState = boardState;
        this.color = color;
    }

    /**
     * Returns the number of items in the data set represented by this Adapter.
     * @return The number of items in the data set.
     */
    @Override
    public int getCount()
    {
        return Board.BOARD_SIZE * Board.BOARD_SIZE; // Total number of cells on the board
    }

    /**
     * Gets the data item associated with the specified position in the data set.
     * @param position The position of the item within the adapter's data set.
     * @return The object at the specified position.
     */
    @Override
    public Object getItem(int position)
    {
        int row = position / Board.BOARD_SIZE;
        int col = position % Board.BOARD_SIZE;
        return boardState[row][col];
    }

    /**
     * Gets the row id associated with the specified position in the list.
     * @param position The position of the item within the adapter's data set.
     * @return The id of the item at the specified position.
     */
    @Override
    public long getItemId(int position)
    {
        return position;
    }

    /**
     * Gets a View that displays the data at the specified position in the data set.
     * @param position The position of the item within the adapter's data set.
     * @param convertView The old view to reuse, if possible.
     * @param parent The parent that this view will eventually be attached to.
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        Log.d("CheckersAdapter", "Getting view for position: " + position);
        SquareImageView imageView;
        if (convertView == null)
        {
            imageView = new SquareImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(140, 140));
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setAdjustViewBounds(true);
        }
        else
        {
            imageView = (SquareImageView) convertView;
        }

        int row = position / Board.BOARD_SIZE;
        int col = position % Board.BOARD_SIZE;

        // Flip the rows and cols for piece setup so white pieces start at the bottom
        if (!color)
        {
            row = Board.BOARD_SIZE - 1 - row;
            col = Board.BOARD_SIZE - 1 - col;
        }

        Piece piece = boardState[row][col];

        int lightSquareColor = Color.rgb(235, 209, 184);
        int darkSquareColor = Color.rgb(72, 43, 25);
        int highlightedLightColor = Color.rgb(255, 239, 214);
        int highlightedDarkColor = Color.rgb(152, 123, 95);

        if (position == highlightedPosition)
        {
            if ((row + col) % 2 == 0)
            {
                imageView.setBackgroundColor(highlightedLightColor); // Highlighted light square
            }
            else
            {
                imageView.setBackgroundColor(highlightedDarkColor); // Highlighted dark square
            }
        }
        else if ((row + col) % 2 == 0)
        {
            imageView.setBackgroundColor(lightSquareColor); // Light squares
        }
        else
        {
            imageView.setBackgroundColor(darkSquareColor); // Dark squares
        }

        // Set the piece image based on the type of piece and whether it's a king
        if (piece != null)
        {
            imageView.setImageResource(piece.getPictureID());
        }
        else
        {
            imageView.setImageDrawable(null); // Clear any previous image if no piece is present
        }

        return imageView;
    }

    /**
     * Updates the game state with a new board configuration.
     * This method is typically used in online play to synchronize the board state across different devices.
     * Upon updating the board state, it notifies any observers that the data has changed to refresh the user interface.
     *
     * @param newBoardState A 2D array of Piece objects representing the new state of the board.
     */
    public void updateGameState(Piece[][] newBoardState)
    {
        this.boardState = newBoardState; // Update the internal representation of the board
        notifyDataSetChanged(); // Notify the adapter that the data has changed, prompting a UI update
    }

    /**
     * Sets the position currently being dragged.
     * @param position The position of the item being dragged.
     */
    public void setDraggingPosition(int position)
    {
        // Position being dragged
        notifyDataSetChanged(); // Notify the adapter to update the view
    }

    /**
     * Sets the position currently highlighted.
     * @param position The position of the item to highlight.
     */
    public void setHighlightedPosition(int position)
    {
        this.highlightedPosition = position;
        notifyDataSetChanged(); // Notify the adapter to update the view
    }
}
