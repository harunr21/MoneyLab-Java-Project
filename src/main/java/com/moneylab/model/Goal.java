package com.moneylab.model;

/**
 * Kullanıcının bireysel hedeflerini temsil eden model sınıfı.
 * Her hedefin bir adı ve ulaşılmak istenen bir tutarı vardır.
 */
public class Goal {
    private String name;        // Hedefin adı (Örn: "Araba", "Tatil")
    private double targetAmount; // Hedeflenen tutar

    public Goal(String name, double targetAmount) {
        setName(name);
        setTargetAmount(targetAmount);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Hedef adı boş olamaz.");
        }
        if (name.contains(",")) {
            throw new IllegalArgumentException("Hedef adında virgül (,) kullanılamaz.");
        }
        this.name = name;
    }

    public double getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(double targetAmount) {
        if (targetAmount <= 0) {
            throw new IllegalArgumentException("Hedef tutarı sıfırdan büyük olmalıdır.");
        }
        this.targetAmount = targetAmount;
    }

    @Override
    public String toString() {
        return "Goal{name='" + name + "', targetAmount=" + targetAmount + "}";
    }
}
