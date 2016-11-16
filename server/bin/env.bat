@echo off

REM Java is already found and set... let's skip settings.
if EXIST "%JAVA%" goto envFine

SET JAVA=""

REM check whether system JAVA_HOME variable is set or not.
IF EXIST "%JAVA_HOME%" goto setJavaFromHome
goto jdkError

:setJavaFromHome
SET JAVA=%JAVA_HOME%\bin\java.exe
echo INFO  Java is found in %JAVA_HOME%

IF EXIST "%JAVA%" goto envFine
goto jdkError

REM check whether setup java is v1.8 or not. If not we exit with an error.
"%JAVA%" -version:1.8 -version > nul 2>&1
IF %ERRORLEVEL%==0 goto envFine

ECHO Server requires Java 8 Runtime environment to run!
GOTO endError

:jdkError
ECHO ERROR No JDK found. Please validate either JDK_HOME or JAVA_HOME points to valid Java 8 installation.
GOTO endError

:versionError
ECHO ERROR Java version compatibility issue. Please install Java 8.
GOTO endError

:endError
EXIT /B 1

:envFine
EXIT /B 0