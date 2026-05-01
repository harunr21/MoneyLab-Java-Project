package src;

import src.model.User;
import src.service.UserManager;

public class Main {

    public static void main(String[] args) {

        UserManager manager = new UserManager();

        // --- Kayıt ol ---
        manager.register("harun",  "sifre123", 5000.0);// deneme
        manager.register("mehmet", "pass456",  10000.0);
        manager.register("harun",  "baska123", 0.0);    // Hata: ad alınmış
        
        // --- Virgül (Hata Yönetimi) Testi ---
        manager.register("ahmet,can", "123456", 100);   // Hata fırlatacak ve yakalanıp ekrana basılacak

        // --- Giriş yap ---
        User loggedIn = manager.login("harun", "sifre123");  // Başarılı

        if (loggedIn != null) {
            System.out.println("Aktif kullanıcı: " + loggedIn);
            System.out.println("Hedef: " + loggedIn.getGoal() + " TL");
        }

        manager.login("harun", "yanlisSifre");  // Hata: yanlış şifre
    }
}
