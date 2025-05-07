# Makefile for Chess Engine with Reinforcement Learning

# Compiler settings
CC = gcc
CXX = g++
CFLAGS = -Wall -Wextra -O3 -g -fPIC
CXXFLAGS = $(CFLAGS) -std=c++11
LDFLAGS = -lm

# Directories
SRC_DIR = .
OBJ_DIR = obj
BIN_DIR = bin
JNI_DIR = ../src/main/java/com/programming/chess/engine

# JNI settings - if JAVA_HOME not set, try to detect it
JAVA_HOME ?= $(shell java -XshowSettings:properties -version 2>&1 | grep java.home | awk '{print $$3}')
# Add JNI includes - explicitly listing the platform-specific directories
JNI_INCLUDES = -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux

# Source files
SRCS = $(wildcard $(SRC_DIR)/*.c)
OBJS = $(patsubst $(SRC_DIR)/%.c,$(OBJ_DIR)/%.o,$(SRCS))

# Target binaries
ENGINE_BIN = $(BIN_DIR)/chess_engine
JNI_LIB = $(BIN_DIR)/libchessengine.so

# Default target
all: directories $(ENGINE_BIN) $(JNI_LIB)

# Create directories
directories:
	@mkdir -p $(OBJ_DIR)
	@mkdir -p $(BIN_DIR)
	@echo "Using JAVA_HOME: $(JAVA_HOME)"
	@if [ ! -f "$(JAVA_HOME)/include/jni.h" ]; then \
		echo "WARNING: jni.h not found in $(JAVA_HOME)/include"; \
	else \
		echo "JNI headers found"; \
	fi

# Standalone engine binary
$(ENGINE_BIN): $(OBJS)
	$(CC) $(CFLAGS) $^ -o $@ $(LDFLAGS)

# JNI shared library
$(JNI_LIB): $(OBJS)
	$(CC) -shared -o $@ $^ $(LDFLAGS)

# Compile C source files - explicitly including JNI headers
$(OBJ_DIR)/%.o: $(SRC_DIR)/%.c
	$(CC) $(CFLAGS) $(JNI_INCLUDES) -c $< -o $@

# JNI Header generation - compiles the Java class and generates C headers
jni_headers:
	@mkdir -p $(SRC_DIR)/jni
	@echo "Generating JNI headers from $(JNI_DIR)/NativeEngine.java"
	javac -h $(SRC_DIR)/jni $(JNI_DIR)/NativeEngine.java

# Clean build artifacts
clean:
	rm -rf $(OBJ_DIR) $(BIN_DIR)

# Test engine
test: $(ENGINE_BIN)
	./$(ENGINE_BIN) bench

# Run engine in UCI mode
run: $(ENGINE_BIN)
	./$(ENGINE_BIN)

# Training mode
train: $(ENGINE_BIN)
	./$(ENGINE_BIN) train 100

# Install the engine
install: $(ENGINE_BIN) $(JNI_LIB)
	@mkdir -p ../src/main/resources/native/linux
	@mkdir -p ../src/main/resources/native/mac
	@mkdir -p ../src/main/resources/native/windows
	cp $(JNI_LIB) ../src/main/resources/native/linux/
	@echo "Engine installed"

.PHONY: all directories clean test run train install jni_headers