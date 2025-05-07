# Simplified Makefile for Chess Engine

# Compiler settings
CC = gcc
CFLAGS = -Wall -Wextra -O3 -g -fPIC
LDFLAGS = -lm

# Directories
SRC_DIR = .
OBJ_DIR = obj
BIN_DIR = bin
RESOURCES_DIR = ../../../resources/native/linux

# JNI settings
JAVA_HOME ?= $(shell java -XshowSettings:properties -version 2>&1 | grep java.home | awk '{print $$3}')
JNI_INCLUDES = -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux

# Source files
SRCS = engine.C bitboard.c position.c search.c learning.c uci.c
OBJS = $(patsubst %.C,$(OBJ_DIR)/%.o,$(patsubst %.c,$(OBJ_DIR)/%.o,$(SRCS)))

# Target binaries
ENGINE_BIN = $(BIN_DIR)/chess_engine
JNI_LIB = $(BIN_DIR)/libchessengine.so

# Default target
all: directories $(ENGINE_BIN) $(JNI_LIB)

# Create directories
directories:
	@mkdir -p $(OBJ_DIR)
	@mkdir -p $(BIN_DIR)
	@mkdir -p $(RESOURCES_DIR)
	@echo "Using JAVA_HOME: $(JAVA_HOME)"

# Standalone engine binary
$(ENGINE_BIN): $(OBJS)
	$(CC) $(CFLAGS) $^ -o $@ $(LDFLAGS)

# JNI shared library
$(JNI_LIB): $(OBJS)
	$(CC) -shared -o $@ $^ $(LDFLAGS)

# Compile C source files
$(OBJ_DIR)/%.o: %.c
	$(CC) $(CFLAGS) $(JNI_INCLUDES) -c $< -o $@

$(OBJ_DIR)/%.o: %.C
	$(CC) $(CFLAGS) $(JNI_INCLUDES) -c $< -o $@

# Install the engine
install: $(JNI_LIB)
	cp $(JNI_LIB) $(RESOURCES_DIR)/
	@echo "Engine installed to $(RESOURCES_DIR)"

# Clean build artifacts
clean:
	rm -rf $(OBJ_DIR) $(BIN_DIR)

.PHONY: all directories clean install