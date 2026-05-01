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
    }

    public void removeTransaction(Transaction transaction) {
        this.transactions.remove(transaction);
    }

    /**
     * Bu metot, sisteme eklenmiş olan ana işlemleri okur ve onların "Sıklık" (Frequency) ayarlarına 
     * göre (Haftalık, Aylık vb.) geçmişten günümüze kadar kaç defa tekrarlandığını hesaplayarak
     * o işlemleri matematiksel olarak çoğaltır.
     * Örneğin: 15 gün önce eklenmiş "Haftalık" bir işlem varsa, bunu liste için 3 adet ayrı işleme dönüştürür.
     */
    public List<Transaction> getExpandedTransactions() {
        return getExpandedTransactions(java.time.LocalDate.now());
    }

    /**
     * Bu metot, sisteme eklenmiş olan ana işlemleri okur ve onların "Sıklık" (Frequency) ayarlarına 
     * göre (Haftalık, Aylık vb.) geçmişten 'hedef tarihe' kadar kaç defa tekrarlandığını hesaplayarak
     * o işlemleri matematiksel olarak çoğaltır.
     */
    public List<Transaction> getExpandedTransactions(java.time.LocalDate targetDate) {
        // Çoğaltılmış işlemlerin tutulacağı geçici liste
        List<Transaction> expanded = new ArrayList<>();

        // Dosyadan okunan her bir 'ana' işlemi sırayla inceliyoruz
        for (Transaction base : transactions) {
            java.time.LocalDate currentDate = base.getDate();

            // Eğer kullanıcının girdiği işlem tarihi hedef tarihten sonraysa, 
            // listeye eklemiyoruz çünkü bu hedefin dışında kalıyor. (Gelecekteki işlem)
            if (currentDate.isAfter(targetDate)) {
                // Burada base.add yapmak kafa karıştırabilir, o yüzden gelecekteki
                // bir işlemse ve hedef tarihi geçiyorsa hiç dahil etmeyelim.
                // Bu sayede "3 ay sonraki" durumu daha doğru hesaplarız.
                continue;
            }

            // İşlemin başlangıç tarihi hedef tarihe gelene kadar çoğaltmaya başla
            while (!currentDate.isAfter(targetDate)) {
                
                // Orijinal işlemi bozmamak için her tekrarda yeni bir nesne (Kopya/Clone) üretiyoruz
                Transaction clone;
                // Çok Biçimlilik (Polymorphism)
                if (base instanceof Income) {
                    clone = new Income(base.getAmount(), base.getDescription(), currentDate, base.getFrequency(), base.getSource());
                } else {
                    clone = new Expense(base.getAmount(), base.getDescription(), currentDate, base.getFrequency(), base.getSource());
                }
                
                // Oluşturduğumuz kopyayı geçici listeye ekliyoruz
                expanded.add(clone);

                // İşlemin sıklığına göre bir sonraki tarihi belirliyoruz
                if (base.getFrequency() == Frequency.ONCE) {
                    break;
                } else if (base.getFrequency() == Frequency.WEEKLY) {
                    currentDate = currentDate.plusWeeks(1); 
                } else if (base.getFrequency() == Frequency.MONTHLY) {
                    currentDate = currentDate.plusMonths(1); 
                } else if (base.getFrequency() == Frequency.YEARLY) {
                    currentDate = currentDate.plusYears(1); 
                }
            }
        }
        
        expanded.sort((t1, t2) -> t1.getDate().compareTo(t2.getDate()));
        
        return expanded;
    }

    public double getTotalBalance() {
        return getTotalBalance(java.time.LocalDate.now());
    }

    /**
     * Toplam bakiyeyi belirli bir hedef tarihe kadar çoğaltılmış işlemler üzerinden hesaplar.
     */
    public double getTotalBalance(java.time.LocalDate targetDate) {
        double total = 0;
        
        for (Transaction t : getExpandedTransactions(targetDate)) {
            total += t.getSignedAmount(); 
        }
        
        return total;
    }

    public List<Transaction> getTransactions() {
        return new ArrayList<>(transactions); // Encapsulation
    }
}
