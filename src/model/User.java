package src.model;

public class User {

    private final int id;
    private String name;
    private String password;
    private double goal;

    public User(int id, String name, String password, double goal) {
        this.id = id;
        setName(name);         // Doğrulama mantığı setter'da
        setPassword(password); // Doğrulama mantığı setter'da
        setGoal(goal);         // Doğrulama mantığı setter'da
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Kullanıcı adı boş olamaz.");
        }
        if (name.contains(",")) {
            throw new IllegalArgumentException("Kullanıcı adında virgül (,) kullanılamaz.");
        }
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Şifre en az 6 karakter olmalıdır.");
        }
        if (password.contains(",")) {
            throw new IllegalArgumentException("Şifrede virgül (,) kullanılamaz.");
        }
        this.password = password;
    }

    public double getGoal() {
        return goal;
    }

    public void setGoal(double goal) {
        if (goal < 0) {
            throw new IllegalArgumentException("Hedef negatif olamaz.");
        }
        this.goal = goal;
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + "', goal=" + goal + "}";
    }
}
