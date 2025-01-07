package com.example.computerapp_aboalrock.checkers;

import static com.example.computerapp_aboalrock.checkers.Checkers.blackKings;
import static com.example.computerapp_aboalrock.checkers.Checkers.blackMen;
import static com.example.computerapp_aboalrock.checkers.Checkers.boardSideLength;
import static com.example.computerapp_aboalrock.checkers.Checkers.boardSquaresNumber;
import static com.example.computerapp_aboalrock.checkers.Checkers.flyingKings;
import static com.example.computerapp_aboalrock.checkers.Checkers.kingChooseWhereToLandAfterJump;
import static com.example.computerapp_aboalrock.checkers.Checkers.whiteKings;
import static com.example.computerapp_aboalrock.checkers.Checkers.whiteMen;

import android.content.Context;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.computerapp_aboalrock.R;
import com.example.computerapp_aboalrock.checkers.Checkers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
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


    public ArrayList<Integer> getUsedMoveDirection() {
        ArrayList<Integer> clone = new ArrayList<>(usedMoveDirection);
        if(isKing&&flyingKings){
            for (int possibleMove:new ArrayList<>(usedMoveDirection)){
                while(pieceId+possibleMove < boardSquaresNumber && pieceId+possibleMove>=0){
                    clone.add(pieceId+possibleMove);
                    possibleMove+=possibleMove;
                }
            }
        }
        ArrayList<Integer> toRemoveList = new ArrayList<>();
        for (Integer cloneInt : clone) {
            if (cloneInt == 0) {
                toRemoveList.add(cloneInt);
                continue;
            }
            int checkingPossibleMoveIndex = cloneInt + pieceId;
            if (!(checkingPossibleMoveIndex < boardSquaresNumber) || !(checkingPossibleMoveIndex >= 0)
                    || (checkingPossibleMoveIndex / boardSideLength) != (pieceId / boardSideLength) + Math.abs(cloneInt) / cloneInt) {
                toRemoveList.add(cloneInt);
            } else {
                Square targetSquare = checkers.findSquareById(checkingPossibleMoveIndex);
                if (targetSquare == null || targetSquare.isOccupied()) {
                    toRemoveList.add(cloneInt);
                }
            }
        }
        clone.removeAll(toRemoveList);
        return clone;
    }

    public boolean manCanJump(ArrayList<Integer> lastCapturesList){
        HashMap<Integer, ArrayList<Integer>> checkIfEmpty = this.getUsedJumpDirection(this.pieceId, lastCapturesList);
        return !(checkIfEmpty==null || checkIfEmpty.isEmpty());
    }

    public HashMap<Integer, ArrayList<Integer>> getUsedJumpDirection(int startingToJumpIndex,ArrayList<Integer> lastCapturesList) {
        HashMap<Integer, ArrayList<Integer>> capturesMap = new HashMap<>();
        for (Integer jumpInteger : this.jumpDirection) {
            Predicate<Integer> movementPredicate;
            Predicate<Integer> isWithinBound = (squareId)->
                    squareId >= 0
                    && squareId < boardSquaresNumber;
            if (jumpInteger == 1 || jumpInteger == -1) {
                // Horizontal
                movementPredicate = (squareId) ->
                        // same row
                        (squareId / boardSideLength) == (startingToJumpIndex / boardSideLength)
                                && squareId >= 0
                                && squareId < boardSquaresNumber;
            } else if (jumpInteger == boardSideLength || jumpInteger == -boardSideLength) {
                // Vertical
                movementPredicate = (squareId) ->
                        // same column
                        (squareId % boardSideLength) == (startingToJumpIndex % boardSideLength)&& isWithinBound.test(squareId);
            } else {
                // Diagonal
                movementPredicate = (squareId) ->
                    // row/column difference check
                    Math.abs(squareId / boardSideLength - (squareId - jumpInteger) / boardSideLength) ==
                            Math.abs(squareId % boardSideLength - (squareId - jumpInteger) % boardSideLength)
                            && isWithinBound.test(squareId);
            }
            HashMap<Integer, ArrayList<Integer>> mapResult = scanInDirection(jumpInteger, movementPredicate,this,lastCapturesList);
            if (mapResult != null && !mapResult.isEmpty()) {
                capturesMap.putAll(mapResult);
            }

        }
    /**  @param capturesMap                The map of captured squares to possible landing squares
*/

        return capturesMap ;
    }

    /**

     * Helper method to scan for captures in a given direction.
     * @param jumpInteger                    The direction increment (e.g. +1, -1, +8, -8, +7, -7, +9, -9, etc.)
     * @param isLegalSquare                  A predicate that returns true if the next square is valid for movement
     * @return A HashMap for the direction that called it which contains  the capturedEnemyPiece Index as key and trhe possible landings after the jump in the same Direction
     */
    private HashMap<Integer, ArrayList<Integer>> scanInDirection(int jumpInteger, Predicate<Integer> isLegalSquare, Piece piece,ArrayList<Integer> lastCapturesList) {
        // We'll keep a temporary list for possible landing squares in this direction
        ArrayList<Integer> possibleLandingSquaresIndexes = new ArrayList<>();
        HashMap<Integer, ArrayList<Integer>> eatenSquareAndPossibleLandings= new HashMap<>();
        int loopCounter = 1;
        int possibleCapturedSquare = piece.pieceId + loopCounter * jumpInteger;
        if(!(this.isKing&&flyingKings)){
            if(isLegalSquare.test(possibleCapturedSquare)&&isLegalSquare.test(possibleCapturedSquare+jumpInteger)){
                if (!lastCapturesList.contains(possibleCapturedSquare)){
                    if (checkers.findSquareById(possibleCapturedSquare).isOccupied() && (!checkers.findSquareById(possibleCapturedSquare+jumpInteger).isOccupied()||lastCapturesList.contains(possibleCapturedSquare+jumpInteger))){
                        if(checkers.findSquareById(possibleCapturedSquare).getPiece().isBlack!=piece.isBlack){
                            possibleLandingSquaresIndexes.add(possibleCapturedSquare+jumpInteger);
                            eatenSquareAndPossibleLandings.put(possibleCapturedSquare,possibleLandingSquaresIndexes);
                        }
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
                if (sq.getPiece().isBlack == piece.isBlack) {
                    break;
                } else {
                    // It's an enemy piece, see if the landing square after the enemy is free
                    possibleCapturedSquare += jumpInteger;
                    if (isLegalSquare.test(possibleCapturedSquare)) {
                        Square landingSq = checkers.findSquareById(possibleCapturedSquare);
                        // If that landing square is occupied, no capture possible in this direction
                        if (landingSq.isOccupied()&& !lastCapturesList.contains(possibleCapturedSquare)) {
                            break;
                        }
                    } else {
                        break;
                    }
                    int capturedSquare = possibleCapturedSquare - jumpInteger;

                    possibleLandingSquaresIndexes.add(possibleCapturedSquare);

                    if (kingChooseWhereToLandAfterJump) {
                        possibleCapturedSquare += jumpInteger;
                        while (isLegalSquare.test(possibleCapturedSquare)) {
                            Square extraSq = checkers.findSquareById(possibleCapturedSquare);
                            if (extraSq.isOccupied()&&!lastCapturesList.contains(possibleCapturedSquare)) {break;}
                            possibleLandingSquaresIndexes.add(possibleCapturedSquare);
                            possibleCapturedSquare += jumpInteger;
                        }
                    }
                    eatenSquareAndPossibleLandings.put(capturedSquare, new ArrayList<>(possibleLandingSquaresIndexes));
                    break;
                }
            }
            loopCounter++;
            possibleCapturedSquare = piece.pieceId + loopCounter * jumpInteger;
        }
        return eatenSquareAndPossibleLandings;
    }

    public boolean manCanMove() {
        if (this.pieceId == null) return false;
        ArrayList<Integer> validDirections = this.getUsedMoveDirection();

        if (validDirections.isEmpty()) {
            return false;
        } else {
            ArrayList<Square> possibleMoveSquares = new ArrayList<>();
            for (int direction : validDirections) {
                int landIndex = this.pieceId + direction;
                Square targetSquare = checkers.findSquareById(landIndex);

                if (targetSquare != null && !targetSquare.isOccupied()) {
                    possibleMoveSquares.add(targetSquare);
                }

                if(isKing&&flyingKings){
                    while (targetSquare != null && !targetSquare.isOccupied()) {
                        possibleMoveSquares.add(targetSquare);

                    }
                }

            }

            if (!possibleMoveSquares.isEmpty()) addAvailableMoveViews(possibleMoveSquares);
            return !possibleMoveSquares.isEmpty();

        }
    }
    public void addAvailableMoveViews(ArrayList<Square> possibleMoveSquares) {
        Square currentSquare=checkers.findSquareById(this.pieceId);
        currentSquare.markPieceCanMove();

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


    public ArrayList<Integer> getJumpDirection() {
        return jumpDirection;
    }

    public void setJumpDirection(ArrayList<Integer> jumpDirection) {
        this.jumpDirection = jumpDirection;
    }

    public void upgradePiece() {
        if(!isKing){
            this.isKing = true;
            this.usedMoveDirection = new ArrayList<>(Arrays.asList(
                    boardSideLength + 1, boardSideLength - 1,
                    -(boardSideLength + 1), -(boardSideLength - 1)
            ));
            this.jumpDirection = new ArrayList<>(this.usedMoveDirection);

            // Change the drawable to king
            if (this.isBlack) {
                this.pieceButton.setBackgroundResource(R.drawable.blackking);
            } else {
                this.pieceButton.setBackgroundResource(R.drawable.whiteking);
            }
            ArrayList <Piece> addKing= isBlack? blackKings:whiteKings;
            addKing.add(this);
            ArrayList <Piece> removeMen= isBlack? blackMen:whiteMen;
            removeMen.remove(this);
        }

    }

}
