package com.moneylab.gui;

import javafx.application.Application;
import javafx.stage.Stage;
import com.moneylab.model.User;
import com.moneylab.service.UserManager;

// Application sınıfı JavaFX uygulamalarının temelidir. Her JavaFX arayüzü bunu miras alır (extends).
public class MainApp extends Application {

    private Stage window; // Stage, uygulamamızın ana penceresidir.
    private UserManager userManager;

    @Override
    public void start(Stage primaryStage) {
        this.window = primaryStage;
        this.window.setTitle("MoneyLab - Kişisel Finans Takibi");
        
        // Kullanıcı işlemlerini (giriş, kayıt vb.) yapacağımız nesneyi oluşturuyoruz
        this.userManager = new UserManager();

        // Uygulama ilk açıldığında giriş ekranını göster
        showLogin();
        
        // Pencereyi ekranda görünür yap
        this.window.show();
    }

    public UserManager getUserManager() {
        return this.userManager;
    }

    // Giriş ekranına geçiş yapmak için metod
    public void showLogin() {
        // LoginView sınıfından bir nesne oluşturup, ana pencereye (Stage) sahne (Scene) olarak veriyoruz.
        LoginView loginView = new LoginView(this, userManager);
        window.setScene(loginView.getScene());
    }

    // Ana ekrana (Dashboard) geçiş yapmak için metod
    public void showDashboard(User user) {
        // Kullanıcı başarıyla giriş yaptığında, DashboardView ekranını oluşturuyoruz.
        DashboardView dashboardView = new DashboardView(this, user);
        window.setScene(dashboardView.getScene());
    }

    // Kayıt ekranına geçiş yapmak için metod
    public void showRegister() {
        RegisterView registerView = new RegisterView(this, userManager);
        window.setScene(registerView.getScene());
    }

    // Java uygulamasını başlatan ana metod
    public static void main(String[] args) {
        // launch() metodu Application sınıfından gelir ve JavaFX penceresini başlatır.
        launch(args);
    }
}
