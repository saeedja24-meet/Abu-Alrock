package com.example.computerapp_aboalrock.checkers;

import static com.example.computerapp_aboalrock.checkers.Piece.availableMenMove;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.computerapp_aboalrock.R; // Import your R file
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * A basic Checkers activity with code that handles board setup, movement, and
 * minimal turn logic. Overlays are adjusted so that piece highlights don't block clicks.
 */
public class Checkers extends AppCompatActivity {

    private GridLayout chessBoardGridLayout;
    private List<Square> chessBoardSquares;

    // Parametric board definitions
    public static final int boardSquaresNumber = 100;         // total squares
    public static final int boardSideLength = (int) Math.sqrt(boardSquaresNumber);
    public static final ArrayList<Integer> directions = new ArrayList<>();
    public static final ArrayList<Piece> whiteMen = new ArrayList<>();
    public static final ArrayList<Piece> blackMen = new ArrayList<>();
    public static final ArrayList<JumpNode> currentJumpsNode = new ArrayList<>();
    private static final boolean menCanEatBackward = false;
    public static final boolean flyingKings = true;
    public static boolean forcedPieceBiggestJumpSequence=true;
    public static final boolean kingChooseWhereToLandAfterJump = true;


    private boolean isBlackTurn = true; // false => White's turn initially

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkers);  //checkers XML layout
        if (directions.isEmpty()) {
            directions.addAll(Arrays.asList(-boardSideLength,boardSideLength,1,-1));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        chessBoardGridLayout = findViewById(R.id.whiteBackgroundGridLayout);
        chessBoardSquares = new ArrayList<>();
        /*Here we Add the directions where piece can move eg.:
          diagonally up=boardSideLength+1 and boardSideLength-1)
           Forward = boardSideLength
           Down = -boardSideLength
           Right = 1
           Left = -1setUsedMoveDirection
           those are all the required movements, if the piece goes up or down will be decided by multiplying
           this ArrayList by 1 or -1, 1 for WhitePieces and -1 for Black ones
          direction.add(boardSideLength-1);
         */

        // Wait for the layout to size itself
        chessBoardGridLayout.post(() -> {
            int gridWidth = chessBoardGridLayout.getWidth();
            int gridHeight = chessBoardGridLayout.getHeight();

            // Each cell dimension (parametric)
            int cellWidth = gridWidth / boardSideLength;
            int cellHeight = gridHeight / boardSideLength;

            chessBoardGridLayout.removeAllViews();
            chessBoardSquares.clear();
            whiteMen.clear();
            blackMen.clear();
            chessBoardGridLayout.setColumnCount(boardSideLength);
            chessBoardGridLayout.setRowCount(boardSideLength);

            // Populate the board squares
            for (int i = 0; i < boardSquaresNumber; i++) {
                // Create a FrameLayout for each cell
                FrameLayout frameLayout = new FrameLayout(this);
                frameLayout.setId(View.generateViewId());

                // Decide if the square is "dark" or "light"
                ImageView squareColor = new ImageView(this);
                boolean darkSquare = ((i / boardSideLength) + (i % boardSideLength)) % 2 == 0;
                if (darkSquare) {
                    squareColor.setBackgroundResource(R.drawable.browncheckerssquare);
                } else {
                    squareColor.setBackgroundResource(R.drawable.lightbrowncheckerssquare);
                }
                frameLayout.addView(squareColor);

                // Create and store Square object (pass `this` as context)
                Square square = new Square(i, frameLayout, this);

                // Place initial pieces
                if (darkSquare && i < boardSideLength * (boardSideLength / 2 - 1)) {
                    // White piece on top rows
                    Piece whiteCheckerPiece = new Piece(i, false, R.drawable.whitecheckerpiece, this, this);
                    ArrayList<Integer> moveDirections = new ArrayList<>();

                    moveDirections.addAll(directions);

                    whiteCheckerPiece.setUsedMoveDirection(moveDirections);
                    ArrayList<Integer> jumpDirections = new ArrayList<>(moveDirections); // [9,7]

                    if (menCanEatBackward) {
                        for (Integer direction : directions) {
                            jumpDirections.add(direction * -1); // Adds -9,-7
                        }
                    }

                    whiteCheckerPiece.setJumpDirection(jumpDirections);
                    square.setPiece(whiteCheckerPiece);
                    whiteMen.add(whiteCheckerPiece);

                } else if (darkSquare && i >= boardSideLength * (boardSideLength / 2 + 1)) {
                    // Black piece on bottom rows
                    Piece blackCheckerPiece = new Piece(i, true, R.drawable.blackcheckerpiece, this, this);

                    ArrayList<Integer> moveDirections = new ArrayList<>();
                    for (Integer direction : directions) {
                        moveDirections.add(direction * -1);
                    }
                    blackCheckerPiece.setUsedMoveDirection(moveDirections);

                    ArrayList<Integer> blackJumpDirections = new ArrayList<>(moveDirections); // [-9,-7]

                    if (menCanEatBackward) {
                        blackJumpDirections.addAll(directions); // Adds 9,7
                    }

                    blackCheckerPiece.setJumpDirection(blackJumpDirections);
                    square.setPiece(blackCheckerPiece);
                    blackMen.add(blackCheckerPiece);
                }

                // Layout params
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.rowSpec = GridLayout.spec(i / boardSideLength);
                params.columnSpec = GridLayout.spec(i % boardSideLength);
                params.width = cellWidth;
                params.height = cellHeight;
                frameLayout.setLayoutParams(params);

                chessBoardGridLayout.addView(frameLayout);
                chessBoardSquares.add(square);
            }

            // After the board is set, highlight initial player moves:
            manageMovement(isBlackTurn);
        });
    }

    public void manageMovement(boolean isBlackTurn) {
        disableClickablityPiecesSet(!isBlackTurn);
        cleanAllPiecePossibleMovementsMarkers();
        cleanAllPieceCanMove();
        boolean anyJumps = availableMenJumps(isBlackTurn);

        if (!anyJumps) {
            availableMenMove(isBlackTurn);
        }
    }

    /**
     * Called after a piece completes a move or jump. Flips the turn and re-highlights.
     */
    public void nextTurn() {
        if (blackMen.isEmpty()||whiteMen.isEmpty()) Toast.makeText(this, "Game Over"+(isBlackTurn?"Black":"White")+" Won", Toast.LENGTH_SHORT).show();
        isBlackTurn = !isBlackTurn;
        manageMovement(isBlackTurn);
    }


    private boolean availableMenJumps(boolean isBlackTurn) {
        ArrayList<Piece> pieces = isBlackTurn ? blackMen : whiteMen;
        ArrayList<Piece> opposedPieces = isBlackTurn ? whiteMen : blackMen;
        HashMap<Piece, HashMap<Integer, ArrayList<Integer>>> pieceJumpAndRoute = new HashMap<>();
        ArrayList<Integer> lastCaptures = new ArrayList<>();
        for (Piece piece : pieces) {
            if (piece.manCanJump(lastCaptures)) {
                pieceJumpAndRoute.put(piece, piece.getUsedJumpDirection(piece.getPieceId(), lastCaptures));
            }
        }
        for (Piece piece: pieceJumpAndRoute.keySet()){
            JumpNode jumpNode=new JumpNode(piece,piece.getPieceId(),new ArrayList<>(),null,this);
            currentJumpsNode.add(jumpNode);
            if(forcedPieceBiggestJumpSequence) {
                jumpNode.findLongestRoute(jumpNode);
            }
            findSquareByPiece(piece).markPieceCanMove();

            piece.getPieceButton().setOnClickListener(v->{
                hideAllMovementsMarkersInvisible();
                jumpNode.markJumpNode();
            });

        }
        return !pieceJumpAndRoute.isEmpty();
    }



    public Square findSquareById(int id) {
        for (Square square : chessBoardSquares) {
            if (square.getId() == id) {
                return square;
            }
        }
        return null;
    }

    private Square findSquareByPiece(Piece piece) {
        for (Square square : chessBoardSquares) {
            if (square.getPiece() == piece) {
                return square;
            }
        }
        return null;
    }

    public void cleanAllPiecePossibleMovementsMarkers() {
        for (Square square : chessBoardSquares) {
            if (square.getPossibleMovesMarks() != null) {
                square.removeSquarePossibleMoveMarkerColor();
            }
        }
    }

    private void hideAllMovementsMarkersInvisible() {
        for (Square square : chessBoardSquares) {
            if (square.getPossibleMovesMarks() != null) {
                Button markButton = square.getPossibleMovesMarks();
                markButton.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void cleanAllPieceCanMove() {
        for (Square square : chessBoardSquares) {
            square.removePieceCanMoveMarker();
        }
    }

    private void disableClickablityPiecesSet(boolean isBlackTurn) {
        ArrayList<Piece> piecesToDisable = isBlackTurn ? blackMen : whiteMen;
        for (Piece piece : piecesToDisable) {
            piece.getPieceButton().setClickable(false);
        }
    }
}
