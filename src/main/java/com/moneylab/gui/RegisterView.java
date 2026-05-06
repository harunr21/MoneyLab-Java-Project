package com.moneylab.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import com.moneylab.service.UserManager;

public class RegisterView {
    private MainApp app;
    private UserManager userManager;

    public RegisterView(MainApp app, UserManager userManager) {
        this.app = app;
        this.userManager = userManager;
    }

    public Scene getScene() {
        // Dış katman: Gradient arka plan
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #1a73e8, #4285f4, #6ba3f7);");

        // Kart kutusu
        VBox card = new VBox(0);
        card.setMaxWidth(380);
        card.setMaxHeight(520);
        card.setStyle(
            "-fx-background-color: white; -fx-background-radius: 16;"
        );
        card.setEffect(new DropShadow(30, Color.rgb(0, 0, 0, 0.3)));

        // --- Kart Üst: Başlık Alanı ---
        VBox headerSection = new VBox(8);
        headerSection.setAlignment(Pos.CENTER);
        headerSection.setPadding(new Insets(35, 30, 25, 30));
        headerSection.setStyle(
            "-fx-background-color: linear-gradient(to right, #34a853, #43b565); " +
            "-fx-background-radius: 16 16 0 0;"
        );

        Label iconLabel = new Label("+");
        iconLabel.setStyle("-fx-font-size: 40px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: rgba(255,255,255,0.2); -fx-background-radius: 50; -fx-min-width: 60; -fx-min-height: 60; -fx-alignment: center;");


        Label titleLabel = new Label("Yeni Hesap Oluştur");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitleLabel = new Label("MoneyLab ailesine katılın");
        subtitleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.85);");

        headerSection.getChildren().addAll(iconLabel, titleLabel, subtitleLabel);

        // --- Kart Alt: Form Alanı ---
        VBox formSection = new VBox(14);
        formSection.setAlignment(Pos.CENTER);
        formSection.setPadding(new Insets(30, 35, 35, 35));

        // Kullanıcı Adı
        VBox usernameGroup = new VBox(5);
        Label usernameLabel = new Label("Kullanıcı Adı");
        usernameLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #555;");
        TextField usernameInput = new TextField();
        usernameInput.setPromptText("Kullanıcı adı seçin");
        usernameInput.setMaxWidth(Double.MAX_VALUE);
        usernameInput.setStyle(
            "-fx-font-size: 13px; -fx-padding: 10; -fx-background-radius: 8; " +
            "-fx-border-radius: 8; -fx-border-color: #ddd;"
        );
        usernameGroup.getChildren().addAll(usernameLabel, usernameInput);

        // Şifre
        VBox passwordGroup = new VBox(5);
        Label passwordLabel = new Label("Şifre");
        passwordLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #555;");
        PasswordField passwordInput = new PasswordField();
        passwordInput.setPromptText("En az 6 karakter");
        passwordInput.setMaxWidth(Double.MAX_VALUE);
        passwordInput.setStyle(
            "-fx-font-size: 13px; -fx-padding: 10; -fx-background-radius: 8; " +
            "-fx-border-radius: 8; -fx-border-color: #ddd;"
        );
        passwordGroup.getChildren().addAll(passwordLabel, passwordInput);

        // Mesaj alanı
        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: #ea4335; -fx-font-weight: bold; -fx-font-size: 12px;");
        messageLabel.setWrapText(true);

        // Kayıt Ol Butonu
        Button registerButton = new Button("Kayıt Ol");
        registerButton.setMaxWidth(Double.MAX_VALUE);
        registerButton.setPrefHeight(40);
        registerButton.setStyle(
            "-fx-background-color: linear-gradient(to right, #34a853, #43b565); " +
            "-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; " +
            "-fx-padding: 10px; -fx-cursor: hand; -fx-background-radius: 8;"
        );

        // Ayırıcı
        HBox separatorBox = new HBox(10);
        separatorBox.setAlignment(Pos.CENTER);
        Separator leftSep = new Separator();
        HBox.setHgrow(leftSep, Priority.ALWAYS);
        Label orLabel = new Label("zaten hesabın var mı?");
        orLabel.setStyle("-fx-text-fill: #999; -fx-font-size: 11px;");
        Separator rightSep = new Separator();
        HBox.setHgrow(rightSep, Priority.ALWAYS);
        separatorBox.getChildren().addAll(leftSep, orLabel, rightSep);

        // Geri Dön Butonu
        Button backButton = new Button("Giriş Ekranına Dön");
        backButton.setMaxWidth(Double.MAX_VALUE);
        backButton.setPrefHeight(38);
        backButton.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #1a73e8; " +
            "-fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 8px; " +
            "-fx-cursor: hand; -fx-border-color: #1a73e8; -fx-border-radius: 8; -fx-background-radius: 8;"
        );

        // Kayıt olma işlemi (Lambda expression ile)
        registerButton.setOnAction(e -> {
            try {
                String username = usernameInput.getText();
                String password = passwordInput.getText();

                // UserManager sınıfını çağırarak kayıt işlemini yapıyoruz
                boolean isRegistered = userManager.register(username, password);

                if (isRegistered) {
                    messageLabel.setStyle("-fx-text-fill: #34a853; -fx-font-weight: bold; -fx-font-size: 12px;");
                    messageLabel.setText("Kayıt başarılı! Giriş yapabilirsiniz.");
                    
                    // İşlem başarılı olunca kutuların içini temizliyoruz
                    usernameInput.clear();
                    passwordInput.clear();
                } else {
                    messageLabel.setStyle("-fx-text-fill: #ea4335; -fx-font-weight: bold; -fx-font-size: 12px;");
                    messageLabel.setText("Bu kullanıcı adı zaten alınmış!");
                }
            } catch (IllegalArgumentException ex) {
                // User sınıfı oluşturulurken fırlatılan özel hataları (şifre 6 karakterden az vb.) yakalıyoruz
                messageLabel.setStyle("-fx-text-fill: #ea4335; -fx-font-weight: bold; -fx-font-size: 12px;");
                messageLabel.setText(ex.getMessage());
            }
        });

        // Geri Dön butonuna basınca LoginView ekranına geçiş yap
        backButton.setOnAction(e -> app.showLogin());

        formSection.getChildren().addAll(usernameGroup, passwordGroup, registerButton, messageLabel, separatorBox, backButton);

        card.getChildren().addAll(headerSection, formSection);
        root.getChildren().add(card);

        return new Scene(root, 500, 580);
    }
}
