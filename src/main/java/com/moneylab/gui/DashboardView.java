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
    private ListView<String> incomeListView; // Aktif gelir listesi
    private ListView<String> expenseListView; // Aktif gider listesi
    private ListView<String> historyListView; // İşlem geçmişi listesi
    
    // Silme işlemini doğru yapmak için RAM'de tutulan geçici listeler
    private List<Transaction> currentIncomeList; 
    private List<Transaction> currentExpenseList;
    private Button calculateButton; // Hedef sayfasını yenilemek için
    private Tab targetTab; // Hedef sekmesinin aktif olup olmadığını kontrol etmek için
    private GoalManager goalManager;
    private ListView<String> goalListView; // Hedef listesi
    private List<Goal> userGoals; // Kullanıcının hedefleri (RAM)
    
    // --- Rapor Sekmesi Grafikleri ---
    private javafx.scene.chart.LineChart<String, Number> balanceChart;
    private javafx.scene.chart.LineChart<String, Number> goalsChart;
    
    private Label totalIncomeSummaryLabel;
    private Label totalExpenseSummaryLabel;
    
    public DashboardView(MainApp app, User user) {
        this.app = app;
        this.user = user;
        this.balance = new Balance(); 
        
        // Veri kaydetme servisimizi başlatıyoruz
        this.transactionManager = new TransactionManager();
        this.goalManager = new GoalManager();
        this.incomeListView = new ListView<>();
        this.expenseListView = new ListView<>();
        this.historyListView = new ListView<>();
        this.goalListView = new ListView<>();
        this.currentIncomeList = new java.util.ArrayList<>();
        this.currentExpenseList = new java.util.ArrayList<>();

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
        Tab assetsTab = new Tab("Ana Sayfa");
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
     // Gelir ve Gider özet etiketleri
        totalIncomeSummaryLabel = new Label("+ 0.0 TL");
        totalExpenseSummaryLabel = new Label("- 0.0 TL");

        totalIncomeSummaryLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 18px; -fx-font-weight: bold;");
        totalExpenseSummaryLabel.setStyle("-fx-text-fill: #F44336; -fx-font-size: 18px; -fx-font-weight: bold;");

        HBox summaryBox = new HBox(40);
        summaryBox.setAlignment(Pos.CENTER);
        summaryBox.getChildren().addAll(totalExpenseSummaryLabel, totalIncomeSummaryLabel);

        // Bakiye kartına özetleri ekle
        balanceCard.getChildren().add(summaryBox);

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
        Tab transactionsTab = new Tab("Bütçe Yönetimi");
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

     // --- Gelir / Gider ve İşlem Geçmişi Seçim Butonları ---
        VBox typeButtonsBox = new VBox(10);
        typeButtonsBox.setPadding(new Insets(18, 20, 5, 20));
        typeButtonsBox.setAlignment(Pos.CENTER);

        Button addIncomeBtn = new Button("Gelir Ekle");
        addIncomeBtn.setMaxWidth(Double.MAX_VALUE);
        addIncomeBtn.setPrefHeight(36);
        addIncomeBtn.setStyle(
            "-fx-background-color: #34a853; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-cursor: hand; -fx-background-radius: 8; -fx-font-size: 12px;"
        );

        Button addExpenseBtn = new Button("Gider Ekle");
        addExpenseBtn.setMaxWidth(Double.MAX_VALUE);
        addExpenseBtn.setPrefHeight(36);
        addExpenseBtn.setStyle(
            "-fx-background-color: #ea4335; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-cursor: hand; -fx-background-radius: 8; -fx-font-size: 12px;"
        );

        Button toggleHistoryBtn = new Button("İşlem Geçmişi");
        toggleHistoryBtn.setMaxWidth(Double.MAX_VALUE);
        toggleHistoryBtn.setPrefHeight(36);
        toggleHistoryBtn.setStyle(
            "-fx-background-color: #1a73e8; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-cursor: hand; -fx-background-radius: 8; -fx-font-size: 12px;"
        );

        typeButtonsBox.getChildren().addAll(addIncomeBtn, addExpenseBtn, toggleHistoryBtn);

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

     // Kaydet ve İptal Butonları
        HBox formActionButtons = new HBox(10);
        
        Button cancelButton = new Button("İptal");
        cancelButton.setMaxWidth(Double.MAX_VALUE);
        cancelButton.setPrefHeight(38);
        HBox.setHgrow(cancelButton, Priority.ALWAYS);
        cancelButton.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #555; " +
            "-fx-font-weight: bold; -fx-cursor: hand; -fx-border-color: #ccc; -fx-border-radius: 8; -fx-font-size: 13px;"
        );

        Button addButton = new Button("Kaydet");
        addButton.setMaxWidth(Double.MAX_VALUE);
        addButton.setPrefHeight(38);
        HBox.setHgrow(addButton, Priority.ALWAYS);
        addButton.setStyle(
            "-fx-background-color: linear-gradient(to right, #1a73e8, #4285f4); -fx-text-fill: white; " +
            "-fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 8; -fx-font-size: 13px;"
        );
        
        formActionButtons.getChildren().addAll(cancelButton, addButton);

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #ea4335; -fx-font-size: 11px; -fx-padding: 5 0 0 0;");
        errorLabel.setWrapText(true);

        // Ayırıcı çizgiler
        Separator sep1 = new Separator();
        sep1.setPadding(new Insets(2, 0, 2, 0));

        formControls.getChildren().addAll(
            dateTimeGroup, sep1, detailGroup, formActionButtons, errorLabel
        );
        
        // İptal Butonu Tıklanma Mantığı
        cancelButton.setOnAction(e -> {
            formControls.setVisible(false);
            formControls.setManaged(false);
            placeholderBox.setVisible(true);
            placeholderBox.setManaged(true);
            
            // Ana butonları geri getir
            addIncomeBtn.setVisible(true); addIncomeBtn.setManaged(true);
            addExpenseBtn.setVisible(true); addExpenseBtn.setManaged(true);
            
            formTitle.setText("Yeni İşlem Ekle");
            formSubTitle.setText("Gelir veya gider türünü seçerek başlayın");
            errorLabel.setText("");
            amountInput.clear();
            descInput.clear();
            sourceInput.clear();
        });

        final String[] currentType = {""};

        addIncomeBtn.setOnAction(e -> {
            formControls.setVisible(true);
            formControls.setManaged(true);
            placeholderBox.setVisible(false);
            placeholderBox.setManaged(false);
            
            // Butonları gizle (Aşağı kaydırma ihtiyacını kaldırmak için)
            addIncomeBtn.setVisible(false); addIncomeBtn.setManaged(false);
            addExpenseBtn.setVisible(false); addExpenseBtn.setManaged(false);
            
            currentType[0] = "Gelir";
            formTitle.setText("Yeni Gelir Ekle");
            formSubTitle.setText("Gelir bilgilerini aşağıya girin");
        });

        addExpenseBtn.setOnAction(e -> {
            formControls.setVisible(true);
            formControls.setManaged(true);
            placeholderBox.setVisible(false);
            placeholderBox.setManaged(false);
            
            // Butonları gizle
            addIncomeBtn.setVisible(false); addIncomeBtn.setManaged(false);
            addExpenseBtn.setVisible(false); addExpenseBtn.setManaged(false);
            
            currentType[0] = "Gider";
            formTitle.setText("Yeni Gider Ekle");
            formSubTitle.setText("Gider bilgilerini aşağıya girin");
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
     // İşlemlerim -> Sağ Taraf: StackPane (Aç/Kapa Mantığı İçin)
        StackPane listsContainer = new StackPane();
        listsContainer.setPadding(new Insets(16));
        listsContainer.setStyle("-fx-background-color: #f0f2f5;");

        // --- 1. GELİR VE GİDER BLOĞU (VARSAYILAN) ---
        VBox activeBox = new VBox(0);
        activeBox.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        activeBox.setEffect(new DropShadow(8, Color.rgb(0, 0, 0, 0.06)));

        HBox activeTitleBar = new HBox();
        activeTitleBar.setPadding(new Insets(12, 16, 12, 16));
        activeTitleBar.setStyle("-fx-background-color: linear-gradient(to right, #1a73e8, #4285f4); -fx-background-radius: 12 12 0 0;");
        Label activeTitle = new Label("Gelir ve Gider Listeleri");
        activeTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: white;");
        activeTitleBar.getChildren().add(activeTitle);

        // İç listeleri tutacak yatay kutu (Sola gelir, sağa gider)
        HBox dualListsBox = new HBox(10);
        dualListsBox.setPadding(new Insets(10));
        VBox.setVgrow(dualListsBox, Priority.ALWAYS);

        // Gelir Listesi Kutusu
        VBox incomeCol = new VBox(0);
        HBox.setHgrow(incomeCol, Priority.ALWAYS);
        incomeCol.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 8;");
        Label incTitle = new Label("Gelir Listesi");
        incTitle.setStyle("-fx-font-weight: bold; -fx-padding: 8; -fx-background-color: #e8f5e9; -fx-background-radius: 8 8 0 0;");
        incTitle.setMaxWidth(Double.MAX_VALUE);
        incomeListView.setStyle("-fx-background-radius: 0 0 8 8; -fx-border-width: 0;");
        VBox.setVgrow(incomeListView, Priority.ALWAYS);
        incomeCol.getChildren().addAll(incTitle, incomeListView);

        // Gider Listesi Kutusu
        VBox expenseCol = new VBox(0);
        HBox.setHgrow(expenseCol, Priority.ALWAYS);
        expenseCol.setStyle("-fx-border-color: #e0e0e0; -fx-border-radius: 8;");
        Label expTitle = new Label("Gider Listesi");
        expTitle.setStyle("-fx-font-weight: bold; -fx-padding: 8; -fx-background-color: #ffebee; -fx-background-radius: 8 8 0 0;");
        expTitle.setMaxWidth(Double.MAX_VALUE);
        expenseListView.setStyle("-fx-background-radius: 0 0 8 8; -fx-border-width: 0;");
        VBox.setVgrow(expenseListView, Priority.ALWAYS);
        expenseCol.getChildren().addAll(expTitle, expenseListView);

        dualListsBox.getChildren().addAll(incomeCol, expenseCol);

        // Ortak Silme Butonu
        Button deleteButton = new Button("Seçili İşlemi Sil");
        deleteButton.setMaxWidth(Double.MAX_VALUE);
        deleteButton.setPrefHeight(36);
        deleteButton.setStyle(
            "-fx-background-color: #ea4335; -fx-text-fill: white; -fx-font-weight: bold; " +
            "-fx-cursor: hand; -fx-background-radius: 0 0 12 12; -fx-font-size: 12px;"
        );
        activeBox.getChildren().addAll(activeTitleBar, dualListsBox, deleteButton);

        // Aynı anda iki listeden seçim yapılmasını engelleme mantığı
        incomeListView.setOnMouseClicked(ev -> expenseListView.getSelectionModel().clearSelection());
        expenseListView.setOnMouseClicked(ev -> incomeListView.getSelectionModel().clearSelection());

        // --- 2. İŞLEM GEÇMİŞİ BLOĞU (GİZLİ) ---
        VBox historyBox = new VBox(0);
        historyBox.setVisible(false); // Başlangıçta gizli
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

        // Blokları Konteynere Ekle
        listsContainer.getChildren().addAll(historyBox, activeBox); // activeBox üstte görünecek
        transactionsLayout.setCenter(listsContainer);
        transactionsTab.setContent(transactionsLayout);

        // Buton Etkileşimi: İşlem Geçmişini Aç / Kapa
        final boolean[] isHistoryVisible = {false};
        toggleHistoryBtn.setOnAction(e -> {
            isHistoryVisible[0] = !isHistoryVisible[0];
            if (isHistoryVisible[0]) {
                activeBox.setVisible(false);
                historyBox.setVisible(true);
                toggleHistoryBtn.setText("Gelir/Gider Listesine Dön");
            } else {
                activeBox.setVisible(true);
                historyBox.setVisible(false);
                toggleHistoryBtn.setText("İşlem Geçmişi");
            }
        });
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

                // Formu kapat, butonları ve yer tutucuyu geri getir
                formControls.setVisible(false);
                formControls.setManaged(false);
                placeholderBox.setVisible(true);
                placeholderBox.setManaged(true);
                addIncomeBtn.setVisible(true); addIncomeBtn.setManaged(true);
                addExpenseBtn.setVisible(true); addExpenseBtn.setManaged(true);
                formTitle.setText("Yeni İşlem Ekle");
                formSubTitle.setText("Gelir veya gider türünü seçerek başlayın");

            } catch (NumberFormatException ex) {
                errorLabel.setText("Lütfen sayısal bir miktar girin!");
            } catch (Exception ex) {
                errorLabel.setText(ex.getMessage());
            }
        });

     // =========================================================================
        // 4. İŞLEM SİLME MANTIĞI (DİNAMİK)
        // =========================================================================
        deleteButton.setOnAction(e -> {
            int incIndex = incomeListView.getSelectionModel().getSelectedIndex();
            int expIndex = expenseListView.getSelectionModel().getSelectedIndex();
            
            Transaction toRemove = null;

            // Hangi listeden seçim yapıldıysa o nesneyi buluyoruz
            if (incIndex >= 0 && incIndex < currentIncomeList.size()) {
                toRemove = currentIncomeList.get(incIndex);
            } else if (expIndex >= 0 && expIndex < currentExpenseList.size()) {
                toRemove = currentExpenseList.get(expIndex);
            }

            if (toRemove != null) {
                // 1. Orijinal işlemi bakiyeden (RAM'den) sil
                balance.removeTransaction(toRemove);
                // 2. Dosyayı (transactions.txt) o işlem olmadan baştan yaz
                transactionManager.rewriteUserTransactions(user.getId(), balance.getTransactions());
                // 3. Ekranı güncelle
                updateDashboard(balanceLabel, pieChart, tabPane);
            }
        });

        // Ekran açılırken grafikleri ve geçmiş listesini ilk kez dolduruyoruz
        updateDashboard(balanceLabel, pieChart, tabPane);
        
     // =========================================================================
        // --- DÖRDÜNCÜ SEKME: RAPOR ---
        // =========================================================================
        Tab reportTab = new Tab("Rapor");
        reportTab.setClosable(false); // Sekmenin yanlışlıkla kapatılmasını engelle
        BorderPane reportLayout = new BorderPane();
        reportLayout.setStyle("-fx-background-color: #f4f6f9;");

        // Sol Menü (Navigasyon Butonları)
        VBox reportMenu = new VBox(10);
        reportMenu.setPadding(new Insets(20));
        reportMenu.setPrefWidth(200);
        reportMenu.setStyle("-fx-background-color: white; -fx-border-color: #e0e0e0; -fx-border-width: 0 1 0 0;");

        Button btnBalanceReport = new Button("Bakiye");
        Button btnIncomeExpenseReport = new Button("Gelir / Gider");
        Button btnGoalsReport = new Button("Hedefler");

        String reportBtnStyle = "-fx-background-color: transparent; -fx-text-fill: #555; -fx-font-weight: bold; -fx-font-size: 14px; -fx-alignment: BASELINE_LEFT; -fx-padding: 10 15; -fx-cursor: hand;";
        String activeBtnStyle = "-fx-background-color: #e8f0fe; -fx-text-fill: #1a73e8; -fx-font-weight: bold; -fx-font-size: 14px; -fx-alignment: BASELINE_LEFT; -fx-padding: 10 15; -fx-cursor: hand; -fx-background-radius: 8;";

        btnBalanceReport.setStyle(activeBtnStyle); // Varsayılan olarak Bakiye açık başlasın
        btnIncomeExpenseReport.setStyle(reportBtnStyle);
        btnGoalsReport.setStyle(reportBtnStyle);

        btnBalanceReport.setMaxWidth(Double.MAX_VALUE);
        btnIncomeExpenseReport.setMaxWidth(Double.MAX_VALUE);
        btnGoalsReport.setMaxWidth(Double.MAX_VALUE);

        reportMenu.getChildren().addAll(btnBalanceReport, btnIncomeExpenseReport, btnGoalsReport);
        reportLayout.setLeft(reportMenu);

        // Sağ Panel (Dinamik Grafik Alanı)
        StackPane reportContentArea = new StackPane();
        reportContentArea.setPadding(new Insets(20));
        reportContentArea.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        reportContentArea.setEffect(new javafx.scene.effect.DropShadow(8, javafx.scene.paint.Color.rgb(0, 0, 0, 0.06)));
        BorderPane.setMargin(reportContentArea, new Insets(20));

        // 1. Bakiye Grafiği (Line Chart)
        javafx.scene.chart.CategoryAxis xAxisBalance = new javafx.scene.chart.CategoryAxis();
        xAxisBalance.setLabel("Zaman");
        javafx.scene.chart.NumberAxis yAxisBalance = new javafx.scene.chart.NumberAxis();
        yAxisBalance.setLabel("Bakiye Tutarı (TL)");
        balanceChart = new javafx.scene.chart.LineChart<>(xAxisBalance, yAxisBalance);
        balanceChart.setTitle("Zamana Göre Bakiye Değişimi");
        balanceChart.setLegendVisible(false);
        balanceChart.setAnimated(false); // Arka planda çizim hatasını önler

        // 2. Gelir/Gider Grafiği (AKTARMASI)
        // Genel Varlıklar'daki mevcut 'pieChart' objesini alıp doğrudan bu StackPane içine yerleştiriyoruz.
        // Bu işlem onu eski yerinden koparıp buraya taşıyacaktır.
        pieChart.setVisible(false); // Başlangıçta gizli başlasın

        // 3. Hedef Grafiği (Multi-Line Chart)
        javafx.scene.chart.CategoryAxis xAxisGoals = new javafx.scene.chart.CategoryAxis();
        xAxisGoals.setLabel("Hedef Gelişimi");
        javafx.scene.chart.NumberAxis yAxisGoals = new javafx.scene.chart.NumberAxis();
        yAxisGoals.setLabel("Hedef Sayısı");
        yAxisGoals.setMinorTickVisible(false);
        yAxisGoals.setTickUnit(1);
        goalsChart = new javafx.scene.chart.LineChart<>(xAxisGoals, yAxisGoals);
        goalsChart.setTitle("Hedef Tamamlama İlerlemesi");
        goalsChart.setVisible(false);
        goalsChart.setAnimated(false); // Arka planda çizim hatasını önler

        // Tüm grafikleri sağ panele ekliyoruz
        reportContentArea.getChildren().addAll(balanceChart, pieChart, goalsChart);
        reportLayout.setCenter(reportContentArea);
        reportTab.setContent(reportLayout);

        // Buton Tıklanma Etkileşimleri (Görünürlükleri Değiştirme)
        btnBalanceReport.setOnAction(e -> {
            btnBalanceReport.setStyle(activeBtnStyle);
            btnIncomeExpenseReport.setStyle(reportBtnStyle);
            btnGoalsReport.setStyle(reportBtnStyle);
            balanceChart.setVisible(true);
            pieChart.setVisible(false);
            goalsChart.setVisible(false);
        });

        btnIncomeExpenseReport.setOnAction(e -> {
            btnIncomeExpenseReport.setStyle(activeBtnStyle);
            btnBalanceReport.setStyle(reportBtnStyle);
            btnGoalsReport.setStyle(reportBtnStyle);
            pieChart.setVisible(true);
            balanceChart.setVisible(false);
            goalsChart.setVisible(false);
        });

        btnGoalsReport.setOnAction(e -> {
            btnGoalsReport.setStyle(activeBtnStyle);
            btnBalanceReport.setStyle(reportBtnStyle);
            btnIncomeExpenseReport.setStyle(reportBtnStyle);
            goalsChart.setVisible(true);
            balanceChart.setVisible(false);
            pieChart.setVisible(false);
        });

        // Sekmeyi sisteme ekle
        tabPane.getTabs().add(reportTab);

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
     // Ana sayfadaki özet etiketlerini güncelle
        totalIncomeSummaryLabel.setText("+ " + totalIncome + " TL");
        totalExpenseSummaryLabel.setText("- " + totalExpense + " TL");
     // Aktif Gelir/Gider listelerini güncelle
        incomeListView.getItems().clear();
        expenseListView.getItems().clear();
        currentIncomeList.clear();
        currentExpenseList.clear();
        
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
                incomeListView.getItems().add("[+] " + t.getAmount() + " TL - " + t.getDescription() + freqDisplay + sourceDisplay + " (" + t.getDate() + ")");
                currentIncomeList.add(t); // İndeks uyumu için RAM'e ekle
            } else if (t instanceof Expense) {
                expenseListView.getItems().add("[-] " + t.getAmount() + " TL - " + t.getDescription() + freqDisplay + sourceDisplay + " (" + t.getDate() + ")");
                currentExpenseList.add(t); // İndeks uyumu için RAM'e ekle
            }
        }
     // 1. ÖNCE DÖNGÜSÜ TAMAMLANAN OTOMATİK İŞLEMLERİ GEÇMİŞE SENKRONİZE ET
        syncRecurringTransactionsToHistory();

        // 2. SONRA İŞLEM GEÇMİŞİ LİSTESİNİ GÜNCELLE
        historyListView.getItems().clear();
        List<Transaction> historyList = transactionManager.loadUserHistory(user.getId());
        
        // Küçük bir UX dokunuşu: Listeyi ters çeviriyoruz ki en yeni işlemler en üstte görünsün
        java.util.Collections.reverse(historyList); 
        
        for (Transaction t : historyList) {
            String typeStr = (t instanceof Income) ? "Gelir İşlendi: " : "Gider İşlendi: ";
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
     // =========================================================
        // RAPOR SEKME GRAFİKLERİNİ GÜNCELLE
        // =========================================================

        // 1. Rapor - Zamana Göre Bakiye Değişimi
        if (balanceChart != null) {
            balanceChart.getData().clear();
            javafx.scene.chart.XYChart.Series<String, Number> balanceSeries = new javafx.scene.chart.XYChart.Series<>();
            balanceSeries.setName("Bakiye Trendi");
            
            List<Transaction> historyData = transactionManager.loadUserHistory(user.getId());
            double runningBalance = 0;
            // Listeyi tarih sırasına göre eski->yeni şeklinde işlemek için tersten okuyoruz (eğer reverse edildiyse)
            for (int i = historyData.size() - 1; i >= 0; i--) {
                Transaction t = historyData.get(i);
                if (t instanceof Income) runningBalance += t.getAmount();
                else if (t instanceof Expense) runningBalance -= t.getAmount();
                
                balanceSeries.getData().add(new javafx.scene.chart.XYChart.Data<String, Number>(t.getDate().toString(), runningBalance));
            }
            balanceChart.getData().add(balanceSeries);
        }

        // 2. Rapor - Hedef İlerleme Grafiği
        if (goalsChart != null) {
            goalsChart.getData().clear();
            javafx.scene.chart.XYChart.Series<String, Number> totalGoalsSeries = new javafx.scene.chart.XYChart.Series<>();
            totalGoalsSeries.setName("Toplam Hedefler");
            
            javafx.scene.chart.XYChart.Series<String, Number> completedGoalsSeries = new javafx.scene.chart.XYChart.Series<>();
            completedGoalsSeries.setName("Tamamlanan Hedefler");
            
            List<Goal> allGoals = goalManager.loadUserGoals(user.getId());
            int totalGoalCount = 0;
            int completedGoalCount = 0;
            
            // Not: Mevcut Goal modelinde 'eklenme tarihi' olmadığı için, zamana göre grafiği şimdilik 
            // hedeflerin listeye eklenme sırasına (Aşama 1, Aşama 2) göre çizdiriyoruz.
            for (int i = 0; i < allGoals.size(); i++) {
                Goal g = allGoals.get(i);
                totalGoalCount++;
                if (balance.getTotalBalance() >= g.getTargetAmount()) {
                    completedGoalCount++;
                }
                
                String stepStr = "Adım " + (i + 1);
                totalGoalsSeries.getData().add(new javafx.scene.chart.XYChart.Data<String, Number>(stepStr, totalGoalCount));
                completedGoalsSeries.getData().add(new javafx.scene.chart.XYChart.Data<String, Number>(stepStr, completedGoalCount));
            }
            goalsChart.getData().addAll(totalGoalsSeries, completedGoalsSeries);
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
    /**
     * Düzenli işlemleri (Haftalık, Aylık) kontrol eder. 
     * Eğer bugünün tarihine kadar gerçekleşmiş ama geçmiş (history.txt) dosyasına 
     * henüz yazılmamış bir döngü varsa, bunu otomatik olarak tespit edip geçmişe ekler.
     */
    private void syncRecurringTransactionsToHistory() {
        List<Transaction> historyList = transactionManager.loadUserHistory(user.getId());
        List<Transaction> expandedPast = balance.getExpandedTransactions(java.time.LocalDate.now());
        List<Integer> matchedIndices = new java.util.ArrayList<>();

        for (Transaction ext : expandedPast) {
            boolean found = false;
            
            for (int i = 0; i < historyList.size(); i++) {
                if (matchedIndices.contains(i)) continue; // Bu geçmiş kaydı daha önce eşleştirildiyse atla
                
                Transaction h = historyList.get(i);
                // Tür, Miktar, Açıklama ve Tarih tamamen eşleşiyorsa bu işlem geçmişte var demektir
                if (h.getClass().equals(ext.getClass()) &&
                    h.getAmount() == ext.getAmount() &&
                    h.getDescription().equals(ext.getDescription()) &&
                    h.getDate().equals(ext.getDate())) {
                    
                    found = true;
                    matchedIndices.add(i); // Eşleşeni işaretle ki aynı döngü tekrar tekrar eklenmesin
                    break;
                }
            }
            
            // Eğer geçmiş loglarında bulunamadıysa (yani zamanı gelmiş yeni bir tekrarsa), dosyaya kaydet
            if (!found) {
                transactionManager.saveToHistory(user.getId(), ext);
                historyList.add(ext);
                matchedIndices.add(historyList.size() - 1);
            }
        }
    }
}
