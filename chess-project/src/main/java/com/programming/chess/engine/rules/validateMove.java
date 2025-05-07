package com.programming.chess.engine.rules;

public class validateMove {
    
    /**
     * Checks if a move from source to destination is valid for a specific piece
     * 
     * @param board The current board state
     * @param fromRow Source row
     * @param fromCol Source column
     * @param toRow Destination row
     * @param toCol Destination column
     * @param draggedPiece The piece being moved (needed for drag operations)
     * @return true if the move is valid, false otherwise
     */
    public static boolean isValidMove(String[][] board, int fromRow, int fromCol, int toRow, int toCol, String draggedPiece) {
        // Don't allow moving to the same square
        if (fromRow == toRow && fromCol == toCol) {
            return false;
        }
        
        if (fromRow < 0 || fromRow >= board.length || fromCol < 0 || fromCol >= board[0].length) {
            return false; // Invalid source position
        }
        
        if (toRow < 0 || toRow >= board.length || toCol < 0 || toCol >= board[0].length) {
            return false; // Invalid destination position
        }
        
        String piece = (board[fromRow][fromCol] != null) ? board[fromRow][fromCol] : draggedPiece;
        if (piece == null) {
            return false; // No piece at source position and no dragged piece
        }
        
        // Check if destination has a piece of the same color
        String destPiece = board[toRow][toCol];
        if (destPiece != null) {
            // Can't capture your own piece
            if (getPieceColor(piece).equals(getPieceColor(destPiece))) {
                return false;
            }
        }
        
        // Get piece type without color (e.g., "pawnW" -> "pawn")
        String pieceType = piece.substring(0, piece.length() - 1);
        String pieceColor = getPieceColor(piece);
        
        // Validate move based on piece type
        return switch (pieceType) {
            case "pawn" -> isValidPawnMove(board, fromRow, fromCol, toRow, toCol, pieceColor);
            case "rook" -> isValidRookMove(board, fromRow, fromCol, toRow, toCol);
            case "knight" -> isValidKnightMove(fromRow, fromCol, toRow, toCol);
            case "bishop" -> isValidBishopMove(board, fromRow, fromCol, toRow, toCol);
            case "queen" -> isValidQueenMove(board, fromRow, fromCol, toRow, toCol);
            case "king" -> isValidKingMove(fromRow, fromCol, toRow, toCol);
            default -> false;
        };
    }
    
    /**
     * Simplified version for non-drag operations
     */
    public static boolean isValidMove(String[][] board, int fromRow, int fromCol, int toRow, int toCol) {
        return isValidMove(board, fromRow, fromCol, toRow, toCol, null);
    }
    
    /**
     * Simple method referenced in the chessBoard class
     */
    public static void canMove() {
        System.out.println("Move validation will be applied here");
    }
    
    /**
     * Get the color of a piece from its identifier
     */
    public static String getPieceColor(String piece) {
        return piece.substring(piece.length() - 1);  // "W" for white, "B" for black
    }
    
    // MOVEMENT VALIDATION METHODS FOR EACH PIECE TYPE
    
    private static boolean isValidPawnMove(String[][] board, int fromRow, int fromCol, int toRow, int toCol, String pieceColor) {
        // Direction depends on color (white pawns move up, black pawns move down)
        int direction = pieceColor.equals("W") ? -1 : 1;
        
        // Check for standard move (1 square forward)
        if (fromCol == toCol && toRow == fromRow + direction && board[toRow][toCol] == null) {
            return true;
        }
        
        // Check for initial 2-square move
        boolean isInitialPosition = (pieceColor.equals("W") && fromRow == 6) || 
                                   (pieceColor.equals("B") && fromRow == 1);
        if (isInitialPosition && fromCol == toCol && toRow == fromRow + 2 * direction &&
            board[toRow][toCol] == null && board[fromRow + direction][fromCol] == null) {
            return true;
        }
        
        // Check for diagonal capture
        if (Math.abs(fromCol - toCol) == 1 && toRow == fromRow + direction && 
            board[toRow][toCol] != null && !getPieceColor(board[toRow][toCol]).equals(pieceColor)) {
            return true;
        }
        
        return false;
    }
    
    private static boolean isValidRookMove(String[][] board, int fromRow, int fromCol, int toRow, int toCol) {
        // Rook moves horizontally or vertically
        if (fromRow != toRow && fromCol != toCol) {
            return false;
        }
        
        // Check for pieces in the way
        if (fromRow == toRow) {
            // Horizontal move
            int start = Math.min(fromCol, toCol);
            int end = Math.max(fromCol, toCol);
            for (int col = start + 1; col < end; col++) {
                if (board[fromRow][col] != null) {
                    return false; // Path is blocked
                }
            }
        } else {
            // Vertical move
            int start = Math.min(fromRow, toRow);
            int end = Math.max(fromRow, toRow);
            for (int row = start + 1; row < end; row++) {
                if (board[row][fromCol] != null) {
                    return false; // Path is blocked
                }
            }
        }
        
        return true;
    }
    
    private static boolean isValidKnightMove(int fromRow, int fromCol, int toRow, int toCol) {
        // Knight moves in an L-shape
        int rowDiff = Math.abs(fromRow - toRow);
        int colDiff = Math.abs(fromCol - toCol);
        
        return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
    }
    
    private static boolean isValidBishopMove(String[][] board, int fromRow, int fromCol, int toRow, int toCol) {
        // Bishop moves diagonally
        if (Math.abs(fromRow - toRow) != Math.abs(fromCol - toCol)) {
            return false;
        }
        
        // Check for pieces in the way
        int rowStep = fromRow < toRow ? 1 : -1;
        int colStep = fromCol < toCol ? 1 : -1;
        
        int row = fromRow + rowStep;
        int col = fromCol + colStep;
        
        while (row != toRow && col != toCol) {
            if (board[row][col] != null) {
                return false; // Path is blocked
            }
            row += rowStep;
            col += colStep;
        }
        
        return true;
    }
    
    private static boolean isValidQueenMove(String[][] board, int fromRow, int fromCol, int toRow, int toCol) {
        // Queen combines rook and bishop moves
        return isValidRookMove(board, fromRow, fromCol, toRow, toCol) || 
               isValidBishopMove(board, fromRow, fromCol, toRow, toCol);
    }
    
    private static boolean isValidKingMove(int fromRow, int fromCol, int toRow, int toCol) {
        // King moves one square in any direction
        int rowDiff = Math.abs(fromRow - toRow);
        int colDiff = Math.abs(fromCol - toCol);
        
        return rowDiff <= 1 && colDiff <= 1 && !(rowDiff == 0 && colDiff == 0);
    }
}