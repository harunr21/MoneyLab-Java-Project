package com.moneylab.model;

/**
 * Bu arayüz, her türlü finansal kaydın (Gelir, Gider vb.) 
 * sahip olması gereken temel metotları tanımlar.
 * Proje isterlerindeki interface zorunluluğunu karşılar.
 */
public interface FinancialRecord {
    
    // İşlemin açıklamasını döndürür
    String getDescription();
    
    // İşlemin bakiyeye olan etkisini (pozitif/negatif) döndürür
    double getSignedAmount();
}