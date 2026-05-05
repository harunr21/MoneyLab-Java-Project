package com.moneylab.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import com.moneylab.model.*;
import com.moneylab.service.TransactionManager;
import com.moneylab.service.GoalManager;

import java.time.LocalDate;
import java.util.List;

public class DashboardView {
    private MainApp app;
    private User user;
    private Balance balance;
    private TransactionManager transactionManager;
    private ListView<String> incomeExpenseListView; // Aktif gelir/gider listesi
    private ListView<String> historyListView; // İşlem geçmişi listesi
    private Button calculateButton; // Hedef sayfasını yenilemek için
    private Tab targetTab; // Hedef sekmesinin aktif olup olmadığını kontrol etmek için
    private GoalManager goalManager;
    private ListView<String> goalListView; // Hedef listesi
    private List<Goal> userGoals; // Kullanıcının hedefleri (RAM)

    public DashboardView(MainApp app, User user) {
        this.app = app;
        this.user = user;
        this.balance = new Balance(); 
        
        // Veri kaydetme servisimizi başlatıyoruz
        this.transactionManager = new TransactionManager();
        this.goalManager = new GoalManager();
        this.incomeExpenseListView = new ListView<>();
        this.historyListView = new ListView<>();
        this.goalListView = new ListView<>();

        // Kullanıcı giriş yaptığında eski işlemlerini dosyadan yüklüyoruz
        List<Transaction> pastTransactions = transactionManager.loadUserTransactions(user.getId());
        for (Transaction t : pastTransactions) {
            balance.addTransaction(t);
        }
    }

    public Scene getScene() {
        BorderPane rootLayout = new BorderPane();
        rootLayout.setStyle("-fx-background-color: #f0f2f5;");

        // =========================================================================
        // 1. ÜST KISIM (Başlık ve Çıkış Butonu)
        // =========================================================================
        HBox topBox = new HBox(20);
        topBox.setPadding(new Insets(14, 24, 14, 24));
        topBox.setStyle("-fx-background-color: linear-gradient(to right, #1a73e8, #4285f4);");
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.setEffect(new DropShadow(8, Color.rgb(0, 0, 0, 0.15)));
        
        Label appIcon = new Label("M");
        appIcon.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1a73e8; -fx-background-color: white; -fx-background-radius: 6; -fx-min-width: 30; -fx-min-height: 30; -fx-alignment: center;");

        VBox userInfoBox = new VBox(2);
        Label welcomeLabel = new Label("Hoş geldin, " + user.getName() + "!");
        welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label appSubLabel = new Label("MoneyLab Kişisel Finans");
        appSubLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.75);");
        userInfoBox.getChildren().addAll(welcomeLabel, appSubLabel);

        // Boşluk doldurucu (Çıkış butonunu en sağa iter)
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logoutButton = new Button("Çıkış Yap");
        logoutButton.setStyle(
            "-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white; " +
            "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 8; " +
            "-fx-border-color: rgba(255,255,255,0.3); -fx-border-radius: 8; -fx-padding: 8 16;"
        );
        logoutButton.setOnAction(e -> app.showLogin());

        topBox.getChildren().addAll(appIcon, userInfoBox, spacer, logoutButton);
        rootLayout.setTop(topBox);

        // =========================================================================
        // 2. SEKME YAPISI (TabPane ile 3 Ayrı Sayfa)
        // =========================================================================
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE); // Sekmelerdeki çarpı (X) tuşunu gizler

        // Genel Etiketler (Güncelleme metodunda kullanılacaklar)
        Label balanceLabel = new Label();
        balanceLabel.setStyle("-fx-font-size: 28px; -fx-text-fill: #1a73e8; -fx-font-weight: bold;");
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Gelir ve Gider Dağılımı");
        pieChart.setStyle("-fx-font-size: 12px;");
        pieChart.setLabelsVisible(true);

        // --- İLK SEKME: GENEL VARLIKLARIM ---
        Tab assetsTab = new Tab("Genel Varlıklarım");
        VBox assetsLayout = new VBox(20);
        assetsLayout.setAlignment(Pos.CENTER);
        assetsLayout.setPadding(new Insets(30));
        assetsLayout.setStyle("-fx-background-color: #f0f2f5;");

        // Bakiye kartı
        VBox balanceCard = new VBox(8);
        balanceCard.setAlignment(Pos.CENTER);
        balanceCard.setPadding(new Insets(25, 40, 25, 40));
        balanceCard.setMaxWidth(450);
        balanceCard.setStyle(
            "-fx-background-color: white; -fx-background-radius: 12;"
        );
        balanceCard.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.08)));
        Label balanceTitle = new Label("Toplam Bakiye");
        balanceTitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #777;");
        balanceCard.getChildren().addAll(balanceTitle, balanceLabel);

        // Grafik kartı
        VBox chartCard = new VBox(0);
        chartCard.setAlignment(Pos.CENTER);
        chartCard.setPadding(new Insets(15));
        chartCard.setMaxWidth(500);
        chartCard.setStyle(
            "-fx-background-color: white; -fx-background-radius: 12;"
        );
        chartCard.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.08)));
        chartCard.getChildren().add(pieChart);

        assetsLayout.getChildren().addAll(balanceCard, chartCard);
        assetsTab.setContent(assetsLayout);

        // --- İKİNCİ SEKME: İŞLEMLERİM ---
        Tab transactionsTab = new Tab("Gelir / Gider İşlemleri");
        BorderPane transactionsLayout = new BorderPane();
        
        // İşlemlerim -> Sol Taraf: Modern Kart Tasarımlı Form
        VBox formBox = new VBox(0);
        formBox.setPrefWidth(280);
        formBox.setMinWidth(280);
        formBox.setStyle("-fx-background-color: linear-gradient(to bottom, #f8f9fc, #eef1f7);");

        // --- Üst Başlık Alanı ---
        VBox headerBox = new VBox(5);
        headerBox.setPadding(new Insets(20, 20, 15, 20));
        headerBox.setStyle("-fx-background-color: linear-gradient(to right, #1a73e8, #4285f4); -fx-background-radius: 0;");

        Label formTitle = new Label("Yeni İşlem Ekle");
        formTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label formSubTitle = new Label("Gelir veya gider türünü seçerek başlayın");
        formSubTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.85);");
        formSubTitle.setWrapText(true);

        headerBox.getChildren().addAll(formTitle, formSubTitle);

        // --- Gelir / Gider Seçim Butonları ---
        HBox typeButtonsBox = new HBox(10);
        typeButtonsBox.setPadding(new Insets(18, 20, 5, 20));
        typeButtonsBox.setAlignment(Pos.CENTER);

        Button addIncomeBtn = new Button("Gelir Ekle");
        addIncomeBtn.setPrefWidth(115);
        addIncomeBtn.setPrefHeight(36);
        addIncomeBtn.setStyle(
            "-fx-background-color: #34a853; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-cursor: hand; -fx-background-radius: 8; -fx-font-size: 12px;"
        );

        Button addExpenseBtn = new Button("Gider Ekle");
        addExpenseBtn.setPrefWidth(115);
        addExpenseBtn.setPrefHeight(36);
        addExpenseBtn.setStyle(
            "-fx-background-color: #ea4335; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-cursor: hand; -fx-background-radius: 8; -fx-font-size: 12px;"
        );

        typeButtonsBox.getChildren().addAll(addIncomeBtn, addExpenseBtn);

        // --- Bilgi mesajı (form görünmeden önce) ---
        VBox placeholderBox = new VBox(10);
        placeholderBox.setAlignment(Pos.CENTER);
        placeholderBox.setPadding(new Insets(30, 20, 30, 20));

        Label placeholderIcon = new Label("+ / -");
        placeholderIcon.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #bbb;");

        Label placeholderText = new Label("Yukarıdaki butonlardan birini\nseçerek işlem eklemeye başlayın.");
        placeholderText.setStyle("-fx-font-size: 12px; -fx-text-fill: #888; -fx-text-alignment: center;");
        placeholderText.setWrapText(true);

        placeholderBox.getChildren().addAll(placeholderIcon, placeholderText);

        // --- Form Alanları ---
        VBox formControls = new VBox(0);
        formControls.setVisible(false);
        formControls.setManaged(false);
        formControls.setPadding(new Insets(5, 20, 15, 20));

        // Sıklık ve Tarih Grubu
        VBox dateTimeGroup = new VBox(8);
        dateTimeGroup.setPadding(new Insets(12, 0, 12, 0));

        Label groupLabel1 = new Label("Zamanlama");
        groupLabel1.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #555;");

        ComboBox<String> frequencyBox = new ComboBox<>();
        frequencyBox.getItems().addAll("Tek Seferlik", "Haftalık", "Aylık");
        frequencyBox.setValue("Tek Seferlik");
        frequencyBox.setMaxWidth(Double.MAX_VALUE);
        frequencyBox.setStyle("-fx-font-size: 12px;");

        DatePicker datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now());
        datePicker.setMaxWidth(Double.MAX_VALUE);
        datePicker.setStyle("-fx-font-size: 12px;");

        dateTimeGroup.getChildren().addAll(groupLabel1, frequencyBox, datePicker);

        // Detay Grubu
        VBox detailGroup = new VBox(8);
        detailGroup.setPadding(new Insets(8, 0, 12, 0));

        Label groupLabel2 = new Label("İşlem Detayları");
        groupLabel2.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #555;");

        TextField amountInput = new TextField();
        amountInput.setPromptText("Miktar (Örn: 150)");
        amountInput.setStyle("-fx-font-size: 12px; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #ccc; -fx-padding: 8;");

        TextField descInput = new TextField();
        descInput.setPromptText("Açıklama (Örn: Market)");
        descInput.setStyle("-fx-font-size: 12px; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #ccc; -fx-padding: 8;");

        TextField sourceInput = new TextField();
        sourceInput.setPromptText("Kaynak (Örn: Maaş, Kira)");
        sourceInput.setStyle("-fx-font-size: 12px; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #ccc; -fx-padding: 8;");

        detailGroup.getChildren().addAll(groupLabel2, amountInput, descInput, sourceInput);

        // Kaydet Butonu
        Button addButton = new Button("Kaydet");
        addButton.setMaxWidth(Double.MAX_VALUE);
        addButton.setPrefHeight(38);
        addButton.setStyle(
            "-fx-background-color: linear-gradient(to right, #1a73e8, #4285f4); -fx-text-fill: white; " +
            "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 8; -fx-font-size: 13px;"
        );

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #ea4335; -fx-font-size: 11px; -fx-padding: 5 0 0 0;");
        errorLabel.setWrapText(true);

        // Ayırıcı çizgiler
        Separator sep1 = new Separator();
        sep1.setPadding(new Insets(2, 0, 2, 0));

        formControls.getChildren().addAll(
            dateTimeGroup, sep1, detailGroup, addButton, errorLabel
        );

        final String[] currentType = {""};

        addIncomeBtn.setOnAction(e -> {
            formControls.setVisible(true);
            formControls.setManaged(true);
            placeholderBox.setVisible(false);
            placeholderBox.setManaged(false);
            currentType[0] = "Gelir";
            formTitle.setText("Yeni Gelir Ekle");
            formSubTitle.setText("Gelir bilgilerini aşağıya girin");
            // Gelir seçiliyken buton stillerini güncelle
            addIncomeBtn.setStyle(
                "-fx-background-color: #2d8f47; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-cursor: hand; -fx-background-radius: 8; -fx-font-size: 12px; " +
                "-fx-border-color: #fff; -fx-border-width: 2; -fx-border-radius: 8;"
            );
            addExpenseBtn.setStyle(
                "-fx-background-color: #ea4335; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-cursor: hand; -fx-background-radius: 8; -fx-font-size: 12px;"
            );
        });

        addExpenseBtn.setOnAction(e -> {
            formControls.setVisible(true);
            formControls.setManaged(true);
            placeholderBox.setVisible(false);
            placeholderBox.setManaged(false);
            currentType[0] = "Gider";
            formTitle.setText("Yeni Gider Ekle");
            formSubTitle.setText("Gider bilgilerini aşağıya girin");
            // Gider seçiliyken buton stillerini güncelle
            addExpenseBtn.setStyle(
                "-fx-background-color: #c5221f; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-cursor: hand; -fx-background-radius: 8; -fx-font-size: 12px; " +
                "-fx-border-color: #fff; -fx-border-width: 2; -fx-border-radius: 8;"
            );
            addIncomeBtn.setStyle(
                "-fx-background-color: #34a853; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-cursor: hand; -fx-background-radius: 8; -fx-font-size: 12px;"
            );
        });

        // ScrollPane ile küçük ekranlarda da kaydırılabilir olması
        ScrollPane formScroll = new ScrollPane(new VBox(typeButtonsBox, placeholderBox, formControls));
        formScroll.setFitToWidth(true);
        formScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        formScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(formScroll, Priority.ALWAYS);

        formBox.getChildren().addAll(headerBox, formScroll);
        transactionsLayout.setLeft(formBox);

        // İşlemlerim -> Sağ Taraf: İki Liste (Gelir-Gider ve İşlem Geçmişi)
        HBox listsBox = new HBox(16);
        listsBox.setPadding(new Insets(16));
        listsBox.setStyle("-fx-background-color: #f0f2f5;");

        VBox activeBox = new VBox(0);
        HBox.setHgrow(activeBox, Priority.ALWAYS);
        activeBox.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        activeBox.setEffect(new DropShadow(8, Color.rgb(0, 0, 0, 0.06)));

        HBox activeTitleBar = new HBox();
        activeTitleBar.setPadding(new Insets(12, 16, 12, 16));
        activeTitleBar.setStyle("-fx-background-color: linear-gradient(to right, #1a73e8, #4285f4); -fx-background-radius: 12 12 0 0;");
        Label activeTitle = new Label("Gelir / Gider Listesi");
        activeTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
        activeTitleBar.getChildren().add(activeTitle);

        incomeExpenseListView.setStyle("-fx-background-radius: 0; -fx-border-width: 0;");
        VBox.setVgrow(incomeExpenseListView, Priority.ALWAYS);
        
        Button deleteButton = new Button("Seçili İşlemi Sil");
        deleteButton.setMaxWidth(Double.MAX_VALUE);
        deleteButton.setPrefHeight(36);
        deleteButton.setStyle(
            "-fx-background-color: #ea4335; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-cursor: hand; -fx-background-radius: 0 0 12 12; -fx-font-size: 12px;"
        );
        activeBox.getChildren().addAll(activeTitleBar, incomeExpenseListView, deleteButton);

        VBox historyBox = new VBox(0);
        HBox.setHgrow(historyBox, Priority.ALWAYS);
        historyBox.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        historyBox.setEffect(new DropShadow(8, Color.rgb(0, 0, 0, 0.06)));

        HBox historyTitleBar = new HBox();
        historyTitleBar.setPadding(new Insets(12, 16, 12, 16));
        historyTitleBar.setStyle("-fx-background-color: linear-gradient(to right, #5f6368, #80868b); -fx-background-radius: 12 12 0 0;");
        Label historyTitle = new Label("İşlem Geçmişi");
        historyTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
        historyTitleBar.getChildren().add(historyTitle);

        historyListView.setStyle("-fx-background-radius: 0 0 12 12; -fx-border-width: 0;");
        VBox.setVgrow(historyListView, Priority.ALWAYS);
        historyBox.getChildren().addAll(historyTitleBar, historyListView);

        listsBox.getChildren().addAll(activeBox, historyBox);
        transactionsLayout.setCenter(listsBox);

        transactionsTab.setContent(transactionsLayout);

        // --- ÜÇÜNCÜ SEKME: HEDEFLERİM ---
        targetTab = new Tab("Hedef");
        BorderPane targetMainLayout = new BorderPane();
        targetMainLayout.setStyle("-fx-background-color: #f0f2f5;");

        // ===== SOL KISIM: Yeni Hedef Ekleme Formu =====
        VBox goalFormBox = new VBox(0);
        goalFormBox.setPrefWidth(280);
        goalFormBox.setMinWidth(280);
        goalFormBox.setStyle("-fx-background-color: linear-gradient(to bottom, #f8f9fc, #eef1f7);");

        // Form Başlık Alanı
        VBox goalHeaderBox = new VBox(5);
        goalHeaderBox.setPadding(new Insets(20, 20, 15, 20));
        goalHeaderBox.setStyle("-fx-background-color: linear-gradient(to right, #1a73e8, #4285f4); -fx-background-radius: 0;");
        Label goalFormTitle = new Label("Yeni Hedef Ekle");
        goalFormTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label goalFormSubTitle = new Label("Hedef adı ve tutarı belirleyin");
        goalFormSubTitle.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.85);");
        goalHeaderBox.getChildren().addAll(goalFormTitle, goalFormSubTitle);

        // Form Alanları
        VBox goalFormFields = new VBox(12);
        goalFormFields.setPadding(new Insets(20));

        Label nameLabel = new Label("Hedef Adı");
        nameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #555;");
        TextField goalNameInput = new TextField();
        goalNameInput.setPromptText("Örn: Araba, Tatil, Bilgisayar");
        goalNameInput.setStyle("-fx-font-size: 12px; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #ccc; -fx-padding: 8;");

        Label amountLabel = new Label("Hedef Tutarı (TL)");
        amountLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #555;");
        TextField goalAmountInput = new TextField();
        goalAmountInput.setPromptText("Örn: 50000");
        goalAmountInput.setStyle("-fx-font-size: 12px; -fx-background-radius: 6; -fx-border-radius: 6; -fx-border-color: #ccc; -fx-padding: 8;");

        Button addGoalBtn = new Button("Hedef Ekle");
        addGoalBtn.setMaxWidth(Double.MAX_VALUE);
        addGoalBtn.setPrefHeight(38);
        addGoalBtn.setStyle(
            "-fx-background-color: linear-gradient(to right, #1a73e8, #4285f4); -fx-text-fill: white; " +
            "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 8; -fx-font-size: 13px;"
        );

        Label goalErrorLabel = new Label();
        goalErrorLabel.setStyle("-fx-text-fill: #ea4335; -fx-font-size: 11px;");
        goalErrorLabel.setWrapText(true);

        goalFormFields.getChildren().addAll(nameLabel, goalNameInput, amountLabel, goalAmountInput, addGoalBtn, goalErrorLabel);

        goalFormBox.getChildren().addAll(goalHeaderBox, goalFormFields);
        targetMainLayout.setLeft(goalFormBox);

        // ===== SAĞ KISIM: Hedef Listesi ve Hesaplama =====
        VBox rightSection = new VBox(16);
        rightSection.setPadding(new Insets(16));
        rightSection.setStyle("-fx-background-color: #f0f2f5;");

        // Hedef Listesi Kartı
        VBox goalListCard = new VBox(0);
        VBox.setVgrow(goalListCard, Priority.ALWAYS);
        goalListCard.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        goalListCard.setEffect(new DropShadow(8, Color.rgb(0, 0, 0, 0.06)));

        HBox goalListTitleBar = new HBox();
        goalListTitleBar.setPadding(new Insets(12, 16, 12, 16));
        goalListTitleBar.setStyle("-fx-background-color: linear-gradient(to right, #1a73e8, #4285f4); -fx-background-radius: 12 12 0 0;");
        Label goalListTitle = new Label("Hedeflerim");
        goalListTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
        goalListTitleBar.getChildren().add(goalListTitle);

        goalListView.setStyle("-fx-background-radius: 0; -fx-border-width: 0;");
        VBox.setVgrow(goalListView, Priority.ALWAYS);

        Button deleteGoalBtn = new Button("Seçili Hedefi Sil");
        deleteGoalBtn.setMaxWidth(Double.MAX_VALUE);
        deleteGoalBtn.setPrefHeight(36);
        deleteGoalBtn.setStyle(
            "-fx-background-color: #ea4335; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-cursor: hand; -fx-background-radius: 0 0 12 12; -fx-font-size: 12px;"
        );

        goalListCard.getChildren().addAll(goalListTitleBar, goalListView, deleteGoalBtn);

        // Hesaplama Kartı
        VBox calcCard = new VBox(12);
        calcCard.setAlignment(Pos.CENTER);
        calcCard.setPadding(new Insets(16, 20, 16, 20));
        calcCard.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        calcCard.setEffect(new DropShadow(8, Color.rgb(0, 0, 0, 0.06)));

        Label savingsTitle = new Label("Gelecekteki Bakiyeni Hesapla");
        savingsTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333;");

        HBox controlBox = new HBox(10);
        controlBox.setAlignment(Pos.CENTER);
        Label monthLabel = new Label("Kaç ay sonrası?");
        monthLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");
        ComboBox<Integer> monthBox = new ComboBox<>();
        monthBox.getItems().addAll(1, 3, 6, 12, 24, 36, 60);
        monthBox.setValue(1);
        monthBox.setStyle("-fx-font-size: 12px;");
        
        calculateButton = new Button("Hesapla");
        calculateButton.setStyle(
            "-fx-background-color: linear-gradient(to right, #1a73e8, #4285f4); -fx-text-fill: white; " +
            "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 8; -fx-padding: 8 16;"
        );
        controlBox.getChildren().addAll(monthLabel, monthBox, calculateButton);

        Label projectedBalanceLabel = new Label("Hesaplanıyor...");
        projectedBalanceLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #1a73e8; -fx-font-weight: bold;");

        calcCard.getChildren().addAll(savingsTitle, controlBox, projectedBalanceLabel);

        rightSection.getChildren().addAll(goalListCard, calcCard);
        targetMainLayout.setCenter(rightSection);
        targetTab.setContent(targetMainLayout);

        // Hedefleri dosyadan yükle ve listeyi doldur
        userGoals = goalManager.loadUserGoals(user.getId());
        refreshGoalList();

        // Hedef ekleme mantığı
        addGoalBtn.setOnAction(e -> {
            try {
                goalErrorLabel.setText("");
                String gName = goalNameInput.getText();
                double gAmount = Double.parseDouble(goalAmountInput.getText());

                Goal newGoal = new Goal(gName, gAmount);
                goalManager.saveGoal(user.getId(), newGoal);
                userGoals.add(newGoal);
                refreshGoalList();

                goalNameInput.clear();
                goalAmountInput.clear();
            } catch (NumberFormatException ex) {
                goalErrorLabel.setText("Lütfen geçerli bir tutar girin!");
            } catch (IllegalArgumentException ex) {
                goalErrorLabel.setText(ex.getMessage());
            }
        });

        // Hedef silme mantığı
        deleteGoalBtn.setOnAction(e -> {
            int selectedIndex = goalListView.getSelectionModel().getSelectedIndex();
            if (selectedIndex >= 0 && selectedIndex < userGoals.size()) {
                userGoals.remove(selectedIndex);
                goalManager.rewriteUserGoals(user.getId(), userGoals);
                refreshGoalList();
            }
        });
        
        // Gelecekteki bakiyeyi hesaplama mantığı
        calculateButton.setOnAction(e -> {
            int monthsAhead = monthBox.getValue();
            LocalDate targetDate = LocalDate.now().plusMonths(monthsAhead);
            
            // Hedef tarihteki toplam bakiye
            double projectedTotal = balance.getTotalBalance(targetDate);
            projectedBalanceLabel.setText(monthsAhead + " Ay Sonraki Bakiye: " + String.format("%.2f", projectedTotal) + " TL");
        });

        // Sayfa açıldığında ilk hesaplamayı varsayılan ay değeriyle (1 ay) çalıştırıyoruz
        calculateButton.fire();

        // Sekmeleri TabPane'e ekle
        tabPane.getTabs().addAll(assetsTab, transactionsTab, targetTab);

        // Hedef sekmesine her tıklandığında hedef listesini ve bakiye hesabını güncelle
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == targetTab) {
                refreshGoalList();
                calculateButton.fire();
            }
        });

        rootLayout.setCenter(tabPane);

        // =========================================================================
        // 3. İŞLEM EKLEME MANTIĞI VE KAYDETME
        // =========================================================================
        addButton.setOnAction(e -> {
            try {
                errorLabel.setText(""); // Hata mesajını temizle
                
                if (currentType[0].isEmpty()) {
                    throw new Exception("Lütfen önce Gelir veya Gider seçin.");
                }

                double amount = Double.parseDouble(amountInput.getText());
                String desc = descInput.getText();
                String type = currentType[0];

                if(desc == null || desc.trim().isEmpty()) {
                    throw new IllegalArgumentException("Açıklama boş olamaz.");
                }
                
                // Dosya formatı virgül tabanlı olduğu için kullanıcının açıklamaya virgül girmesini engelliyoruz
                if(desc.contains(",")) {
                    throw new IllegalArgumentException("Açıklamada virgül (,) kullanılamaz.");
                }

                // Kaynak alanını formdan alıyoruz, boşsa varsayılan değer veriyoruz
                String source = sourceInput.getText();
                if (source == null || source.trim().isEmpty()) {
                    source = "Belirtilmedi";
                }
                if (source.contains(",")) {
                    throw new IllegalArgumentException("Kaynak alanında virgül (,) kullanılamaz.");
                }

                // Sıklık (Frequency) seçimi string olduğu için, modelimizdeki ENUM yapısına dönüştürüyoruz.
                String freqStr = frequencyBox.getValue();
                Frequency frequency = Frequency.ONCE;
                if (freqStr.equals("Haftalık")) frequency = Frequency.WEEKLY;
                else if (freqStr.equals("Aylık")) frequency = Frequency.MONTHLY;

                // Ekranda seçilen takvim tarihini alıyoruz, eğer seçilmediyse güvenli olarak bugünü atıyoruz.
                LocalDate selectedDate = datePicker.getValue();
                if (selectedDate == null) {
                    selectedDate = LocalDate.now();
                }

                Transaction transaction;
                if (type.equals("Gelir")) {
                    transaction = new Income(amount, desc, selectedDate, frequency, source);
                } else {
                    transaction = new Expense(amount, desc, selectedDate, frequency, source);
                }

                // 1. İşlemi anlık bakiyeye (RAM'e) ekle
                balance.addTransaction(transaction);
                
                // 2. İşlemi dosyaya (txt) kaydet (Kalıcı hale getir)
                transactionManager.saveTransaction(user.getId(), transaction);
                
                // 3. Tarihçeye kaydet
                transactionManager.saveToHistory(user.getId(), transaction);
                
                // 4. Ekrandaki grafiği ve listeyi hemen güncelle
                updateDashboard(balanceLabel, pieChart, tabPane);

                amountInput.clear();
                descInput.clear();
                sourceInput.clear();

            } catch (NumberFormatException ex) {
                errorLabel.setText("Lütfen sayısal bir miktar girin!");
            } catch (Exception ex) {
                errorLabel.setText(ex.getMessage());
            }
        });

        // =========================================================================
        // 4. İŞLEM SİLME MANTIĞI
        // =========================================================================
        deleteButton.setOnAction(e -> {
            // Kullanıcının ListView'da kaçıncı satırı seçtiğini buluyoruz
            int selectedIndex = incomeExpenseListView.getSelectionModel().getSelectedIndex();
            
            if (selectedIndex >= 0) {
                List<Transaction> baseList = balance.getTransactions();
                
                if (selectedIndex < baseList.size()) {
                    Transaction toRemove = baseList.get(selectedIndex);
                    
                    // 1. Orijinal işlemi bakiyeden (RAM'den) sil
                    balance.removeTransaction(toRemove);
                    
                    // 2. Dosyayı (transactions.txt) o işlem olmadan baştan yaz
                    transactionManager.rewriteUserTransactions(user.getId(), balance.getTransactions());
                    
                    // 3. Ekranı güncelle (İşlem geçmişine dokunulmaz)
                    updateDashboard(balanceLabel, pieChart, tabPane);
                }
            }
        });

        // Ekran açılırken grafikleri ve geçmiş listesini ilk kez dolduruyoruz
        updateDashboard(balanceLabel, pieChart, tabPane);

        return new Scene(rootLayout, 950, 600);
    }

    // Ekrandaki bakiyeyi, pasta grafiğini ve geçmiş listesini güncelleyen metod
    private void updateDashboard(Label balanceLabel, PieChart pieChart, TabPane tabPane) {
        
        balanceLabel.setText("Mevcut Bakiye: " + balance.getTotalBalance() + " TL");

        // Gelir/Gider toplamları için çoğaltılmış (expanded) işlemleri kullanıyoruz
        // Bu sayede tekrarlanan işlemler (aylık, haftalık vb.) toplama doğru yansır
        double totalIncome = 0;
        double totalExpense = 0;
        List<Transaction> expanded = balance.getExpandedTransactions();
        for (Transaction t : expanded) {
            if (t instanceof Income) {
                totalIncome += t.getAmount();
            } else {
                totalExpense += t.getAmount();
            }
        }

        // Aktif Gelir/Gider listesini güncelle (Sadece base işlemleri gösteriyoruz, silme yapılabilmesi için)
        incomeExpenseListView.getItems().clear();
        List<Transaction> baseList = balance.getTransactions();

        for (Transaction t : baseList) {
            String freqDisplay = "";
            if (t.getFrequency() == Frequency.WEEKLY) freqDisplay = " [Haftalık]";
            else if (t.getFrequency() == Frequency.MONTHLY) freqDisplay = " [Aylık]";

            String sourceDisplay = "";
            if (t.getSource() != null && !t.getSource().isEmpty()) {
                sourceDisplay = " - " + t.getSource();
            }

            if (t instanceof Income) {
                incomeExpenseListView.getItems().add("[+] " + t.getAmount() + " TL - " + t.getDescription() + freqDisplay + sourceDisplay + " (" + t.getDate() + ")");
            } else if (t instanceof Expense) {
                incomeExpenseListView.getItems().add("[-] " + t.getAmount() + " TL - " + t.getDescription() + freqDisplay + sourceDisplay + " (" + t.getDate() + ")");
            }
        }

        // İşlem geçmişi listesini güncelle
        historyListView.getItems().clear();
        List<Transaction> historyList = transactionManager.loadUserHistory(user.getId());
        for (Transaction t : historyList) {
            String typeStr = (t instanceof Income) ? "Gelir Eklendi: " : "Gider Eklendi: ";
            String prefix = (t instanceof Income) ? "[+]" : "[-]";
            historyListView.getItems().add(prefix + " " + typeStr + t.getAmount() + " TL - " + t.getDescription() + " (" + t.getDate() + ")");
        }

        // Grafik verilerini güncelle
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        if (totalIncome > 0) {
            pieChartData.add(new PieChart.Data("Gelirler (" + totalIncome + " TL)", totalIncome));
        }
        if (totalExpense > 0) {
            pieChartData.add(new PieChart.Data("Giderler (" + totalExpense + " TL)", totalExpense));
        }

        pieChart.setData(pieChartData);

        // Yeşil ve Kırmızı renkleri koru
        for (PieChart.Data data : pieChart.getData()) {
            if (data.getName().startsWith("Gelirler")) {
                data.getNode().setStyle("-fx-pie-color: #4CAF50;");
            } else if (data.getName().startsWith("Giderler")) {
                data.getNode().setStyle("-fx-pie-color: #F44336;");
            }
        }

        // Hedef sayfasını SADECE o sekme açıksa güncelle
        if (calculateButton != null && tabPane != null) {
            Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
            if (selectedTab == targetTab) {
                calculateButton.fire();
            }
        }
    }

    /**
     * Hedef listesini (goalListView) güncel verilerle yeniden doldurur.
     * Her hedef için mevcut bakiyeye göre ilerleme durumunu hesaplar ve gösterir.
     */
    private void refreshGoalList() {
        goalListView.getItems().clear();
        double currentBalance = balance.getTotalBalance();

        for (Goal goal : userGoals) {
            double progress = (goal.getTargetAmount() > 0) ? (currentBalance / goal.getTargetAmount()) * 100 : 0;
            if (progress < 0) progress = 0;
            if (progress > 100) progress = 100;

            String status;
            if (progress >= 100) {
                status = "TAMAMLANDI";
            } else {
                status = "%" + String.format("%.1f", progress);
            }

            goalListView.getItems().add(
                goal.getName() + "  |  " + goal.getTargetAmount() + " TL  |  " + status
            );
        }
    }
}
