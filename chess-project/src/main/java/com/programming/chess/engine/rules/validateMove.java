package com.programming.chess.engine.rules;

public class validateMove {
    
    public static boolean canMove() {
        // Check if a piece can move and will call isMoveValid()
        boolean canMove = true; // init pawns and knights can only move

        return canMove;
    }

    private static boolean isMoveValid() { // will probably have to call detechCheck to make sure the kind isnt in check and gameState to see surrounding pieces and what not
        boolean isValid = false;

        return isValid;
    }
}
