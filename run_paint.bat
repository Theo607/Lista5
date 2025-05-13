@echo off
setlocal

REM === CONFIGURATION ===
set GSON_LIB=lib\gson-2.10.1.jar
set SRC_DIR=src
set APP_DIR=app
set OUT_DIR=out
set DOCS_DIR=docs

REM Create output directory if it doesn't exist
if not exist %OUT_DIR% mkdir %OUT_DIR%

REM === COMPILE ===
echo Compiling Java files...
javac -cp "%GSON_LIB%" -d "%OUT_DIR%" "%APP_DIR%\Paint.java" %SRC_DIR%\*.java

if errorlevel 1 (
    echo ❌ Compilation failed.
    exit /b 1
)

REM === RUN ===
echo ✅ Compilation successful. Running app.Paint...
java -cp "%OUT_DIR%;%GSON_LIB%" app.Paint

REM === JAVADOC ===
set /p generate_docs=Generate Javadoc? (y/n): 
if /i "%generate_docs%"=="y" (
    if not exist %DOCS_DIR% mkdir %DOCS_DIR%
    echo 📄 Generating Javadoc...
    javadoc -cp "%GSON_LIB%" -d "%DOCS_DIR%" "%APP_DIR%\Paint.java" %SRC_DIR%\*.java

    if errorlevel 1 (
        echo ❌ Javadoc generation failed.
    ) else (
        echo ✅ Javadoc generated in .\%DOCS_DIR%\
    )
) else (
    echo 📄 Skipping Javadoc generation.
)

endlocal
