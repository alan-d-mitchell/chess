/**
 * position.h
 * Board representation and state management
 */

 #ifndef POSITION_H
 #define POSITION_H
 
 #include "engine.h"
 
 /* FEN string for starting position */
 #define START_POSITION "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
 
 /* Board initialization */
 void board_reset(Board* board);
 bool board_from_fen(Board* board, const char* fen);
 char* board_to_fen(const Board* board);
 
 /* Board manipulation */
 bool make_move(Board* board, Move move);
 void unmake_move(Board* board);
 bool is_legal_move(const Board* board, Move move);
 
 /* Position evaluation */
 bool is_check(const Board* board);
 bool is_checkmate(const Board* board);
 bool is_stalemate(const Board* board);
 bool is_draw_by_material(const Board* board);
 bool is_draw_by_repetition(const Board* board);
 bool is_draw_by_fifty_move(const Board* board);
 
 /* Position key generation (Zobrist hashing) */
 uint64_t calculate_hash(const Board* board);
 extern uint64_t piece_keys[2][7][64];  /* [color][piece_type][square] */
 extern uint64_t castling_keys[16];
 extern uint64_t side_to_move_key;
 extern uint64_t en_passant_keys[64];
 void init_hash_keys();
 
 /* Utility functions */
 void print_board(const Board* board);
 Square algebraic_to_square(const char* algebraic);
 const char* square_to_algebraic(Square square);
 const char* move_to_string(Move move, char* buffer);
 Move parse_move(const Board* board, const char* move_str);
 
 #endif /* POSITION_H */