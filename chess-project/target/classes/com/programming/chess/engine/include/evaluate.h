/**
 * evaluate.h
 * Position evaluation functions
 */

 #ifndef EVALUATE_H
 #define EVALUATE_H
 
 #include "engine.h"
 
 /* Piece values in centipawns */
 #define VALUE_PAWN    100
 #define VALUE_KNIGHT  320
 #define VALUE_BISHOP  330
 #define VALUE_ROOK    500
 #define VALUE_QUEEN   900
 #define VALUE_KING    20000
 
 /* Special evaluation scores */
 #define SCORE_MATE     30000
 #define SCORE_MATED   -30000
 #define SCORE_DRAW     0
 
 /* Evaluation constants */
 #define MOBILITY_FACTOR          2    /* Value per legal move */
 #define PAWN_STRUCTURE_FACTOR   10    /* Value for good pawn structure */
 #define KING_SAFETY_FACTOR      15    /* Value for king safety */
 #define CONNECTED_ROOKS_BONUS   25    /* Bonus for connected rooks */
 #define CENTER_CONTROL_BONUS    10    /* Bonus for controlling center */
 
 /* Phase definitions (for tapered evaluation) */
 #define PHASE_MIDGAME  0
 #define PHASE_ENDGAME  1
 #define TOTAL_PHASE   256       /* Full game phase value */
 
 /* Evaluation functions */
 int evaluate(const Board* board);
 int evaluate_material(const Board* board);
 int evaluate_piece_square_tables(const Board* board);
 int evaluate_mobility(const Board* board);
 int evaluate_pawn_structure(const Board* board);
 int evaluate_king_safety(const Board* board);
 int evaluate_rooks(const Board* board);
 int evaluate_center_control(const Board* board);
 
 /* Phase calculation (for tapered evaluation) */
 int calculate_phase(const Board* board);
 int calculate_tapered_score(int mg_score, int eg_score, int phase);
 
 /* Neural network evaluation - called when using reinforcement learning */
 float evaluate_nn(const Board* board, const NeuralNetwork* network);
 
 /* Utility evaluation functions */
 bool is_endgame(const Board* board);
 int get_piece_value(PieceType piece, int phase);
 
 /* External piece-square table definitions */
 extern const int PIECE_SQUARE_TABLES[2][7][64]; /* [phase][piece][square] */
 
 #endif /* EVALUATE_H */