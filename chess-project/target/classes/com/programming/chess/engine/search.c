/**
 * search.c
 * Implementation of search algorithms
 */

 #include "include/search.h"
 #include <stdio.h>
 #include <stdlib.h>
 #include <string.h>
 #include <time.h>
 
 // Global transposition table
 TranspositionTable TT;
 
 /**
  * Initialize search parameters
  */
 void init_search() {
     printf("Search initialized\n");
 }
 
 /**
  * Initialize transposition table
  */
 void tt_init(TranspositionTable* tt, int size_mb) {
     // Calculate number of entries based on size
     size_t entries = (size_mb * 1024 * 1024) / sizeof(TTEntry);
     
     // Allocate memory for transposition table
     tt->entries = (TTEntry*)malloc(entries * sizeof(TTEntry));
     tt->size = entries;
     
     // Clear the table
     tt_clear(tt);
     
     printf("Transposition table initialized with %zu entries\n", entries);
 }
 
 /**
  * Free transposition table memory
  */
 void tt_free(TranspositionTable* tt) {
     if (tt->entries != NULL) {
         free(tt->entries);
         tt->entries = NULL;
         tt->size = 0;
     }
 }
 
 /**
  * Clear the transposition table
  */
 void tt_clear(TranspositionTable* tt) {
     memset(tt->entries, 0, tt->size * sizeof(TTEntry));
 }
 
 /**
  * Simple search function to find the best move
  */
 SearchResult search_position(Board* board, SearchParams* params) {
     SearchResult result;
     memset(&result, 0, sizeof(SearchResult));
     
     // For simplicity, just return a random legal move
     // In a real implementation, this would be a minimax/alpha-beta search
     
     // Get the first available move for the current side
     Move moves[16];
     int move_count = 0;
     
     // Add some sample moves for white
     if (board->side_to_move == WHITE) {
         moves[move_count++] = CREATE_MOVE(8, 16);  // a2a3
         moves[move_count++] = CREATE_MOVE(9, 17);  // b2b3
         moves[move_count++] = CREATE_MOVE(10, 18); // c2c3
         moves[move_count++] = CREATE_MOVE(11, 19); // d2d3
     } 
     // Add some sample moves for black
     else {
         moves[move_count++] = CREATE_MOVE(48, 40); // a7a6
         moves[move_count++] = CREATE_MOVE(49, 41); // b7b6
         moves[move_count++] = CREATE_MOVE(50, 42); // c7c6
         moves[move_count++] = CREATE_MOVE(51, 43); // d7d6
     }
     
     // Pick a random move
     if (move_count > 0) {
         int random_index = rand() % move_count;
         result.best_move = moves[random_index];
     } else {
         result.best_move = MOVE_NONE;
     }
     
     result.depth = params->max_depth;
     result.score = 0;
     result.nodes_searched = 100;
     result.selective_depth = params->max_depth;
     result.pv[0] = result.best_move;
     result.pv_length = 1;
     
     return result;
 }