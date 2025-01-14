package com.example.computerapp_aboalrock.checkers;

import static com.example.computerapp_aboalrock.checkers.Checkers.blackMen;
import static com.example.computerapp_aboalrock.checkers.Checkers.boardSideLength;
import static com.example.computerapp_aboalrock.checkers.Checkers.boardSquaresNumber;
import static com.example.computerapp_aboalrock.checkers.Checkers.flyingKings;
import static com.example.computerapp_aboalrock.checkers.Checkers.kingChooseWhereToLandAfterJump;
import static com.example.computerapp_aboalrock.checkers.Checkers.whiteMen;

import android.content.Context;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.computerapp_aboalrock.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Predicate;


/**
 * Represents a checker piece as an ImageButton.
 */
public class Piece {
    private final ImageButton pieceButton;
    private boolean isKing;
    private boolean isBlack;

    private Integer pieceId;
    private final Checkers checkers;

    private ArrayList<Integer> usedMoveDirection;
    // Directions for jumps (if you want to keep them separate)
    private ArrayList<Integer> jumpDirection;

    public Piece(int id, Boolean isBlack, int drawableRes, Context context, Checkers checkers) {
        this.isBlack = isBlack;
        this.pieceButton = new ImageButton(context);
        this.pieceButton.setBackgroundResource(drawableRes);
        this.isKing = false;
        this.checkers = checkers;
        this.pieceId = id;
        this.pieceButton.setOnClickListener(v -> {
        });
    }


    public void setPieceId(int id) {
        this.pieceId = id;
    }

    public int getPieceId() {
        return this.pieceId;
    }

    public boolean getIsBlack() {
        return isBlack;
    }

    public ImageButton getPieceButton() {
        return pieceButton;
    }

    public boolean isKing() {
        return isKing;
    }

    /**
     * Upgrades this piece to a king, giving it 2-way movement.
     */
    public void setUsedMoveDirection(ArrayList<Integer> usedMoveDirection) {
        this.usedMoveDirection = usedMoveDirection;
    }


    public void setDrawableFile(int drawable) {
        this.pieceButton.setBackgroundResource(drawable);
    }

    public static boolean availableMenMove(boolean isBlackTurn) {
        ArrayList<Piece> pieces = isBlackTurn ? blackMen : whiteMen;
        ArrayList<Piece> piecesThatCanMove = new ArrayList<>();

        for (Piece piece : pieces) {
            if (piece.manCanMove()) {
                piecesThatCanMove.add(piece);
            }
        }
        return !piecesThatCanMove.isEmpty();
    }


    public ArrayList<Integer> getPossibleMoves() {
        ArrayList<Integer> possibleMoves=new ArrayList<>();
        for (int direction : new ArrayList<>(this.usedMoveDirection)) {
            Predicate<Integer> movementPredicate = getPredicate(direction, pieceId);
            int possibleMove=pieceId+direction;
            if (movementPredicate.test(possibleMove)){
                if(!checkers.findSquareById(possibleMove).isOccupied()){
                    possibleMoves.add(possibleMove);
                    if(flyingKings && isKing){
                        possibleMove+=direction;
                        while (movementPredicate.test(possibleMove)){
                            if (checkers.findSquareById(possibleMove).isOccupied())break;
                            possibleMoves.add(possibleMove);
                            possibleMove+=direction;
                        }
                    }
                }
            }
        }

        Log.d("Clone value", "cloneRefactored value is"+possibleMoves);

        return possibleMoves;
    }


    public boolean manCanJump(ArrayList<Integer> lastCapturesList){
        HashMap<Integer, ArrayList<Integer>> checkIfEmpty = this.getUsedJumpDirection(this.pieceId, lastCapturesList);
        return !(checkIfEmpty==null || checkIfEmpty.isEmpty());
    }

    public Predicate<Integer> getPredicate(int currentCheckDirection,int currentSquareIndex){
        Predicate<Integer> movementPredicate;
        Predicate<Integer> isWithinBound = (landingSquareIndex)->
                landingSquareIndex >= 0
                        && landingSquareIndex < boardSquaresNumber;
        if (currentCheckDirection==1||currentCheckDirection==-1) {
            // Horizontal
            movementPredicate = (landingSquareIndex) ->
                    // same row
                    (landingSquareIndex / boardSideLength) == (currentSquareIndex / boardSideLength)
                            && isWithinBound.test(landingSquareIndex);
        }

        else if (currentCheckDirection%boardSideLength==0) {
            // Vertical
            movementPredicate = (landingSquareIndex) ->
                    // same column
                    (landingSquareIndex % boardSideLength) == (currentSquareIndex % boardSideLength)&& isWithinBound.test(landingSquareIndex);
        }

        else {
            // Diagonal
            movementPredicate = (landingSquareIndex) ->
                    // row/column difference check
                    Math.abs(landingSquareIndex/boardSideLength - currentSquareIndex / boardSideLength) ==
                            Math.abs(landingSquareIndex % boardSideLength - currentSquareIndex % boardSideLength)
                            && isWithinBound.test(landingSquareIndex);
        }
        return movementPredicate;
    }

    public HashMap<Integer, ArrayList<Integer>> getUsedJumpDirection(int startingToJumpIndex,ArrayList<Integer> lastCapturesList) {
        HashMap<Integer, ArrayList<Integer>> capturesMap = new HashMap<>();
        if (lastCapturesList==null){
            lastCapturesList=new ArrayList<Integer>();
        }
        for (Integer jumpInteger : this.jumpDirection) {
            HashMap<Integer, ArrayList<Integer>> mapResult = scanInDirection(jumpInteger, getPredicate(jumpInteger,startingToJumpIndex),this,startingToJumpIndex,lastCapturesList);
            if (!mapResult.isEmpty()) {
                capturesMap.putAll(mapResult);
            }

        }

        return capturesMap ;
    }

    /**

     * Helper method to scan for captures in a given direction.
     * @param jumpInteger                    The direction increment (e.g. +1, -1, +8, -8, +7, -7, +9, -9, etc.)
     * @param isLegalSquare                  A predicate that returns true if the next square is valid for movement
     * @return A HashMap for the direction that called it which contains  the capturedEnemyPiece Index as key and the possible landings after the jump in the same Direction
     */
    private HashMap<Integer, ArrayList<Integer>> scanInDirection(int jumpInteger, Predicate<Integer> isLegalSquare, Piece piece,int currentLocation,ArrayList<Integer> lastCapturesList) {
        // We'll keep a temporary list for possible landing squares in this direction
        ArrayList<Integer> possibleLandingSquaresIndexes = new ArrayList<>();
        HashMap<Integer, ArrayList<Integer>> eatenSquareAndPossibleLandings= new HashMap<>();
        int possibleCapturedSquare = currentLocation + jumpInteger;
        int possibleLandingSquare= possibleCapturedSquare+jumpInteger;
        if(!(this.isKing&&flyingKings)){
            if(isLegalSquare.test(possibleCapturedSquare) && isLegalSquare.test(possibleLandingSquare)){
                Square possibleCap=checkers.findSquareById(possibleCapturedSquare);
                if (possibleCap.isOccupied() && !lastCapturesList.contains(possibleCapturedSquare) && possibleCap.getPiece().isBlack!=piece.isBlack){
                    if ((!checkers.findSquareById(possibleLandingSquare).isOccupied()||lastCapturesList.contains(possibleCapturedSquare+jumpInteger))){
                        possibleLandingSquaresIndexes.add(possibleLandingSquare);
                        eatenSquareAndPossibleLandings.put(possibleCapturedSquare,possibleLandingSquaresIndexes);
                    }
                }

            }
            return eatenSquareAndPossibleLandings;
        }


        // Keep scanning until we exit the board or no longer meet "in-line" rules
        while (isLegalSquare.test(possibleCapturedSquare)) {
            Square sq = checkers.findSquareById(possibleCapturedSquare);
            if (sq.isOccupied()&&!lastCapturesList.contains(possibleCapturedSquare)) {
                // If the piece encountered is the same color, no capture possible
                if (sq.getPiece().isBlack == piece.isBlack) break;
                // It's an enemy piece, see if the landing square after the enemy is free
                if (!isLegalSquare.test(possibleLandingSquare))break;
                Square landingSq = checkers.findSquareById(possibleLandingSquare);
                // If that landing square is occupied, no capture possible in this direction
                if (landingSq.isOccupied()&& !lastCapturesList.contains(possibleLandingSquare)) break;
                possibleLandingSquaresIndexes.add(possibleLandingSquare);
                if (kingChooseWhereToLandAfterJump) {
                    possibleLandingSquare += jumpInteger;
                    while (isLegalSquare.test(possibleLandingSquare)) {
                        Square extraSq = checkers.findSquareById(possibleLandingSquare);
                        if (extraSq.isOccupied()&&!lastCapturesList.contains(possibleLandingSquare)) {break;}
                        possibleLandingSquaresIndexes.add(possibleLandingSquare);
                        possibleLandingSquare += jumpInteger;
                    }
                }
                eatenSquareAndPossibleLandings.put(possibleCapturedSquare,possibleLandingSquaresIndexes);
                break;
            }
            possibleCapturedSquare += jumpInteger;
            possibleLandingSquare+=jumpInteger;
        }
        return eatenSquareAndPossibleLandings;
    }

    public boolean manCanMove() {
        if (this.pieceId == null) return false;
        ArrayList<Square> validDirections = new ArrayList<>();
        for (int square: getPossibleMoves()){
            validDirections.add(checkers.findSquareById(square));
        }
        addAvailableMoveViews(validDirections);
        Square currentSquare=checkers.findSquareById(this.pieceId);
        if (validDirections.isEmpty()) return false;
        currentSquare.markPieceCanMove();
        return true;
    }


    public void addAvailableMoveViews(ArrayList<Square> possibleMoveSquares) {
        Square currentSquare=checkers.findSquareById(this.pieceId);

        currentSquare.getFrameLayout().bringChildToFront(currentSquare.getPiece().getPieceButton());
        currentSquare.getPiece().getPieceButton().setOnClickListener(v -> {
            checkers.cleanAllPiecePossibleMovementsMarkers();
            for (Square moveSquare : possibleMoveSquares) {
                moveSquare.squarePossibleMarkerSquare(R.drawable.canlandafterjumpsquare);
                Button markerButton = moveSquare.getPossibleMovesMarks();
                if (markerButton != null) {
                    markerButton.setOnClickListener(view -> {
                        if (currentSquare.getPiece() != null) {
                            Piece piece=currentSquare.getPiece();
                            currentSquare.removePiece();
                            boolean isBlack=piece.isBlack;
                            ArrayList<Piece> pieces= isBlack? blackMen:whiteMen;
                            pieces.remove(piece);
                            if(moveSquare.getId()/boardSideLength == (piece.getIsBlack()? 0:boardSideLength-1)) piece.upgradePiece();
                            moveSquare.setPiece(piece);
                            pieces.add(piece);
                            checkers.nextTurn();

                        }
                    });
                }
            }
        });
    }



    public void setJumpDirection(ArrayList<Integer> jumpDirection) {
        this.jumpDirection = jumpDirection;
    }

    public void upgradePiece() {
        if(!isKing){
            ArrayList<Integer> toAdd=new ArrayList<>();
            for (int direction: usedMoveDirection) {
                if(!toAdd.contains(-direction)&&!this.usedMoveDirection.contains(-direction))toAdd.add(-direction);//insure adding reverse directions without duplicates(minimizing the needed calculations for other parts)
            }
            this.isKing = true;
            this.usedMoveDirection.addAll(toAdd);
            this.jumpDirection = new ArrayList<>(this.usedMoveDirection);

            // Change the drawable to king
            if (this.isBlack) {
                this.pieceButton.setBackgroundResource(R.drawable.blackking);
            } else {
                this.pieceButton.setBackgroundResource(R.drawable.whiteking);
            }
        }

    }

}
