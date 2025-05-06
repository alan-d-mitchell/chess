package com.programming.chess.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

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
        
        chessDisplay = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                calculateBoardDimensions();
                
                drawBoard(g);
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
                    // square click method (row, col)
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new chessBoard().setVisible(true);
        });
    }
}