@echo off

call .\env.bat
IF %ERRORLEVEL%==0 goto okJava
goto end

:okJava
SET CURRENT_DIR=%CD%
cd %CURRENT_DIR%\..
SET CLSPATH=".;lib\*"
SET NY_ARGS=
CALL "%JAVA%" %NY_ARGS% -classpath %CLSPATH% com.virtusa.gto.nyql.server.NyServer %*
goto end

:end
exit /B %ERRORLEVEL%
