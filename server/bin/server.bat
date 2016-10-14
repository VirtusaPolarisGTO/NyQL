@echo off

set CURRENT_DIR=%cd%

if exist "%JAVA_HOME:"=%\bin\java.exe" goto setJavaHome
set JAVA=java
goto okJava

:setJavaHome
set JAVA="%JAVA_HOME:"=%\bin\java"

