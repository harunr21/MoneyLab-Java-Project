package com.moneylab.service;

import com.moneylab.model.User;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserManager {

    private static final String FILE_PATH = "users.txt";


    public boolean register(String name, String password, double goal) {
        try {
            if (isNameTaken(name)) {
                System.out.println("Hata: Bu kullanıcı adı zaten alınmış.");
                return false;
            }

            int newId = generateNextId();
            // User constructor geçersiz bir değer alırsa IllegalArgumentException fırlatır
            User newUser = new User(newId, name, password, goal);

            saveUserToFile(newUser); // Doğrudan dosyaya ekle
            
            System.out.println("Kayıt başarılı: " + newUser);
            return true;
        } catch (IllegalArgumentException e) {
            System.out.println("Kayıt Başarısız -> " + e.getMessage());
            return false;
        }
    }

    public User login(String name, String password) {
        // Her giriş denemesinde tüm kullanıcıları dosyadan okuruz
        List<User> users = loadAllUsersFromFile();
        
        for (User user : users) {
            if (user.getName().equals(name) && user.getPassword().equals(password)) {
                System.out.println("Giriş başarılı. Hoş geldiniz, " + user.getName() + "!");
                return user;
            }
        }
        System.out.println("Hata: Kullanıcı adı veya şifre yanlış.");
        return null;
    }

    private List<User> loadAllUsersFromFile() {
        List<User> users = new ArrayList<>();
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return users; // Dosya yoksa boş liste döner
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    try {
                        users.add(parseUser(line));
                    } catch (Exception e) {
                        System.out.println("Hata: Satır okunamadı ve atlandı -> " + line + " (" + e.getMessage() + ")");
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Dosya okuma hatası: " + e.getMessage());
        }

        return users;
    }

    private void saveUserToFile(User user) {
        // 'true' parametresi dosyanın üstüne yazmak yerine sonuna ekleme (append) yapar
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(formatUser(user));
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Dosya yazma hatası: " + e.getMessage());
        }
    }

    private boolean isNameTaken(String name) {
        // İsim kullanılmış mı diye bakmak için dosyayı okuruz
        List<User> users = loadAllUsersFromFile();
        for (User user : users) {
            if (user.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private int generateNextId() {
        // En yüksek ID'yi bulmak için dosyayı okuruz
        List<User> users = loadAllUsersFromFile();
        if (users.isEmpty()) return 1;
        
        int maxId = 0;
        for (User user : users) {
            if (user.getId() > maxId) {
                maxId = user.getId();
            }
        }
        return maxId + 1;
    }

    // Dosyaya yazılacak String formatını belirler
    private String formatUser(User user) {
        return user.getId() + "," + user.getName() + "," + user.getPassword() + "," + user.getGoal();
    }

    // Dosyadan okunan satırı User nesnesine çevirir
    private User parseUser(String line) {
        String[] parts = line.split(",");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Geçersiz veri formatı");
        }
        int id = Integer.parseInt(parts[0].trim());
        String name = parts[1].trim();
        String password = parts[2].trim();
        double goal = Double.parseDouble(parts[3].trim());
        return new User(id, name, password, goal);
    }
}
