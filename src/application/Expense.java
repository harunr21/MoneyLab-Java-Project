package application;
import java.time.LocalDate;

public class Expense extends Transaction {
    public Expense(String description, double amount, LocalDate startDate, String frequency) {
        super(description, amount, startDate, frequency);
    }

    @Override
    public String getType() {
        return "EXPENSE";
    }
}