/**
 * main.c
 * Entry point for the chess engine
 */

 #include <stdio.h>
 #include <stdlib.h>
 #include <string.h>
 #include <time.h>
 #include <signal.h>
 
 #include "include/engine.h"
 #include "include/bitboard.h"
 #include "include/position.h"
 #include "include/movegen.h"
 #include "include/evaluate.h"
 #include "include/search.h"
 #include "include/learning.h"
 #include "include/uci.h"
 #include "include/jni_bridge.h"
 
 /* Global variables */
 TranspositionTable TT;
 NeuralNetwork NN;
 bool stop_search = false;
 bool use_nn_evaluation = false;
 
 /* Handle Ctrl+C */
 void signal_handler(int signal) {
     stop_search = true;
 }
 
 /* Engine initialization */
 void init_engine() {
     /* Seed random number generator */
     srand((unsigned int)time(NULL));
     
     /* Initialize bitboards */
     init_bitboards();
     
     /* Initialize Zobrist hash keys */
     init_hash_keys();
     
     /* Initialize transposition table (default: 64 MB) */
     tt_init(&TT, 64);
     
     /* Initialize search parameters */
     init_search();
     
     /* Initialize UCI options */
     init_uci_options();
     
     /* Try to load neural network if it exists */
     if (nn_load(&NN, "weights.bin")) {
         printf("Neural network loaded successfully\n");
         use_nn_evaluation = true;
     } else {
         /* Initialize a new neural network if no existing one is found */
         printf("Initializing new neural network\n");
         nn_init(&NN, 768, 256, 4096);  /* Example sizes, adjust as needed */
         use_nn_evaluation = false;
     }
     
     /* Set up signal handler for Ctrl+C */
     signal(SIGINT, signal_handler);
     
     printf("Engine initialized successfully\n");
 }
 
 /* Entry point when called as a standalone program */
 int main(int argc, char* argv[]) {
     printf("Chess Engine with Reinforcement Learning\n");
     printf("Version 1.0\n");
     
     /* Initialize the engine */
     init_engine();
     
     /* Process command line arguments */
     if (argc > 1) {
         /* Training mode */
         if (strcmp(argv[1], "train") == 0) {
             int num_games = 100;  /* Default number of games */
             
             /* Parse number of games if provided */
             if (argc > 2) {
                 num_games = atoi(argv[2]);
             }
             
             printf("Starting self-play training for %d games\n", num_games);
             
             /* Initialize training parameters */
             LearningParams params;
             params.learning_rate = 0.01f;
             params.momentum = 0.9f;
             params.l2_regularization = 0.0001f;
             params.batch_size = 256;
             params.epochs = 10;
             params.exploration_rate = 0.25f;
             params.mcts_simulations = 800;
             params.dirichlet_alpha = 0.3f;
             params.dirichlet_epsilon = 0.25f;
             params.temperature = 1.0f;
             params.c_puct = 1.0f;
             
             /* Initialize training data */
             TrainingData data;
             training_data_init(&data, 10000);
             
             /* Run self-play and training */
             self_play(&NN, &data, num_games, &params);
             train_network(&NN, &data, &params);
             
             /* Save the trained network */
             nn_save(&NN, "weights.bin");
             save_training_data(&data, "training_data.bin");
             
             /* Free resources */
             training_data_free(&data);
             
             printf("Training completed and model saved\n");
             return 0;
         }
         /* Benchmark mode */
         else if (strcmp(argv[1], "bench") == 0) {
             int depth = 6;  /* Default depth */
             
             /* Parse depth if provided */
             if (argc > 2) {
                 depth = atoi(argv[2]);
             }
             
             printf("Running benchmark at depth %d\n", depth);
             
             /* Initialize a standard board position */
             Board board;
             board_reset(&board);
             
             /* Record start time */
             clock_t start = clock();
             
             /* Search to specified depth */
             SearchParams params;
             params.max_depth = depth;
             params.max_nodes = UINT64_MAX;
             params.max_time_ms = UINT64_MAX;
             params.use_nn = use_nn_evaluation;
             params.use_transposition_table = true;
             params.contempt = 0;
             
             SearchResult result = search_position(&board, &params);
             
             /* Calculate elapsed time */
             clock_t end = clock();
             double elapsed = (double)(end - start) / CLOCKS_PER_SEC;
             
             /* Print benchmark results */
             printf("Depth: %d\n", result.depth);
             printf("Nodes: %llu\n", result.nodes_searched);
             printf("Time: %.2f seconds\n", elapsed);
             printf("Nodes per second: %.0f\n", result.nodes_searched / elapsed);
             printf("Best move: %s\n", move_to_string(result.best_move, NULL));
             
             return 0;
         }
     }
     
     /* Default: UCI mode */
     printf("Starting UCI mode\n");
     uci_loop();
     
     /* Cleanup */
     tt_free(&TT);
     nn_free(&NN);
     
     return 0;
 }
 
 /* Entry point for JNI calls */
 JNIEXPORT void JNICALL Java_com_programming_chess_engine_NativeEngine_initEngine(JNIEnv* env, jobject obj) {
     init_engine();
 }
 
 /* Implementation of JNI getBestMove */
 JNIEXPORT jstring JNICALL Java_com_programming_chess_engine_NativeEngine_getBestMove(
     JNIEnv* env, jobject obj, jstring fen, jint depth) {
     
     /* Convert Java string to C string */
     const char* fen_str = (*env)->GetStringUTFChars(env, fen, NULL);
     
     /* Initialize board from FEN */
     Board board;
     if (!board_from_fen(&board, fen_str)) {
         (*env)->ReleaseStringUTFChars(env, fen, fen_str);
         return (*env)->NewStringUTF(env, "");
     }
     
     /* Release Java string */
     (*env)->ReleaseStringUTFChars(env, fen, fen_str);
     
     /* Set up search parameters */
     SearchParams params;
     params.max_depth = depth;
     params.max_nodes = UINT64_MAX;
     params.max_time_ms = 10000;  /* Default to 10 seconds max */
     params.use_nn = use_nn_evaluation;
     params.use_transposition_table = true;
     params.contempt = 0;
     
     /* Search for best move */
     SearchResult result = search_position(&board, &params);
     
     /* Convert move to string */
     char move_str[6];
     move_to_string(result.best_move, move_str);
     
     /* Return as Java string */
     return (*env)->NewStringUTF(env, move_str);
 }
 
 /* Implementation of JNI trainNetwork */
 JNIEXPORT void JNICALL Java_com_programming_chess_engine_NativeEngine_trainNetwork(
     JNIEnv* env, jobject obj, jint games) {
     
     /* Initialize training parameters */
     LearningParams params;
     params.learning_rate = 0.01f;
     params.momentum = 0.9f;
     params.l2_regularization = 0.0001f;
     params.batch_size = 256;
     params.epochs = 10;
     params.exploration_rate = 0.25f;
     params.mcts_simulations = 800;
     params.dirichlet_alpha = 0.3f;
     params.dirichlet_epsilon = 0.25f;
     params.temperature = 1.0f;
     params.c_puct = 1.0f;
     
     /* Initialize training data */
     TrainingData data;
     training_data_init(&data, 10000);
     
     /* Run self-play and training */
     self_play(&NN, &data, games, &params);
     train_network(&NN, &data, &params);
     
     /* Save the trained network */
     nn_save(&NN, "weights.bin");
     save_training_data(&data, "training_data.bin");
     
     /* Free resources */
     training_data_free(&data);
     
     /* Set flag to use neural network evaluation */
     use_nn_evaluation = true;
 }