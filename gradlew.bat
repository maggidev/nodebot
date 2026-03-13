@REM
@REM ##############################################################################
@REM #
@REM #  Gradle wrapper
@REM #
@REM ##############################################################################

@IF "%DEBUG%" == "" @SET DEBUG=false

@IF "%OS%" == "Windows_NT" @(SET "COLOR_SUPPORTED=true") ELSE @(SET "COLOR_SUPPORTED=false")

@IF "%COLOR_SUPPORTED%" == "true" (
    @FOR /F "tokens=1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,100,101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,116,117,118,119,120,121,122,123,124,125,126,127,128" %%A IN ('cmd /c "for /f "tokens=1*" %%a in ('reg query HKCU\Console /v VirtualTerminalLevel 2^>nul') do @echo %%b"') DO @SET "_VT_LEVEL=%%A"
    @IF "%_VT_LEVEL%" == "0x1" @SET "COLOR_SUPPORTED=true"
    @IF "%_VT_LEVEL%" == "0x2" @SET "COLOR_SUPPORTED=true"
    @IF "%_VT_LEVEL%" == "" @SET "COLOR_SUPPORTED=false"
)

@IF "%COLOR_SUPPORTED%" == "true" (
    @SET "TERM_RESET=\x1b[0m"
    @SET "TERM_BOLD=\x1b[1m"
    @SET "TERM_RED=\x1b[31m"
    @SET "TERM_GREEN=\x1b[32m"
    @SET "TERM_YELLOW=\x1b[33m"
    @SET "TERM_BLUE=\x1b[34m"
    @SET "TERM_MAGENTA=\x1b[35m"
    @SET "TERM_CYAN=\x1b[36m"
    @SET "TERM_WHITE=\x1b[37m"
)

@SET "APP_NAME=Gradle"
@SET "APP_BASE_NAME=%~n0"

@REM Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
@SET "DEFAULT_JVM_OPTS=-Xmx64m -Xms64m"

@REM Determine the Java command to use to start the JVM.
@IF DEFINED JAVA_HOME (
    @SET "JAVACMD=%JAVA_HOME%\bin\java.exe"
) ELSE (
    @SET "JAVACMD=java.exe"
)

@IF NOT EXIST "%JAVACMD%" (
    @ECHO %TERM_RED%ERROR: JAVA_HOME is not set and no 'java.exe' command could be found in your PATH.%TERM_RESET%
    @ECHO %TERM_RED%Please set the JAVA_HOME environment variable in your environment to match the%TERM_RESET%
    @ECHO %TERM_RED%location of your Java installation.%TERM_RESET%
    @EXIT /B 1
)

@REM Determine the script directory.
@SET "SCRIPT_DIR=%~dp0"

@REM Determine the Gradle distribution URL.
@SET "GRADLE_DISTRIBUTION_URL=https://services.gradle.org/distributions/gradle-8.1.1-bin.zip"

@REM Execute Gradle.
@"%JAVACMD%" %DEFAULT_JVM_OPTS% -classpath "%SCRIPT_DIR%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
