package com.moneylab.model;

import java.time.LocalDate;

public class Expense extends Transaction {

    public Expense(double amount, String description, LocalDate date, Frequency frequency, String source) {
        super(amount, description, date, frequency, source);
    }

    // Gider olduğu için miktar bakiyeden düşülmek üzere negatif döndürülür
    @Override
    public double getSignedAmount() {
        return -getAmount();
    }
}
