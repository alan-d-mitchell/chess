/**
 * jni_bridge.h
 * Java Native Interface functions for integration with the Java GUI
 */

 #ifndef JNI_BRIDGE_H
 #define JNI_BRIDGE_H
 
 #include <jni.h>
 #include "engine.h"
 
 /* JNI function prototypes */
 #ifdef __cplusplus
 extern "C" {
 #endif
 
 /* Initialize the engine */
 JNIEXPORT void JNICALL Java_com_programming_chess_engine_NativeEngine_initEngine(JNIEnv* env, jobject obj);
 
 /* Get the best move for a position */
 JNIEXPORT jstring JNICALL Java_com_programming_chess_engine_NativeEngine_getBestMove(
     JNIEnv* env, jobject obj, jstring fen, jint depth);
 
 /* Start the reinforcement learning training process */
 JNIEXPORT void JNICALL Java_com_programming_chess_engine_NativeEngine_trainNetwork(
     JNIEnv* env, jobject obj, jint games);
 
 /* Save the neural network to a file */
 JNIEXPORT void JNICALL Java_com_programming_chess_engine_NativeEngine_saveNetwork(
     JNIEnv* env, jobject obj, jstring filename);
 
 /* Load the neural network from a file */
 JNIEXPORT jboolean JNICALL Java_com_programming_chess_engine_NativeEngine_loadNetwork(
     JNIEnv* env, jobject obj, jstring filename);
 
 /* Evaluate a position with the neural network */
 JNIEXPORT jfloat JNICALL Java_com_programming_chess_engine_NativeEngine_evaluatePosition(
     JNIEnv* env, jobject obj, jstring fen);
 
 /* Get policy (move probabilities) for a position */
 JNIEXPORT jobjectArray JNICALL Java_com_programming_chess_engine_NativeEngine_getPolicyMoves(
     JNIEnv* env, jobject obj, jstring fen, jint topN);
 
 /* Stop any ongoing calculations */
 JNIEXPORT void JNICALL Java_com_programming_chess_engine_NativeEngine_stopCalculation(
     JNIEnv* env, jobject obj);
 
 /* Check if a move is legal */
 JNIEXPORT jboolean JNICALL Java_com_programming_chess_engine_NativeEngine_isMoveLegal(
     JNIEnv* env, jobject obj, jstring fen, jstring move);
 
 /* Get engine version */
 JNIEXPORT jstring JNICALL Java_com_programming_chess_engine_NativeEngine_getEngineVersion(
     JNIEnv* env, jobject obj);
 
 /* Set the network visualization callback */
 JNIEXPORT void JNICALL Java_com_programming_chess_engine_NativeEngine_setTrainingCallback(
     JNIEnv* env, jobject obj, jobject callback);
 
 /* JNI utility functions */
 void jni_update_training_progress(int game, int total_games, float win_rate, float loss);
 void jni_send_self_play_move(const char* fen, const char* move);
 
 #ifdef __cplusplus
 }
 #endif
 
 #endif /* JNI_BRIDGE_H */