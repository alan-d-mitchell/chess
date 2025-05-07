package com.programming.chess.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

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
    
    // Game state reference
    private gameState state;
    
    // Track whose turn it is for the move display
    private boolean isWhiteTurn = true;

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
        
        // Mouse press for starting drag
        chessDisplay.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int col = (e.getX() - xOffset) / squareSize;
                int row = (e.getY() - yOffset) / squareSize;
                
                // Make sure the click is within the board
                if (col >= 0 && col < BOARD_SIZE && row >= 0 && row < BOARD_SIZE) {
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
            
            @Override
            public void mouseReleased(MouseEvent e) {
                if (isDragging) {
                    int col = (e.getX() - xOffset) / squareSize;
                    int row = (e.getY() - yOffset) / squareSize;
                    
                    boolean validMove = false;
                    
                    // Make sure the release is within the board
                    if (col >= 0 && col < BOARD_SIZE && row >= 0 && row < BOARD_SIZE) {
                        // Check if this is a valid move
                        validMove = validateMove.isValidMove(board, dragSourceRow, dragSourceCol, row, col, draggedPiece);
                        
                        if (validMove) {
                            // Capture the piece at destination if any
                            String capturedPiece = board[row][col];
                            
                            // Check if this is a castling move
                            boolean isCastling = draggedPiece.startsWith("king") && Math.abs(dragSourceCol - col) == 2;
                            
                            // Complete the move
                            board[row][col] = draggedPiece;
                            
                            // Handle castling if needed
                            if (isCastling) {
                                handleCastling(dragSourceRow, dragSourceCol, row, col);
                            }
                            
                            // Record the move in game state
                            state.makeMove(dragSourceRow, dragSourceCol, row, col, draggedPiece, capturedPiece);
                            
                            // Add the move to the move history display
                            moveHistoryPanel.addMove(
                                dragSourceRow, dragSourceCol, row, col, 
                                draggedPiece, capturedPiece, isCastling,
                                board, isWhiteTurn);
                            
                            // Toggle turn for move display
                            isWhiteTurn = !isWhiteTurn;
                            
                            if (capturedPiece != null) {
                                System.out.println("Captured: " + capturedPiece);
                            }
                            
                            System.out.println("Valid move: " + draggedPiece + " from " + 
                                              (char)('a' + dragSourceCol) + (8 - dragSourceRow) + " to " + 
                                              (char)('a' + col) + (8 - row));
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
        });
        
        // Mouse motion for dragging
        chessDisplay.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging) {
                    dragX = e.getX();
                    dragY = e.getY();
                    chessDisplay.repaint();
                }
            }
        });
        
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