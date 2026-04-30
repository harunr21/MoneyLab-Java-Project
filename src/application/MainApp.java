package application;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainApp extends Application {
    private Profile currentProfile;
    private AppView view;

    @Override
    public void start(Stage primaryStage) {
        view = new AppView(this);
        primaryStage.setTitle("Masaüstü Kumbara ve Finans Takibi");
        primaryStage.setScene(view.getScene());
        primaryStage.show();
    }

    public boolean isProfileLoaded() { return currentProfile != null; }

    public void createNewProfile() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Yeni Profil");
        dialog.setHeaderText("Yeni Bir Profil Oluşturun");
        dialog.setContentText("Profil Adı:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            String trimmedName = name.trim();
            if (!trimmedName.isEmpty() && !view.profileComboBox.getItems().contains(trimmedName)) {
                FileManager.saveProfileName(trimmedName); 
                view.profileComboBox.getItems().add(trimmedName); 
                view.profileComboBox.setValue(trimmedName); 
                loadProfileData(trimmedName); 
            } else if (view.profileComboBox.getItems().contains(trimmedName)) {
                showAlert("Uyarı", "Bu profil zaten mevcut!");
            }
        });
    }

    public void deleteSelectedProfile(String selected) {
        if (selected == null || selected.isEmpty()) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Profili Sil");
        confirm.setHeaderText(selected + " profilini silmek istediğinize emin misiniz?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                FileManager.deleteProfile(selected); 
                view.profileComboBox.getItems().remove(selected); 
                view.profileComboBox.setValue(null);
                currentProfile = null;
                updateUI(); 
            }
        });
    }

    public void loadProfileData(String profileName) {
        if (profileName == null || profileName.trim().isEmpty()) return;
        currentProfile = FileManager.loadProfile(profileName);
        updateUI();
    }

    public void addTransaction(Transaction tx) {
        if (currentProfile != null) {
            currentProfile.addTransaction(tx);
            FileManager.rewriteProfileData(currentProfile);
            updateUI();
        }
    }

    public void deleteTransaction(Transaction selected) {
        if (selected != null && currentProfile != null) {
            currentProfile.removeTransaction(selected);
            FileManager.rewriteProfileData(currentProfile);
            updateUI(); 
        }
    }

    public void addNewGoalByDate(String name, double total, java.time.LocalDate targetDate) {
        if (currentProfile != null && targetDate != null) {
            Goal g = new Goal(name, total);
            double monthly = g.calculateMonthlySavingByDate(targetDate);
            showAlert("Plan Oluşturuldu", "Hedefine " + targetDate + " tarihinde ulaşmak için aylık " + String.format("%.2f", monthly) + " ₺ biriktirmelisin.");
            currentProfile.addGoal(g);
            FileManager.rewriteProfileData(currentProfile);
            updateUI();
        }
    }

    public void addNewGoalByBudget(String name, double total, double monthlyAmount) {
        if (currentProfile != null && monthlyAmount > 0) {
            Goal g = new Goal(name, total);
            double duration = g.calculateRequiredTimeForSaving(monthlyAmount);
            showAlert("Plan Oluşturuldu", "Aylık " + monthlyAmount + " ₺ ile hedefine yaklaşık " + String.format("%.1f", duration) + " ay sonra ulaşacaksın.");
            currentProfile.addGoal(g);
            FileManager.rewriteProfileData(currentProfile);
            updateUI();
        }
    }

    public void removeGoal(Goal goal) {
        if (currentProfile != null && goal != null) {
            currentProfile.removeGoal(goal);
            FileManager.rewriteProfileData(currentProfile);
            updateUI();
        }
    }

    public void addFundsToGoal(Goal goal, double amount) {
        if (currentProfile != null && goal != null && amount > 0) {
            goal.addFunds(amount);
            if (goal.isCompleted()) {
                showAlert("Tebrikler!", goal.getName() + " hedefine ulaştın!");
            }
            FileManager.rewriteProfileData(currentProfile);
            updateUI();
        }
    }

    private ObservableList<Transaction> getExpandedHistory(List<Transaction> baseTransactions) {
        ObservableList<Transaction> expandedList = FXCollections.observableArrayList();
        LocalDate today = LocalDate.now();

        for (Transaction t : baseTransactions) {
            long occurrences = t.getOccurrencesUpTo(today);
            if (occurrences == 0) {
                expandedList.add(t);
                continue;
            }
            for (int i = 0; i < occurrences; i++) {
                LocalDate nextDate = t.getStartDate();
                if (t.getFrequency().equals("Günlük")) nextDate = nextDate.plusDays(i);
                else if (t.getFrequency().equals("Aylık")) nextDate = nextDate.plusMonths(i);

                Transaction virtualTx = t.getType().equals("INCOME") 
                    ? new Income(t.getDescription(), t.getAmount(), nextDate, t.getFrequency())
                    : new Expense(t.getDescription(), t.getAmount(), nextDate, t.getFrequency());
                expandedList.add(virtualTx);
            }
        }
        expandedList.sort(Comparator.comparing(Transaction::getStartDate).reversed());
        return expandedList;
    }

    public void updateUI() {
        if (currentProfile == null) {
            view.balanceLabel.setText("Aktif Bakiye: 0.0 ₺");
            view.historyTable.getItems().clear();
            view.incomeListView.getItems().clear();
            view.expenseListView.getItems().clear();
            view.goalsListView.getItems().clear();
            view.pieChart.getData().clear();
            return;
        }

        view.balanceLabel.setText("Aktif Bakiye: " + currentProfile.getCurrentBalance() + " ₺");

        ObservableList<Goal> goalList = FXCollections.observableArrayList(currentProfile.getGoals());
        view.goalsListView.setItems(goalList);
        view.goalsListView.refresh();

        ObservableList<Transaction> tableData = getExpandedHistory(currentProfile.getTransactions());
        view.historyTable.setItems(tableData);

        ObservableList<Transaction> incomeTransactions = FXCollections.observableArrayList(
            currentProfile.getTransactions().stream().filter(t -> t.getType().equals("INCOME")).collect(Collectors.toList())
        );
        view.incomeListView.setItems(incomeTransactions);

        ObservableList<Transaction> expenseTransactions = FXCollections.observableArrayList(
            currentProfile.getTransactions().stream().filter(t -> t.getType().equals("EXPENSE")).collect(Collectors.toList())
        );
        view.expenseListView.setItems(expenseTransactions);

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        LocalDate today = LocalDate.now();
        List<Transaction> orderedForChart = new java.util.ArrayList<>();

        // 1. Önce sadece Gelirler (INCOME)
        for (Transaction t : currentProfile.getTransactions()) {
            if (t.getType().equals("INCOME")) {
                long occurrences = t.getOccurrencesUpTo(today);
                if (occurrences > 0) {
                    double totalAmount = t.getAmount() * occurrences;
                    pieChartData.add(new PieChart.Data(t.getDescription() + " (" + totalAmount + " ₺)", totalAmount));
                    orderedForChart.add(t);
                }
            }
        }

        // 2. Sonra sadece Giderler (EXPENSE)
        for (Transaction t : currentProfile.getTransactions()) {
            if (t.getType().equals("EXPENSE")) {
                long occurrences = t.getOccurrencesUpTo(today);
                if (occurrences > 0) {
                    double totalAmount = t.getAmount() * occurrences;
                    pieChartData.add(new PieChart.Data(t.getDescription() + " (" + totalAmount + " ₺)", totalAmount));
                    orderedForChart.add(t);
                }
            }
        }
        
        view.pieChart.setData(pieChartData);

        int index = 0;
        for (Transaction t : orderedForChart) {
            Node sliceNode = pieChartData.get(index).getNode();
            if (sliceNode != null) {
                sliceNode.setStyle(t.getType().equals("INCOME") ? "-fx-pie-color: #4CAF50;" : "-fx-pie-color: #F44336;");
            }
            index++;
        }
    }

    public void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) { launch(args); }
}