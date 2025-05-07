/**
 * movegen.h
 * Move generation functions
 */

 #ifndef MOVEGEN_H
 #define MOVEGEN_H
 
 #include "engine.h"
 
 /* Move generation modes */
 typedef enum {
     ALL_MOVES,     /* Generate all legal moves */
     CAPTURE_MOVES, /* Generate only captures */
     QUIET_MOVES    /* Generate only non-captures */
 } MoveGenType;
 
 /* Move generation functions */
 void generate_moves(const Board* board, MoveList* list, MoveGenType type);
 void generate_legal_moves(const Board* board, MoveList* list, MoveGenType type);
 
 /* Individual piece move generators */
 void generate_pawn_moves(const Board* board, MoveList* list, MoveGenType type);
 void generate_knight_moves(const Board* board, MoveList* list, MoveGenType type);
 void generate_bishop_moves(const Board* board, MoveList* list, MoveGenType type);
 void generate_rook_moves(const Board* board, MoveList* list, MoveGenType type);
 void generate_queen_moves(const Board* board, MoveList* list, MoveGenType type);
 void generate_king_moves(const Board* board, MoveList* list, MoveGenType type);
 void generate_castling_moves(const Board* board, MoveList* list);
 
 /* Move list manipulation */
 void add_move(MoveList* list, Move move);
 bool contains_move(const MoveList* list, Move move);
 void print_move_list(const Board* board, const MoveList* list);
 
 #endif /* MOVEGEN_H */