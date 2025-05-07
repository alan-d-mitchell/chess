package com.programming.chess.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class gameState {

    private String currentPlayer;
    private List<Move> moveHistory; 
    private boolean activeGame;
    private String statusMessage;
    private boolean isInCheck = false;
    private boolean isInCheckMate = false;
    
    // Track whether kings and rooks have moved (needed for castling)
    private Map<String, Boolean> pieceHasMoved;

    private static gameState instance;

    private gameState() {
        currentPlayer = "W";
        moveHistory = new ArrayList<>();
        activeGame = true;
        statusMessage = "White's turn to move";
        pieceHasMoved = new HashMap<>();
        resetPieceMovementTracking();
    }

    public static gameState getInstance() {
        if (instance == null) {
            instance = new gameState();
        }

        return instance;
    }
    
    private void resetPieceMovementTracking() {
        // Initialize tracking for kings and rooks (used for castling)
        pieceHasMoved.put("kingW", false);
        pieceHasMoved.put("kingB", false);
        
        // Track rooks by position: [color][side] -> kingside = 0, queenside = 7
        pieceHasMoved.put("rookW_0", false); // White kingside rook (h1)
        pieceHasMoved.put("rookW_7", false); // White queenside rook (a1)
        pieceHasMoved.put("rookB_0", false); // Black kingside rook (h8)
        pieceHasMoved.put("rookB_7", false); // Black queenside rook (a8)
    }

    public void resetGame() {
        currentPlayer = "W";
        moveHistory.clear();
        activeGame = true;
        statusMessage = "White's turn to move";
        isInCheck = false;
        isInCheckMate = false;
        resetPieceMovementTracking();
    }

    public void makeMove(int fromRow, int fromCol, int toRow, int toCol, String piece, String capturedPiece) {
        Move move = new Move(fromRow, fromCol, toRow, toCol, piece, capturedPiece);
        moveHistory.add(move);
        
        // Track piece movement for castling
        if (piece.startsWith("king")) {
            pieceHasMoved.put(piece, true);
            
            // Handle castling move - move the rook as well
            if (Math.abs(fromCol - toCol) == 2) {
                // This is a castling move
                move.setCastling(true);
                
                // Determine rook positions based on castling side
                if (toCol > fromCol) {
                    // Kingside castling
                    move.setCastlingSide("kingside");
                } else {
                    // Queenside castling
                    move.setCastlingSide("queenside");
                }
            }
        } 
        else if (piece.startsWith("rook")) {
            // Track which rook has moved
            String rookKey = piece + "_" + fromCol;
            pieceHasMoved.put(rookKey, true);
        }

        switchTurn();

        // The status message will be updated when check status is set
        statusMessage = (currentPlayer.equals("W") ? "White" : "Black") + "'s turn to move";
    }

    /**
     * Set the check and checkmate status
     */
    public void setCheckStatus(boolean inCheck, boolean inCheckMate) {
        this.isInCheck = inCheck;
        this.isInCheckMate = inCheckMate;
        
        // Update status message
        if (isInCheckMate) {
            statusMessage = (currentPlayer.equals("W") ? "White" : "Black") + " is in checkmate! " +
                            (currentPlayer.equals("W") ? "Black" : "White") + " wins!";
            activeGame = false;
        } else if (isInCheck) {
            statusMessage = (currentPlayer.equals("W") ? "White" : "Black") + " is in check!";
        } else {
            statusMessage = (currentPlayer.equals("W") ? "White" : "Black") + "'s turn to move";
        }
    }
    
    /**
     * Check if the current player is in check
     */
    public boolean isInCheck() {
        return isInCheck;
    }

    /**
     * Check if the current player is in checkmate
     */
    public boolean isInCheckMate() {
        return isInCheckMate;
    }

    /**
     * Check if castling is possible
     * @param color Color of the king ("W" or "B")
     * @param side "kingside" or "queenside"
     */
    public boolean canCastle(String color, String side) {
        // Check if king has moved
        if (pieceHasMoved.get("king" + color)) {
            return false;
        }
        
        // Check if rook has moved (column 0 for kingside, 7 for queenside)
        int rookCol = side.equals("kingside") ? 0 : 7;
        if (pieceHasMoved.get("rook" + color + "_" + rookCol)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Get castling rook move details when king performs castling
     * @param kingToCol The destination column of the king
     * @param color The color of the pieces
     * @return Array with [rookFromRow, rookFromCol, rookToRow, rookToCol]
     */
    public int[] getCastlingRookMove(int kingToCol, String color) {
        int kingRow = color.equals("W") ? 7 : 0;
        
        if (kingToCol == 6) {
            // Kingside castling (O-O)
            return new int[] {kingRow, 7, kingRow, 5}; // Rook from h1/h8 to f1/f8
        } else if (kingToCol == 2) {
            // Queenside castling (O-O-O)
            return new int[] {kingRow, 0, kingRow, 3}; // Rook from a1/a8 to d1/d8
        }
        
        return null;
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

    public Move createTempMove(int fromRow, int fromCol, int toRow, int toCol, String piece, String capturedPiece) {
        return new Move(fromRow, fromCol, toRow, toCol, piece, capturedPiece);
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
        public int fromRow, fromCol, toRow, toCol;
        public String piece;
        public String capturedPiece;
        private boolean isCastling = false;
        private String castlingSide = null;
        
        public Move(int fromRow, int fromCol, int toRow, int toCol, String piece, String capturedPiece) {
            this.fromRow = fromRow;
            this.fromCol = fromCol;
            this.toRow = toRow;
            this.toCol = toCol;
            this.piece = piece;
            this.capturedPiece = capturedPiece;
        }
        
        public void setCastling(boolean isCastling) {
            this.isCastling = isCastling;
        }
        
        public void setCastlingSide(String side) {
            this.castlingSide = side;
        }
        
        public boolean isCastling() {
            return isCastling;
        }
        
        public String getCastlingSide() {
            return castlingSide;
        }
        
        @Override
        public String toString() {
            // Convert to chess notation
            char fromFile = (char)('a' + fromCol);
            int fromRank = 8 - fromRow;
            char toFile = (char)('a' + toCol);
            int toRank = 8 - toRow;
            
            if (isCastling) {
                return castlingSide.equals("kingside") ? "O-O" : "O-O-O";
            }
            
            return piece + ": " + fromFile + fromRank + " to " + toFile + toRank + 
                  (capturedPiece != null ? " captures " + capturedPiece : "");
        }
    }
}