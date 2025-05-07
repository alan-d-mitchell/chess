/**
 * search.h
 * Search algorithms for finding the best move
 */

 #ifndef SEARCH_H
 #define SEARCH_H
 
 #include "engine.h"
 
 /* Search limits */
 #define MAX_DEPTH 64
 #define MAX_PLY 128
 
 /* Search result structure */
 typedef struct {
     Move best_move;
     int score;
     int depth;
     uint64_t nodes_searched;
     int selective_depth;
     Move pv[MAX_PLY];
     int pv_length;
     uint64_t time_spent_ms;
 } SearchResult;
 
 /* Transposition table entry types */
 typedef enum {
     TT_NONE,
     TT_EXACT,    /* Exact score */
     TT_ALPHA,    /* Upper bound (fail low) */
     TT_BETA      /* Lower bound (fail high) */
 } TTEntryType;
 
 /* Transposition table entry */
 typedef struct {
     uint64_t key;        /* Position hash */
     Move best_move;      /* Best move in this position */
     int16_t score;       /* Position score */
     uint8_t depth;       /* Search depth */
     uint8_t type;        /* Entry type */
     int age;             /* Used for replacement strategy */
 } TTEntry;
 
 /* Transposition table */
 typedef struct {
     TTEntry* entries;    /* Array of entries */
     int size;            /* Size of table in entries */
 } TranspositionTable;
 
 /* Search parameters */
 typedef struct {
     int max_depth;               /* Maximum search depth */
     uint64_t max_nodes;          /* Maximum nodes to search */
     uint64_t max_time_ms;        /* Maximum time to search in milliseconds */
     bool use_nn;                 /* Whether to use neural network for evaluation */
     bool use_transposition_table; /* Whether to use transposition table */
     int contempt;                /* Contempt factor for draws */
 } SearchParams;
 
 /* Search functions */
 void init_search();
 SearchResult search_position(Board* board, SearchParams* params);
 Move get_best_move(Board* board, int depth);
 int alpha_beta(Board* board, int depth, int alpha, int beta, bool is_pv, Move* pv, int* pv_length);
 int quiescence_search(Board* board, int alpha, int beta);
 
 /* Transposition table functions */
 void tt_init(TranspositionTable* tt, int size_mb);
 void tt_free(TranspositionTable* tt);
 TTEntry* tt_probe(TranspositionTable* tt, uint64_t key, bool* found);
 void tt_store(TranspositionTable* tt, uint64_t key, Move move, int score, int depth, TTEntryType type, int age);
 void tt_clear(TranspositionTable* tt);
 
 /* External transposition table */
 extern TranspositionTable TT;
 
 #endif /* SEARCH_H */