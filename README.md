# MoneyLab — Kişisel Finans Yönetim Uygulaması

MoneyLab, JavaFX ile geliştirilmiş masaüstü tabanlı bir kişisel finans uygulamasıdır. Gelir/gider takibi, finansal hedef belirleme ve gelecek bakiye projeksiyonu gibi özellikler sunar.

---

## Gereksinimler

| Araç | Sürüm |
|------|-------|
| Java JDK | 17 veya üzeri |
| Apache Maven | 3.8 veya üzeri |

> **Not:** Maven yüklü değilse projedeki `mvnw.cmd` (Maven Wrapper) kullanılabilir; ek kurulum gerekmez.

---

## Çalıştırma

### Yöntem 1 — Geliştirme ortamında doğrudan çalıştırma (önerilen)

Proje kök dizininde terminal açıp şu komutu çalıştırın:

```bash
mvnw.cmd javafx:run
```

Bu komut bağımlılıkları indirir, projeyi derler ve uygulamayı başlatır.

---

### Yöntem 2 — Eclipse IDE üzerinden çalıştırma

1. Eclipse'i açın ve **File → Import → Maven → Existing Maven Projects** yolunu izleyin.
2. **Root Directory** olarak projenin kök klasörünü seçin ve **Finish** butonuna tıklayın.
3. Eclipse projeyi Maven bağımlılıklarıyla birlikte otomatik olarak yapılandıracaktır (birkaç dakika sürebilir).
4. Sol paneldeki **Project Explorer**'da projeye sağ tıklayın.
5. **Run As → Maven Build...** seçeneğini seçin.
6. Açılan pencerede **Goals** alanına şunu yazın:

   ```
   javafx:run
   ```

7. **Run** butonuna tıklayın — uygulama başlayacaktır.

> **Not:** Eclipse'in e(fx)clipse eklentisi yüklü olsa bile JavaFX modül sorunları yaşanabilir. Bu durumda yukarıdaki Maven Build yöntemi en güvenli çalıştırma seçeneğidir.

---

### Yöntem 3 — Çalıştırılabilir JAR oluşturma ve çalıştırma

Önce JAR dosyasını oluşturun:

```bash
mvnw.cmd clean package
```

Ardından oluşturulan JAR'ı çalıştırın:

```bash
java -jar target/MoneyLab-1.0-SNAPSHOT.jar
```

---

## Kullanım

1. Uygulama başladığında **giriş ekranı** açılır.
2. Yeni kullanıcıysanız **"Kayıt Ol"** butonuyla hesap oluşturun.
3. Giriş yaptıktan sonra üç sekme kullanılabilir:

| Sekme | Açıklama |
|-------|----------|
| **Genel Varlıklarım** | Toplam gelir/gider pasta grafiği |
| **Gelir / Gider İşlemleri** | İşlem ekleme, silme ve geçmiş görüntüleme |
| **Hedef** | Finansal hedef takibi ve gelecek bakiye projeksiyonu |

---

## Veri Depolama

Uygulama, verileri proje kök dizinindeki `data/` klasöründe düz metin dosyalarına kaydeder:

```
data/
├── users.txt        # Kullanıcı hesapları
├── transactions.txt # Gelir ve gider kayıtları
├── goals.txt        # Finansal hedefler
└── history.txt      # İşlem geçmişi
```

Veriler kalıcıdır; uygulama kapatılıp yeniden açıldığında korunur.

---

## Proje Yapısı

```
src/main/java/com/moneylab/
├── Main.java                  # JAR için başlatıcı sınıf
├── gui/
│   ├── MainApp.java           # JavaFX uygulama giriş noktası
│   ├── LoginView.java         # Giriş ekranı
│   ├── RegisterView.java      # Kayıt ekranı
│   └── DashboardView.java     # Ana panel (3 sekme)
├── model/
│   ├── Transaction.java       # Soyut işlem sınıfı
│   ├── Income.java            # Gelir
│   ├── Expense.java           # Gider
│   ├── Balance.java           # Bakiye hesaplama
│   ├── Goal.java              # Finansal hedef
│   ├── User.java              # Kullanıcı
│   └── Frequency.java         # Tekrar sıklığı (ONCE / WEEKLY / MONTHLY)
└── service/
    ├── UserManager.java       # Kullanıcı yönetimi ve kimlik doğrulama
    ├── TransactionManager.java
    └── GoalManager.java
```

---

## Teknolojiler

- **Java 17**
- **JavaFX 17.0.6** — GUI bileşenleri ve grafikler
- **Apache Maven** — Bağımlılık yönetimi ve derleme
