public class Main {
    public static void main(String[] args) {
        // balance nesnesi
        Balance myBalance = new Balance();

        // gelir nesnesi oluşturdum
        RegularIncome salary = new RegularIncome(15000.0, "Nisan Ayı Maaşı", "Aylık");

        // balance a salary ekleme
        myBalance.addIncome(salary);

        // balance yazdır
        System.out.println("Güncel Toplam Gelir: " + myBalance.getTotalBalance());
    }
}//