#!/bin/bash

# === CONFIGURATION ===
GSON_LIB="lib/gson-2.10.1.jar"
SRC_DIR="src"
APP_DIR="app"
OUT_DIR="out"
DOCS_DIR="docs"
DOXYGEN_DIR="doxygen_docs"

# Ensure output directories exist
mkdir -p "$OUT_DIR"

# === COMPILE ===
echo "Compiling Java files..."
javac -cp "$GSON_LIB" -d "$OUT_DIR" "$APP_DIR"/Paint.java "$SRC_DIR"/*.java

if [ $? -ne 0 ]; then
    echo "Compilation failed."
    exit 1
fi

# === RUN ===
echo "Compilation successful. Running app.Paint..."
java -cp "$OUT_DIR:$GSON_LIB" app.Paint

# === JAVADOC PROMPT ===
read -p "Generate Javadoc? (y/n): " generate_javadoc
if [[ "$generate_javadoc" =~ ^[Yy]$ ]]; then
    mkdir -p "$DOCS_DIR"
    echo "Generating Javadoc..."
    javadoc -cp "$GSON_LIB" -d "$DOCS_DIR" "$APP_DIR"/Paint.java "$SRC_DIR"/*.java
    if [ $? -eq 0 ]; then
        echo "Javadoc generated in $DOCS_DIR/"
    else
        echo "Javadoc generation failed."
    fi
else
    echo "Skipping Javadoc generation."
fi

# === DOXYGEN PROMPT ===
read -p "Generate Doxygen docs? (y/n): " generate_doxygen
if [[ "$generate_doxygen" =~ ^[Yy]$ ]]; then
    if ! command -v doxygen &> /dev/null; then
        echo "Doxygen is not installed. Skipping."
    else
        mkdir -p "$DOXYGEN_DIR"
        if [ ! -f Doxyfile ]; then
            echo "Generating default Doxyfile..."
            doxygen -g
        fi
        echo "Updating Doxyfile output directory..."
        sed -i.bak "s|^OUTPUT_DIRECTORY.*|OUTPUT_DIRECTORY = $DOXYGEN_DIR|" Doxyfile
        echo "Running Doxygen..."
        doxygen Doxyfile
        echo "Doxygen docs generated in $DOXYGEN_DIR/"
    fi
else
    echo "Skipping Doxygen generation."
fi
