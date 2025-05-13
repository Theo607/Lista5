#!/bin/bash

# Set the path to the Gson library (adjust if necessary)
GSON_LIB_PATH="./lib/gson-2.10.1.jar"

# Set the classpath to include the Gson library and the current directory
CLASSPATH="$GSON_LIB_PATH:."

# Compile the Java file (if not already compiled)
javac -cp $CLASSPATH Paint.java

# Run the Paint class
java -cp $CLASSPATH Paint
