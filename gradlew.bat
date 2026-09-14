@rem Standard Gradle wrapper launcher script for Windows.
@rem See gradlew (the Unix version) for a note on the missing wrapper jar.

@if "%DEBUG%"=="" @echo off
setlocal

set DIRNAME=%~dp0
set APP_HOME=%DIRNAME%

if "%JAVA_HOME%"=="" (
    set JAVA_EXE=java.exe
) else (
    set JAVA_EXE=%JAVA_HOME%\bin\java.exe
)

set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar

"%JAVA_EXE%" -Dorg.gradle.appname=%~n0 -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

endlocal
