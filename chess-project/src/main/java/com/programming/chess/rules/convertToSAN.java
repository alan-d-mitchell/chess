package com.programming.chess.rules;

import com.programming.chess.rules.gameState.Move;

/**
 * Utility class for converting chess moves to standard algebraic notation.
 */
public class convertToSAN {
    
    /**
     * Converts a chess move to standard algebraic notation.
     * 
     * @param move The gameState.Move object to convert
     * @param boardState The current board state
     * @return The move in standard algebraic notation (e.g., "e4", "Nf3", "Bxe5")
     */
    public static String toStandardAlgebraicNotation(Move move, String[][] boardState) {
        // If it's a castling move, return appropriate notation
        if (move.isCastling()) {
            return move.getCastlingSide().equals("kingside") ? "O-O" : "O-O-O";
        }
        
        StringBuilder notation = new StringBuilder();
        
        // Get piece type character (P, N, B, R, Q, K)
        char pieceType = determinePieceType(move.piece);
        
        // For pieces other than pawns, add the piece letter
        if (pieceType != 'P') {
            notation.append(pieceType);
        }
        
        // Handle captures
        boolean isCapture = (move.capturedPiece != null);
        if (isCapture) {
            // For pawn captures, include the departure file
            if (pieceType == 'P') {
                notation.append((char)('a' + move.fromCol));
            }
            notation.append('x');
        }
        
        // Destination square
        notation.append((char)('a' + move.toCol));
        notation.append(8 - move.toRow); // Convert from 0-7 row index to chess 1-8 notation
        
        return notation.toString();
    }
    
    /**
     * Determines the type of piece being moved and returns the SAN character.
     */
    private static char determinePieceType(String piece) {
        if (piece == null) return 'P'; // Default to pawn if unknown
        
        // Remove the color character to get the piece type
        String pieceType = piece.substring(0, piece.length() - 1);
        
        return switch (pieceType) {
            case "pawn" -> 'P';
            case "knight" -> 'N';
            case "bishop" -> 'B';
            case "rook" -> 'R';
            case "queen" -> 'Q';
            case "king" -> 'K';
            default -> 'P'; // Default to pawn for unknown pieces
        };
    }
    
    /**
     * Helper method to convert from an external Move to standard algebraic notation
     * This method creates a gameState.Move and then converts it to SAN
     */
    public static String externalMoveToSAN(int fromRow, int fromCol, int toRow, int toCol, 
                                         String piece, String capturedPiece, boolean isCastling,
                                         String[][] boardState) {
        // Get the gameState instance
        gameState state = gameState.getInstance();
        
        // Create a temporary gameState.Move object
        gameState.Move tempMove = state.createTempMove(fromRow, fromCol, toRow, toCol, piece, capturedPiece);
        
        // Set castling information if needed
        if (isCastling) {
            tempMove.setCastling(true);
            // Determine castling side based on the to and from columns
            boolean isKingside = toCol > fromCol;
            tempMove.setCastlingSide(isKingside ? "kingside" : "queenside");
        }
        
        // Use the standard notation converter
        return toStandardAlgebraicNotation(tempMove, boardState);
    }
}