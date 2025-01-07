package com.example.computerapp_aboalrock.checkers;

/**
 * Represents a node in the jump sequence tree.
 */
public class JumpNode {
    private final int startingSquareIndex;
    private int capturedSquare;
    private JumpNode parent;
    private JumpNode leftUp;
    private JumpNode leftDown;
    private JumpNode rightUp;
    private JumpNode rightDown;

    public void setCapturedSquare(int capturedSquare) {
        this.capturedSquare = capturedSquare;
    }

    public int getCapturedSquare() {
        return capturedSquare;
    }

    public JumpNode(int startingSquareIndex, int capturedSquare) {
        this.startingSquareIndex = startingSquareIndex;
        this.capturedSquare=capturedSquare;
    }


    public int getStartingSquareIndex() {
        return startingSquareIndex;
    }
    public JumpNode getParent() {
        return parent;
    }

    public void setParent(JumpNode parent) {
        this.parent = parent;
    }


    public JumpNode getLeftUp() {
        return leftUp;
    }

    public void setLeftUp(JumpNode leftUp) {
        this.leftUp = leftUp;
    }


    public JumpNode getLeftDown() {
        return leftDown;
    }

    public void setLeftDown(JumpNode leftDown) {
        this.leftDown = leftDown;
    }


    public JumpNode getRightUp() {
        return rightUp;
    }


    public void setRightUp(JumpNode rightUp) {
        this.rightUp = rightUp;
    }


    public JumpNode getRightDown() {
        return rightDown;
    }

    public void setRightDown(JumpNode rightDown) {
        this.rightDown = rightDown;
    }

}
