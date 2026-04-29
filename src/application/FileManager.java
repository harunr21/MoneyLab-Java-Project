package application;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FileManager {
    
    private static final String PROFILES_FILE = "profiles_list.txt";

    // --- YENİ: Profil Listesini Okuma, Yazma ve Silme Metotları ---

    public static List<String> loadProfileList() {
        List<String> profiles = new ArrayList<>();
        File file = new File(PROFILES_FILE);
        if (!file.exists()) return profiles;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    profiles.add(line.trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Profil listesi okuma hatası: " + e.getMessage());
        }
        return profiles;
    }

    public static void saveProfileName(String profileName) {
        List<String> profiles = loadProfileList();
        if (!profiles.contains(profileName)) {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(PROFILES_FILE, true))) {
                bw.write(profileName);
                bw.newLine();
            } catch (IOException e) {
                System.err.println("Profil adı kaydetme hatası: " + e.getMessage());
            }
        }
    }

    public static void deleteProfile(String profileName) {
        // 1. Profil ismini listeden çıkarıp listeyi tekrar yazıyoruz
        List<String> profiles = loadProfileList();
        profiles.remove(profileName);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(PROFILES_FILE, false))) {
            for (String p : profiles) {
                bw.write(p);
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Profil silme hatası: " + e.getMessage());
        }

        // 2. Profile ait verilerin olduğu txt dosyasını bilgisayardan siliyoruz
        File dataFile = new File(profileName + "_data.txt");
        if (dataFile.exists()) {
            dataFile.delete();
        }
    }

    // --- Önceki Veri Kaydetme ve Okuma Metotları (Aynı Kaldı) ---

    public static void saveTransaction(String profileName, Transaction t) {
        String fileName = profileName + "_data.txt";
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, true))) {
            bw.write(t.toString());
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Dosya yazma hatası: " + e.getMessage());
        }
    }

    public static void rewriteProfileData(Profile profile) {
        String fileName = profile.getName() + "_data.txt";
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(fileName, false))) { 
            for (Transaction t : profile.getTransactions()) {
                bw.write(t.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Dosya yeniden yazma hatası: " + e.getMessage());
        }
    }

    public static Profile loadProfile(String profileName) {
        Profile profile = new Profile(profileName);
        File file = new File(profileName + "_data.txt");

        if (!file.exists()) return profile;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 5) {
                    String type = parts[0];
                    String desc = parts[1];
                    double amount = Double.parseDouble(parts[2]);
                    LocalDate date = LocalDate.parse(parts[3]);
                    String frequency = parts[4]; 

                    if (type.equals("INCOME")) {
                        profile.addTransaction(new Income(desc, amount, date, frequency));
                    } else {
                        profile.addTransaction(new Expense(desc, amount, date, frequency));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Dosya okuma hatası: " + e.getMessage());
        }
        return profile;
    }
}