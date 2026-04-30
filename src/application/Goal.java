package application;

public class Goal {
    private String name;
    private double targetAmount;
    private double savedAmount;
    private boolean isCompleted;

    public Goal(String name, double targetAmount) {
        this.name = name;
        this.targetAmount = targetAmount;
        this.savedAmount = 0.0; 
        this.isCompleted = false;
    }

    public double calculateRequiredSavingForTime(int timeDuration) {
        double remainingAmount = getRemainingAmount();
        if (remainingAmount <= 0 || timeDuration <= 0) return 0.0;
        return remainingAmount / timeDuration;
    }

    public double calculateRequiredTimeForSaving(double savingAmountPerPeriod) {
        double remainingAmount = getRemainingAmount();
        if (remainingAmount <= 0 || savingAmountPerPeriod <= 0) return 0.0;
        return remainingAmount / savingAmountPerPeriod;
    }
    /**
     * Seçenek 1: Belirlenen hedef tarihine kadar aylık ne kadar ayrılması gerektiğini hesaplar.
     */
    public double calculateMonthlySavingByDate(java.time.LocalDate targetDate) {
        java.time.LocalDate today = java.time.LocalDate.now();
        if (targetDate.isBefore(today) || targetDate.isEqual(today)) return getRemainingAmount();
        
        // DÜZELTME: java.time.temporal eklendi
        long months = java.time.temporal.ChronoUnit.MONTHS.between(
            today.withDayOfMonth(1), 
            targetDate.withDayOfMonth(1)
        );
        
        if (months <= 0) return getRemainingAmount();
        return getRemainingAmount() / months;
    }

    public void addFunds(double amount) {
        if (amount > 0 && !isCompleted) {
            this.savedAmount += amount;
            checkCompletion();
        }
    }

    private void checkCompletion() {
        if (this.savedAmount >= this.targetAmount) {
            this.isCompleted = true;
            this.savedAmount = this.targetAmount; 
        }
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getTargetAmount() { return targetAmount; }
    public void setTargetAmount(double targetAmount) { 
        this.targetAmount = targetAmount; 
        checkCompletion(); 
    }

    public double getSavedAmount() { return savedAmount; }
    
    public double getRemainingAmount() {
        return Math.max(0, targetAmount - savedAmount);
    }

    public boolean isCompleted() { return isCompleted; }
    
    public void setCompleted(boolean completed) {
        this.isCompleted = completed;
        if (completed) this.savedAmount = this.targetAmount;
    }
}