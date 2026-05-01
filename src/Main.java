package src;

import src.gui.MainApp;

public class Main {

    public static void main(String[] args) {
        
        // OOP projenize arayüz eklediğimiz için artık konsol testi yerine
        // doğrudan JavaFX arayüzümüzü başlatıyoruz.
        System.out.println("Arayüz başlatılıyor...");
        
        // MainApp sınıfındaki JavaFX uygulamasını çalıştırır
        MainApp.main(args);
    }
}
