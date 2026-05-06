package com.moneylab.service;

import com.moneylab.model.User;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserManager {

    // Dosya yolunu çalışma dizinine göre belirliyoruz
    // Bu sayede uygulama hangi dizinden çalıştırılırsa çalıştırılsın dosyayı doğru yerde arar
    private static final String FILE_PATH = System.getProperty("user.dir") + File.separator + "users.txt";


    public boolean register(String name, String password) {
        // Dosyadan tüm kullanıcıları BİR KERE okuyoruz
        // Eskiden isNameTaken() ve generateNextId() ayrı ayrı okuyordu (2 kere), şimdi tek okuma ile hallettik
        List<User> users = loadAllUsersFromFile();

        // İsim kontrolü — dosyayı tekrar okumadan, elimizdeki listeden bakıyoruz
        for (User user : users) {
            if (user.getName().equalsIgnoreCase(name)) {
                System.out.println("Hata: Bu kullanıcı adı zaten alınmış.");
                return false;
            }
        }

        // En yüksek ID'yi bul — yine dosyayı tekrar okumadan
        int maxId = 0;
        for (User user : users) {
            if (user.getId() > maxId) {
                maxId = user.getId();
            }
        }
        int newId = maxId + 1;

        // User constructor geçersiz bir değer alırsa IllegalArgumentException fırlatır
        // Bu hatayı BURADA yakalamıyoruz, doğrudan GUI katmanına (RegisterView) iletiyoruz
        // Böylece kullanıcı ekranda hatanın tam sebebini (şifre kısa, isim boş vb.) görebilir
        User newUser = new User(newId, name, password, 0.0);

        saveUserToFile(newUser);
        
        System.out.println("Kayıt başarılı: " + newUser);
        return true;
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

        // try-finally yapısı: dosyayı okuduktan sonra finally bloğunda kapatıyoruz
        // Bu sayede hata olsa bile dosya her zaman kapatılır
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
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
        } finally {
            // Dosyayı her durumda kapatıyoruz (hata olsa bile)
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.out.println("Dosya kapatma hatası: " + e.getMessage());
                }
            }
        }

        return users;
    }

    private void saveUserToFile(User user) {
        // 'true' parametresi dosyanın üstüne yazmak yerine sonuna ekleme (append) yapar
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(FILE_PATH, true));
            writer.write(formatUser(user));
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Dosya yazma hatası: " + e.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    System.out.println("Dosya kapatma hatası: " + e.getMessage());
                }
            }
        }
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

    public void updateUser(User updatedUser) {
        List<User> users = loadAllUsersFromFile();
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId() == updatedUser.getId()) {
                users.set(i, updatedUser);
                break;
            }
        }
        
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(FILE_PATH, false)); // false = overwrite
            for (User u : users) {
                writer.write(formatUser(u));
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Dosya yazma hatası: " + e.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    System.out.println("Dosya kapatma hatası: " + e.getMessage());
                }
            }
        }
    }
}
