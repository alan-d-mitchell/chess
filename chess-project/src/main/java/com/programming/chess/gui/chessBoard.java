package com.programming.chess.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.programming.chess.rules.detectCheck;
import com.programming.chess.rules.gameState;
import com.programming.chess.rules.validateMove;

public class chessBoard extends JFrame {

    private final int BOARD_SIZE = 8;
    private Map<String, ImageIcon> pieceImages = new HashMap<>();
    private String[][] board = new String[BOARD_SIZE][BOARD_SIZE];
    private JPanel chessDisplay;
    private moveDisplay moveHistoryPanel; // Add the move display panel
    
    // Board dimensions
    private int squareSize;
    private int xOffset = 0;
    private int yOffset = 0;
    
    // Variables for dragging pieces
    private boolean isDragging = false;
    private int dragSourceRow = -1;
    private int dragSourceCol = -1;
    private int dragX = -1;
    private int dragY = -1;
    private String draggedPiece = null;
    
    // Variables for drawing arrows
    private List<Arrow> arrows = new ArrayList<>();
    private boolean isDrawingArrow = false;
    private int arrowStartRow = -1;
    private int arrowStartCol = -1;
    private int arrowEndRow = -1;
    private int arrowEndCol = -1;
    private boolean shiftPressed = false;
    
    // Game state reference
    private gameState state;
    
    // Track whose turn it is for the move display
    private boolean isWhiteTurn = true;
    
    // Track check status for highlighting
    private boolean isWhiteKingInCheck = false;
    private boolean isBlackKingInCheck = false;
    private int whiteKingRow = 7, whiteKingCol = 4; // Initial positions
    private int blackKingRow = 0, blackKingCol = 4; // Initial positions
    
    // Track pawns that can do en passant captures
    private List<int[]> enPassantPawns = new ArrayList<>();
    
    // Inner class to represent an arrow
    private class Arrow {
        int startRow, startCol, endRow, endCol;
        Color color;
        
        public Arrow(int startRow, int startCol, int endRow, int endCol, Color color) {
            this.startRow = startRow;
            this.startCol = startCol;
            this.endRow = endRow;
            this.endCol = endCol;
            this.color = color;
        }
    }

    public chessBoard() {
        super("Chess");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(950, 800)); // Increased width to accommodate move history

        // Initialize game state
        state = gameState.getInstance();
        
        // Initialize move history panel
        moveHistoryPanel = new moveDisplay();
        
        loadImages();
        initializeBoard();
        
        chessDisplay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                calculateBoardDimensions();

                drawBoard(g);
                drawPieces(g);
                
                // Draw all saved arrows
                for (Arrow arrow : arrows) {
                    drawArrow(g, arrow.startRow, arrow.startCol, arrow.endRow, arrow.endCol, arrow.color);
                }
                
                // Draw the arrow currently being created
                if (isDrawingArrow) {
                    drawArrow(g, arrowStartRow, arrowStartCol, arrowEndRow, arrowEndCol, Color.RED);
                }
                
                // Draw the dragged piece last so it appears on top
                if (isDragging && draggedPiece != null && pieceImages.containsKey(draggedPiece)) {
                    ImageIcon icon = pieceImages.get(draggedPiece);
                    // Center the piece on the cursor
                    g.drawImage(icon.getImage(), 
                               dragX - squareSize/2, 
                               dragY - squareSize/2, 
                               squareSize, squareSize, null);
                }
                
                // Draw status message
                g.setColor(Color.WHITE);
                g.setFont(new Font("SansSerif", Font.BOLD, 16));
                g.drawString(state.getStatusMessage(), 10, getHeight() - 20);
            }
        };

        chessDisplay.setBackground(Color.DARK_GRAY);
        
        // Mouse press for starting drag or arrow
        chessDisplay.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int col = (e.getX() - xOffset) / squareSize;
                int row = (e.getY() - yOffset) / squareSize;
                
                // Make sure the click is within the board
                if (col >= 0 && col < BOARD_SIZE && row >= 0 && row < BOARD_SIZE) {
                    if (shiftPressed) {
                        // Start drawing an arrow
                        isDrawingArrow = true;
                        arrowStartRow = row;
                        arrowStartCol = col;
                        arrowEndRow = row;
                        arrowEndCol = col;
                    } else {
                        String piece = board[row][col];
                        
                        if (piece != null) {
                            // Only allow dragging pieces of the current player
                            if (state.isCurrentPlayersPiece(piece)) {
                                dragSourceRow = row;
                                dragSourceCol = col;
                                draggedPiece = piece;
                                isDragging = true;
                                dragX = e.getX();
                                dragY = e.getY();
                                
                                // Temporarily remove the piece from the board during dragging
                                board[row][col] = null;
                                
                                chessDisplay.repaint();
                            } else {
                                state.setStatusMessage("It's " + 
                                    (state.getCurrentPlayer().equals("W") ? "White" : "Black") + 
                                    "'s turn to move");
                                chessDisplay.repaint();
                            }
                        }
                    }
                }
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDrawingArrow) {
                    int col = (e.getX() - xOffset) / squareSize;
                    int row = (e.getY() - yOffset) / squareSize;
                    
                    // Make sure the release is within the board
                    if (col >= 0 && col < BOARD_SIZE && row >= 0 && row < BOARD_SIZE) {
                        // Only add arrow if start and end are different
                        if (arrowStartRow != row || arrowStartCol != col) {
                            arrows.add(new Arrow(arrowStartRow, arrowStartCol, row, col, Color.RED));
                        }
                    }
                    
                    isDrawingArrow = false;
                    chessDisplay.repaint();
                } else if (isDragging) {
                    int col = (e.getX() - xOffset) / squareSize;
                    int row = (e.getY() - yOffset) / squareSize;
                    
                    boolean validMove = false;
                    
                    // Make sure the release is within the board
                    if (col >= 0 && col < BOARD_SIZE && row >= 0 && row < BOARD_SIZE) {
                        // Check if this is a valid move
                        validMove = validateBoardMove(dragSourceRow, dragSourceCol, row, col, draggedPiece);
                        
                        if (validMove) {
                            // Capture the piece at destination if any
                            String capturedPiece = board[row][col];
                            
                            // Check if this is a castling move
                            boolean isCastling = draggedPiece.startsWith("king") && Math.abs(dragSourceCol - col) == 2;
                            
                            // Check if this is an en passant capture
                            boolean isEnPassant = validateMove.isEnPassantCapture(dragSourceRow, dragSourceCol, row, col, draggedPiece);
                            
                            if (isEnPassant) {
                                // Get the position of the captured pawn
                                int[] capturedPos = validateMove.getEnPassantCapturedPawnPosition(row, col);
                                if (capturedPos != null) {
                                    // Set the captured piece
                                    capturedPiece = board[capturedPos[0]][capturedPos[1]];
                                    // Remove the captured pawn from the board
                                    board[capturedPos[0]][capturedPos[1]] = null;
                                    System.out.println("En passant capture: " + capturedPiece + " at " + 
                                                     (char)('a' + capturedPos[1]) + (8 - capturedPos[0]));
                                }
                            }
                            
                            // Update king position tracking for kings
                            if (draggedPiece.equals("kingW")) {
                                whiteKingRow = row;
                                whiteKingCol = col;
                            } else if (draggedPiece.equals("kingB")) {
                                blackKingRow = row;
                                blackKingCol = col;
                            }
                            
                            // Complete the move
                            board[row][col] = draggedPiece;
                            
                            // Handle castling if needed
                            if (isCastling) {
                                handleCastling(dragSourceRow, dragSourceCol, row, col);
                            }
                            
                            // Record the move in game state
                            state.makeMove(dragSourceRow, dragSourceCol, row, col, draggedPiece, capturedPiece);
                            
                            // Update pawns that can do en passant after the move
                            updateEnPassantPawns();
                            
                            // Check for check/checkmate
                            String opponentColor = state.getCurrentPlayer(); // Current player is the opponent now
                            boolean isInCheck = detectCheck.isCheck(board, opponentColor);
                            boolean isInCheckMate = false;
                            
                            if (isInCheck) {
                                isInCheckMate = detectCheck.isCheckMate(board, opponentColor);
                            }
                            
                            // Update check status for highlighting
                            isWhiteKingInCheck = opponentColor.equals("W") && isInCheck;
                            isBlackKingInCheck = opponentColor.equals("B") && isInCheck;
                            
                            // Update game state
                            state.setCheckStatus(isInCheck, isInCheckMate);
                            
                            // Add the move to the move history display with check/checkmate status
                            moveHistoryPanel.addMove(
                                dragSourceRow, dragSourceCol, row, col, 
                                draggedPiece, capturedPiece, isCastling, isEnPassant,
                                board, isWhiteTurn, isInCheck, isInCheckMate);
                            
                            // Toggle turn for move display
                            isWhiteTurn = !isWhiteTurn;
                            
                            if (capturedPiece != null) {
                                System.out.println("Captured: " + capturedPiece);
                            }
                            
                            System.out.println("Valid move: " + draggedPiece + " from " + 
                                              (char)('a' + dragSourceCol) + (8 - dragSourceRow) + " to " + 
                                              (char)('a' + col) + (8 - row));
                            
                            // Clear arrows after making a move
                            clearArrows();
                        } else {
                            // Invalid move, return the piece to its original position
                            board[dragSourceRow][dragSourceCol] = draggedPiece;
                            state.setStatusMessage("Invalid move. " + 
                                (state.getCurrentPlayer().equals("W") ? "White" : "Black") + 
                                "'s turn to move");
                            System.out.println("Invalid move attempted");
                        }
                    } else {
                        // Released outside the board, return piece to original position
                        board[dragSourceRow][dragSourceCol] = draggedPiece;
                    }
                    
                    // Reset drag variables
                    isDragging = false;
                    draggedPiece = null;
                    dragSourceRow = -1;
                    dragSourceCol = -1;
                    
                    chessDisplay.repaint();
                }
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    clearArrows();
                }
            }
        });
        
        // Mouse motion for dragging
        chessDisplay.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDrawingArrow) {
                    int col = (e.getX() - xOffset) / squareSize;
                    int row = (e.getY() - yOffset) / squareSize;
                    
                    // Make sure it's within the board
                    if (col >= 0 && col < BOARD_SIZE && row >= 0 && row < BOARD_SIZE) {
                        arrowEndRow = row;
                        arrowEndCol = col;
                        chessDisplay.repaint();
                    }
                } else if (isDragging) {
                    dragX = e.getX();
                    dragY = e.getY();
                    chessDisplay.repaint();
                }
            }
        });
        
        // Key listener for shift key
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    shiftPressed = true;
                }
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    shiftPressed = false;
                    
                    // Cancel any arrow drawing in progress when shift is released
                    if (isDrawingArrow) {
                        isDrawingArrow = false;
                        chessDisplay.repaint();
                    }
                }
            }
        });
        
        // Make sure the frame can receive key events
        setFocusable(true);
        requestFocus();
        
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                chessDisplay.repaint();
            }
        });
        
        // Set up the layout with board on left and move history on right
        setLayout(new BorderLayout());
        add(chessDisplay, BorderLayout.CENTER);
        add(moveHistoryPanel, BorderLayout.EAST);
        
        setResizable(true);
        pack();

        setMinimumSize(new Dimension(550, 400));
    }
    
    /**
     * Helper method to draw an arrow
     */
    private void drawArrow(Graphics g, int startRow, int startCol, int endRow, int endCol, Color color) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(squareSize / 8)); // Arrow line thickness
        
        // Calculate center points of the squares
        int startX = xOffset + startCol * squareSize + squareSize / 2;
        int startY = yOffset + startRow * squareSize + squareSize / 2;
        int endX = xOffset + endCol * squareSize + squareSize / 2;
        int endY = yOffset + endRow * squareSize + squareSize / 2;
        
        // Draw the arrow line
        g2d.drawLine(startX, startY, endX, endY);
        
        // Draw the arrow head
        double angle = Math.atan2(endY - startY, endX - startX);
        int arrowSize = squareSize / 3;
        
        // Create arrowhead
        Polygon arrowHead = new Polygon();
        arrowHead.addPoint(endX, endY);
        arrowHead.addPoint((int) (endX - arrowSize * Math.cos(angle - Math.PI/6)), 
                           (int) (endY - arrowSize * Math.sin(angle - Math.PI/6)));
        arrowHead.addPoint((int) (endX - arrowSize * Math.cos(angle + Math.PI/6)), 
                           (int) (endY - arrowSize * Math.sin(angle + Math.PI/6)));
        
        g2d.fillPolygon(arrowHead);
    }
    
    /**
     * Clear all arrows from the board
     */
    private void clearArrows() {
        arrows.clear();
        chessDisplay.repaint();
    }
    
    /**
     * Update the list of pawns that can perform en passant captures
     */
    private void updateEnPassantPawns() {
        enPassantPawns.clear();
        
        // Only check if en passant is possible
        int enPassantCol = state.getEnPassantCol();
        int enPassantRow = state.getEnPassantRow();
        
        if (enPassantCol == -1 || enPassantRow == -1) {
            return; // No en passant possible
        }
        
        // Get the player who can perform en passant (current player)
        String currentPlayer = state.getCurrentPlayer();
        
        // Search for pawns that can perform en passant
        // They must be on the same rank as the en passant target and adjacent files
        for (int adjacentCol = enPassantCol - 1; adjacentCol <= enPassantCol + 1; adjacentCol += 2) {
            if (adjacentCol < 0 || adjacentCol >= BOARD_SIZE) {
                continue; // Skip if outside the board
            }
            
            // Check if there's a pawn of the current player at this position
            String piece = board[enPassantRow][adjacentCol];
            if (piece != null && piece.equals("pawn" + currentPlayer)) {
                // This pawn can perform en passant
                enPassantPawns.add(new int[] {enPassantRow, adjacentCol});
            }
        }
    }
    
    /**
     * Validate a move using basic validation
     */
    private boolean validateBoardMove(int fromRow, int fromCol, int toRow, int toCol, String piece) {
        // Check with basic validation
        return validateMove.isValidMove(board, fromRow, fromCol, toRow, toCol, piece);
    }
    
    /**
     * Handle castling move by moving the rook to its new position
     */
    private void handleCastling(int kingFromRow, int kingFromCol, int kingToRow, int kingToCol) {
        String pieceColor = draggedPiece.substring(draggedPiece.length() - 1);
        
        if (kingToCol > kingFromCol) {
            // Kingside castling (O-O)
            // Move the kingside rook from h1/h8 to f1/f8
            String rookPiece = "rook" + pieceColor;
            board[kingToRow][5] = rookPiece; // Move to f1/f8
            board[kingToRow][7] = null;     // Remove from h1/h8
            System.out.println("Kingside castling: Moved " + rookPiece + " from " + 
                              (char)('a' + 7) + (8 - kingToRow) + " to " + 
                              (char)('a' + 5) + (8 - kingToRow));
        } else {
            // Queenside castling (O-O-O)
            // Move the queenside rook from a1/a8 to d1/d8
            String rookPiece = "rook" + pieceColor;
            board[kingToRow][3] = rookPiece; // Move to d1/d8
            board[kingToRow][0] = null;     // Remove from a1/a8
            System.out.println("Queenside castling: Moved " + rookPiece + " from " + 
                              (char)('a' + 0) + (8 - kingToRow) + " to " + 
                              (char)('a' + 3) + (8 - kingToRow));
        }
    }

    private void calculateBoardDimensions() {
        int width = chessDisplay.getWidth();
        int height = chessDisplay.getHeight();
        
        squareSize = Math.min(width, height) / BOARD_SIZE;
    
        xOffset = (width - (squareSize * BOARD_SIZE)) / 2;
        yOffset = (height - (squareSize * BOARD_SIZE)) / 2;
    }

    private void drawBoard(Graphics g) {
        Color lightSquare = new Color(204, 219, 255);
        Color darkSquare = new Color(121, 154, 176);
        Color dragSourceHighlight = new Color(255, 165, 0, 120); // Orange with transparency
        Color checkHighlight = new Color(255, 0, 0, 120); // Red with transparency
        Color enPassantHighlight = new Color(0, 255, 0, 120); // Green with transparency for pawns that can do en passant

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                boolean isLightSquare = (row + col) % 2 == 0;
                g.setColor(isLightSquare ? lightSquare : darkSquare);

                int x = xOffset + col * squareSize;
                int y = yOffset + row * squareSize;
                g.fillRect(x, y, squareSize, squareSize);
                
                // Highlight source square during drag
                if (isDragging && row == dragSourceRow && col == dragSourceCol) {
                    g.setColor(dragSourceHighlight);
                    g.fillRect(x, y, squareSize, squareSize);
                }
                
                // Highlight pawns that can perform en passant capture
                for (int[] pawn : enPassantPawns) {
                    if (row == pawn[0] && col == pawn[1]) {
                        g.setColor(enPassantHighlight);
                        g.fillRect(x, y, squareSize, squareSize);
                    }
                }
                
                // Highlight king in check with red
                if ((isWhiteKingInCheck && row == whiteKingRow && col == whiteKingCol) ||
                    (isBlackKingInCheck && row == blackKingRow && col == blackKingCol)) {
                    g.setColor(checkHighlight);
                    g.fillRect(x, y, squareSize, squareSize);
                }

                int fontSize = Math.max(10, squareSize / 8);
                g.setFont(new Font("SansSerif", Font.PLAIN, fontSize));
                
                // Draw coordinates
                if (row == BOARD_SIZE - 1) {
                    g.setColor(isLightSquare ? darkSquare : lightSquare);
                    g.drawString(String.valueOf((char) ('a' + col)), 
                                x + squareSize - (squareSize / 5), 
                                y + squareSize - (squareSize / 10));
                }

                if (col == 0) {
                    g.setColor(isLightSquare ? darkSquare : lightSquare);
                    g.drawString(String.valueOf(8 - row), 
                                x + (squareSize / 10), 
                                y + (squareSize / 5));
                }
            }
        }
    }

    private void drawPieces(Graphics g) {
        if (board == null) return;

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                String piece = board[row][col];

                // Don't draw the piece being dragged (it will be drawn separately)
                if (piece != null && pieceImages.containsKey(piece) && 
                    !(isDragging && row == dragSourceRow && col == dragSourceCol)) {
                    ImageIcon icon = pieceImages.get(piece);
                    int x = xOffset + col * squareSize;
                    int y = yOffset + row * squareSize;

                    g.drawImage(icon.getImage(), x, y, squareSize, squareSize, null);
                }
            }
        }
    }

    private void initializeBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                board[i][j] = null;
            }
        }

        for (int col = 0; col < BOARD_SIZE; col++) {
            board[1][col] = "pawnB";
            board[6][col] = "pawnW";
        }

        String[] backRow = {"rook", "knight", "bishop", "queen", "king", "bishop", "knight", "rook"};
        for (int col = 0; col < BOARD_SIZE; col++) {
            board[0][col] = backRow[col] + "B";
            board[7][col] = backRow[col] + "W";
        }
        
        // Reset king positions
        whiteKingRow = 7;
        whiteKingCol = 4;
        blackKingRow = 0;
        blackKingCol = 4;
        
        // Reset check status
        isWhiteKingInCheck = false;
        isBlackKingInCheck = false;
        
        // Reset en passant pawns
        enPassantPawns.clear();
        
        // Reset arrows
        arrows.clear();
        
        // Reset game state
        state.resetGame();
        
        // Reset move history
        moveHistoryPanel.clearHistory();
        
        // Reset turn tracking
        isWhiteTurn = true;
        
        // Debug the board state after initialization
        System.out.println("Board initialized:");
        for (int row = 0; row < BOARD_SIZE; row++) {
            StringBuilder rowStr = new StringBuilder();
            for (int col = 0; col < BOARD_SIZE; col++) {
                rowStr.append(board[row][col] != null ? board[row][col] : ".").append("\t");
            }
            System.out.println(rowStr.toString());
        }
    }

    private void loadImages() {
        String[] pieces = {"pawnW", "knightW", "bishopW", "rookW", "queenW", "kingW", 
                         "pawnB", "knightB", "bishopB", "rookB", "queenB", "kingB"};
        
        System.out.println("Starting to load images...");
        
        for (String piece : pieces) {
            try {
                // Try multiple paths to find the resources
                String[] pathsToTry = {
                    "/pieces/" + piece + ".png",
                    "pieces/" + piece + ".png", 
                    "/resources/pieces/" + piece + ".png",
                    "/main/resources/pieces/" + piece + ".png",
                    "/src/main/resources/pieces/" + piece + ".png"
                };
                
                URL imageURL = null;
                String usedPath = null;
                
                for (String path : pathsToTry) {
                    System.out.println("Trying path: " + path);
                    imageURL = getClass().getResource(path);
                    if (imageURL != null) {
                        usedPath = path;
                        System.out.println("Found image at: " + path + " -> " + imageURL);
                        break;
                    }
                }

                if (imageURL == null) {
                    System.out.println("ERROR: Could not find image for " + piece + " in any location");
                    continue;
                }

                Image img = new ImageIcon(imageURL).getImage();
                pieceImages.put(piece, new ImageIcon(img));
                System.out.println("Successfully loaded: " + piece + " from " + usedPath);
            }
            catch (Exception e) {
                System.out.println("Exception loading image " + piece + ".png");
                e.printStackTrace();
            }
        }
        
        System.out.println("Finished loading images. Total: " + pieceImages.size() + " of 12 expected");
        System.out.println("Loaded piece keys: " + pieceImages.keySet());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new chessBoard().setVisible(true);
        });
    }
}