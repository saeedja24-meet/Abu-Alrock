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
import java.util.HashMap;
import java.util.List;

/**
 * A basic Checkers activity with code that handles board setup, movement, and
 * minimal turn logic. Overlays are adjusted so that piece highlights don't block clicks.
 */
public class Checkers extends AppCompatActivity {

    private GridLayout chessBoardGridLayout;
    private List<Square> chessBoardSquares;

    // Parametric board definitions
    public static final int boardSquaresNumber = 64;         // total squares
    public static final int boardSideLength    = (int) Math.sqrt(boardSquaresNumber);
    public static final ArrayList<Integer> directions = new ArrayList<>();
    public static final ArrayList<Piece> whiteMen = new ArrayList<>();
    public static final ArrayList<Piece> blackMen = new ArrayList<>();
    public static final ArrayList<Piece> whiteKings = new ArrayList<>();
    public static final ArrayList<Piece> blackKings = new ArrayList<>();
    private static final boolean menCanEatBackward = true;
    public static final boolean flyingKings = true;
    public static final boolean kingChooseWhereToLandAfterJump = true;



    private boolean isBlackTurn = true; // false => White's turn initially

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checkers);  //checkers XML layout
        if (directions.isEmpty()) {
            directions.add(boardSideLength-1);// 7 for 8x8 board
            directions.add(boardSideLength+1);// 9 for 8x8 board
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        chessBoardGridLayout = findViewById(R.id.whiteBackgroundGridLayout);
        chessBoardSquares = new ArrayList<>();
        /**
            * Here we Add the directions where piece can move eg.:
            * diagonally up=boardSideLength+1 and boardSideLength-1)
         *  Forward = boardSideLength
         *  Down = -boardSideLength
         *  Right = 1
         *  Left = -1setUsedMoveDirection
         *
         *  those are all the required movements, if the piece goes up or down will be decided by multiplying
         *  this ArrayList by 1 or -1, 1 for WhitePieces and -1 for Black ones
            * direction.add(boardSideLength-1);
         */

        // Wait for the layout to size itself
        chessBoardGridLayout.post(() -> {
            int gridWidth  = chessBoardGridLayout.getWidth();
            int gridHeight = chessBoardGridLayout.getHeight();

            // Each cell dimension (parametric)
            int cellWidth  = gridWidth  / boardSideLength;
            int cellHeight = gridHeight / boardSideLength;

            chessBoardGridLayout.removeAllViews();
            chessBoardSquares.clear();
            whiteMen.clear();
            blackMen.clear();

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
                    Piece whiteCheckerPiece = new Piece(i,false, R.drawable.whitecheckerpiece, this,this);
                    ArrayList<Integer> moveDirections = new ArrayList<>();

                    for (Integer direction :directions){
                        moveDirections.add(direction);
                    }
                    whiteCheckerPiece.setUsedMoveDirection(moveDirections);
                    ArrayList<Integer> jumpDirections = new ArrayList<>(moveDirections); // [9,7]

                    if (menCanEatBackward) {
                        for (Integer direction : directions){
                            jumpDirections.add(direction * -1); // Adds -9,-7
                        }
                    }

                    whiteCheckerPiece.setJumpDirection(jumpDirections);
                    square.setPiece(whiteCheckerPiece);
                    whiteMen.add(whiteCheckerPiece);

                } else if (darkSquare && i >= boardSideLength * (boardSideLength / 2 + 1)) {
                    // Black piece on bottom rows
                    Piece blackCheckerPiece = new Piece(i,true, R.drawable.blackcheckerpiece, this,this);

                    ArrayList<Integer> moveDirections = new ArrayList<>();
                    for (Integer direction :directions){
                        moveDirections.add(direction*-1);
                    }
                    blackCheckerPiece.setUsedMoveDirection(moveDirections);

                    ArrayList<Integer> blackJumpDirections = new ArrayList<>(moveDirections); // [-9,-7]

                    if (menCanEatBackward) {
                        for (Integer direction : directions){
                            blackJumpDirections.add(direction); // Adds 9,7
                        }
                    }

                    blackCheckerPiece.setJumpDirection(blackJumpDirections);
                    square.setPiece(blackCheckerPiece);
                    blackMen.add(blackCheckerPiece);
                }

                // Layout params
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.rowSpec    = GridLayout.spec(i / boardSideLength);
                params.columnSpec = GridLayout.spec(i % boardSideLength);
                params.width      = cellWidth;
                params.height     = cellHeight;
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
        isBlackTurn = !isBlackTurn;
        manageMovement(isBlackTurn);
    }



    private boolean availableMenJumps(boolean isBlackTurn) {
        ArrayList<Piece> pieces = isBlackTurn ? blackMen : whiteMen;
        ArrayList<Piece> opposedPieces = isBlackTurn ?  whiteMen:blackMen ;
        ArrayList<Piece> piecesThatCanJump = new ArrayList<>();
        HashMap<Piece, HashMap<Integer, ArrayList<Integer>>> pieceJumpAndRoute=new HashMap<>();
        ArrayList<Integer> lastCaptures = new ArrayList<>();
        for (Piece piece : pieces) {
            if (piece.manCanJump(lastCaptures)) {
                pieceJumpAndRoute.put(piece,piece.getUsedJumpDirection(piece.getPieceId(),lastCaptures));
                piecesThatCanJump.add(piece);
            }
        }

        // Mark those pieces on the board
    for (Piece piece:pieceJumpAndRoute.keySet()) {
        Square square = findSquareByPiece(piece);
            square.markPieceCanMove();
            piece.getPieceButton().setOnClickListener(v-> {
                cleanAllPiecePossibleMovementsMarkers();
                for (Integer capturedSquareIndex : pieceJumpAndRoute.get(piece).keySet()) {
                    Square capturedSquare = findSquareById(capturedSquareIndex);
                    capturedSquare.squarePossibleMarkerSquare(R.drawable.willbeeaten);
                    capturedSquare.getPossibleMovesMarks().setClickable(false);
                    for (Integer landingSquareIndex : pieceJumpAndRoute.get(piece).get(capturedSquareIndex)) {
                        Square landingSquare = findSquareById(landingSquareIndex);
                        landingSquare.squarePossibleMarkerSquare(R.drawable.canlandafterjumpsquare);
                        landingSquare.getPossibleMovesMarks().setOnClickListener(v1 -> {
                            opposedPieces.remove(capturedSquare.getPiece());
                            capturedSquare.removePiece();
                            square.removePiece();
                            pieces.remove(piece);
                            if( landingSquare.getId()/boardSideLength == (isBlackTurn?0:boardSideLength-1))piece.upgradePiece();
                            landingSquare.setPiece(piece);
                            pieces.add(landingSquare.getPiece());
                            nextTurn();
                        });
                    }

                }
            });


        }

        return !piecesThatCanJump.isEmpty();
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

    private void changeVisibilityAllPiecePossibleMovementsMarkers() {
        for (Square square : chessBoardSquares) {
            if (square.getPossibleMovesMarks() != null) {
                Button markButton = square.getPossibleMovesMarks();
                if (markButton.getVisibility() == View.VISIBLE) {
                    markButton.setVisibility(View.INVISIBLE);
                } else {
                    markButton.setVisibility(View.VISIBLE);
                }
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

/**
 private JumpNode manJumpSequence(int currentIndex, ArrayList<Integer> jumpDirections, boolean isBlack, Integer doNotReturnSquare) {
 JumpNode jumpSequence = new JumpNode(currentIndex,(currentIndex+doNotReturnSquare)/2);
 for (Integer jumpDirection : jumpDirections) {
 int capturedSquareIndex = currentIndex + jumpDirection ;
 int landingIndex        = currentIndex + jumpDirection * 2;

 if (doNotReturnSquare == null || landingIndex != doNotReturnSquare) {

 if (allowedManJump(currentIndex, landingIndex, isBlack)) {
 JumpNode continuationSequence = manJumpSequence(
 landingIndex,
 jumpDirections,
 isBlack,
 currentIndex
 );


 continuationSequence.setParent(jumpSequence);
 if (jumpDirection == 1-boardSideLength ) {

 jumpSequence.setLeftUp(continuationSequence);
 }
 if (jumpDirection==boardSideLength-1) {
 jumpSequence.setLeftDown(continuationSequence);
 }
 if (jumpDirection== -boardSideLength-1) {
 jumpSequence.setRightUp(continuationSequence);
 }
 if (jumpDirection==boardSideLength+1){
 jumpSequence.setRightDown(continuationSequence);
 }
 }
 }
 }
 return jumpSequence;
 }

 private void makeJumpingSquare(@NonNull JumpNode jumpingNode) {

 Square landingSquare=findSquareById(jumpingNode.getStartingSquareIndex());
 landingSquare.squarePossibleMarkerSquare(R.drawable.canlandafterjumpsquare);
 landingSquare.getPossibleMovesMarks().setOnClickListener(v -> {
 JumpNode currentSquareInNode = jumpingNode;
 Square copyCurrentSquare = findSquareById(currentSquareInNode.getStartingSquareIndex());
 while (currentSquareInNode.getParent() != null
 && !findSquareById(currentSquareInNode.getParent().getStartingSquareIndex()).isOccupied()) {
 Toast.makeText(this,"copy currentSquare Id is "+jumpingNode.getStartingSquareIndex(),Toast.LENGTH_SHORT).show();

 Square capturedSquare=findSquareById(currentSquareInNode.getCapturedSquare());
 Piece piece = findSquareById(currentSquareInNode.getCapturedSquare()).getPiece();
 capturedSquare.removePiece();

 copyCurrentSquare.setPiece(piece);
 }
 Square capturedSquare=findSquareById(currentSquareInNode.getCapturedSquare());
 Piece piece = findSquareById(currentSquareInNode.getCapturedSquare()).getPiece();
 Toast.makeText(this,"currentSquareInNode is "+currentSquareInNode.getStartingSquareIndex(),Toast.LENGTH_SHORT).show();

 Toast.makeText(this,"the CapturedPiece Id is "+currentSquareInNode.getCapturedSquare(),Toast.LENGTH_SHORT).show();
 Toast.makeText(this,"the CapturedPiece color is "+(piece.getIsBlack()? "Black":"White"),Toast.LENGTH_SHORT).show();


 capturedSquare.removePiece();

 copyCurrentSquare.setPiece(piece);

 // Check if more jumps are possible
 if (piece.manCanJump(-1)) {
 cleanAllPieceCanMove();
 cleanAllPiecePossibleMovementsMarkers();
 copyCurrentSquare.markPieceCanMove();

 piece.getPieceButton().setClickable(true);
 piece.getPieceButton().setOnClickListener(v1 -> changeVisibilityAllPiecePossibleMovementsMarkers());

 markPossibleJumpingSquare(manJumpSequence(
 copyCurrentSquare.getId(),
 piece.getJumpDirection(),
 piece.getIsBlack(),
 capturedSquare.getId()
 ));
 } else {
 nextTurn();
 }
 });
 }


 private void makeEatAndCapturedSquare(JumpNode jumpingNodeContinuation){
 Square capturedSquare = findSquareById(jumpingNodeContinuation.getCapturedSquare());
 capturedSquare.squarePossibleMarkerSquare(R.drawable.willbeeaten);
 capturedSquare.getPossibleMovesMarks().setClickable(false);
 makeJumpingSquare(jumpingNodeContinuation);
 markPossibleJumpingSquare(jumpingNodeContinuation);
 }

 private void markPossibleJumpingSquare(JumpNode jumpingNode) {
 if (jumpingNode.getLeftDown() != null) {
 makeEatAndCapturedSquare(jumpingNode.getLeftDown());
 }

 if (jumpingNode.getLeftUp() != null) {
 makeEatAndCapturedSquare(jumpingNode.getLeftUp());
 }

 if (jumpingNode.getRightDown() != null) {
 makeEatAndCapturedSquare(jumpingNode.getRightDown());
 }

 if (jumpingNode.getRightUp() != null) {
 makeEatAndCapturedSquare(jumpingNode.getRightUp());
 }
 }




 */
