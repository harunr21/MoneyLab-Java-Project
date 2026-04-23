package src.model;

public class RegularExpense extends Expense {
    private String frequency;

    public RegularExpense(double amount, String description, String date, String frequency) {
        super(amount, description, date);
        this.frequency = frequency;
    }

    public String getFrequency() { return frequency; }

    public void setFrequency(String frequency) { this.frequency = frequency; }

}
