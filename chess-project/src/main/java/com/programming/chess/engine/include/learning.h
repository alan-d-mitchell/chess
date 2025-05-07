/**
 * learning.h
 * Reinforcement learning implementation
 */

 #ifndef LEARNING_H
 #define LEARNING_H
 
 #include "engine.h"
 
 /* Training example structure */
 typedef struct {
     Board position;      /* Board position */
     float value;         /* Game outcome or predicted value */
     float policy[4096];  /* Move probabilities (indexed by from_square*64 + to_square) */
 } TrainingExample;
 
 /* Training dataset */
 typedef struct {
     TrainingExample* examples;
     int count;
     int capacity;
 } TrainingData;
 
 /* Learning parameters */
 typedef struct {
     float learning_rate;          /* Learning rate for gradient descent */
     float momentum;               /* Momentum factor for gradient descent */
     float l2_regularization;      /* L2 regularization coefficient */
     int batch_size;               /* Batch size for training */
     int epochs;                   /* Number of training epochs */
     float exploration_rate;       /* Exploration rate for MCTS */
     int mcts_simulations;         /* Number of MCTS simulations per move */
     float dirichlet_alpha;        /* Dirichlet noise parameter for root node */
     float dirichlet_epsilon;      /* Dirichlet noise weight for root node */
     float temperature;            /* Temperature for move selection */
     float c_puct;                 /* Exploration constant for PUCT */
 } LearningParams;
 
 /* MCTS node structure */
 typedef struct MCTSNode {
     Board position;
     float prior;                  /* Prior probability from policy network */
     float value_sum;              /* Sum of values from simulations */
     int visit_count;              /* Number of visits during search */
     Move move;                    /* Move that led to this position */
     struct MCTSNode* parent;      /* Parent node */
     struct MCTSNode** children;   /* Child nodes */
     int num_children;             /* Number of children */
     bool expanded;                /* Whether node has been expanded */
 } MCTSNode;
 
 /* Neural network functions */
 void nn_init(NeuralNetwork* network, int input_size, int hidden_size, int policy_size);
 void nn_free(NeuralNetwork* network);
 void nn_forward(const NeuralNetwork* network, const float* input, float* value_output, float* policy_output);
 void nn_backward(NeuralNetwork* network, const float* input, const float* value_target, const float* policy_target);
 void nn_save(const NeuralNetwork* network, const char* filename);
 bool nn_load(NeuralNetwork* network, const char* filename);
 
 /* Board feature extraction for neural network input */
 void board_to_features(const Board* board, float* features);
 
 /* Reinforcement learning functions */
 void self_play(NeuralNetwork* network, TrainingData* data, int num_games, LearningParams* params);
 void train_network(NeuralNetwork* network, const TrainingData* data, LearningParams* params);
 float evaluate_network(const NeuralNetwork* network, int num_games);
 void save_training_data(const TrainingData* data, const char* filename);
 bool load_training_data(TrainingData* data, const char* filename);
 
 /* Monte Carlo Tree Search functions */
 MCTSNode* mcts_create_node(const Board* board, float prior, Move move, MCTSNode* parent);
 void mcts_free_node(MCTSNode* node);
 MCTSNode* mcts_select_leaf(MCTSNode* root, const NeuralNetwork* network, LearningParams* params);
 void mcts_expand_node(MCTSNode* node, const NeuralNetwork* network);
 float mcts_simulate(MCTSNode* node, const NeuralNetwork* network);
 void mcts_backpropagate(MCTSNode* node, float value);
 Move mcts_select_move(MCTSNode* root, float temperature);
 MCTSNode* mcts_search(const Board* board, const NeuralNetwork* network, LearningParams* params, int num_simulations);
 
 /* Training data management */
 void training_data_init(TrainingData* data, int initial_capacity);
 void training_data_free(TrainingData* data);
 void add_training_example(TrainingData* data, const Board* board, float value, const float* policy);
 void shuffle_training_data(TrainingData* data);
 
 /* External neural network */
 extern NeuralNetwork NN;
 
 #endif /* LEARNING_H */