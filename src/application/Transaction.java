package application;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public abstract class Transaction {
    private String description;
    private double amount;
    private LocalDate startDate;
    private String frequency; // Sadece: "Tek Seferlik", "Günlük", "Aylık"

    public Transaction(String description, double amount, LocalDate startDate, String frequency) {
        this.description = description;
        this.amount = amount;
        this.startDate = startDate;
        this.frequency = frequency;
    }

    public String getDescription() { return description; }
    public double getAmount() { return amount; }
    public LocalDate getStartDate() { return startDate; }
    public String getFrequency() { return frequency; }

    public long getOccurrencesUpTo(LocalDate targetDate) {
        if (startDate.isAfter(targetDate)) {
            return 0; // Tarihi gelmemişse sıfır kez gerçekleşti
        }
        long occurrences = 1; // Tarihi geldiyse en az 1 kere gerçekleşmiştir
        
        switch (frequency) {
            case "Günlük":
                occurrences += ChronoUnit.DAYS.between(startDate, targetDate);
                break;
            case "Aylık":
                occurrences += ChronoUnit.MONTHS.between(startDate, targetDate);
                break;
            // "Haftalık" case'i isteğin üzerine tamamen kaldırıldı.
        }
        return occurrences;
    }

    public abstract String getType();

    @Override
    public String toString() {
        return getType() + ";" + description + ";" + amount + ";" + startDate + ";" + frequency;
    }
}