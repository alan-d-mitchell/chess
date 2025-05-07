package com.programming.chess.rules;

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
     * @param ignoreCheck Whether to ignore check validation (used during checkmate detection)
     * @return true if the move is valid, false otherwise
     */
    public static boolean isValidMove(String[][] board, int fromRow, int fromCol, int toRow, int toCol, 
                                    String draggedPiece, boolean ignoreCheck) {
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
        boolean basicMoveValid = switch (pieceType) {
            case "pawn" -> isValidPawnMove(board, fromRow, fromCol, toRow, toCol, pieceColor);
            case "rook" -> isValidRookMove(board, fromRow, fromCol, toRow, toCol);
            case "knight" -> isValidKnightMove(fromRow, fromCol, toRow, toCol);
            case "bishop" -> isValidBishopMove(board, fromRow, fromCol, toRow, toCol);
            case "queen" -> isValidQueenMove(board, fromRow, fromCol, toRow, toCol);
            case "king" -> isValidKingMove(board, fromRow, fromCol, toRow, toCol, pieceColor);
            default -> false;
        };
        
        // If the basic move isn't valid or we're ignoring check, return the result
        if (!basicMoveValid || ignoreCheck) {
            return basicMoveValid;
        }
        
        // Final check: verify this move doesn't leave the king in check
        return detectCheck.isMoveLegal(board, fromRow, fromCol, toRow, toCol, piece);
    }
    
    /**
     * Simplified version for non-drag operations
     */
    public static boolean isValidMove(String[][] board, int fromRow, int fromCol, int toRow, int toCol) {
        return isValidMove(board, fromRow, fromCol, toRow, toCol, null, false);
    }
    
    /**
     * Simplified version when draggedPiece is provided but ignoreCheck is not
     */
    public static boolean isValidMove(String[][] board, int fromRow, int fromCol, int toRow, int toCol, String draggedPiece) {
        return isValidMove(board, fromRow, fromCol, toRow, toCol, draggedPiece, false);
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
    
    private static boolean isValidKingMove(String[][] board, int fromRow, int fromCol, int toRow, int toCol, String pieceColor) {
        // Basic king move - one square in any direction
        int rowDiff = Math.abs(fromRow - toRow);
        int colDiff = Math.abs(fromCol - toCol);
        
        if (rowDiff <= 1 && colDiff <= 1 && !(rowDiff == 0 && colDiff == 0)) {
            return true;
        }
        
        // Check for castling move
        if (rowDiff == 0 && colDiff == 2) {
            // This could be a castling move
            // Need to verify with gameState that king and rook haven't moved
            
            // Get castle direction
            String castleSide = fromCol < toCol ? "kingside" : "queenside";
            
            // Check if castling is allowed
            gameState state = gameState.getInstance();
            if (state.canCastle(pieceColor, castleSide)) {
                return isValidCastling(fromRow, fromCol, toRow, toCol, pieceColor, castleSide);
            }
        }
        
        return false;
    }
    
    private static boolean isValidCastling(int fromRow, int fromCol, int toRow, int toCol, 
                                          String pieceColor, String castleSide) {
        // Castling requires empty squares between king and rook
        // For the current board state, we need to check if the path is clear
        
        // Proper rows for kings based on color
        int properRow = pieceColor.equals("W") ? 7 : 0;
        
        // Verify king is in the correct position
        if (fromRow != properRow || fromCol != 4) {
            return false;
        }
        
        // Verify destination is correct for castling
        if (toRow != properRow) {
            return false;
        }
        
        if (castleSide.equals("kingside")) {
            // King moves from e1/e8 (4) to g1/g8 (6)
            if (toCol != 6) {
                return false;
            }
            
            // Check if squares between king and rook are empty
            // f1/f8 (5) must be empty
            return true; // Assuming the validateMove already checked if path is clear
        } else { // queenside
            // King moves from e1/e8 (4) to c1/c8 (2)
            if (toCol != 2) {
                return false;
            }
            
            // Check if squares between king and rook are empty
            // b1/b8 (1), c1/c8 (2), d1/d8 (3) must be empty
            return true; // Assuming the validateMove already checked if path is clear
        }
    }
}