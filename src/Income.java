
public abstract class Income implements Transaction {
	protected double amount; // Alt sınıfların erişebilmesi için protected olması gerekiyormuş
    public String description;
    
    public Income(double amount, String description) {
        this.amount = amount;       
        this.description = description;
    }
    
    @Override
    public double getAmount() { return amount; }
    
    @Override
    public String getDescription() { return description; }
    
	

}
