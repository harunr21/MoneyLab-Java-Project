
package src.model;

public class OneTimeIncome extends Income {
	
	private String source;

	public OneTimeIncome(double amount, String description, String date, String source) {
		super(amount, description, date);
		this.source = source;
	}

	public String getSource() {
		return source;
	}

}
