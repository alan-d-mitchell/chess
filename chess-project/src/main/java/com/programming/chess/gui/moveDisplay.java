package com.programming.chess.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import com.programming.chess.rules.convertToSAN;

/**
 * Panel that displays the history of moves in standard algebraic notation.
 */
public class moveDisplay extends JPanel {
    private final JTextArea moveHistoryArea;
    private final List<String> moveNotations = new ArrayList<>();
    
    /**
     * Creates a new move history panel.
     */
    public moveDisplay() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(),
                "Move History",
                TitledBorder.CENTER,
                TitledBorder.TOP));
        
        moveHistoryArea = new JTextArea();
        moveHistoryArea.setEditable(false);
        moveHistoryArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        
        JScrollPane scrollPane = new JScrollPane(moveHistoryArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setPreferredSize(new Dimension(150, 300));
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Adds a move to the history and updates the display.
     * 
     * @param notation The standard algebraic notation of the move (e.g., "e4", "Nf3")
     * @param isWhiteMove True if it's a white move, false for black
     */
    public void addMove(String notation, boolean isWhiteMove) {
        moveNotations.add(notation);
        updateMoveDisplay();
    }
    
    /**
     * Adds a move to the history using coordinates and board state.
     * 
     * @param fromRow Starting row (0-7)
     * @param fromCol Starting column (0-7)
     * @param toRow Ending row (0-7)
     * @param toCol Ending column (0-7)
     * @param piece The piece being moved
     * @param capturedPiece The captured piece, if any (null if none)
     * @param isCastling Whether this is a castling move
     * @param isEnPassant Whether this is an en passant capture
     * @param boardState Current board state
     * @param isWhiteMove True if it's white's move
     * @param isCheck True if the move results in check
     * @param isCheckMate True if the move results in checkmate
     */
    public void addMove(int fromRow, int fromCol, int toRow, int toCol, String piece, 
                       String capturedPiece, boolean isCastling, boolean isEnPassant,
                       String[][] boardState, boolean isWhiteMove,
                       boolean isCheck, boolean isCheckMate) {
        String notation = convertToSAN.externalMoveToSAN(
            fromRow, fromCol, toRow, toCol, piece, capturedPiece, isCastling, isEnPassant,
            boardState, isCheck, isCheckMate);
        addMove(notation, isWhiteMove);
    }
    
    /**
     * Overloaded method for backward compatibility without en passant
     */
    public void addMove(int fromRow, int fromCol, int toRow, int toCol, String piece, 
                       String capturedPiece, boolean isCastling, 
                       String[][] boardState, boolean isWhiteMove,
                       boolean isCheck, boolean isCheckMate) {
        addMove(fromRow, fromCol, toRow, toCol, piece, capturedPiece, isCastling, false,
              boardState, isWhiteMove, isCheck, isCheckMate);
    }
    
    /**
     * Overloaded method for backward compatibility
     */
    public void addMove(int fromRow, int fromCol, int toRow, int toCol, String piece, 
                       String capturedPiece, boolean isCastling, 
                       String[][] boardState, boolean isWhiteMove) {
        addMove(fromRow, fromCol, toRow, toCol, piece, capturedPiece, isCastling, false,
              boardState, isWhiteMove, false, false);
    }
    
    /**
     * Updates the text display with all moves in standard chess notation.
     */
    private void updateMoveDisplay() {
        StringBuilder display = new StringBuilder();
        for (int i = 0; i < moveNotations.size(); i++) {
            // Add move number at the start of white's move
            if (i % 2 == 0) {
                display.append((i / 2) + 1).append(". ");
            }
            
            // Add the algebraic notation of the move
            display.append(moveNotations.get(i));
            
            // Add space or newline
            if (i % 2 == 0) {
                display.append(" "); // Space after white's move
            } else {
                display.append("\n"); // New line after black's move
            }
        }
        
        moveHistoryArea.setText(display.toString());
        
        // Auto-scroll to the bottom
        moveHistoryArea.setCaretPosition(moveHistoryArea.getDocument().getLength());
    }
    
    /**
     * Clears the move history.
     */
    public void clearHistory() {
        moveNotations.clear();
        moveHistoryArea.setText("");
    }
}