package application;

import java.util.ArrayList;
import java.util.List;

public class Profile {
    private String name;
    private List<Transaction> transactions;
    private List<Goal> goals; 

    public Profile(String name) {
        this.name = name;
        this.transactions = new ArrayList<>();
        this.goals = new ArrayList<>(); 
    }

    public String getName() { return name; }
    
    public List<Transaction> getTransactions() { return transactions; }
    public void addTransaction(Transaction t) { transactions.add(t); }
    public void removeTransaction(Transaction t) { transactions.remove(t); }

    public List<Goal> getGoals() { return goals; }
    public void addGoal(Goal g) { goals.add(g); }
    public void removeGoal(Goal g) { goals.remove(g); }

    public double getCurrentBalance() {
        double balance = 0;
        for (Transaction t : transactions) {
            if (t.getType().equals("INCOME")) {
                balance += t.getAmount();
            } else if (t.getType().equals("EXPENSE")) {
                balance -= t.getAmount();
            }
        }
        return balance;
    }
}