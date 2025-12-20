package classFiles;

public class Holding {

	private Stock stock;
	private int shares;
	private double initialValue;
	private double currentValue;

	public Holding (Stock stock, int shares) {
		this.stock = stock;
		this.shares = shares;

		this.initialValue = stock.getValue() * shares;
		this.currentValue = this.initialValue;
	}

}
