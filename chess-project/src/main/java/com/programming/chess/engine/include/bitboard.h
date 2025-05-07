/**
 * bitboard.h
 * Bitboard operations and utilities
 */

 #ifndef BITBOARD_H
 #define BITBOARD_H
 
 #include "engine.h"
 
 /* Bitboard utility macros */
 #define BB_SET_BIT(bb, square) ((bb) |= (1ULL << (square)))
 #define BB_CLEAR_BIT(bb, square) ((bb) &= ~(1ULL << (square)))
 #define BB_TEST_BIT(bb, square) ((bb) & (1ULL << (square)))
 
 /* Pre-calculated attack tables */
 extern Bitboard KNIGHT_ATTACKS[64];
 extern Bitboard KING_ATTACKS[64];
 extern Bitboard PAWN_ATTACKS[2][64]; /* [color][square] */
 
 /* Pre-calculated masks */
 extern Bitboard RANK_MASKS[8];
 extern Bitboard FILE_MASKS[8];
 extern Bitboard DIAGONAL_MASKS[15];
 extern Bitboard ANTIDIAGONAL_MASKS[15];
 
 /* Sliding piece attack functions */
 Bitboard get_bishop_attacks(Square square, Bitboard occupied);
 Bitboard get_rook_attacks(Square square, Bitboard occupied);
 Bitboard get_queen_attacks(Square square, Bitboard occupied);
 
 /* Bit manipulation */
 int pop_lsb(Bitboard* bb);
 int count_bits(Bitboard bb);
 Square get_lsb(Bitboard bb);
 
 /* Bitboard initialization */
 void init_bitboards();
 
 /* Debug */
 void print_bitboard(Bitboard bb);
 
 #endif /* BITBOARD_H */