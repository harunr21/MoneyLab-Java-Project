package com.moneylab.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import com.moneylab.model.User;
import com.moneylab.service.UserManager;

public class LoginView {
    private MainApp app; // Ekranlar arası geçiş yapabilmek için ana uygulamayı (MainApp) tutuyoruz
    private UserManager userManager; // Giriş işlemlerini yapmak için servis sınıfımız

    public LoginView(MainApp app, UserManager userManager) {
        this.app = app;
        this.userManager = userManager;
    }

    // Bu metod bize giriş ekranının sahnesini (Scene) döndürecek
    public Scene getScene() {
        // Dış katman: Gradient arka plan
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #1a73e8, #4285f4, #6ba3f7);");

        // Kart kutusu
        VBox card = new VBox(0);
        card.setMaxWidth(380);
        card.setMaxHeight(500);
        card.setStyle(
            "-fx-background-color: white; -fx-background-radius: 16;"
        );
        card.setEffect(new DropShadow(30, Color.rgb(0, 0, 0, 0.3)));

        // --- Kart Üst: Başlık Alanı ---
        VBox headerSection = new VBox(8);
        headerSection.setAlignment(Pos.CENTER);
        headerSection.setPadding(new Insets(35, 30, 25, 30));
        headerSection.setStyle(
            "-fx-background-color: linear-gradient(to right, #1a73e8, #4285f4); " +
            "-fx-background-radius: 16 16 0 0;"
        );

        Label iconLabel = new Label("M");
        iconLabel.setStyle("-fx-font-size: 40px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 50; -fx-min-width: 60; -fx-min-height: 60; -fx-alignment: center;");


        Label titleLabel = new Label("MoneyLab");
        titleLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitleLabel = new Label("Kişisel Finans Takip Sistemi");
        subtitleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.85);");

        headerSection.getChildren().addAll(iconLabel, titleLabel, subtitleLabel);

        // --- Kart Alt: Form Alanı ---
        VBox formSection = new VBox(14);
        formSection.setAlignment(Pos.CENTER);
        formSection.setPadding(new Insets(30, 35, 35, 35));

        Label formTitle = new Label("Giriş Yap");
        formTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");

        // Kullanıcı Adı Giriş Alanı
        VBox usernameGroup = new VBox(5);
        Label usernameLabel = new Label("Kullanıcı Adı");
        usernameLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #555;");
        TextField usernameInput = new TextField();
        usernameInput.setPromptText("Kullanıcı adınızı girin"); // Kutu boşken görünen silik yazı
        usernameInput.setMaxWidth(Double.MAX_VALUE);
        usernameInput.setStyle(
            "-fx-font-size: 13px; -fx-padding: 10; -fx-background-radius: 8; " +
            "-fx-border-radius: 8; -fx-border-color: #ddd;"
        );
        usernameGroup.getChildren().addAll(usernameLabel, usernameInput);

        // Şifre Giriş Alanı (Yazılanlar gizli çıkar)
        VBox passwordGroup = new VBox(5);
        Label passwordLabel = new Label("Şifre");
        passwordLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #555;");
        PasswordField passwordInput = new PasswordField();
        passwordInput.setPromptText("Şifrenizi girin");
        passwordInput.setMaxWidth(Double.MAX_VALUE);
        passwordInput.setStyle(
            "-fx-font-size: 13px; -fx-padding: 10; -fx-background-radius: 8; " +
            "-fx-border-radius: 8; -fx-border-color: #ddd;"
        );
        passwordGroup.getChildren().addAll(passwordLabel, passwordInput);

        // Kullanıcı yanlış bilgi girerse burada hata mesajı göstereceğiz
        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: #ea4335; -fx-font-weight: bold; -fx-font-size: 12px;");
        messageLabel.setWrapText(true);

        // Giriş Yap Butonu
        Button loginButton = new Button("Giriş Yap");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        loginButton.setPrefHeight(40);
        loginButton.setStyle(
            "-fx-background-color: linear-gradient(to right, #1a73e8, #4285f4); " +
            "-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; " +
            "-fx-padding: 10px; -fx-cursor: hand; -fx-background-radius: 8;"
        );

        // Butona tıklandığında ne olacağını belirliyoruz (Lambda gösterimi kullanarak)
        loginButton.setOnAction(e -> {
            String username = usernameInput.getText();
            String password = passwordInput.getText();
            
            // UserManager sınıfını kullanarak giriş yapmayı deniyoruz
            User user = userManager.login(username, password);
            if (user != null) {
                // Eğer user boş değilse giriş başarılıdır, ana ekrana (Dashboard) geç!
                app.showDashboard(user);
            } else {
                // Hatalıysa kırmızı yazıyla uyarı ver
                messageLabel.setText("Hatalı kullanıcı adı veya şifre!");
            }
        });

        // Ayırıcı
        HBox separatorBox = new HBox(10);
        separatorBox.setAlignment(Pos.CENTER);
        Separator leftSep = new Separator();
        HBox.setHgrow(leftSep, Priority.ALWAYS);
        Label orLabel = new Label("veya");
        orLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");
        Separator rightSep = new Separator();
        HBox.setHgrow(rightSep, Priority.ALWAYS);
        separatorBox.getChildren().addAll(leftSep, orLabel, rightSep);

        // Kayıt Ol Butonu
        Button registerButton = new Button("Yeni Hesap Oluştur");
        registerButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setPrefHeight(38);
        registerButton.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #1a73e8; " +
            "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8px; " +
            "-fx-cursor: hand; -fx-border-color: #1a73e8; -fx-border-radius: 8; -fx-background-radius: 8;"
        );

        // Kayıt ol butonuna tıklayınca app.showRegister() metodunu çağırıyoruz
        registerButton.setOnAction(e -> {
            app.showRegister();
        });

        // Oluşturduğumuz tüm bu elemanları form bölümüne ekliyoruz
        formSection.getChildren().addAll(formTitle, usernameGroup, passwordGroup, loginButton, messageLabel, separatorBox, registerButton);

        card.getChildren().addAll(headerSection, formSection);
        root.getChildren().add(card);

        // Sahneyi oluştur (Genişlik: 450, Yükseklik: 480) ve geri döndür
        return new Scene(root, 500, 580);
    }
}
