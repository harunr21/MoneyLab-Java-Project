package src.model;

import java.time.LocalDate;

public class Income extends Transaction {

    public Income(double amount, String description, LocalDate date, Frequency frequency, String source) {
        super(amount, description, date, frequency, source);
    }

    // Gelir olduğu için miktar bakiyeye pozitif olarak yansıtılır
    @Override
    public double getSignedAmount() {
        return getAmount();
    }
}
