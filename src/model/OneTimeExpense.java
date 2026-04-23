package src.model;

public class OneTimeExpense extends Expense {

    private String source;

    public OneTimeExpense(double amount, String description, String date, String source) {
        super(amount, description, date);
        this.source = source;
    }

    public String getSource() {
        return source;
    }

}
