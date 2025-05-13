@echo off
setlocal

REM === CONFIGURATION ===
set GSON_LIB=lib\gson-2.10.1.jar
set SRC_DIR=src
set APP_DIR=app
set OUT_DIR=out
set DOCS_DIR=docs
set DOXYGEN_DIR=doxygen_docs

REM Create output directory if needed
if not exist %OUT_DIR% mkdir %OUT_DIR%

REM === COMPILE ===
echo Compiling Java files...
javac -cp "%GSON_LIB%" -d "%OUT_DIR%" "%APP_DIR%\Paint.java" %SRC_DIR%\*.java

if errorlevel 1 (
    echo Compilation failed.
    exit /b 1
)

REM === RUN ===
echo Compilation successful. Running app.Paint...
java -cp "%OUT_DIR%;%GSON_LIB%" app.Paint

REM === JAVADOC PROMPT ===
set /p generate_javadoc=Generate Javadoc? (y/n): 
if /i "%generate_javadoc%"=="y" (
    if not exist %DOCS_DIR% mkdir %DOCS_DIR%
    echo Generating Javadoc...
    javadoc -cp "%GSON_LIB%" -d "%DOCS_DIR%" "%APP_DIR%\Paint.java" %SRC_DIR%\*.java
    if errorlevel 1 (
        echo Javadoc generation failed.
    ) else (
        echo Javadoc generated in %DOCS_DIR%\
    )
) else (
    echo Skipping Javadoc generation.
)

REM === DOXYGEN PROMPT ===
set /p generate_doxygen=Generate Doxygen docs? (y/n): 
if /i "%generate_doxygen%"=="y" (
    where doxygen >nul 2>nul
    if errorlevel 1 (
        echo Doxygen not found in PATH. Skipping.
    ) else (
        if not exist %DOXYGEN_DIR% mkdir %DOXYGEN_DIR%
        if not exist Doxyfile (
            echo Generating default Doxyfile...
            doxygen -g
        )
        echo Updating output directory in Doxyfile...
        powershell -Command "(Get-Content Doxyfile) -replace '^(OUTPUT_DIRECTORY\s*=).*', 'OUTPUT_DIRECTORY = %DOXYGEN_DIR%' | Set-Content Doxyfile"
        echo Running Doxygen...
        doxygen Doxyfile
        echo Doxygen docs generated in %DOXYGEN_DIR%\
    )
) else (
    echo Skipping Doxygen generation.
)

endlocal
