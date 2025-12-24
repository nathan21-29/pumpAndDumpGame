package classFiles;

public class Candlestick {

	private boolean type;
	private double openPrice;
	private double closePrice;

	public Candlestick(double openPrice, double closePrice) {
		this.openPrice = openPrice;
		this.closePrice = closePrice;
		if(openPrice - closePrice >= 0) //positive
			type = true; //render this Candlestick as green
		else
			type = false;
	}
	
	public double getOpenPrice() {
		return openPrice;
	}
	
	public double getClosePrice() {
		return closePrice;
	}
	
	public boolean getType() {
		return type;
	}
	
	public String toString() {
		return "Open price: " + openPrice + "  Close price: " + closePrice + "\n";
	}
}
