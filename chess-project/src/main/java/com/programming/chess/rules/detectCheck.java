package com.programming.chess.rules;

public class detectCheck {
    
    /**
     * Determines if the specified player's king is in check
     * @param board The current board state
     * @param playerColor The color of the player to check ("W" or "B")
     * @return true if the king is in check, false otherwise
     */
    public static boolean isCheck(String[][] board, String playerColor) {
        // Find the king's position
        int[] kingPosition = findKingPosition(board, playerColor);
        if (kingPosition == null) {
            return false; // King not found (should not happen in a valid game)
        }
        
        int kingRow = kingPosition[0];
        int kingCol = kingPosition[1];
        
        // Opponent's color
        String opponentColor = playerColor.equals("W") ? "B" : "W";
        
        // Check if any opponent piece can attack the king
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                String piece = board[row][col];
                
                // Skip empty squares and pieces of the same color
                if (piece == null || !piece.endsWith(opponentColor)) {
                    continue;
                }
                
                // Check if this opponent piece can move to the king's position
                if (validateMove.isValidMove(board, row, col, kingRow, kingCol, null, false)) {
                    return true; // King is in check
                }
            }
        }
        
        return false; // King is not in check
    }
    
    /**
     * Determines if the specified player is in checkmate
     * @param board The current board state
     * @param playerColor The color of the player to check ("W" or "B")
     * @return true if the player is in checkmate, false otherwise
     */
    public static boolean isCheckMate(String[][] board, String playerColor) {
        // First, check if the king is in check
        if (!isCheck(board, playerColor)) {
            return false; // Not in checkmate if not in check
        }
        
        // Check if any legal move can get the player out of check
        // For each piece of the player
        for (int fromRow = 0; fromRow < board.length; fromRow++) {
            for (int fromCol = 0; fromCol < board[0].length; fromCol++) {
                String piece = board[fromRow][fromCol];
                
                // Skip empty squares and opponent's pieces
                if (piece == null || !piece.endsWith(playerColor)) {
                    continue;
                }
                
                // Try all possible destination squares
                for (int toRow = 0; toRow < board.length; toRow++) {
                    for (int toCol = 0; toCol < board[0].length; toCol++) {
                        // Skip if it's the same position
                        if (fromRow == toRow && fromCol == toCol) {
                            continue;
                        }
                        
                        // Check if this is a valid move
                        if (validateMove.isValidMove(board, fromRow, fromCol, toRow, toCol, piece, true)) {
                            // Make a temporary move
                            String[][] tempBoard = copyBoard(board);
                            tempBoard[toRow][toCol] = piece;
                            tempBoard[fromRow][fromCol] = null;
                            
                            // Check if this move gets the king out of check
                            if (!isCheck(tempBoard, playerColor)) {
                                return false; // Found a move that prevents checkmate
                            }
                        }
                    }
                }
            }
        }
        
        return true; // No legal moves to escape check
    }
    
    /**
     * Helper method to find the king's position on the board
     * @param board The current board state
     * @param playerColor The color of the king to find ("W" or "B")
     * @return int array with [row, col] of the king's position, or null if not found
     */
    private static int[] findKingPosition(String[][] board, String playerColor) {
        String kingPiece = "king" + playerColor;
        
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[0].length; col++) {
                if (kingPiece.equals(board[row][col])) {
                    return new int[] {row, col};
                }
            }
        }
        
        return null; // King not found (should not happen in a valid game)
    }
    
    /**
     * Helper method to create a copy of the board
     */
    private static String[][] copyBoard(String[][] board) {
        String[][] copy = new String[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, board[i].length);
        }
        return copy;
    }
    
    /**
     * Checks if a move would leave the player's king in check
     * @param board The current board state
     * @param fromRow Starting row
     * @param fromCol Starting column
     * @param toRow Ending row
     * @param toCol Ending column
     * @param piece The piece being moved
     * @return true if the move is legal (doesn't leave king in check), false otherwise
     */
    public static boolean isMoveLegal(String[][] board, int fromRow, int fromCol, int toRow, int toCol, String piece) {
        // Create a temporary board to simulate the move
        String[][] tempBoard = copyBoard(board);
        
        // Make the move on the temporary board
        tempBoard[toRow][toCol] = piece;
        tempBoard[fromRow][fromCol] = null;
        
        // Get the player's color
        String playerColor = piece.substring(piece.length() - 1);
        
        // Check if the player's king is in check after the move
        return !isCheck(tempBoard, playerColor);
    }
}