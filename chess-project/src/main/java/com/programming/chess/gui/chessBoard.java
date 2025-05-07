package com.programming.chess.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class chessBoard extends JFrame {

    private final int BOARD_SIZE = 8;
    private Map<String, ImageIcon> pieceImages = new HashMap<>();
    private String[][] board = new String[BOARD_SIZE][BOARD_SIZE];
    private JPanel chessDisplay;
    
    // Declare these variables but don't set them to constants
    private int squareSize;
    private int xOffset = 0;
    private int yOffset = 0;

    public chessBoard() {
        super("Chess");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(800, 800));

        loadImages();
        initializeBoard();
        
        chessDisplay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                calculateBoardDimensions();

                drawBoard(g);
                drawPieces(g);
            }
        };

        chessDisplay.setBackground(Color.DARK_GRAY);
        
        chessDisplay.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = (e.getX() - xOffset) / squareSize;
                int row = (e.getY() - yOffset) / squareSize;
                
                // Make sure the click is within the board
                if (col >= 0 && col < BOARD_SIZE && row >= 0 && row < BOARD_SIZE) {
                    System.out.println("Clicked on square: " + (char)('a' + col) + (8 - row));
                    System.out.println("Piece at this position: " + (board[row][col] != null ? board[row][col] : "empty"));
                }
            }
        });
        
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                chessDisplay.repaint();
            }
        });
        
        setResizable(true);
        
        add(chessDisplay);
        pack();

        setMinimumSize(new Dimension(400, 400));
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

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                boolean isLightSquare = (row + col) % 2 == 0;
                g.setColor(isLightSquare ? lightSquare : darkSquare);

                int x = xOffset + col * squareSize;
                int y = yOffset + row * squareSize;
                g.fillRect(x, y, squareSize, squareSize);

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

        System.out.println("Drawing pieces...");
        int piecesDrawn = 0;

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                String piece = board[row][col];

                if (piece != null) {
                    if (pieceImages.containsKey(piece)) {
                        ImageIcon icon = pieceImages.get(piece);

                        int x = xOffset + col * squareSize;
                        int y = yOffset + row * squareSize;

                        Image image = icon.getImage();
                        Image scaledImage = image.getScaledInstance(squareSize, squareSize, Image.SCALE_SMOOTH);

                        g.drawImage(scaledImage, x, y, null);
                        piecesDrawn++;
                    } else {
                        System.out.println("Missing piece image for: " + piece + " at position " + row + "," + col);
                    }
                }
            }
        }
        
        System.out.println("Pieces drawn: " + piecesDrawn);
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