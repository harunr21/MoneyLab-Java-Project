package src.model;

public class RegularIncome extends Income {
    private String frequency; // Aylık, Haftalık geçici

    public RegularIncome(double amount, String description, String date, String frequency) {
        // Üst sınıftaki (Income) constructor'ı çağırır
        super(amount, description, date);
        this.frequency = frequency;
    }

    // Getter ve Setter
    public String getFrequency() { return frequency; }
    
    public void setFrequency(String frequency) { this.frequency = frequency; }
}
