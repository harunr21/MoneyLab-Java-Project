package src.model;

import java.util.ArrayList;
import java.util.List;

public class Balance {
    private List<Transaction> transactions;

    public Balance() {
        this.transactions = new ArrayList<>();
    }

    public void addTransaction(Transaction transaction) {
        this.transactions.add(transaction);
        // Console log çıkarıldı (SRP İhlali Düzeltildi)
    }

    public double getTotalBalance() {
        double total = 0;
        // Çok Biçimlilik (Polymorphism) kullanılarak tek listeyle hesaplama
        for (Transaction t : transactions) {
            total += t.getSignedAmount();
        }
        return total;
    }

    public List<Transaction> getTransactions() {
        return new ArrayList<>(transactions); // Encapsulation: kopyasını döndürüyoruz
    }
}
