package com.moneylab.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import com.moneylab.model.*;
import com.moneylab.service.TransactionManager;

import java.time.LocalDate;
import java.util.List;

public class DashboardView {
    private MainApp app;
    private User user;
    private Balance balance;
    private TransactionManager transactionManager;
    private ListView<String> transactionListView = new ListView<>(); 
    private ListView<String> incomeListView = new ListView<>();
    private ListView<String> expenseListView = new ListView<>();
    private List<Transaction> currentExpandedBaseTransactions;
    private List<Transaction> currentIncomeBaseTransactions;
    private List<Transaction> currentExpenseBaseTransactions;
    private Button calculateButton; // Tasarruf sayfasını otomatik yenilemek için

    public DashboardView(MainApp app, User user) {
        this.app = app;
        this.user = user;
        this.balance = new Balance(); 
        
        // Veri kaydetme servisimizi başlatıyoruz
        this.transactionManager = new TransactionManager();


        // Kullanıcı giriş yaptığında eski işlemlerini dosyadan yüklüyoruz
        List<Transaction> pastTransactions = transactionManager.loadUserTransactions(user.getId());
        for (Transaction t : pastTransactions) {
            balance.addTransaction(t);
        }
    }

    public Scene getScene() {
        BorderPane rootLayout = new BorderPane();
        rootLayout.setStyle("-fx-background-color: #ffffff;");

        // =========================================================================
        // 1. ÜST KISIM (Başlık, Hedef ve Çıkış Butonu)
        // =========================================================================
        HBox topBox = new HBox(20);
        topBox.setPadding(new Insets(15, 20, 15, 20));
        topBox.setStyle("-fx-background-color: #2196F3;"); // Mavi çubuk
        topBox.setAlignment(Pos.CENTER_LEFT);
        
        VBox userInfoBox = new VBox(5);
        Label welcomeLabel = new Label("Hoş geldin, " + user.getName() + "!");
        welcomeLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        Label goalLabel = new Label("Birikim Hedefin: " + user.getGoal() + " TL");
        goalLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e3f2fd;");
        userInfoBox.getChildren().addAll(welcomeLabel, goalLabel);

        // Boşluk doldurucu (Çıkış butonunu en sağa iter)
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logoutButton = new Button("Çıkış Yap");
        logoutButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        logoutButton.setOnAction(e -> app.showLogin());

        topBox.getChildren().addAll(userInfoBox, spacer, logoutButton);
        rootLayout.setTop(topBox);

        // =========================================================================
        // 2. SEKME YAPISI (TabPane ile 2 Ayrı Sayfa)
        // =========================================================================
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE); // Sekmelerdeki çarpı (X) tuşunu gizler

        // Genel Etiketler (Güncelleme metodunda kullanılacaklar)
        Label balanceLabel = new Label();
        balanceLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #333; -fx-font-weight: bold;");
        PieChart pieChart = new PieChart();
        pieChart.setTitle("Gelir ve Gider Dağılımı");

        // --- İLK SEKME: GENEL VARLIKLARIM ---
        Tab assetsTab = new Tab("Genel Varlıklarım");
        VBox assetsLayout = new VBox(20);
        assetsLayout.setAlignment(Pos.CENTER);
        assetsLayout.setPadding(new Insets(30));
        assetsLayout.getChildren().addAll(balanceLabel, pieChart);
        assetsTab.setContent(assetsLayout);

     // --- İKİNCİ SEKME: İŞLEMLERİM ---
        Tab transactionsTab = new Tab("Bütçe Yönetimi");
        BorderPane transactionsLayout = new BorderPane();
        
     // İşlemlerim -> Sol Taraf: Form (Dinamik Yapı)
        VBox formBox = new VBox(15);
        formBox.setPadding(new Insets(20));
        formBox.setPrefWidth(250);
        formBox.setStyle("-fx-background-color: #f4f4f9; -fx-border-color: #ddd; -fx-border-width: 0 1px 0 0;");

     // --- GÖRÜNÜM 1: SADECE BUTONLAR ---
        VBox buttonsLayout = new VBox(15);
        buttonsLayout.setAlignment(Pos.CENTER);
        
        Button btnIncome = new Button("Gelir Ekle");
        btnIncome.setMaxWidth(Double.MAX_VALUE);
        btnIncome.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12px; -fx-cursor: hand;");

        Button btnExpense = new Button("Gider Ekle");
        btnExpense.setMaxWidth(Double.MAX_VALUE);
        btnExpense.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12px; -fx-cursor: hand;");
        
        // YENİ EKLENEN BUTON: İŞLEM GEÇMİŞİ
        Button btnHistory = new Button("İşlem Geçmişi");
        btnHistory.setMaxWidth(Double.MAX_VALUE);
        btnHistory.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12px; -fx-cursor: hand;");

        // Üç butonu da listeye ekliyoruz
        buttonsLayout.getChildren().addAll(btnIncome, btnExpense, btnHistory);

        // --- GÖRÜNÜM 2: GİRİŞ FORMU ---
        VBox inputsLayout = new VBox(15);
        
        Label formTitle = new Label("Detaylar");
        formTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        ComboBox<String> frequencyBox = new ComboBox<>();
        frequencyBox.getItems().addAll("Tek Seferlik", "Haftalık", "Aylık", "Yıllık");
        frequencyBox.setValue("Tek Seferlik");
        frequencyBox.setMaxWidth(Double.MAX_VALUE);

        DatePicker datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now());
        datePicker.setMaxWidth(Double.MAX_VALUE);

        TextField amountInput = new TextField();
        amountInput.setPromptText("Miktar (Örn: 150)");

        TextField descInput = new TextField();
        descInput.setPromptText("Açıklama (Örn: Market)");

        Button submitButton = new Button("Kaydet");
        submitButton.setMaxWidth(Double.MAX_VALUE);
        submitButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        Button cancelButton = new Button("İptal (Geri Dön)");
        cancelButton.setMaxWidth(Double.MAX_VALUE);
        cancelButton.setStyle("-fx-background-color: #9e9e9e; -fx-text-fill: white; -fx-cursor: hand;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 12px;");

        inputsLayout.getChildren().addAll(formTitle, frequencyBox, datePicker, amountInput, descInput, submitButton, cancelButton, errorLabel);

        // Başlangıçta form kutusuna sadece butonları ekliyoruz
        formBox.getChildren().add(buttonsLayout);

        // Seçilen işlem tipini hafızada tutmak için dizi kullanıyoruz (Lambda kuralları gereği)
        final String[] transactionType = {""};

        // Butonlara basıldığında formu göster ve başlığı değiştir
        btnIncome.setOnAction(e -> {
            transactionType[0] = "Gelir";
            formTitle.setText("Yeni Gelir Ekle");
            formTitle.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 18px; -fx-font-weight: bold;");
            formBox.getChildren().setAll(inputsLayout); // Ekranı forma çevir
        });

        btnExpense.setOnAction(e -> {
            transactionType[0] = "Gider";
            formTitle.setText("Yeni Gider Ekle");
            formTitle.setStyle("-fx-text-fill: #f44336; -fx-font-size: 18px; -fx-font-weight: bold;");
            formBox.getChildren().setAll(inputsLayout); // Ekranı forma çevir
        });

        // İptal butonuna basılırsa kutuları temizle ve ilk ekrana dön
        cancelButton.setOnAction(e -> {
            amountInput.clear();
            descInput.clear();
            errorLabel.setText("");
            formBox.getChildren().setAll(buttonsLayout);
        });

        transactionsLayout.setLeft(formBox);
     // İşlemlerim -> Sağ Taraf: İşlem Geçmişi (Liste)
        VBox historyBox = new VBox(10);
        historyBox.setPadding(new Insets(20));
        
        Label historyTitle = new Label("İşlem Geçmişi");
        historyTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        // Listeyi büyüyebildiği kadar büyüt
        VBox.setVgrow(transactionListView, Priority.ALWAYS);
        
        Button deleteButton = new Button("Seçili İşlemi Sil");
        deleteButton.setMaxWidth(Double.MAX_VALUE);
        deleteButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        // YENİ: GEÇMİŞİ GİZLE BUTONU
        Button closeHistoryButton = new Button("Geçmişi Gizle");
        closeHistoryButton.setMaxWidth(Double.MAX_VALUE);
        closeHistoryButton.setStyle("-fx-background-color: #9e9e9e; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        historyBox.getChildren().addAll(historyTitle, transactionListView, deleteButton, closeHistoryButton);

        // BAŞLANGIÇTA GEÇMİŞİ GİZLE (Pane'in ortasını boş bırakıyoruz)
        transactionsLayout.setCenter(null);

        // =========================================================================
        // GEÇMİŞ GÖSTER / GİZLE AKSİYONLARI
        // =========================================================================
        
        // Sol menüdeki İşlem Geçmişi butonuna tıklanınca
        btnHistory.setOnAction(e -> {
            transactionsLayout.setCenter(historyBox); // Ekranın ortasına listeyi getir
        });

        // Listenin altındaki Geçmişi Gizle butonuna tıklanınca
        closeHistoryButton.setOnAction(e -> {
            transactionsLayout.setCenter(null); // Ekranın ortasını tekrar boşalt
        });
        
        transactionsTab.setContent(transactionsLayout);
        // --- ÜÇÜNCÜ SEKME: TASARRUF TAKİBİ ---
        Tab savingsTab = new Tab("Tasarruf Takibi");
        VBox savingsLayout = new VBox(20);
        savingsLayout.setAlignment(Pos.CENTER);
        savingsLayout.setPadding(new Insets(30));

        Label savingsTitle = new Label("Gelecekteki Bakiyeni Hesapla");
        savingsTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        HBox controlBox = new HBox(10);
        controlBox.setAlignment(Pos.CENTER);
        Label monthLabel = new Label("Kaç ay sonrasını görmek istiyorsun?");
        ComboBox<Integer> monthBox = new ComboBox<>();
        monthBox.getItems().addAll(1, 3, 6, 12, 24, 36, 60);
        monthBox.setValue(1); // Varsayılan 1 ay
        
        calculateButton = new Button("Hesapla");
        calculateButton.setStyle("-fx-background-color: #9C27B0; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        
        controlBox.getChildren().addAll(monthLabel, monthBox, calculateButton);

        Label projectedBalanceLabel = new Label("Hesaplanıyor...");
        projectedBalanceLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #333; -fx-font-weight: bold;");
        
        Label goalStatusLabel = new Label("");
        goalStatusLabel.setStyle("-fx-font-size: 16px;");

        ProgressBar goalProgressBar = new ProgressBar(0);
        goalProgressBar.setPrefWidth(400);
        goalProgressBar.setPrefHeight(30);
        goalProgressBar.setStyle("-fx-accent: #4CAF50;");

        savingsLayout.getChildren().addAll(savingsTitle, controlBox, projectedBalanceLabel, goalStatusLabel, goalProgressBar);
        savingsTab.setContent(savingsLayout);
        
        // Gelecekteki bakiyeyi hesaplama mantığı
        calculateButton.setOnAction(e -> {
            int monthsAhead = monthBox.getValue();
            LocalDate targetDate = LocalDate.now().plusMonths(monthsAhead);
            
            // Hedef tarihteki toplam bakiye
            double projectedTotal = balance.getTotalBalance(targetDate);
            projectedBalanceLabel.setText(monthsAhead + " Ay Sonraki Bakiye: " + projectedTotal + " TL");
            
            // Hedefe ulaşma durumu
            double currentGoal = user.getGoal();
            if (currentGoal > 0) {
                double progress = projectedTotal / currentGoal;
                if (progress < 0) progress = 0; // Negatif bakiyede bar sıfır görünsün
                
                if (progress >= 1.0) {
                    goalProgressBar.setProgress(1.0);
                    goalProgressBar.setStyle("-fx-accent: #4CAF50;"); // Yeşil
                    goalStatusLabel.setText("Tebrikler! Hedefine ulaştın! (%" + String.format("%.1f", progress * 100) + ")");
                    goalStatusLabel.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                } else {
                    goalProgressBar.setProgress(progress);
                    goalProgressBar.setStyle("-fx-accent: #2196F3;"); // Mavi
                    goalStatusLabel.setText("Hedefe ulaşmaya %" + String.format("%.1f", (1.0 - progress) * 100) + " kaldı.");
                    goalStatusLabel.setStyle("-fx-text-fill: #333;");
                }
            } else {
                goalStatusLabel.setText("Henüz bir hedef belirlemedin.");
                goalProgressBar.setProgress(0);
            }
        });

        // Sayfa açıldığında ilk hesaplamayı varsayılan ay değeriyle (1 ay) çalıştırıyoruz
        calculateButton.fire();

        // Sekmeleri TabPane'e ekle
        tabPane.getTabs().addAll(assetsTab, transactionsTab, savingsTab);
        rootLayout.setCenter(tabPane);

        // =========================================================================
        // 3. İŞLEM EKLEME MANTIĞI VE KAYDETME
        // =========================================================================
     // =========================================================================
        // 3. İŞLEM EKLEME MANTIĞI VE KAYDETME
        // =========================================================================
        submitButton.setOnAction(e -> {
            try {
                errorLabel.setText(""); // Hata mesajını temizle
                
                double amount = Double.parseDouble(amountInput.getText());
                String desc = descInput.getText();
                String type = transactionType[0]; // Kullanıcının tıkladığı butona göre belirlendi

                if(desc == null || desc.trim().isEmpty()) {
                    throw new IllegalArgumentException("Açıklama boş olamaz.");
                }
                if(desc.contains(",")) {
                    throw new IllegalArgumentException("Açıklamada virgül (,) kullanılamaz.");
                }

                String freqStr = frequencyBox.getValue();
                Frequency frequency = Frequency.ONCE;
                if (freqStr.equals("Haftalık")) frequency = Frequency.WEEKLY;
                else if (freqStr.equals("Aylık")) frequency = Frequency.MONTHLY;
                else if (freqStr.equals("Yıllık")) frequency = Frequency.YEARLY;

                LocalDate selectedDate = datePicker.getValue();
                if (selectedDate == null) {
                    selectedDate = LocalDate.now();
                }

                Transaction transaction;
                if (type.equals("Gelir")) {
                    transaction = new Income(amount, desc, selectedDate, frequency, "Kullanıcı");
                } else {
                    transaction = new Expense(amount, desc, selectedDate, frequency, "Kullanıcı");
                }

                // 1. İşlemi RAM'e ekle
                balance.addTransaction(transaction);
                
                // 2. Dosyaya kaydet
                // NOT: Eğer yeni TXT yapısına (id yerine isim) geçtiysek burayı user.getName() yapmalısın. 
                // Geçmediysek user.getId() olarak kalabilir.
                transactionManager.saveTransaction(user.getId(), transaction); 
                
                // 3. Arayüzü güncelle
                updateDashboard(balanceLabel, pieChart);

                // *** 4. EKRANI SIFIRLA VE BUTONLARA GERİ DÖN ***
                amountInput.clear();
                descInput.clear();
                formBox.getChildren().setAll(buttonsLayout);

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
            int selectedIndex = transactionListView.getSelectionModel().getSelectedIndex();
            
            if (selectedIndex >= 0 && currentExpandedBaseTransactions != null) {
                // Ekranda görünen "Kopya" işlemin aslında arka planda hangi "Orijinal (Base)" 
                // işleme ait olduğunu 'currentExpandedBaseTransactions' listemizden öğreniyoruz.
                Transaction baseTransactionToRemove = currentExpandedBaseTransactions.get(selectedIndex);
                
                // 1. Orijinal ana işlemi anlık bakiyeden (RAM objesinden) kalıcı olarak sil
                balance.removeTransaction(baseTransactionToRemove);
                
                // 2. Dosyayı (transactions.txt) o işlem olmadan baştan aşağı yeniden yazdır
                // Böylece tüm kopyalar ve orijinal işlem dosyadan temizlenmiş olur
                transactionManager.rewriteUserTransactions(user.getId(), balance.getTransactions());
                
                // 3. Ekrandaki grafiği, toplam bakiyeyi ve listeyi anında yenile
                updateDashboard(balanceLabel, pieChart);
            }
        });

        // Ekran açılırken grafikleri ve geçmiş listesini ilk kez dolduruyoruz
        updateDashboard(balanceLabel, pieChart);

        return new Scene(rootLayout, 850, 550);
    }

    // Ekrandaki bakiyeyi, pasta grafiğini ve geçmiş listesini güncelleyen metod
    private void updateDashboard(Label balanceLabel, PieChart pieChart) {
        
        balanceLabel.setText("Mevcut Bakiye: " + balance.getTotalBalance() + " TL");

        double totalIncome = 0;
        double totalExpense = 0;
        
        // Listeyi temizle ve dosyadaki verilerle yeniden doldur
        transactionListView.getItems().clear();
        currentExpandedBaseTransactions = new java.util.ArrayList<>();

        // UI (arayüz) için işlemleri, tekrarlarına göre matematiksel çoğaltılmış halleriyle alıyoruz
        List<Transaction> expanded = balance.getExpandedTransactions();
        
        // Bu ise asıl kaydedilmiş (çoğaltılmamış) olan kök işlemler listemiz
        List<Transaction> baseList = balance.getTransactions();

        for (Transaction t : expanded) {
            
            // "Bu kopya işlem aslında hangi orijinal işlemi temsil ediyor?" diye arıyoruz
            Transaction parent = null;
            for (Transaction b : baseList) {
                // Miktar, açıklama ve sıklık eşleşiyorsa bu kopya o orijinalin yavrusudur diyoruz
                if (b.getAmount() == t.getAmount() && b.getDescription().equals(t.getDescription()) && b.getFrequency() == t.getFrequency()) {
                    parent = b;
                    break;
                }
            }
            // Bulduğumuz orijinal işlemi, daha sonra silme butonunda kullanabilmek için eşleştirme listesine ekliyoruz
            currentExpandedBaseTransactions.add(parent);

            String freqDisplay = "";
            if (t.getFrequency() == Frequency.WEEKLY) freqDisplay = " [Haftalık]";
            else if (t.getFrequency() == Frequency.MONTHLY) freqDisplay = " [Aylık]";
            else if (t.getFrequency() == Frequency.YEARLY) freqDisplay = " [Yıllık]";

            if (t instanceof Income) {
                totalIncome += t.getAmount();
                transactionListView.getItems().add("[+] " + t.getAmount() + " TL - " + t.getDescription() + freqDisplay + " (" + t.getDate() + ")");
            } else if (t instanceof Expense) {
                totalExpense += t.getAmount();
                transactionListView.getItems().add("[-] " + t.getAmount() + " TL - " + t.getDescription() + freqDisplay + " (" + t.getDate() + ")");
            }
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

        // Eğer Tasarruf Takibi sayfası oluşturulduysa, bakiyeye bağlı olarak o sayfayı da anında güncelleyelim
        if (calculateButton != null) {
            calculateButton.fire();
        }
    }
}
