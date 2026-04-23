package src.service;

import src.model.User;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserManager {

    private static final String FILE_PATH = "users.txt";

    // -------------------------------------------------------
    // Kayıt Ol — yeni kullanıcıyı .txt'e ekler
    // -------------------------------------------------------
    public boolean register(String name, String password, double goal) {

        if (name == null || name.isBlank()) {
            System.out.println("Hata: Kullanıcı adı boş olamaz.");
            return false;
        }
        if (password == null || password.length() < 6) {
            System.out.println("Hata: Şifre en az 6 karakter olmalıdır.");
            return false;
        }
        if (isNameTaken(name)) {
            System.out.println("Hata: Bu kullanıcı adı zaten alınmış.");
            return false;
        }

        int newId = generateNextId();
        User newUser = new User(newId, name, password, goal);

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(FILE_PATH, true))) {   // append = true
            writer.write(newUser.toFileString());
            writer.newLine();
            System.out.println("Kayıt başarılı: " + newUser);
            return true;
        } catch (IOException e) {
            System.out.println("Dosya yazma hatası: " + e.getMessage());
            return false;
        }
    }

    // -------------------------------------------------------
    // Giriş Yap — name + password eşleşmesini kontrol eder
    // -------------------------------------------------------
    public User login(String name, String password) {

        List<User> users = loadAllUsers();

        for (User user : users) {
            if (user.getName().equals(name) &&
                user.getPassword().equals(password)) {
                System.out.println("Giriş başarılı. Hoş geldiniz, " + user.getName() + "!");
                return user;          // oturum açan kullanıcı döner
            }
        }

        System.out.println("Hata: Kullanıcı adı veya şifre yanlış.");
        return null;                  // başarısız giriş
    }

    // -------------------------------------------------------
    // Tüm kullanıcıları .txt'den yükler
    // -------------------------------------------------------
    public List<User> loadAllUsers() {

        List<User> users = new ArrayList<>();
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return users;             // dosya yoksa boş liste döner
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    try {
                        users.add(User.fromFileString(line));
                    } catch (Exception e) {
                        System.out.println("Hata: Satır okunamadı ve atlandı -> " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Dosya okuma hatası: " + e.getMessage());
        }

        return users;
    }

    // -------------------------------------------------------
    // Yardımcı: Kullanıcı adı daha önce alınmış mı?
    // -------------------------------------------------------
    private boolean isNameTaken(String name) {
        for (User user : loadAllUsers()) {
            if (user.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    // -------------------------------------------------------
    // Yardımcı: Sonraki benzersiz ID'yi üretir
    // -------------------------------------------------------
    private int generateNextId() {
        List<User> users = loadAllUsers();
        if (users.isEmpty()) return 1;
        int maxId = 0;
        for (User user : users) {
            if (user.getId() > maxId) maxId = user.getId();
        }
        return maxId + 1;
    }
}
