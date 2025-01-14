package com.example.computerapp_aboalrock.checkers;

import android.util.Log;

import com.example.computerapp_aboalrock.R;
import static com.example.computerapp_aboalrock.checkers.Checkers.currentJumpsNode;
import static com.example.computerapp_aboalrock.checkers.Checkers.blackMen;
import static com.example.computerapp_aboalrock.checkers.Checkers.boardSideLength;
import android.util.Log;
import static com.example.computerapp_aboalrock.checkers.Checkers.boardSquaresNumber;
import static com.example.computerapp_aboalrock.checkers.Checkers.flyingKings;
import static com.example.computerapp_aboalrock.checkers.Checkers.kingChooseWhereToLandAfterJump;
import static com.example.computerapp_aboalrock.checkers.Checkers.whiteMen;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;
import android.graphics.Color; // Make sure to import this at the top of your file


/**
 * Represents a node in the jump sequence tree.
 */
public class JumpNode {
    private int startingSquareIndex;
    private JumpNode parent;
    private ArrayList<Integer> previousCapturedSquaresList;
    private ArrayList<Integer> possibleLandingSquares;
    private HashMap<Integer, ArrayList<Integer>> possibleContinuation;
    private ArrayList<JumpNode> possibleSequences;
    private Checkers checkers;

    public JumpNode(Piece piece, int startingIndex, ArrayList<Integer> previousCaptures, JumpNode parent, Checkers checkers) {
        Log.d("possibleSequences", "JumpNode has been triggered");
        this.possibleSequences = new ArrayList<>();
        if (previousCaptures == null) previousCaptures = new ArrayList<>();
        this.previousCapturedSquaresList = new ArrayList<>(previousCaptures);
        this.parent = parent;
        this.startingSquareIndex = startingIndex;
        this.checkers = checkers;

        possibleContinuation = piece.getUsedJumpDirection(startingIndex, previousCapturedSquaresList);

        for (int willCapturedSquare : possibleContinuation.keySet()) {
            this.possibleLandingSquares = possibleContinuation.get(willCapturedSquare);
            if (this.possibleLandingSquares == null) this.possibleLandingSquares = new ArrayList<>();
            for (int landingSquare : possibleLandingSquares) {
                ArrayList<Integer> capturesToSend = new ArrayList<>(previousCapturedSquaresList);
                capturesToSend.add(willCapturedSquare);
                this.possibleSequences.add(new JumpNode(piece, landingSquare, capturesToSend, this, checkers));
            }
        }
    }

    public void findLongestRoute(JumpNode currentJumpNode){
        ArrayList<String> copy=new ArrayList<>();
        JumpNode toAdjust=currentJumpNode;
        while(toAdjust.parent!=null){
            toAdjust=toAdjust.parent;
        }
        routeLength(toAdjust,"",copy);
        ArrayList<String> lengths=new ArrayList<>();
        int max=0;
        for (String length:copy){
            int temp=length.length();
            if(temp<max)continue;
            if(temp>max){
                max=temp;
                lengths.clear();
            }
            lengths.add(length);
        }
        Log.d("length","lengthList contains"+lengths);
        Log.d("length", String.valueOf(currentJumpNode.possibleSequences.size()));
        filterRodes(lengths);


    }

    private void filterRodes(ArrayList<String> lengths) {
        if (lengths == null || lengths.isEmpty()) return;
        if (lengths.get(0).isEmpty())return;
        ArrayList<Integer> toSave = new ArrayList<>();
        ArrayList<String> lengthsToSend = new ArrayList<>();

        int prev = Integer.parseInt(lengths.get(0).substring(0, 1));
        int curr=-1;
        for (String route : lengths) {
            if(route.isEmpty())continue;
            curr = Integer.parseInt(route.substring(0, 1));
            if (curr == prev) {
                lengthsToSend.add(route.substring(1));
            } else {
                this.possibleSequences.get(prev).filterRodes(lengthsToSend);
                lengthsToSend.clear();
                lengthsToSend.add(route.substring(1));
                toSave.add(prev);
                prev = curr;
            }
        }
        this.possibleSequences.get(curr).filterRodes(lengthsToSend);
        toSave.add(curr);
        for (int i = this.possibleSequences.size() - 1; i >= 0; i--) {
            if(!toSave.contains(i)) this.possibleSequences.remove(i);
            if(!toSave.contains(i)) this.possibleContinuation.remove(i);

        }
    }
/**
 * @param stringToIntegerToSubmit represents the path by which we reach the latest jump Node for all possible Sequences, for example one path is parentNode -> possibleSequence[2] -> possibleSequence[5] will return 136
 * */
    public void routeLength(JumpNode currNode, String stringToIntegerToSubmit, ArrayList<String> lengths){
        if(currNode.possibleSequences==null || currNode.possibleSequences.isEmpty()){
            lengths.add(stringToIntegerToSubmit);
        }
        for (int i=0;i<currNode.possibleSequences.size();i++){
            routeLength(currNode.possibleSequences.get(i),stringToIntegerToSubmit+String.valueOf(i),lengths);
        }
    }
    public void markJumpNode() {
        if (!this.possibleContinuation.keySet().isEmpty()) {
            for (int possibleCaptureIndex : this.possibleContinuation.keySet()) {
                Square capturedSquare = checkers.findSquareById(possibleCaptureIndex);
                capturedSquare.squarePossibleMarkerSquare(R.drawable.willbeeaten);
            }
        }

        if (this.possibleSequences != null && !this.possibleSequences.isEmpty()) {
            for (JumpNode childNode : this.possibleSequences) {
                Square childJumpNodeSquare = checkers.findSquareById(childNode.startingSquareIndex);
                childJumpNodeSquare.squarePossibleMarkerSquare(childNode.possibleSequences.isEmpty()?(R.drawable.canlandafterjumpsquare):(R.drawable.can_jump_again));
                if(this.possibleSequences.size()>1)childJumpNodeSquare.getPossibleMovesMarks().setBackgroundColor(Color.parseColor("#111111"));
                childJumpNodeSquare.getPossibleMovesMarks().setOnClickListener(v->{
                    childNode.removeIrrelevantParents();
                    if(!currentJumpsNode.isEmpty())checkers.findSquareById(childNode.startingSquareIndex).markPieceCanMove();
                });

                childNode.markJumpNode();
            }
        }
    }
    public void removeIrrelevantParents(){
        if(this.parent==null)return;
        JumpNode parent=this.parent;
        parent.possibleSequences.remove(this);

        this.parent=null;
        int x=this.startingSquareIndex;
        Square currentNodeSquare=checkers.findSquareById(x);

        /// remove all captured pieces in the path leading to this square(aka JumpNode)
        while(parent!=null){
            for (int capturedIndex : parent.possibleContinuation.keySet()){
                if(Objects.requireNonNull(parent.possibleContinuation.get(capturedIndex)).contains(x)){
                    Piece piece=checkers.findSquareById(capturedIndex).removePiece();
                    (piece.getIsBlack()? blackMen:whiteMen).remove(piece);
                    break;
                }
            }
            x=parent.startingSquareIndex;
            parent=parent.parent;
        }
        /// all leading captured Squares have been removed


        /// set the piece from the starting square to the square that was clicked
        Piece piece=checkers.findSquareById(x).removePiece();
        piece.getPieceButton().setOnClickListener(v->this.markJumpNode());
        currentNodeSquare.setPiece(piece);



        ArrayList <JumpNode> copyCurrentJumpsNode=new ArrayList<>(currentJumpsNode);
        for (JumpNode toSend:copyCurrentJumpsNode){
            Square square=checkers.findSquareById(toSend.startingSquareIndex);
            if (square.isOccupied()){
                Piece checkPiece=square.getPiece();
                if(checkPiece.getIsBlack()==piece.getIsBlack())checkPiece.getPieceButton().setOnClickListener(null);
            }
            currentJumpsNode.remove(toSend);
            removeNode(toSend);
        }

        if(this.possibleContinuation!=null && !this.possibleContinuation.isEmpty())currentJumpsNode.add(this);
        if(currentJumpsNode.isEmpty()&&possibleSequences.isEmpty()){
            Square square=checkers.findSquareById(piece.getPieceId());
            if(piece.getPieceId()/boardSideLength == (piece.getIsBlack()? 0:boardSideLength-1)) piece.upgradePiece();
            square.removePieceCanMoveMarker();
            checkers.nextTurn();

        }
    }
    public void removeNode(JumpNode toRemoveNode){
        for ( int capturedIndex : toRemoveNode.possibleContinuation.keySet()){
            for(int landingIndex : toRemoveNode.possibleContinuation.get(capturedIndex)){
                checkers.findSquareById(landingIndex).removeAllMarkers();
            }
            checkers.findSquareById(capturedIndex).removeAllMarkers();
        }
        toRemoveNode.possibleContinuation.clear();
        checkers.findSquareById(toRemoveNode.startingSquareIndex).removeAllMarkers();
        for (JumpNode callBack: toRemoveNode.possibleSequences){
            removeNode(callBack);
        }
    }
}
