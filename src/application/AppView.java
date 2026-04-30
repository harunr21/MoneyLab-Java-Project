package application;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import java.time.LocalDate;

public class AppView {
    private MainApp mainApp;
    private Scene scene;
    private BorderPane root;

    public Label balanceLabel;
    public PieChart pieChart;
    public ComboBox<String> profileComboBox;
    public TableView<Transaction> historyTable;
    public ListView<Transaction> incomeListView;
    public ListView<Transaction> expenseListView;
    
    // Hedefler UI Bileşenleri
    public ListView<Goal> goalsListView;
    private VBox goalsLeftBox;
    private VBox goalsFormBox;
    private VBox goalsButtonsBox;
    private ToggleGroup goalCalcToggleGroup;
    
    // Bütçe Yönetimi UI Bileşenleri
    private VBox leftBox;
    private HBox listsBox;
    private VBox statsBox;
    private VBox squareButtonsBox;
    private VBox formBox;

    private String activeTransactionType = "";
    private TextField descField;
    private TextField amountField;
    private DatePicker datePicker;
    private ComboBox<String> freqBox;

    public AppView(MainApp mainApp) {
        this.mainApp = mainApp;
        buildUI();
    }

    private void buildUI() {
        root = new BorderPane();

        // --- ÜST PANEL ---
        HBox topBox = new HBox(10);
        topBox.setPadding(new Insets(10));
        
        profileComboBox = new ComboBox<>();
        profileComboBox.getItems().addAll(FileManager.loadProfileList());
        profileComboBox.setPromptText("Profil Seçin");
        
        Button loadBtn = new Button("Yükle");
        loadBtn.setOnAction(e -> mainApp.loadProfileData(profileComboBox.getValue()));

        Button addProfileBtn = new Button("Yeni Profil");
        addProfileBtn.setOnAction(e -> mainApp.createNewProfile());

        Button deleteProfileBtn = new Button("Profili Sil");
        deleteProfileBtn.setOnAction(e -> mainApp.deleteSelectedProfile(profileComboBox.getValue()));

        Button budgetBtn = new Button("Bütçe Yönetimi");
        budgetBtn.setStyle("-fx-base: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;"); 
        budgetBtn.setOnAction(e -> toggleBudgetMode());

        Button goalsBtn = new Button("Hedefler");
        goalsBtn.setStyle("-fx-base: #FF9800; -fx-text-fill: white; -fx-font-weight: bold;");
        goalsBtn.setOnAction(e -> toggleGoalsMode());

        topBox.getChildren().addAll(new Label("Profil:"), profileComboBox, loadBtn, addProfileBtn, deleteProfileBtn, new Separator(javafx.geometry.Orientation.VERTICAL), budgetBtn, goalsBtn);

        // --- SOL PANEL (Bütçe Ekleme) ---
        leftBox = new VBox(15);
        leftBox.setPadding(new Insets(10));
        leftBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 0 1 0 0;");
        leftBox.setPrefWidth(250);
        leftBox.setAlignment(Pos.TOP_CENTER); 

        squareButtonsBox = new VBox(20);
        squareButtonsBox.setAlignment(Pos.CENTER);

        Button addIncomeBtn = new Button("+\nGelir Ekle");
        addIncomeBtn.setPrefSize(150, 150); 
        addIncomeBtn.setTextAlignment(TextAlignment.CENTER);
        addIncomeBtn.setStyle("-fx-base: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");

        Button addExpenseBtn = new Button("+\nGider Ekle");
        addExpenseBtn.setPrefSize(150, 150); 
        addExpenseBtn.setTextAlignment(TextAlignment.CENTER);
        addExpenseBtn.setStyle("-fx-base: #F44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");

        squareButtonsBox.getChildren().addAll(addIncomeBtn, addExpenseBtn);

        formBox = new VBox(10);
        Label formTitle = new Label("Yeni İşlem");
        formTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        descField = new TextField(); descField.setPromptText("Açıklama");
        amountField = new TextField(); amountField.setPromptText("Miktar (₺)");
        datePicker = new DatePicker(); datePicker.setPromptText("Tarih Seçin");
        freqBox = new ComboBox<>(); freqBox.getItems().addAll("Tek Seferlik", "Günlük", "Aylık");
        freqBox.setValue("Tek Seferlik");

        HBox actionBtnsBox = new HBox(10);
        Button saveBtn = new Button("Kaydet");
        saveBtn.setStyle("-fx-base: #2196F3; -fx-text-fill: white;");
        Button cancelBtn = new Button("İptal");
        actionBtnsBox.getChildren().addAll(saveBtn, cancelBtn);

        formBox.getChildren().addAll(formTitle, descField, amountField, datePicker, freqBox, actionBtnsBox);

        javafx.event.EventHandler<javafx.event.ActionEvent> openFormHandler = e -> {
            if (!mainApp.isProfileLoaded()) {
                mainApp.showAlert("Hata", "Lütfen önce profil seçin!");
                return;
            }
            activeTransactionType = (e.getSource() == addIncomeBtn) ? "Gelir" : "Gider";
            formTitle.setText("Yeni " + activeTransactionType + " Ekle");
            leftBox.getChildren().remove(squareButtonsBox);
            leftBox.getChildren().add(0, formBox);
        };

        addIncomeBtn.setOnAction(openFormHandler);
        addExpenseBtn.setOnAction(openFormHandler);
        cancelBtn.setOnAction(e -> resetForm());

        saveBtn.setOnAction(e -> {
            try {
                Transaction newTx = activeTransactionType.equals("Gelir") 
                    ? new Income(descField.getText(), Double.parseDouble(amountField.getText()), datePicker.getValue(), freqBox.getValue()) 
                    : new Expense(descField.getText(), Double.parseDouble(amountField.getText()), datePicker.getValue(), freqBox.getValue());
                mainApp.addTransaction(newTx);
                resetForm();
            } catch (Exception ex) { mainApp.showAlert("Hata", "Verileri doğru formatta girin."); }
        });

        leftBox.getChildren().add(squareButtonsBox);

        // --- ORTA PANEL (Bütçe İstatistikleri) ---
        pieChart = new PieChart();
        pieChart.setTitle("Gerçekleşen Gelir/Gider Dağılımı");
        pieChart.setPrefHeight(250);

        incomeListView = new ListView<>();
        expenseListView = new ListView<>();
        setupListViews();

        VBox incomeBox = new VBox(5, new Label("Gelirler (Sağ tıkla sil)"), incomeListView);
        VBox expenseBox = new VBox(5, new Label("Giderler (Sağ tıkla sil)"), expenseListView);
        HBox.setHgrow(incomeBox, Priority.ALWAYS);
        HBox.setHgrow(expenseBox, Priority.ALWAYS);
        
        listsBox = new HBox(15, incomeBox, expenseBox);
        listsBox.setPrefHeight(250);

        statsBox = new VBox(15);
        statsBox.setPadding(new Insets(10));
        balanceLabel = new Label("Aktif Bakiye: 0.0 ₺");
        balanceLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        statsBox.getChildren().addAll(balanceLabel, pieChart);

        // --- HEDEFLER ARAYÜZÜNÜ HAZIRLA ---
        buildGoalsUI();

        // --- ALT PANEL (İşlem Geçmişi) ---
        VBox bottomBox = new VBox(5);
        bottomBox.setPadding(new Insets(10));
        Label tableLabel = new Label("İşlem Geçmişi (Döngüler Dahil)");
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

        // Ana Yerleşim (Varsayılan olarak Bütçe Yönetimi)
        root.setTop(topBox);
        root.setCenter(statsBox);
        root.setLeft(leftBox);
        root.setBottom(bottomBox);

        scene = new Scene(root, 900, 750);
    }

    private void buildGoalsUI() {
        // Sol Panel Kontrolleri
        goalsLeftBox = new VBox(15);
        goalsLeftBox.setPadding(new Insets(10));
        goalsLeftBox.setStyle("-fx-border-color: lightgray; -fx-border-width: 0 1 0 0;");
        goalsLeftBox.setPrefWidth(250);
        goalsLeftBox.setAlignment(Pos.TOP_CENTER);

        goalsButtonsBox = new VBox(20);
        goalsButtonsBox.setAlignment(Pos.CENTER);
        Button btnOpenGoalForm = new Button("+\nHedef Ekle");
        btnOpenGoalForm.setPrefSize(150, 150);
        btnOpenGoalForm.setTextAlignment(TextAlignment.CENTER);
        btnOpenGoalForm.setStyle("-fx-base: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        
        Button btnDebtPlan = new Button("Borç\nPlanla"); 
        btnDebtPlan.setPrefSize(150, 150);
        btnDebtPlan.setTextAlignment(TextAlignment.CENTER);
        btnDebtPlan.setDisable(true); // Şimdilik işlevsiz

        goalsButtonsBox.getChildren().addAll(btnOpenGoalForm, btnDebtPlan);

        // Hedef Ekleme Formu
        goalsFormBox = new VBox(10);
        Label formTitle = new Label("Yeni Hedef Belirle");
        formTitle.setStyle("-fx-font-weight: bold;");

        TextField nameField = new TextField(); nameField.setPromptText("Hedef Adı (Örn: Araba)");
        TextField amountField = new TextField(); amountField.setPromptText("Hedef Tutar (₺)");

        goalCalcToggleGroup = new ToggleGroup();
        RadioButton rbDate = new RadioButton("Tarih olarak hedefle");
        rbDate.setToggleGroup(goalCalcToggleGroup);
        rbDate.setSelected(true);
        RadioButton rbBudget = new RadioButton("Bütçe olarak hedefle");
        rbBudget.setToggleGroup(goalCalcToggleGroup);

        DatePicker targetDatePicker = new DatePicker();
        targetDatePicker.setPromptText("Hedef Ay/Yıl");
        
        TextField monthlySavingField = new TextField();
        monthlySavingField.setPromptText("Aylık Biriktirilecek Tutar");
        monthlySavingField.setDisable(true); // Varsayılan tarih seçili olduğu için

        rbDate.setOnAction(e -> { targetDatePicker.setDisable(false); monthlySavingField.setDisable(true); });
        rbBudget.setOnAction(e -> { targetDatePicker.setDisable(true); monthlySavingField.setDisable(false); });

        HBox goalActionBtns = new HBox(10);
        Button btnSaveGoal = new Button("Kaydet");
        btnSaveGoal.setStyle("-fx-base: #2196F3; -fx-text-fill: white;");
        Button btnCancelGoal = new Button("İptal");
        goalActionBtns.getChildren().addAll(btnSaveGoal, btnCancelGoal);

        goalsFormBox.getChildren().addAll(formTitle, nameField, amountField, rbDate, targetDatePicker, rbBudget, monthlySavingField, goalActionBtns);

        btnOpenGoalForm.setOnAction(e -> {
            goalsLeftBox.getChildren().remove(goalsButtonsBox);
            goalsLeftBox.getChildren().add(0, goalsFormBox);
        });

        btnCancelGoal.setOnAction(e -> {
            nameField.clear(); amountField.clear(); targetDatePicker.setValue(null); monthlySavingField.clear();
            goalsLeftBox.getChildren().remove(goalsFormBox);
            goalsLeftBox.getChildren().add(0, goalsButtonsBox);
        });

        btnSaveGoal.setOnAction(e -> {
            try {
                String name = nameField.getText();
                double total = Double.parseDouble(amountField.getText());
                if (rbDate.isSelected()) {
                    mainApp.addNewGoalByDate(name, total, targetDatePicker.getValue());
                } else {
                    mainApp.addNewGoalByBudget(name, total, Double.parseDouble(monthlySavingField.getText()));
                }
                nameField.clear(); amountField.clear(); targetDatePicker.setValue(null); monthlySavingField.clear();
                goalsLeftBox.getChildren().remove(goalsFormBox);
                goalsLeftBox.getChildren().add(0, goalsButtonsBox);
            } catch (Exception ex) { mainApp.showAlert("Hata", "Lütfen tüm alanları doğru doldurun."); }
        });

        goalsLeftBox.getChildren().add(goalsButtonsBox);

        // Sağ Liste Görünümü (Hedefler Modunda Ortada Gösterilecek)
        goalsListView = new ListView<>();
        goalsListView.setPadding(new Insets(10));
        goalsListView.setCellFactory(param -> new ListCell<Goal>() {
            @Override
            protected void updateItem(Goal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else {
                    setText(item.getName() + " - Biriken: " + item.getSavedAmount() + " ₺ / Hedef: " + item.getTargetAmount() + " ₺");
                }
            }
        });

        ContextMenu goalMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Hedefi Sil");
        deleteItem.setOnAction(e -> mainApp.removeGoal(goalsListView.getSelectionModel().getSelectedItem()));
        goalMenu.getItems().add(deleteItem);
        goalsListView.setContextMenu(goalMenu);
    }

    private void setupListViews() {
        incomeListView.setCellFactory(param -> new ListCell<Transaction>() {
            @Override
            protected void updateItem(Transaction item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getDescription() + " (" + item.getAmount() + " ₺)");
            }
        });

        expenseListView.setCellFactory(param -> new ListCell<Transaction>() {
            @Override
            protected void updateItem(Transaction item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getDescription() + " (" + item.getAmount() + " ₺)");
            }
        });

        ContextMenu incomeMenu = new ContextMenu();
        MenuItem deleteIncome = new MenuItem("Geliri Sil");
        deleteIncome.setOnAction(e -> mainApp.deleteTransaction(incomeListView.getSelectionModel().getSelectedItem()));
        incomeMenu.getItems().add(deleteIncome);
        incomeListView.setContextMenu(incomeMenu);

        ContextMenu expenseMenu = new ContextMenu();
        MenuItem deleteExpense = new MenuItem("Gideri Sil");
        deleteExpense.setOnAction(e -> mainApp.deleteTransaction(expenseListView.getSelectionModel().getSelectedItem()));
        expenseMenu.getItems().add(deleteExpense);
        expenseListView.setContextMenu(expenseMenu);
    }

    private void toggleBudgetMode() {
        if (!mainApp.isProfileLoaded()) {
            mainApp.showAlert("Hata", "Lütfen önce profil seçin!");
            return;
        }
        root.setCenter(statsBox);
        root.setLeft(leftBox);
    }

    private void toggleGoalsMode() {
        if (!mainApp.isProfileLoaded()) {
            mainApp.showAlert("Hata", "Lütfen önce profil seçin!");
            return;
        }
        root.setCenter(goalsListView);
        root.setLeft(goalsLeftBox);
    }

    private void resetForm() {
        descField.clear(); amountField.clear(); datePicker.setValue(null); freqBox.setValue("Tek Seferlik");
        leftBox.getChildren().remove(formBox);
        leftBox.getChildren().add(0, squareButtonsBox);
    }

    public Scene getScene() { return scene; }
}