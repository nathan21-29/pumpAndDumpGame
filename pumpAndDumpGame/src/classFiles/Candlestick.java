package classFiles;

//Nathan Chan Jan 18, 2026
//This is the class file for Candlesticks, which are the objects
//used to encode the price change for a time window. Each candlestick
//has an openPrice representing the price at the beginning of the time
//window and a closePrice representing the price at the end of the time
//window. type == true means price went up, type == false means price
//went down.
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
