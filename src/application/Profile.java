package application;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Profile {
    private String name;
    private List<Transaction> transactions;

    public Profile(String name) {
        this.name = name;
        this.transactions = new ArrayList<>();
    }

    public String getName() { return name; }
    public List<Transaction> getTransactions() { return transactions; }

    public void addTransaction(Transaction t) {
        transactions.add(t);
    }

    // YENİ EKLENDİ: İşlem silme metodu
    public void removeTransaction(Transaction t) {
        transactions.remove(t);
    }

    public double[] calculateTotals() {
        double totalIncome = 0;
        double totalExpense = 0;
        LocalDate today = LocalDate.now();

        for (Transaction t : transactions) {
            long occurrences = t.getOccurrencesUpTo(today);
            if (occurrences > 0) {
                if (t instanceof Income) {
                    totalIncome += (t.getAmount() * occurrences);
                } else if (t instanceof Expense) {
                    totalExpense += (t.getAmount() * occurrences);
                }
            }
        }
        return new double[]{totalIncome, totalExpense};
    }

    public double getCurrentBalance() {
        double[] totals = calculateTotals();
        return totals[0] - totals[1];
    }
}