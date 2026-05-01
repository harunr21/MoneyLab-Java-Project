@echo off
echo Java dosyalari derleniyor...
javac --module-path javafx-sdk\lib --add-modules javafx.controls,javafx.fxml src\Main.java src\gui\*.java src\model\*.java src\service\*.java

echo.
echo Arayuz baslatiliyor...
java --module-path javafx-sdk\lib --add-modules javafx.controls,javafx.fxml src.Main

pause
