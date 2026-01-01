package classFiles;

public class Transaction {

	private Stock stock;
	private boolean isBuy;
	private int shares;
	private double sharePrice;

	public Transaction (Stock stock, int shares, double sharePrice, boolean isBuy) {
		this.stock = stock;
		this.shares = shares;
		this.sharePrice = sharePrice;
		this.isBuy = isBuy;
	}
}
