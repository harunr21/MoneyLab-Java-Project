import java.util.ArrayList;
import java.util.List;

public class Balance {
    private List<Income> incomes;
    private List<Expense> expenses;

    public Balance() {
        this.incomes = new ArrayList<>();
        this.expenses = new ArrayList<>();
    }

    // listeye gelir ekleme
    public void addIncome(Income income) {
        this.incomes.add(income);
        System.out.println("Sisteme gelir eklendi: " + income.getAmount() + " TL");
    }

    // toplam bakiye//
    public double getTotalBalance() {
        double total = 0;
        for (Income inc : incomes) {
            total += inc.getAmount();
        }
       
        return total;
    }
}