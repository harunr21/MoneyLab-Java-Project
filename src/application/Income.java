package application;
import java.time.LocalDate;

public class Income extends Transaction {
    public Income(String description, double amount, LocalDate startDate, String frequency) {
        super(description, amount, startDate, frequency);
    }

    @Override
    public String getType() {
        return "INCOME";
    }
}