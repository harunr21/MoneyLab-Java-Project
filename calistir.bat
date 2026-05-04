@echo off
setlocal

echo ==============================================
echo  MoneyLab - Profesyonel Baslatici (Maven)
echo ==============================================
echo.

set MAVEN_VERSION=3.9.6
set MAVEN_DIR=.maven
set MAVEN_HOME=%CD%\%MAVEN_DIR%\apache-maven-%MAVEN_VERSION%
set MVN_CMD=%MAVEN_HOME%\bin\mvn.cmd

if not exist "%MAVEN_HOME%" (
    echo [BILGI] Maven bulunamadi. Ilk kurulum icin indiriliyor...
    if not exist "%MAVEN_DIR%" mkdir "%MAVEN_DIR%"
    powershell -Command "Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/%MAVEN_VERSION%/apache-maven-%MAVEN_VERSION%-bin.zip' -OutFile '%MAVEN_DIR%\maven.zip'"
    echo [BILGI] Maven cikariliyor...
    powershell -Command "Expand-Archive -Path '%MAVEN_DIR%\maven.zip' -DestinationPath '%MAVEN_DIR%' -Force"
    del "%MAVEN_DIR%\maven.zip"
    echo [BILGI] Maven kurulumu tamamlandi.
)

if not exist "target\MoneyLab-1.0-SNAPSHOT.jar" (
    echo.
    echo [BILGI] Proje derleniyor ve calistirilabilir dosya olusturuluyor...
    call "%MVN_CMD%" clean package
)

echo.
echo [BILGI] Uygulama baslatiliyor...
java -jar target\MoneyLab-1.0-SNAPSHOT.jar

pause
