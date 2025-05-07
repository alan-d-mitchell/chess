/**
 * bitboard.c
 * Implementation of bitboard operations
 */

 #include "include/bitboard.h"
 #include <stdio.h>
 
 // Pre-calculated attack tables
 Bitboard KNIGHT_ATTACKS[64] = {0};
 Bitboard KING_ATTACKS[64] = {0};
 Bitboard PAWN_ATTACKS[2][64] = {{0}}; // [color][square]
 
 // Pre-calculated masks
 Bitboard RANK_MASKS[8] = {0};
 Bitboard FILE_MASKS[8] = {0};
 Bitboard DIAGONAL_MASKS[15] = {0};
 Bitboard ANTIDIAGONAL_MASKS[15] = {0};
 
 /**
  * Initialize all bitboard tables and masks
  */
 void init_bitboards() {
     int i, j;
     
     // Initialize rank masks (all bits in a rank)
     for (i = 0; i < 8; i++) {
         RANK_MASKS[i] = 0xFFULL << (i * 8);
     }
     
     // Initialize file masks (all bits in a file)
     for (i = 0; i < 8; i++) {
         FILE_MASKS[i] = 0x0101010101010101ULL << i;
     }
     
     // Initialize knight attack patterns
     for (i = 0; i < 64; i++) {
         int r = i / 8;
         int c = i % 8;
         Bitboard b = 0;
         
         // Knight moves
         if (r >= 2 && c >= 1) b |= 1ULL << ((r-2) * 8 + (c-1));
         if (r >= 2 && c <= 6) b |= 1ULL << ((r-2) * 8 + (c+1));
         if (r >= 1 && c >= 2) b |= 1ULL << ((r-1) * 8 + (c-2));
         if (r >= 1 && c <= 5) b |= 1ULL << ((r-1) * 8 + (c+2));
         if (r <= 6 && c >= 2) b |= 1ULL << ((r+1) * 8 + (c-2));
         if (r <= 6 && c <= 5) b |= 1ULL << ((r+1) * 8 + (c+2));
         if (r <= 5 && c >= 1) b |= 1ULL << ((r+2) * 8 + (c-1));
         if (r <= 5 && c <= 6) b |= 1ULL << ((r+2) * 8 + (c+1));
         
         KNIGHT_ATTACKS[i] = b;
     }
     
     // Initialize king attack patterns
     for (i = 0; i < 64; i++) {
         int r = i / 8;
         int c = i % 8;
         Bitboard b = 0;
         
         // King moves (all 8 directions)
         for (int dr = -1; dr <= 1; dr++) {
             for (int dc = -1; dc <= 1; dc++) {
                 if (dr == 0 && dc == 0) continue;
                 int nr = r + dr;
                 int nc = c + dc;
                 if (nr >= 0 && nr < 8 && nc >= 0 && nc < 8) {
                     b |= 1ULL << (nr * 8 + nc);
                 }
             }
         }
         
         KING_ATTACKS[i] = b;
     }
     
     // Initialize pawn attack patterns
     for (i = 0; i < 64; i++) {
         int r = i / 8;
         int c = i % 8;
         
         // White pawn attacks (up-left, up-right)
         if (r > 0) {
             if (c > 0) PAWN_ATTACKS[WHITE][i] |= 1ULL << ((r-1) * 8 + (c-1));
             if (c < 7) PAWN_ATTACKS[WHITE][i] |= 1ULL << ((r-1) * 8 + (c+1));
         }
         
         // Black pawn attacks (down-left, down-right)
         if (r < 7) {
             if (c > 0) PAWN_ATTACKS[BLACK][i] |= 1ULL << ((r+1) * 8 + (c-1));
             if (c < 7) PAWN_ATTACKS[BLACK][i] |= 1ULL << ((r+1) * 8 + (c+1));
         }
     }
     
     printf("Bitboards initialized successfully\n");
 }
 
 /**
  * Get bishop attacks from a square on a given board
  */
 Bitboard get_bishop_attacks(Square square, Bitboard occupied) {
     // Simple implementation - just return a pattern without considering blockages
     int r = square / 8;
     int c = square % 8;
     Bitboard attacks = 0;
     
     // Simplified diagonal attacks
     for (int dr = -1; dr <= 1; dr += 2) {
         for (int dc = -1; dc <= 1; dc += 2) {
             for (int i = 1; i < 8; i++) {
                 int nr = r + i * dr;
                 int nc = c + i * dc;
                 if (nr < 0 || nr >= 8 || nc < 0 || nc >= 8) break;
                 
                 attacks |= 1ULL << (nr * 8 + nc);
                 if (occupied & (1ULL << (nr * 8 + nc))) break;
             }
         }
     }
     
     return attacks;
 }
 
 /**
  * Get rook attacks from a square on a given board
  */
 Bitboard get_rook_attacks(Square square, Bitboard occupied) {
     // Simple implementation - just return a pattern without considering blockages
     int r = square / 8;
     int c = square % 8;
     Bitboard attacks = 0;
     
     // Horizontal attacks
     for (int dc = -1; dc <= 1; dc += 2) {
         for (int i = 1; i < 8; i++) {
             int nc = c + i * dc;
             if (nc < 0 || nc >= 8) break;
             
             attacks |= 1ULL << (r * 8 + nc);
             if (occupied & (1ULL << (r * 8 + nc))) break;
         }
     }
     
     // Vertical attacks
     for (int dr = -1; dr <= 1; dr += 2) {
         for (int i = 1; i < 8; i++) {
             int nr = r + i * dr;
             if (nr < 0 || nr >= 8) break;
             
             attacks |= 1ULL << (nr * 8 + c);
             if (occupied & (1ULL << (nr * 8 + c))) break;
         }
     }
     
     return attacks;
 }
 
 /**
  * Get queen attacks from a square on a given board
  */
 Bitboard get_queen_attacks(Square square, Bitboard occupied) {
     return get_bishop_attacks(square, occupied) | get_rook_attacks(square, occupied);
 }
 
 /**
  * Count the number of set bits in a bitboard
  */
 int count_bits(Bitboard bb) {
     int count = 0;
     while (bb) {
         count++;
         bb &= bb - 1;  // Clear the least significant bit
     }
     return count;
 }
 
 /**
  * Get the index of the least significant bit
  */
 Square get_lsb(Bitboard bb) {
     if (bb == 0) return 0;
     return __builtin_ctzll(bb);  // GCC builtin for count trailing zeros
 }
 
 /**
  * Get and clear the least significant bit
  */
 int pop_lsb(Bitboard* bb) {
     int lsb = get_lsb(*bb);
     *bb &= *bb - 1;  // Clear the least significant bit
     return lsb;
 }
 
 /**
  * Print a bitboard for debugging
  */
 void print_bitboard(Bitboard bb) {
     printf("Bitboard: 0x%016llx\n", (unsigned long long)bb);
     for (int row = 0; row < 8; row++) {
         for (int col = 0; col < 8; col++) {
             int square = row * 8 + col;
             printf("%c ", (bb & (1ULL << square)) ? '1' : '.');
         }
         printf("\n");
     }
 }