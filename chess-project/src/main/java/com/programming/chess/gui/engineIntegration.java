package com.programming.chess.gui;

import com.programming.chess.engine.nativeEngine;
import com.programming.chess.rules.gameState;

/**
 * Integration class to connect the chess GUI with the native engine.
 * This class handles the conversion between GUI representation and engine representation.
 */
public class engineIntegration {
    private nativeEngine engine;
    private static engineIntegration instance;
    
    // Singleton pattern
    private engineIntegration() {
        try {
            engine = new nativeEngine();
            System.out.println("Chess engine initialized successfully");
        } catch (Exception e) {
            System.err.println("Failed to initialize chess engine: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static engineIntegration getInstance() {
        if (instance == null) {
            instance = new engineIntegration();
        }
        return instance;
    }
    
    /**
     * Convert our board representation to FEN notation for the engine
     */
    public String boardToFEN(String[][] board) {
        StringBuilder fen = new StringBuilder();
        
        // Add piece placement
        for (int row = 0; row < 8; row++) {
            int emptyCount = 0;
            
            for (int col = 0; col < 8; col++) {
                String piece = board[row][col];
                
                if (piece == null) {
                    emptyCount++;
                } else {
                    // If there were empty squares before this piece, add the count
                    if (emptyCount > 0) {
                        fen.append(emptyCount);
                        emptyCount = 0;
                    }
                    
                    // Convert our piece format to FEN character
                    char fenChar = pieceToFENChar(piece);
                    fen.append(fenChar);
                }
            }
            
            // If there are empty squares at the end of the row
            if (emptyCount > 0) {
                fen.append(emptyCount);
            }
            
            // Add row separator (except for last row)
            if (row < 7) {
                fen.append('/');
            }
        }
        
        // Add active color
        gameState state = gameState.getInstance();
        fen.append(' ').append(state.getCurrentPlayer().equals("W") ? 'w' : 'b');
        
        // Add castling availability (simplified for now)
        fen.append(" KQkq");
        
        // Add en passant target square (none for now)
        fen.append(" -");
        
        // Add halfmove clock and fullmove number
        fen.append(" 0 1");
        
        return fen.toString();
    }
    
    /**
     * Convert our piece representation to FEN character
     */
    private char pieceToFENChar(String piece) {
        if (piece == null) return ' ';
        
        // Get piece type and color
        String pieceType = piece.substring(0, piece.length() - 1);
        String color = piece.substring(piece.length() - 1);
        
        char fenChar;
        
        switch (pieceType) {
            case "pawn":
                fenChar = 'p';
                break;
            case "knight":
                fenChar = 'n';
                break;
            case "bishop":
                fenChar = 'b';
                break;
            case "rook":
                fenChar = 'r';
                break;
            case "queen":
                fenChar = 'q';
                break;
            case "king":
                fenChar = 'k';
                break;
            default:
                return ' ';
        }
        
        // Uppercase for white pieces
        if (color.equals("W")) {
            fenChar = Character.toUpperCase(fenChar);
        }
        
        return fenChar;
    }
    
    /**
     * Get the best move from the engine for the current board position
     * 
     * @param board Current board state
     * @param depth Search depth
     * @return Best move in format [fromRow, fromCol, toRow, toCol]
     */
    public int[] getBestMove(String[][] board, int depth) {
        if (engine == null) {
            System.err.println("Engine not initialized");
            return null;
        }
        
        // Convert board to FEN
        String fen = boardToFEN(board);
        
        // Get best move from engine
        String moveStr = engine.getBestMove(fen, depth);
        
        if (moveStr == null || moveStr.length() < 4) {
            return null;
        }
        
        // Parse engine move format (e.g., "e2e4") to our coordinate system
        int fromCol = moveStr.charAt(0) - 'a';
        int fromRow = '8' - moveStr.charAt(1);
        int toCol = moveStr.charAt(2) - 'a';
        int toRow = '8' - moveStr.charAt(3);
        
        return new int[] {fromRow, fromCol, toRow, toCol};
    }
    
    /**
     * Check if a move is legal using the engine
     */
    public boolean isMoveLegal(String[][] board, int fromRow, int fromCol, int toRow, int toCol) {
        if (engine == null) {
            // Fall back to our basic move validation if engine isn't available
            return true;
        }
        
        // Convert board to FEN
        String fen = boardToFEN(board);
        
        // Convert move to algebraic notation
        String moveStr = String.format("%c%c%c%c", 
                                     (char)('a' + fromCol), 
                                     (char)('8' - fromRow),
                                     (char)('a' + toCol), 
                                     (char)('8' - toRow));
        
        // Check with engine
        return engine.isMoveLegal(fen, moveStr);
    }
    
    /**
     * Evaluate the current position
     */
    public float evaluatePosition(String[][] board) {
        if (engine == null) {
            return 0.0f;
        }
        
        String fen = boardToFEN(board);
        return engine.evaluatePosition(fen);
    }
    
    /**
     * Start engine training
     */
    public void trainEngine(int numGames) {
        if (engine == null) {
            System.err.println("Engine not initialized");
            return;
        }
        
        new Thread(() -> {
            System.out.println("Starting engine training with " + numGames + " games");
            engine.trainNetwork(numGames);
            System.out.println("Engine training completed");
        }).start();
    }
}