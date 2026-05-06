package com.moneylab.service;

import com.moneylab.model.*;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionManager {

    // Dosya yolunu çalışma dizinine göre belirliyoruz
    private static final String FILE_PATH = System.getProperty("user.dir") + File.separator + "transactions.txt";
    private static final String HISTORY_FILE_PATH = System.getProperty("user.dir") + File.separator + "history.txt";

    // Kullanıcının işlemlerini dosyadan okur ve bir liste olarak döndürür
    public List<Transaction> loadUserTransactions(int userId) {
        List<Transaction> userTransactions = new ArrayList<>();
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return userTransactions; // Dosya yoksa boş liste döner
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    try {
                        // Satırı parçalıyoruz
                        String[] parts = line.split(",");
                        int currentUserId = Integer.parseInt(parts[0].trim());

                        // Sadece giriş yapan kullanıcının işlemlerini alıyoruz
                        if (currentUserId == userId) {
                            String type = parts[1].trim();
                            double amount = Double.parseDouble(parts[2].trim());
                            String description = parts[3].trim();
                            LocalDate date = LocalDate.parse(parts[4].trim());
                            Frequency frequency = Frequency.valueOf(parts[5].trim());
                            String source = parts[6].trim();

                            Transaction transaction;
                            if (type.equals("INCOME")) {
                                transaction = new Income(amount, description, date, frequency, source);
                            } else {
                                transaction = new Expense(amount, description, date, frequency, source);
                            }
                            
                            userTransactions.add(transaction);
                        }
                    } catch (Exception e) {
                        System.out.println("Hata: İşlem satırı okunamadı -> " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Dosya okuma hatası: " + e.getMessage());
        } finally {
            // Dosyayı her durumda kapatıyoruz
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.out.println("Dosya kapatma hatası: " + e.getMessage());
                }
            }
        }

        return userTransactions;
    }

    // Yeni bir işlemi kullanıcının ID'si ile birlikte dosyaya kaydeder
    public void saveTransaction(int userId, Transaction transaction) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(FILE_PATH, true));
            
            String type = (transaction instanceof Income) ? "INCOME" : "EXPENSE";
            
            // Format: userId,type,amount,description,date,frequency,source
            String line = userId + "," 
                        + type + "," 
                        + transaction.getAmount() + "," 
                        + transaction.getDescription() + "," 
                        + transaction.getDate().toString() + "," 
                        + transaction.getFrequency().toString() + "," 
                        + transaction.getSource();
                        
            writer.write(line);
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

    // Yeni bir işlemi tarihçe dosyasına kaydeder
    public void saveToHistory(int userId, Transaction transaction) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(HISTORY_FILE_PATH, true));
            
            String type = (transaction instanceof Income) ? "INCOME" : "EXPENSE";
            
            // Format: userId,type,amount,description,date,frequency,source
            String line = userId + "," 
                        + type + "," 
                        + transaction.getAmount() + "," 
                        + transaction.getDescription() + "," 
                        + transaction.getDate().toString() + "," 
                        + transaction.getFrequency().toString() + "," 
                        + transaction.getSource();
                        
            writer.write(line);
            writer.newLine();
            
        } catch (IOException e) {
            System.out.println("Tarihçe dosya yazma hatası: " + e.getMessage());
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    System.out.println("Tarihçe dosya kapatma hatası: " + e.getMessage());
                }
            }
        }
    }

    // Kullanıcının işlem geçmişini okur
    public List<Transaction> loadUserHistory(int userId) {
        List<Transaction> userHistory = new ArrayList<>();
        File file = new File(HISTORY_FILE_PATH);

        if (!file.exists()) {
            return userHistory; 
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
                            String type = parts[1].trim();
                            double amount = Double.parseDouble(parts[2].trim());
                            String description = parts[3].trim();
                            LocalDate date = LocalDate.parse(parts[4].trim());
                            Frequency frequency = Frequency.valueOf(parts[5].trim());
                            String source = parts[6].trim();

                            Transaction transaction;
                            if (type.equals("INCOME")) {
                                transaction = new Income(amount, description, date, frequency, source);
                            } else {
                                transaction = new Expense(amount, description, date, frequency, source);
                            }
                            
                            userHistory.add(transaction);
                        }
                    } catch (Exception e) {
                        System.out.println("Hata: İşlem satırı okunamadı -> " + line);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Tarihçe dosya okuma hatası: " + e.getMessage());
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    System.out.println("Tarihçe dosya kapatma hatası: " + e.getMessage());
                }
            }
        }

        return userHistory;
    }

    // Kullanıcının tüm işlemlerini yeniden yazar (Silme işlemi için kullanılır)
    public void rewriteUserTransactions(int userId, List<Transaction> userTransactions) {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        List<String> otherUsersLines = new ArrayList<>();
        
        // 1. Önce dosyayı okuyup, diğer kullanıcıların işlemlerini hafızada tutuyoruz
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.isBlank()) {
                    try {
                        String[] parts = line.split(",");
                        int currentUserId = Integer.parseInt(parts[0].trim());
                        // Eğer satır başka bir kullanıcıya aitse, onu korumak için listeye ekliyoruz
                        if (currentUserId != userId) {
                            otherUsersLines.add(line);
                        }
                    } catch (Exception e) {
                        System.out.println("Satır atlandı: " + line);
                    }
                }
            }
        } catch (Exception e) {
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

        // 2. Dosyayı baştan yaratıyoruz (Eski veriler silinir)
        // FileWriter'da false parametresi (veya parametresiz kullanım) dosyanın üzerine yazar
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(FILE_PATH));
            
            // 3. Diğer kullanıcıların verilerini geri yazıyoruz
            for (String line : otherUsersLines) {
                writer.write(line);
                writer.newLine();
            }

            // 4. Güncel (silinmiş haliyle) kullanıcı işlemlerini yazıyoruz
            for (Transaction transaction : userTransactions) {
                String type = (transaction instanceof Income) ? "INCOME" : "EXPENSE";
                String line = userId + "," 
                            + type + "," 
                            + transaction.getAmount() + "," 
                            + transaction.getDescription() + "," 
                            + transaction.getDate().toString() + "," 
                            + transaction.getFrequency().toString() + "," 
                            + transaction.getSource();
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
