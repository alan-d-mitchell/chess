package com.programming.chess.engine.rules;

import java.util.ArrayList;
import java.util.List;

public class gameState {

    private String currentPlayer;
    private List<Move> moveHistory; 
    private boolean activeGame;
    private String statusMessage;

    private static gameState instance;

    private gameState() {
        currentPlayer = "W";
        moveHistory = new ArrayList<>();
        activeGame = true;
        statusMessage = "White's turn to move";
    }

    public static gameState getInstance() {
        if (instance == null) {
            instance = new gameState();
        }

        return instance;
    }

    public void resetGame() {
        currentPlayer = "W";
        moveHistory.clear();
        activeGame = true;
        statusMessage = "White's turn to move";
    }

    public void makeMove(int fromRow, int fromCol, int toRow, int toCol, String piece, String capturedPiece) {
        Move move = new Move(fromRow, fromCol, toRow, toCol, piece, capturedPiece);
        moveHistory.add(move);

        switchTurn();

        statusMessage = (currentPlayer.equals("W") ? "White" : "Black") + "'s turn to move";
    }

    /**
     * Switch the current player
     */
    private void switchTurn() {
        currentPlayer = currentPlayer.equals("W") ? "B" : "W";
    }
    
    /**
     * Get the current player
     */
    public String getCurrentPlayer() {
        return currentPlayer;
    }
    
    /**
     * Get the game status message
     */
    public String getStatusMessage() {
        return statusMessage;
    }
    
    /**
     * Set a custom status message
     */
    public void setStatusMessage(String message) {
        statusMessage = message;
    }
    
    /**
     * Check if a piece belongs to the current player
     */
    public boolean isCurrentPlayersPiece(String piece) {
        if (piece == null) return false;
        String pieceColor = piece.substring(piece.length() - 1);
        return pieceColor.equals(currentPlayer);
    }

    public class Move {
        private int fromRow, fromCol, toRow, toCol;
        private String piece;
        private String capturedPiece;
        
        public Move(int fromRow, int fromCol, int toRow, int toCol, String piece, String capturedPiece) {
            this.fromRow = fromRow;
            this.fromCol = fromCol;
            this.toRow = toRow;
            this.toCol = toCol;
            this.piece = piece;
            this.capturedPiece = capturedPiece;
        }
        
        @Override
        public String toString() {
            // Convert to chess notation
            char fromFile = (char)('a' + fromCol);
            int fromRank = 8 - fromRow;
            char toFile = (char)('a' + toCol);
            int toRank = 8 - toRow;
            
            return piece + ": " + fromFile + fromRank + " to " + toFile + toRank + 
                  (capturedPiece != null ? " captures " + capturedPiece : "");
        }
    }

}
