/**
 * chess_engine.h
 * Main header file for the chess engine
 */

 #ifndef ENGINE_H
 #define ENGINE_H
 
 #include <stdint.h>
 #include <stdbool.h>
 
 /**
  * Bitboard representation using 64-bit integer
  * Each bit represents a square on the board:
  * - Bit 0 = a1, Bit 1 = b1, ..., Bit 7 = h1
  * - Bit 8 = a2, ...
  * - Bit 63 = h8
  */
 typedef uint64_t Bitboard;
 
 /* Piece definitions */
 typedef enum {
     EMPTY = 0,
     PAWN = 1,
     KNIGHT = 2,
     BISHOP = 3,
     ROOK = 4,
     QUEEN = 5,
     KING = 6
 } PieceType;
 
 /* Color definitions */
 typedef enum {
     WHITE = 0,
     BLACK = 1
 } Color;
 
 /* Square indices (0-63) */
 typedef enum {
     A1, B1, C1, D1, E1, F1, G1, H1,
     A2, B2, C2, D2, E2, F2, G2, H2,
     A3, B3, C3, D3, E3, F3, G3, H3,
     A4, B4, C4, D4, E4, F4, G4, H4,
     A5, B5, C5, D5, E5, F5, G5, H5,
     A6, B6, C6, D6, E6, F6, G6, H6,
     A7, B7, C7, D7, E7, F7, G7, H7,
     A8, B8, C8, D8, E8, F8, G8, H8
 } Square;
 
 /* Castling rights */
 #define CASTLE_WHITE_KING   1
 #define CASTLE_WHITE_QUEEN  2
 #define CASTLE_BLACK_KING   4
 #define CASTLE_BLACK_QUEEN  8
 
 /**
  * Move structure (16 bits)
  * - from: 6 bits (0-63)
  * - to: 6 bits (0-63)
  * - promotion: 3 bits (0=none, 1=N, 2=B, 3=R, 4=Q)
  * - flags: 1 bit (0=normal, 1=special)
  */
 typedef uint16_t Move;
 
 #define MOVE_NONE 0
 #define MOVE_NULL 65535
 
 /* Move creation and access macros */
 #define CREATE_MOVE(from, to) ((from) | ((to) << 6))
 #define CREATE_PROMOTION_MOVE(from, to, piece) ((from) | ((to) << 6) | ((piece) << 12) | (1 << 15))
 #define CREATE_SPECIAL_MOVE(from, to) ((from) | ((to) << 6) | (1 << 15))
 
 #define GET_MOVE_FROM(move) ((move) & 0x3F)
 #define GET_MOVE_TO(move) (((move) >> 6) & 0x3F)
 #define GET_MOVE_PROMOTION(move) (((move) >> 12) & 0x7)
 #define IS_MOVE_SPECIAL(move) ((move) & (1 << 15))
 
 /**
  * Board representation
  */
 typedef struct {
     /* Bitboards for piece positions */
     Bitboard pieces[2][7];  /* [color][piece_type] */
     Bitboard occupied[2];   /* [color] */
     Bitboard all_pieces;    /* All pieces */
     
     /* Game state */
     Color side_to_move;
     int castling_rights;
     Square en_passant_square;
     int halfmove_clock;
     int fullmove_number;
     
     /* Position hash (for transposition table) */
     uint64_t hash;
     
     /* Move history for unmake_move */
     struct {
         Move move;
         int castling_rights;
         Square en_passant_square;
         int halfmove_clock;
         uint64_t hash;
         int captured_piece;
     } history[1024];
     int history_ply;
 } Board;
 
 /**
  * Move list structure
  */
 typedef struct {
     Move moves[256];
     int count;
 } MoveList;
 
 /* Neural network parameters */
 typedef struct {
     float* weights;
     float* biases;
     int size;
 } NNLayer;
 
 /**
  * Neural network for reinforcement learning
  */
 typedef struct {
     /* Input layer for board features */
     int input_size;
     
     /* Hidden layers */
     int hidden_layers;
     NNLayer* hidden;
     
     /* Output layer */
     NNLayer output_value;  /* Value head (evaluation) */
     NNLayer output_policy; /* Policy head (move probabilities) */
 } NeuralNetwork;
 
 /* Function declarations */
 #include "bitboard.h"
 #include "position.h"
 #include "movegen.h"
 #include "evaluate.h"
 #include "search.h"
 #include "learning.h"
 #include "uci.h"
 
 #endif /* CHESS_ENGINE_H */