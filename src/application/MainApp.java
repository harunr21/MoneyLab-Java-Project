package application;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainApp extends Application {
    private Profile currentProfile;
    private Label balanceLabel;
    private PieChart pieChart;
    private ComboBox<String> profileComboBox;
    private TableView<Transaction> historyTable;
    
    private ListView<Transaction> incomeListView;
    private ListView<Transaction> expenseListView;
    
    private HBox listsBox;
    
    // Formun hangi tür veri kaydedeceğini aklında tutması için bir değişken
    private String activeTransactionType = "";

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Masaüstü Kumbara ve Finans Takibi");
        BorderPane root = new BorderPane();

        // --- ÜST PANEL: Profil Seçimi ve Bütçe Butonu ---
        HBox topBox = new HBox(10);
        topBox.setPadding(new Insets(10));
        
        profileComboBox = new ComboBox<>();
        profileComboBox.getItems().addAll(FileManager.loadProfileList());
        profileComboBox.setPromptText("Profil Seçin");
        
        Button loadBtn = new Button("Yükle");
        loadBtn.setOnAction(e -> loadProfileData(profileComboBox.getValue()));

        Button addProfileBtn = new Button("Yeni Profil");
        addProfileBtn.setOnAction(e -> createNewProfile());

        Button deleteProfileBtn = new Button("Profili Sil");
        deleteProfileBtn.setOnAction(e -> deleteSelectedProfile());

        // --- SOL PANEL: Dinamik Form ve Buton Alanı ---
        VBox leftBox = new VBox(15);
        leftBox.setPadding(new Insets(10));
        leftBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 0 1 0 0;");
        leftBox.setPrefWidth(250);
        leftBox.setAlignment(Pos.TOP_CENTER); // Butonları ortalamak için

        // DURUM 1: Kare Butonların Olduğu Kutu
        VBox squareButtonsBox = new VBox(20);
        squareButtonsBox.setAlignment(Pos.CENTER);

        Button addIncomeBtn = new Button("+\nGelir Ekle");
        addIncomeBtn.setPrefSize(150, 150); // Kare Boyutları
        addIncomeBtn.setTextAlignment(TextAlignment.CENTER);
        addIncomeBtn.setStyle("-fx-base: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");

        Button addExpenseBtn = new Button("+\nGider Ekle");
        addExpenseBtn.setPrefSize(150, 150); // Kare Boyutları
        addExpenseBtn.setTextAlignment(TextAlignment.CENTER);
        addExpenseBtn.setStyle("-fx-base: #F44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");

        squareButtonsBox.getChildren().addAll(addIncomeBtn, addExpenseBtn);

        // DURUM 2: Formun Olduğu Kutu (Başlangıçta Gizli)
        VBox formBox = new VBox(10);
        Label formTitle = new Label("Yeni İşlem");
        formTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        TextField descField = new TextField();
        descField.setPromptText("Açıklama (Örn: Maaş, Kira)");

        TextField amountField = new TextField();
        amountField.setPromptText("Miktar (₺)");

        DatePicker datePicker = new DatePicker();
        datePicker.setPromptText("Tarih Seçin");

        ComboBox<String> freqBox = new ComboBox<>();
        freqBox.getItems().addAll("Tek Seferlik", "Günlük", "Aylık");
        freqBox.setValue("Tek Seferlik");

        HBox actionBtnsBox = new HBox(10);
        Button saveBtn = new Button("Kaydet");
        saveBtn.setStyle("-fx-base: #2196F3; -fx-text-fill: white;");
        Button cancelBtn = new Button("İptal");
        actionBtnsBox.getChildren().addAll(saveBtn, cancelBtn);

        formBox.getChildren().addAll(formTitle, descField, amountField, datePicker, freqBox, actionBtnsBox);

        // Butonlara Tıklanınca Formu Açma İşlemi
        javafx.event.EventHandler<javafx.event.ActionEvent> openFormHandler = e -> {
            if (currentProfile == null) {
                showAlert("Hata", "Lütfen önce bir profil seçin veya oluşturun!");
                return;
            }
            // Hangi butona tıklandığını hafızaya al
            activeTransactionType = (e.getSource() == addIncomeBtn) ? "Gelir" : "Gider";
            formTitle.setText("Yeni " + activeTransactionType + " Ekle");
            
            // Kare butonları ekrandan kaldırıp formu ekle
            leftBox.getChildren().remove(squareButtonsBox);
            leftBox.getChildren().add(0, formBox);
        };

        addIncomeBtn.setOnAction(openFormHandler);
        addExpenseBtn.setOnAction(openFormHandler);

        // İptal Butonu İşlemi (Formu kapatıp kare butonlara dön)
        cancelBtn.setOnAction(e -> {
            descField.clear(); amountField.clear(); datePicker.setValue(null); freqBox.setValue("Tek Seferlik");
            leftBox.getChildren().remove(formBox);
            leftBox.getChildren().add(0, squareButtonsBox);
        });

        // Kaydet Butonu İşlemi
        saveBtn.setOnAction(e -> {
            try {
                String desc = descField.getText();
                double amount = Double.parseDouble(amountField.getText());
                LocalDate date = datePicker.getValue();
                String freq = freqBox.getValue();

                if (date == null || desc.isEmpty()) throw new IllegalArgumentException("Boş alan bırakmayın.");

                Transaction newTx = activeTransactionType.equals("Gelir") 
                    ? new Income(desc, amount, date, freq) 
                    : new Expense(desc, amount, date, freq);

                currentProfile.addTransaction(newTx);
                FileManager.saveTransaction(currentProfile.getName(), newTx);
                
                updateUI(); 
                
                // Formu temizle ve KARE BUTONLARA GERİ DÖN
                descField.clear(); amountField.clear(); datePicker.setValue(null); freqBox.setValue("Tek Seferlik");
                leftBox.getChildren().remove(formBox);
                leftBox.getChildren().add(0, squareButtonsBox);

            } catch (Exception ex) {
                showAlert("Hata", "Lütfen verileri doğru formatta girin.\n" + ex.getMessage());
            }
        });

        // Başlangıçta sol panelde sadece kare butonlar olsun
        leftBox.getChildren().add(squareButtonsBox);

        // --- ORTA PANEL İÇERİKLERİ ---
        pieChart = new PieChart();
        pieChart.setTitle("Gerçekleşen Gelir/Gider Dağılımı");
        pieChart.setPrefHeight(250);

        incomeListView = new ListView<>();
        expenseListView = new ListView<>();
        
        incomeListView.setCellFactory(param -> new ListCell<Transaction>() {
            @Override
            protected void updateItem(Transaction item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.getDescription() + " (" + item.getAmount() + " ₺)");
            }
        });

        expenseListView.setCellFactory(param -> new ListCell<Transaction>() {
            @Override
            protected void updateItem(Transaction item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else setText(item.getDescription() + " (" + item.getAmount() + " ₺)");
            }
        });

        ContextMenu incomeMenu = new ContextMenu();
        MenuItem deleteIncome = new MenuItem("Geliri Sil");
        deleteIncome.setOnAction(e -> deleteSelectedTransaction(incomeListView.getSelectionModel().getSelectedItem()));
        incomeMenu.getItems().add(deleteIncome);
        incomeListView.setContextMenu(incomeMenu);

        ContextMenu expenseMenu = new ContextMenu();
        MenuItem deleteExpense = new MenuItem("Gideri Sil");
        deleteExpense.setOnAction(e -> deleteSelectedTransaction(expenseListView.getSelectionModel().getSelectedItem()));
        expenseMenu.getItems().add(deleteExpense);
        expenseListView.setContextMenu(expenseMenu);

        VBox incomeBox = new VBox(5, new Label("Gelirler (Silmek için sağ tıkla)"), incomeListView);
        VBox expenseBox = new VBox(5, new Label("Giderler (Silmek için sağ tıkla)"), expenseListView);
        
        HBox.setHgrow(incomeBox, Priority.ALWAYS);
        HBox.setHgrow(expenseBox, Priority.ALWAYS);
        VBox.setVgrow(incomeListView, Priority.ALWAYS);
        VBox.setVgrow(expenseListView, Priority.ALWAYS);
        
        listsBox = new HBox(15, incomeBox, expenseBox);
        listsBox.setPrefHeight(250);

        VBox statsBox = new VBox(15);
        statsBox.setPadding(new Insets(10));
        balanceLabel = new Label("Aktif Bakiye: 0.0 ₺");
        balanceLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Başlangıçta PieChart gösteriliyor
        statsBox.getChildren().addAll(balanceLabel, pieChart);

        Button budgetBtn = new Button("Bütçe Yönetimi");
        budgetBtn.setStyle("-fx-base: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;"); 
        budgetBtn.setOnAction(e -> {
            boolean isBudgetMode = root.getLeft() != null;
            if (isBudgetMode) {
                root.setLeft(null); // Sol paneli kapat
                statsBox.getChildren().remove(listsBox); // Listeleri gizle
                if (!statsBox.getChildren().contains(pieChart)) {
                    statsBox.getChildren().add(pieChart); // Grafiği getir
                }
            } else {
                root.setLeft(leftBox); // Sol paneli aç
                statsBox.getChildren().remove(pieChart); // Grafiği gizle
                if (!statsBox.getChildren().contains(listsBox)) {
                    statsBox.getChildren().add(listsBox); // Listeleri getir
                }
            }
        });

        topBox.getChildren().addAll(new Label("Profil:"), profileComboBox, loadBtn, addProfileBtn, deleteProfileBtn, new Separator(javafx.geometry.Orientation.VERTICAL), budgetBtn);

        // --- ALT PANEL: İşlem Geçmişi Tablosu ---
        VBox bottomBox = new VBox(5);
        bottomBox.setPadding(new Insets(10));
        Label tableLabel = new Label("İşlem Geçmişi");
        tableLabel.setStyle("-fx-font-weight: bold;");

        historyTable = new TableView<>();
        historyTable.setPrefHeight(200);
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Transaction, String> typeCol = new TableColumn<>("Tür");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<Transaction, String> descCol = new TableColumn<>("Açıklama");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<Transaction, Double> amountCol = new TableColumn<>("Miktar (₺)");
        amountCol.setCellValueFactory(new PropertyValueFactory<>("amount"));

        TableColumn<Transaction, LocalDate> dateCol = new TableColumn<>("Tarih");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("startDate"));

        TableColumn<Transaction, String> freqCol = new TableColumn<>("Sıklık");
        freqCol.setCellValueFactory(new PropertyValueFactory<>("frequency"));

        historyTable.getColumns().addAll(typeCol, descCol, amountCol, dateCol, freqCol);
        bottomBox.getChildren().addAll(tableLabel, historyTable);

        // --- ANA YERLEŞİM BİRLEŞTİRME ---
        root.setTop(topBox);
        root.setCenter(statsBox);
        root.setBottom(bottomBox);

        Scene scene = new Scene(root, 850, 750);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void createNewProfile() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Yeni Profil");
        dialog.setHeaderText("Yeni Bir Profil Oluşturun");
        dialog.setContentText("Profil Adı:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            String trimmedName = name.trim();
            if (!trimmedName.isEmpty() && !profileComboBox.getItems().contains(trimmedName)) {
                FileManager.saveProfileName(trimmedName); 
                profileComboBox.getItems().add(trimmedName); 
                profileComboBox.setValue(trimmedName); 
                loadProfileData(trimmedName); 
            } else if (profileComboBox.getItems().contains(trimmedName)) {
                showAlert("Uyarı", "Bu profil zaten mevcut!");
            }
        });
    }

    private void deleteSelectedProfile() {
        String selected = profileComboBox.getValue();
        if (selected == null || selected.isEmpty()) {
            showAlert("Uyarı", "Lütfen önce silinecek bir profil seçin.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Profili Sil");
        confirm.setHeaderText(selected + " profilini silmek istediğinize emin misiniz?");
        confirm.setContentText("Bu işlem geri alınamaz ve profile ait tüm veriler silinir.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                FileManager.deleteProfile(selected); 
                profileComboBox.getItems().remove(selected); 
                profileComboBox.setValue(null);
                currentProfile = null;
                updateUI(); 
            }
        });
    }

    private void deleteSelectedTransaction(Transaction selected) {
        if (selected != null && currentProfile != null) {
            currentProfile.removeTransaction(selected);
            FileManager.rewriteProfileData(currentProfile);
            updateUI(); 
        }
    }

    private void loadProfileData(String profileName) {
        if (profileName == null || profileName.trim().isEmpty()) return;
        currentProfile = FileManager.loadProfile(profileName);
        updateUI();
    }

    private void updateUI() {
        if (currentProfile == null) {
            balanceLabel.setText("Aktif Bakiye: 0.0 ₺");
            historyTable.getItems().clear();
            incomeListView.getItems().clear();
            expenseListView.getItems().clear();
            pieChart.getData().clear();
            return;
        }

        double[] totals = currentProfile.calculateTotals();
        double currentBalance = currentProfile.getCurrentBalance();
        balanceLabel.setText("Aktif Bakiye: " + currentBalance + " ₺");

        ObservableList<Transaction> tableData = FXCollections.observableArrayList(currentProfile.getTransactions());
        historyTable.setItems(tableData);

        ObservableList<Transaction> incomeTransactions = FXCollections.observableArrayList(
            currentProfile.getTransactions().stream()
                .filter(t -> t.getType().equals("INCOME"))
                .collect(Collectors.toList())
        );
        incomeListView.setItems(incomeTransactions);

        ObservableList<Transaction> expenseTransactions = FXCollections.observableArrayList(
            currentProfile.getTransactions().stream()
                .filter(t -> t.getType().equals("EXPENSE"))
                .collect(Collectors.toList())
        );
        expenseListView.setItems(expenseTransactions);

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        LocalDate today = LocalDate.now();

        java.util.List<Transaction> orderedForChart = new java.util.ArrayList<>();

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
        
        pieChart.setData(pieChartData);

        int index = 0;
        for (Transaction t : orderedForChart) {
            Node sliceNode = pieChartData.get(index).getNode();
            if (sliceNode != null) {
                if (t.getType().equals("INCOME")) {
                    sliceNode.setStyle("-fx-pie-color: #4CAF50;");
                } else {
                    sliceNode.setStyle("-fx-pie-color: #F44336;");
                }
            }
            index++;
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}