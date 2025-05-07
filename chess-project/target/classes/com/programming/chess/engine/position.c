/**
 * position.c
 * Implementation of board representation and move generation
 */

 #include "include/position.h"
 #include <stdio.h>
 #include <stdlib.h>
 #include <string.h>
 
 // Zobrist hash keys
 uint64_t piece_keys[2][7][64];  // [color][piece_type][square]
 uint64_t castling_keys[16];
 uint64_t side_to_move_key;
 uint64_t en_passant_keys[64];
 
 /**
  * Initialize Zobrist hash keys for board representation
  */
 void init_hash_keys() {
     // Initialize random number generator
     srand(0xf00d);  // Fixed seed for reproducibility
     
     // Initialize piece keys
     for (int color = 0; color < 2; color++) {
         for (int piece = 0; piece < 7; piece++) {
             for (int square = 0; square < 64; square++) {
                 piece_keys[color][piece][square] = ((uint64_t)rand() << 32) | rand();
             }
         }
     }
     
     // Initialize castling keys
     for (int i = 0; i < 16; i++) {
         castling_keys[i] = ((uint64_t)rand() << 32) | rand();
     }
     
     // Initialize side to move key
     side_to_move_key = ((uint64_t)rand() << 32) | rand();
     
     // Initialize en passant keys
     for (int square = 0; square < 64; square++) {
         en_passant_keys[square] = ((uint64_t)rand() << 32) | rand();
     }
     
     printf("Hash keys initialized successfully\n");
 }
 
 /**
  * Calculate Zobrist hash for a board position
  */
 uint64_t calculate_hash(const Board* board) {
     uint64_t hash = 0;
     
     // Add piece contributions
     for (int color = 0; color < 2; color++) {
         for (int piece = 1; piece < 7; piece++) {
             Bitboard pieces = board->pieces[color][piece];
             while (pieces) {
                 int square = pop_lsb(&pieces);
                 hash ^= piece_keys[color][piece][square];
             }
         }
     }
     
     // Add castling rights
     hash ^= castling_keys[board->castling_rights];
     
     // Add side to move
     if (board->side_to_move == BLACK) {
         hash ^= side_to_move_key;
     }
     
     // Add en passant
     if (board->en_passant_square != -1) {
         hash ^= en_passant_keys[board->en_passant_square];
     }
     
     return hash;
 }
 
 /**
  * Reset the board to the starting position
  */
 void board_reset(Board* board) {
     memset(board, 0, sizeof(Board));
     
     // Set up pieces
     // White pawns
     board->pieces[WHITE][PAWN] = 0x000000000000FF00ULL;
     // White knights
     board->pieces[WHITE][KNIGHT] = 0x0000000000000042ULL;
     // White bishops
     board->pieces[WHITE][BISHOP] = 0x0000000000000024ULL;
     // White rooks
     board->pieces[WHITE][ROOK] = 0x0000000000000081ULL;
     // White queens
     board->pieces[WHITE][QUEEN] = 0x0000000000000008ULL;
     // White king
     board->pieces[WHITE][KING] = 0x0000000000000010ULL;
     
     // Black pawns
     board->pieces[BLACK][PAWN] = 0x00FF000000000000ULL;
     // Black knights
     board->pieces[BLACK][KNIGHT] = 0x4200000000000000ULL;
     // Black bishops
     board->pieces[BLACK][BISHOP] = 0x2400000000000000ULL;
     // Black rooks
     board->pieces[BLACK][ROOK] = 0x8100000000000000ULL;
     // Black queens
     board->pieces[BLACK][QUEEN] = 0x0800000000000000ULL;
     // Black king
     board->pieces[BLACK][KING] = 0x1000000000000000ULL;
     
     // Calculate aggregate bitboards
     for (int piece = 1; piece < 7; piece++) {
         board->occupied[WHITE] |= board->pieces[WHITE][piece];
         board->occupied[BLACK] |= board->pieces[BLACK][piece];
     }
     
     board->all_pieces = board->occupied[WHITE] | board->occupied[BLACK];
     
     // Game state
     board->side_to_move = WHITE;
     board->castling_rights = CASTLE_WHITE_KING | CASTLE_WHITE_QUEEN | CASTLE_BLACK_KING | CASTLE_BLACK_QUEEN;
     board->en_passant_square = -1;
     board->halfmove_clock = 0;
     board->fullmove_number = 1;
     board->history_ply = 0;
     
     // Calculate hash
     board->hash = calculate_hash(board);
     
     printf("Board reset to initial position\n");
 }
 
 /**
  * Convert FEN string to board representation
  */
 bool board_from_fen(Board* board, const char* fen) {
     memset(board, 0, sizeof(Board));
     
     // Example minimal FEN parser
     int square = 0;
     int field = 0;
     
     // Parse piece placement (first field)
     while (field == 0) {
         if (*fen == ' ') {
             field++;
             fen++;
             continue;
         }
         
         if (*fen == '/') {
             fen++;
             continue;
         }
         
         if (*fen >= '1' && *fen <= '8') {
             square += *fen - '0';
             fen++;
             continue;
         }
         
         // Parse piece
         PieceType piece;
         Color color;
         
         switch (*fen) {
             case 'P': piece = PAWN; color = WHITE; break;
             case 'N': piece = KNIGHT; color = WHITE; break;
             case 'B': piece = BISHOP; color = WHITE; break;
             case 'R': piece = ROOK; color = WHITE; break;
             case 'Q': piece = QUEEN; color = WHITE; break;
             case 'K': piece = KING; color = WHITE; break;
             case 'p': piece = PAWN; color = BLACK; break;
             case 'n': piece = KNIGHT; color = BLACK; break;
             case 'b': piece = BISHOP; color = BLACK; break;
             case 'r': piece = ROOK; color = BLACK; break;
             case 'q': piece = QUEEN; color = BLACK; break;
             case 'k': piece = KING; color = BLACK; break;
             default: return false; // Invalid FEN
         }
         
         board->pieces[color][piece] |= 1ULL << square;
         square++;
         fen++;
     }
     
     // Parse active color (second field)
     if (*fen == 'w') {
         board->side_to_move = WHITE;
     } else if (*fen == 'b') {
         board->side_to_move = BLACK;
     } else {
         return false; // Invalid FEN
     }
     
     // Skip to next field
     fen += 2;
     field++;
     
     // Parse castling rights (third field)
     board->castling_rights = 0;
     if (*fen == '-') {
         fen += 2;
     } else {
         while (*fen != ' ') {
             switch (*fen) {
                 case 'K': board->castling_rights |= CASTLE_WHITE_KING; break;
                 case 'Q': board->castling_rights |= CASTLE_WHITE_QUEEN; break;
                 case 'k': board->castling_rights |= CASTLE_BLACK_KING; break;
                 case 'q': board->castling_rights |= CASTLE_BLACK_QUEEN; break;
                 default: return false; // Invalid FEN
             }
             fen++;
         }
         fen++;
     }
     field++;
     
     // Parse en passant target square (fourth field)
     if (*fen == '-') {
         board->en_passant_square = -1;
         fen += 2;
     } else {
         char file = *fen++;
         char rank = *fen++;
         board->en_passant_square = (8 - (rank - '0')) * 8 + (file - 'a');
         fen++;
     }
     field++;
     
     // Parse halfmove clock (fifth field)
     board->halfmove_clock = atoi(fen);
     
     // Skip to next field
     while (*fen != ' ' && *fen != '\0') fen++;
     if (*fen == ' ') fen++;
     field++;
     
     // Parse fullmove number (sixth field)
     if (field < 6) {
         board->fullmove_number = 1; // Default
     } else {
         board->fullmove_number = atoi(fen);
     }
     
     // Calculate aggregate bitboards
     for (int piece = 1; piece < 7; piece++) {
         board->occupied[WHITE] |= board->pieces[WHITE][piece];
         board->occupied[BLACK] |= board->pieces[BLACK][piece];
     }
     
     board->all_pieces = board->occupied[WHITE] | board->occupied[BLACK];
     
     // Calculate hash
     board->hash = calculate_hash(board);
     
     return true;
 }
 
 /**
  * Convert move to algebraic notation
  */
 const char* move_to_string(Move move, char* buffer) {
     static char static_buffer[6];
     if (buffer == NULL) buffer = static_buffer;
     
     if (move == MOVE_NONE) {
         strcpy(buffer, "none");
         return buffer;
     }
     
     if (move == MOVE_NULL) {
         strcpy(buffer, "null");
         return buffer;
     }
     
     int from = GET_MOVE_FROM(move);
     int to = GET_MOVE_TO(move);
     int promotion = GET_MOVE_PROMOTION(move);
     
     buffer[0] = 'a' + (from % 8);
     buffer[1] = '8' - (from / 8);
     buffer[2] = 'a' + (to % 8);
     buffer[3] = '8' - (to / 8);
     
     if (promotion != 0) {
         char promo_pieces[] = "nbrq";
         buffer[4] = promo_pieces[promotion - 1];
         buffer[5] = '\0';
     } else {
         buffer[4] = '\0';
     }
     
     return buffer;
 }