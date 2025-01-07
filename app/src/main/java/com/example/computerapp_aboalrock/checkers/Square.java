package com.example.computerapp_aboalrock.checkers;

import com.example.computerapp_aboalrock.R;
import android.content.Context;
import android.widget.Button;
import android.widget.FrameLayout;

/**
 * Each square on the board: frameLayout + piece + possible highlights.
 */
public class Square {
    private final int id;
    private final FrameLayout frameLayout;
    private Button piecePossibleMovesMarkerColor;  // highlight for possible squares
    private Button pieceCanMoveMarkerColor;         // highlight under the piece
    private Piece piece;
    private final Context context; // We store a Context reference for creating views

    public Square(int id, FrameLayout frameLayout, Context context) {
        this.id = id;
        this.frameLayout = frameLayout;
        this.context = context;
    }

    public int getId() {
        return id;
    }

    public FrameLayout getFrameLayout() {
        return frameLayout;
    }

    public boolean isOccupied() {
        return piece != null;
    }

    public Piece getPiece() {
        return piece;
    }

    /**
     * Sets the piece on this square, removing any old piece first.
     */
    public void setPiece(Piece piece) {
        if (this.piece != null) {
            removePiece();
        }
        this.piece = piece;
        piece.setPieceId(getId());
        frameLayout.addView(piece.getPieceButton());
    }

    public void removePiece() {
        if (this.piece != null) {
            frameLayout.removeView(this.piece.getPieceButton());
            this.piece = null;
        }
    }

    /** Mark this square as a possible landing square (for moves/jumps). */
    public void squarePossibleMarkerSquare(int drawableRes) {
        // Remove old marker if present
        if (this.piecePossibleMovesMarkerColor != null) {
            removeSquarePossibleMoveMarkerColor();
        }
        this.piecePossibleMovesMarkerColor = new Button(context);
        this.piecePossibleMovesMarkerColor.setBackgroundResource(drawableRes);
        this.piecePossibleMovesMarkerColor.setClickable(true);
        this.piecePossibleMovesMarkerColor.setFocusable(true);

        frameLayout.addView(piecePossibleMovesMarkerColor);
    }

    /** Mark this square’s piece as "can move" (highlight under the piece). */
    public void markPieceCanMove() {
        // Remove old highlight if present
        if (this.pieceCanMoveMarkerColor != null) {
            removePieceCanMoveMarker();
        }
        this.pieceCanMoveMarkerColor = new Button(context);
        this.pieceCanMoveMarkerColor.setBackgroundResource(R.drawable.canmovemarker);
        this.pieceCanMoveMarkerColor.setClickable(false);
        this.pieceCanMoveMarkerColor.setFocusable(false);

        frameLayout.addView(pieceCanMoveMarkerColor);

        // Ensure the piece is above the highlight
        if (piece != null) {
            frameLayout.bringChildToFront(piece.getPieceButton());
        }
    }

    public void removeSquarePossibleMoveMarkerColor() {
        if (this.piecePossibleMovesMarkerColor != null) {
            frameLayout.removeView(this.piecePossibleMovesMarkerColor);
            this.piecePossibleMovesMarkerColor = null;
        }
    }

    public void removePieceCanMoveMarker() {
        if (this.pieceCanMoveMarkerColor != null) {
            frameLayout.removeView(this.pieceCanMoveMarkerColor);
            this.pieceCanMoveMarkerColor = null;
        }
    }

    /** Returns the clickable marker for "moving" so we can attach an onClick. */
    public Button getPossibleMovesMarks() {
        return this.piecePossibleMovesMarkerColor;
    }
}
