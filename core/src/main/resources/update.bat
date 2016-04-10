@echo off

rem You can set JAVA_HOME to point to your JVM

set JAVA_CMD=java
if NOT "%JAVA_HOME%"=="" set JAVA_CMD=%JAVA_HOME%\bin\java
echo "%JAVA_CMD%" -cp lib/myant.jar ant.StartupScript lib .
"%JAVA_CMD%" -cp lib/myant.jar ant.StartupScript lib .
@pause
