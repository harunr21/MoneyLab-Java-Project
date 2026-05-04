package com.moneylab.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
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
        // VBox: Elemanları alt alta dizen bir yerleşim düzenidir (Vertical Box)
        VBox layout = new VBox(15); // Elemanlar arasına 15px boşluk koy
        layout.setPadding(new Insets(30)); // Kenarlardan 30px boşluk bırak
        layout.setAlignment(Pos.CENTER); // Elemanları tam ortaya hizala
        layout.setStyle("-fx-background-color: #f4f4f9;"); // Temiz, açık gri bir arka plan rengi

        // Ana Başlık
        Label titleLabel = new Label("MoneyLab Giriş");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");

        // Kullanıcı Adı Giriş Alanı
        TextField usernameInput = new TextField();
        usernameInput.setPromptText("Kullanıcı Adı"); // Kutu boşken görünen silik yazı
        usernameInput.setMaxWidth(250); // Kutunun genişliği
        usernameInput.setStyle("-fx-font-size: 14px; -fx-padding: 8px;");

        // Şifre Giriş Alanı (Yazılanlar gizli çıkar)
        PasswordField passwordInput = new PasswordField();
        passwordInput.setPromptText("Şifre");
        passwordInput.setMaxWidth(250);
        passwordInput.setStyle("-fx-font-size: 14px; -fx-padding: 8px;");

        // Kullanıcı yanlış bilgi girerse burada hata mesajı göstereceğiz
        Label messageLabel = new Label();
        messageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

        // Giriş Yap Butonu
        Button loginButton = new Button("Giriş Yap");
        loginButton.setPrefWidth(250); // Buton, girdi kutuları ile aynı genişlikte olsun
        loginButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10px; -fx-cursor: hand;");

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

        // Kayıt Ol Butonu
        Button registerButton = new Button("Kayıt Ol");
        registerButton.setPrefWidth(250);
        registerButton.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 8px; -fx-cursor: hand;");

        // Kayıt ol butonuna tıklayınca app.showRegister() metodunu çağırıyoruz
        registerButton.setOnAction(e -> {
            app.showRegister();
        });

        // Oluşturduğumuz tüm bu elemanları VBox'ın içine ekliyoruz (sırasıyla alt alta görünürler)
        layout.getChildren().addAll(titleLabel, usernameInput, passwordInput, loginButton, registerButton, messageLabel);

        // Sahneyi oluştur (Genişlik: 450, Yükseklik: 480) ve geri döndür
        return new Scene(layout, 450, 480);
    }
}
