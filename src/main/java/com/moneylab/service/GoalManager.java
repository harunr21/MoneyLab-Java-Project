package com.moneylab.service;

import com.moneylab.model.Goal;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Hedeflerin dosyaya kaydedilmesi, okunması ve silinmesi işlemlerini yöneten servis sınıfı.
 * Her satır formatı: userId,goalName,targetAmount
 */
public class GoalManager {

    // Hedef dosyasının yolu
    private static final String FILE_PATH = System.getProperty("user.dir") + File.separator + "goals.txt";

    /**
     * Belirli bir kullanıcının tüm hedeflerini dosyadan okuyup liste olarak döndürür.
     */
    public List<Goal> loadUserGoals(int userId) {
        List<Goal> goals = new ArrayList<>();
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return goals;
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    try {
                        String[] parts = line.split(",");
                        int currentUserId = Integer.parseInt(parts[0].trim());

                        if (currentUserId == userId) {
                            String name = parts[1].trim();
                            double targetAmount = Double.parseDouble(parts[2].trim());
                            goals.add(new Goal(name, targetAmount));
                        }
                    } catch (Exception e) {
                        System.out.println("Hata: Hedef satırı okunamadı -> " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Hedef dosya okuma hatası: " + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.out.println("Dosya kapatma hatası: " + e.getMessage());
                }
            }
        }

        return goals;
    }

    /**
     * Yeni bir hedefi dosyaya kaydeder (mevcut dosyanın sonuna ekler).
     */
    public void saveGoal(int userId, Goal goal) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(FILE_PATH, true));
            // Format: userId,goalName,targetAmount
            String line = userId + "," + goal.getName() + "," + goal.getTargetAmount();
            writer.write(line);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Hedef dosya yazma hatası: " + e.getMessage());
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

    /**
     * Bir hedefi sildikten sonra kullanıcının kalan hedeflerini dosyaya yeniden yazar.
     * TransactionManager.rewriteUserTransactions ile aynı mantıkta çalışır.
     */
    public void rewriteUserGoals(int userId, List<Goal> userGoals) {
        File file = new File(FILE_PATH);

        // Diğer kullanıcıların satırlarını korumak için önce dosyayı okuyoruz
        List<String> otherUsersLines = new ArrayList<>();

        if (file.exists()) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.isBlank()) {
                        try {
                            String[] parts = line.split(",");
                            int currentUserId = Integer.parseInt(parts[0].trim());
                            if (currentUserId != userId) {
                                otherUsersLines.add(line);
                            }
                        } catch (Exception e) {
                            System.out.println("Satır atlandı: " + line);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Dosya okuma hatası: " + e.getMessage());
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        System.out.println("Dosya kapatma hatası: " + e.getMessage());
                    }
                }
            }
        }

        // Dosyayı baştan yazıyoruz
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(FILE_PATH));

            // Diğer kullanıcıların verilerini geri yaz
            for (String line : otherUsersLines) {
                writer.write(line);
                writer.newLine();
            }

            // Bu kullanıcının güncel hedeflerini yaz
            for (Goal goal : userGoals) {
                String line = userId + "," + goal.getName() + "," + goal.getTargetAmount();
                writer.write(line);
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
