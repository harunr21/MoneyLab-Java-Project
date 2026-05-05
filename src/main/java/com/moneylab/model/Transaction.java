package com.moneylab.model;

import java.time.LocalDate;

public abstract class Transaction {
    private double amount;
    private String description;
    private LocalDate date;
    private Frequency frequency; // ONCE ise tek seferlik, değilse düzenli işlemdir
    private String source;       // Gelir kaynağı veya giderin yapıldığı yer

    public Transaction(double amount, String description, LocalDate date, Frequency frequency, String source) {
        setAmount(amount);
        setDescription(description);
        this.date = (date != null) ? date : LocalDate.now();
        this.frequency = (frequency != null) ? frequency : Frequency.ONCE;
        this.source = source;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Miktar negatif olamaz.");
        }
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description == null || description.isBlank()) {
            this.description = "Belirtilmedi";
        } else {
            this.description = description;
        }
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    // Çok biçimlilik (Polymorphism) - Alt sınıflar (Income/Expense) burayı kendine göre dolduracak
    public abstract double getSignedAmount();
}
