package com.programming.chess.engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Java Native Interface for the C++ chess engine.
 * This class provides access to the native chess engine functionality.
 */
public class nativeEngine {
    
    // Native library loading
    static {
        try {
            // Determine OS for loading the appropriate library
            String osName = System.getProperty("os.name").toLowerCase();
            String libPath;
            String libName;
            
            if (osName.contains("windows")) {
                libPath = "/native/windows/libchessengine.dll";
                libName = "libchessengine.dll";
            } 
            else if (osName.contains("mac")) {
                libPath = "/native/mac/libchessengine.dylib";
                libName = "libchessengine.dylib";
            } 
            else {
                // Default to Linux
                libPath = "/native/linux/libchessengine.so";
                libName = "libchessengine.so";
            }
            
            System.out.println("Loading native library from: " + libPath);
            
            // Get the library as a resource using class context (not instance)
            InputStream stream = nativeEngine.class.getResourceAsStream(libPath);

            if (stream == null) {
                throw new FileNotFoundException("Could not find library: " + libPath);
            }
            
            // Create a temp file to extract the library to
            File tempFile = File.createTempFile("libchessengine", 
                libPath.endsWith(".dll") ? ".dll" : 
                libPath.endsWith(".dylib") ? ".dylib" : ".so");
            tempFile.deleteOnExit();
            
            // Copy the library to the temp file
            FileOutputStream out = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = stream.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }
            stream.close();
            out.close();
            
            // Load the library from the temp file
            System.load(tempFile.getAbsolutePath());
            System.out.println("Successfully loaded library from temp file: " + tempFile.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Failed to load native library: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Constructor
    public nativeEngine() {
        // Initialize the engine when an instance is created
        initEngine();
    }
    
    // Native method declarations
    
    /**
     * Initialize the chess engine.
     */
    public native void initEngine();
    
    /**
     * Get the best move for a given board position.
     * 
     * @param fen FEN string representing the board position
     * @param depth Search depth
     * @return String representation of the best move (e.g., "e2e4")
     */
    public native String getBestMove(String fen, int depth);
    
    /**
     * Train the neural network for a specified number of games.
     * 
     * @param games Number of self-play games for training
     */
    public native void trainNetwork(int games);
    
    /**
     * Save the neural network to a file.
     * 
     * @param filename File to save the network to
     */
    public native void saveNetwork(String filename);
    
    /**
     * Load a neural network from a file.
     * 
     * @param filename File to load the network from
     * @return true if loading was successful, false otherwise
     */
    public native boolean loadNetwork(String filename);
    
    /**
     * Evaluate a position with the neural network.
     * 
     * @param fen FEN string representing the board position
     * @return Evaluation score
     */
    public native float evaluatePosition(String fen);
    
    /**
     * Get policy (move probabilities) for a position.
     * 
     * @param fen FEN string representing the board position
     * @param topN Number of top moves to return
     * @return Array of strings with move and probability pairs
     */
    public native String[] getPolicyMoves(String fen, int topN);
    
    /**
     * Stop any ongoing calculations.
     */
    public native void stopCalculation();
    
    /**
     * Check if a move is legal.
     * 
     * @param fen FEN string representing the board position
     * @param move Move stream algebraic notation (e.g., "e2e4")
     * @return true if the move is legal, false otherwise
     */
    public native boolean isMoveLegal(String fen, String move);
    
    /**
     * Get the engine version.
     * 
     * @return Engine version string
     */
    public native String getEngineVersion();
    
    /**
     * Set a callback for training progress updates.
     * 
     * @param callback Callback object implementing the TrainingCallback interface
     */
    public native void setTrainingCallback(TrainingCallback callback);
    
    /**
     * Interface for receiving training progress updates.
     */
    public interface TrainingCallback {
        void onProgressUpdate(int game, int totalGames, float winRate, float loss);
        void onSelfPlayMove(String fen, String move);
    }
}