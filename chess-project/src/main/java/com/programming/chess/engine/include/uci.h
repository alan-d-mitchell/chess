/**
 * uci.h
 * Implementation of the Universal Chess Interface protocol
 */

 #ifndef UCI_H
 #define UCI_H
 
 #include "engine.h"
 
 /* UCI option types */
 typedef enum {
     UCI_OPTION_CHECK,    /* Boolean option */
     UCI_OPTION_SPIN,     /* Integer option with min/max */
     UCI_OPTION_COMBO,    /* String option with predefined choices */
     UCI_OPTION_STRING,   /* String option */
     UCI_OPTION_BUTTON    /* Button (no value) */
 } UCIOptionType;
 
 /* UCI option structure */
 typedef struct {
     char name[64];           /* Option name */
     UCIOptionType type;      /* Option type */
     void* value;             /* Current value */
     void* default_value;     /* Default value */
     int min;                 /* Minimum value (for spin) */
     int max;                 /* Maximum value (for spin) */
     char** combo_options;    /* Available options (for combo) */
     int combo_option_count;  /* Number of available options */
     void (*on_change)(void*); /* Callback when option changes */
 } UCIOption;
 
 /* UCI interface functions */
 void uci_loop();
 void uci_position(char* command);
 void uci_go(char* command);
 void uci_set_option(char* command);
 void uci_new_game();
 void uci_print_info(const SearchResult* result);
 void uci_register_option(const char* name, UCIOptionType type, void* default_value, 
                          int min, int max, char** combo_options, int combo_option_count,
                          void (*on_change)(void*));
 
 /* UCI command parsing */
 void parse_position(char* command, Board* board);
 void parse_go(char* command, SearchParams* params);
 void parse_setoption(char* command);
 
 /* UCI options management */
 void init_uci_options();
 UCIOption* find_option(const char* name);
 void update_option(UCIOption* option, void* value);
 
 /* Debug mode */
 extern bool uci_debug_mode;
 
 #endif /* UCI_H */