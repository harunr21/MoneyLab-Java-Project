package src.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import src.service.UserManager;

public class RegisterView {
    private MainApp app;
    private UserManager userManager;

    public RegisterView(MainApp app, UserManager userManager) {
        this.app = app;
        this.userManager = userManager;
    }

    public Scene getScene() {
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #f4f4f9;");

        Label titleLabel = new Label("Yeni Hesap Oluştur");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");

        TextField usernameInput = new TextField();
        usernameInput.setPromptText("Kullanıcı Adı");
        usernameInput.setMaxWidth(250);
        usernameInput.setStyle("-fx-font-size: 14px; -fx-padding: 8px;");

        PasswordField passwordInput = new PasswordField();
        passwordInput.setPromptText("Şifre (En az 6 karakter)");
        passwordInput.setMaxWidth(250);
        passwordInput.setStyle("-fx-font-size: 14px; -fx-padding: 8px;");

        TextField goalInput = new TextField();
        goalInput.setPromptText("Birikim Hedefiniz (Örn: 5000)");
        goalInput.setMaxWidth(250);
        goalInput.setStyle("-fx-font-size: 14px; -fx-padding: 8px;");

        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

        Button registerButton = new Button("Kayıt Ol");
        registerButton.setPrefWidth(250);
        registerButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10px; -fx-cursor: hand;");

        Button backButton = new Button("Geri Dön");
        backButton.setPrefWidth(250);
        backButton.setStyle("-fx-background-color: #9e9e9e; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 8px; -fx-cursor: hand;");

        // Kayıt olma işlemi (Lambda expression ile)
        registerButton.setOnAction(e -> {
            try {
                String username = usernameInput.getText();
                String password = passwordInput.getText();
                
                // Kullanıcı hedef kısmına boş bırakırsa veya metin girerse NumberFormatException fırlatır
                double goal = Double.parseDouble(goalInput.getText());

                // UserManager sınıfını çağırarak kayıt işlemini yapıyoruz
                boolean isRegistered = userManager.register(username, password, goal);

                if (isRegistered) {
                    messageLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    messageLabel.setText("Kayıt başarılı! Giriş yapabilirsiniz.");
                    
                    // İşlem başarılı olunca kutuların içini temizliyoruz
                    usernameInput.clear();
                    passwordInput.clear();
                    goalInput.clear();
                } else {
                    messageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    messageLabel.setText("Bu kullanıcı adı zaten alınmış!");
                }
            } catch (NumberFormatException ex) {
                messageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                messageLabel.setText("Lütfen hedefinizi sayısal bir değer girin!");
            } catch (IllegalArgumentException ex) {
                // User sınıfı oluşturulurken fırlatılan özel hataları (şifre 6 karakterden az vb.) yakalıyoruz
                messageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                messageLabel.setText(ex.getMessage());
            }
        });

        // Geri Dön butonuna basınca LoginView ekranına geçiş yap
        backButton.setOnAction(e -> app.showLogin());

        layout.getChildren().addAll(titleLabel, usernameInput, passwordInput, goalInput, registerButton, backButton, messageLabel);

        return new Scene(layout, 450, 480);
    }
}
