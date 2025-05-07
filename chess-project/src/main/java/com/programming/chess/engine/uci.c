/**
 * uci.c
 * Implementation of the Universal Chess Interface protocol
 */

 #include "include/uci.h"
 #include <stdio.h>
 #include <stdlib.h>
 #include <string.h>
 
 /**
  * Initialize UCI options
  */
 void init_uci_options() {
     printf("UCI options initialized\n");
 }
 
 /**
  * Main UCI protocol loop
  */
 void uci_loop() {
     printf("info string Minimal Chess Engine\n");
     printf("uciok\n");
     
     // Simplified stub implementation
     // In a real implementation, this would read commands from stdin and respond
     printf("Stub UCI implementation - would normally wait for commands\n");
 }