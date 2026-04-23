package src.model;

public abstract class Expense implements Transaction {

    private double amount;
    private String description;
    private String date;

    public Expense(double amount, String description) {
        this(amount, description, null);
    }

    public Expense(double amount, String description, String date) {
        this.amount = amount;
        this.description = description;
        this.date = date;
    }

    @Override
    public double getAmount() { return amount; }

    public void setAmount(double amount) { this.amount = amount; }

    @Override
    public String getDescription() { return description; }

    @Override
    public String getDate() { return date; }

    public void setDate(String date) { this.date = date; }

}
