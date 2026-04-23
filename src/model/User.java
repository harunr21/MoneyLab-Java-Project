package src.model;

public class User {

    // --- Instance Variables (Encapsulation: private) ---
    private int id;
    private String name;
    private String password;
    private double goal;

    // --- Constructor ---
    public User(int id, String name, String password, double goal) {
        this.id       = id;
        this.name     = name;
        if (password != null && password.length() >= 6) {
            this.password = password;
        } else {
            this.password = "default123"; // Veya hata fırlatılabilir, şimdilik güvenli bir varsayılan
            System.out.println("Uyarı: Geçersiz şifre! Varsayılan şifre atandı.");
        }
        this.goal     = goal;
    }

    // --- Getters ---
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public double getGoal() {
        return goal;
    }

    // --- Setters ---
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        if (password == null || password.length() < 6) {
            System.out.println("Hata: Şifre en az 6 karakter olmalıdır.");
            return;
        }
        this.password = password;
    }

    public void setGoal(double goal) {
        if (goal < 0) {
            System.out.println("Hata: Hedef negatif olamaz.");
            return;
        }
        this.goal = goal;
    }

    // --- .txt'e yazarken kullanılacak format ---
    // Format: id,name,password,goal
    public String toFileString() {
        return id + "," + name + "," + password + "," + goal;
    }

    // --- .txt'den okurken kullanılacak static factory metod ---
    public static User fromFileString(String line) {
        String[] parts = line.split(",");
        int    id       = Integer.parseInt(parts[0].trim());
        String name     = parts[1].trim();
        String password = parts[2].trim();
        double goal     = Double.parseDouble(parts[3].trim());
        return new User(id, name, password, goal);
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "', goal=" + goal + "}";
    }
}
