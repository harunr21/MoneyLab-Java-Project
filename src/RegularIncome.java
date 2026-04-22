public class RegularIncome extends Income {
    private String frequency; // Aylık, Haftalık geçici

    public RegularIncome(double amount, String description, String frequency) {
        // Üst sınıftaki (Income) constructor'ı çağırır
        super(amount, description);
        this.frequency = frequency;
    }

    // Getter ve Setter
    public String getFrequency() { return frequency; }
    
    public void setFrequency(String frequency) { this.frequency = frequency; }

	@Override
	public double getAmount() {
		// TODO Auto-generated method stub
		return this.amount;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return this.description;
	}
}