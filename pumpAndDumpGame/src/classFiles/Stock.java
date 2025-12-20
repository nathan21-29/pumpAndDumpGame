package classFiles;

import java.util.*;

public class Stock {

	private String symbol;
	private double value; //if using candlesticks
	//value can be derived using close price of final candlestick
	
	static ArrayList<Stock> market = new ArrayList<>();

	private Deque<Candlestick> priceHistory;

	public Stock(String symbol, double value) {
		this.symbol = symbol;
		priceHistory = new LinkedList<>();
	}
 
	public String getSymbol () {
		return this.symbol;
	}

	public double getValue () {
		return this.value;
	}
	
	public void setValue(double newPrice) {
		priceHistory.offerLast(new Candlestick(priceHistory.peekLast().getClosePrice(), newPrice));
	}
	
	public void setValue(double openPrice, double closePrice) {
		priceHistory.offerLast(new Candlestick(openPrice, closePrice));
	}
}
