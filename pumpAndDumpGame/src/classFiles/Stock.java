package classFiles;

import java.awt.Color;
import java.awt.Graphics;
import java.util.*;

public class Stock {
	
	public static ArrayList<Stock> market = new ArrayList<>();

	private final static boolean POSITIVE = true;
	private final static boolean NEGATIVE = false;
	private ArrayList<Candlestick> priceHistory;
	private String symbol;
	private double volatility;
	private double baselineSum;
	private double momentum;
	private double maxPrice;
	private double minPrice;

	public Stock(String symbol, int initialPrice, double volatility) {
		maxPrice = initialPrice;
		minPrice = initialPrice;
		this.symbol = symbol;
		this.volatility = volatility;
		momentum = 2; //default
		priceHistory = new ArrayList<>();
		priceHistory.add(generateInitialCandlestick(initialPrice));
	}
 
	public String getSymbol () {
		return this.symbol;
	}

	public double getValue () {
		return priceHistory.getLast().getClosePrice();
	}
	
	public double getMaxPrice() {
		return maxPrice;
	}
	
	public double getMinPrice() {
		return minPrice;
	}
	
	public ArrayList<Candlestick> getPriceHistory() {
		return priceHistory;
	}
	
	public void setValue(double newPrice) {
		priceHistory.addLast(new Candlestick(priceHistory.getLast().getClosePrice(), newPrice));
	}
	
	public void setValue(double openPrice, double closePrice) {
		priceHistory.addLast(new Candlestick(openPrice, closePrice));
	}
	
	public String toString() {
		return priceHistory.toString();
	}

	//volatility is a float from 0 exclusive to inf, values above 1 create larger swings
	public Candlestick generateInitialCandlestick(double openPrice) {
		//for a volatility of 1, generate 
		double change = Math.random() * ((0.085 * openPrice) * volatility) + (0.015 * openPrice);
		if(Math.random() >= 0.5) {
			momentum += Math.random() * 0.1 + 0.05;
			baselineSum += openPrice + change;
			maxPrice = openPrice + change;
			return new Candlestick(openPrice, openPrice + change);
		}
		else {
			momentum -= Math.random() * 0.1 + 0.05;
			baselineSum += openPrice - change;
			minPrice = openPrice - change;
			return new Candlestick(openPrice, openPrice - change);
		}
	}
	
	public void nextCandleStick() {
		//first, find pressure
		Deque<Candlestick> recent = new LinkedList<Candlestick>
		(priceHistory.subList(Math.max(priceHistory.size() - 25,  0), priceHistory.size()));
		
		int recentSize = Math.min(priceHistory.size(), 25);
		
		//baselinePrice is the average price of the last 25 available candlesticks + 10%
		double baselinePrice = baselineSum / recentSize * 1.01;
//		System.out.println(baselinePrice);
		//find pressure as %difference from baselinePrice
		double percentDiff = (baselinePrice - this.getValue()) / baselinePrice;
//		System.out.println("Target: " + baselinePrice);
//		System.out.println("diff: " + percentDiff);
		double pressure;
		if(percentDiff > 0) { //undervalued; there should be positive pressure
			//when volatility is 1, max pressure is reached when stock is 20% under target
			pressure = Math.min(percentDiff / (0.2 * Math.pow(volatility, 1.2)), 1);
		}
		else {
			pressure = Math.max(percentDiff / (0.2 * Math.pow(volatility, 1.2)), -1);
		}
//		System.out.println("Pressure: " + pressure);
//		System.out.println("ChanceFactor: " + (pressure + momentum));
		//roll positive or negative candlestick
		if(Math.random() * 4 <= momentum + pressure) { //positive roll
			generateCandleStick(POSITIVE);
		}
		else { //negative roll
			generateCandleStick(NEGATIVE);
		}
		
		//correct baselineSum
		if(recentSize == 25) {
			baselineSum -= priceHistory.get(priceHistory.size() - 25).getClosePrice();
		}
	}
	
	//pass in sublist so that we can still access values after changing it
	public void generateCandleStick(boolean type) {
		double change = Math.random() * ((0.085 * this.getValue()) * volatility) + (0.015 * this.getValue());
		if(type == POSITIVE) {
			if(priceHistory.getLast().getType() == NEGATIVE) { //swing from negative -> positive
//				momentum = 2; //reset momentum
				//maxroll momentum in the other direction
				momentum = Math.min(momentum + 0.25, 2.7);
			}
			momentum = Math.min(momentum + Math.sqrt(Math.random()) * 0.2 + 0.05, 2.7);
			baselineSum += this.getValue() + change;
			if(this.getValue() + change > maxPrice) {
				maxPrice = this.getValue() + change;
			}
			priceHistory.add(new Candlestick(this.getValue(), this.getValue() + change));
		}
		else { //type == NEGATIVE
			if(priceHistory.getLast().getType() == POSITIVE) { //swing from positive -> negative
//				momentum = 2; //reset momentum
				//maxroll momentum in the other direction
				momentum = Math.max(momentum - 0.25, 1.3);
			}
			momentum = Math.max(momentum - Math.random() * 0.2 + 0.05, 1.3);
			baselineSum += this.getValue() - change;
			if(this.getValue() - change < minPrice) {
				minPrice = this.getValue() - change;
			}
			priceHistory.add(new Candlestick(this.getValue(), this.getValue() - change));
		}
	}
	
	public void drawDemo(long startTime, int candlestickCount, int candlestickWidth, Graphics g) {
//		g.setColor(Color.WHITE);
		double topBound = maxPrice * 1.05;
		double bottomBound = minPrice * 0.95;
		int openPixel;
		int closePixel;
		for (int i = 0; i < Math.min((System.currentTimeMillis() - startTime) / 50, candlestickCount); i++) {
			openPixel = getDrawPixel(0, 1000, topBound, bottomBound, priceHistory.get(i).getOpenPrice());
			closePixel = getDrawPixel(0, 1000, topBound, bottomBound, priceHistory.get(i).getClosePrice());
			if(openPixel >= closePixel) { //upward candle
				g.setColor(Color.GREEN);
			}
			else { //downward candle
				g.setColor(Color.RED);
			}
//			g.drawRect(i * candlestickWidth, 
//					getDrawPixel(0, 1900, topBound, bottomBound, priceHistory.get(i).getOpenPrice()),
//					candlestickWidth,
//					-100);
			g.fillRect(i * candlestickWidth, Math.min(openPixel, closePixel), candlestickWidth, Math.abs(openPixel - closePixel));
			
			
		}
	}
	
	public int getDrawPixel(int fieldTop, int fieldBottom, double topBound, double bottomBound, double target) {
		return (int) (fieldTop + (fieldBottom - fieldTop) * (1 - (target - bottomBound) / (topBound - bottomBound)));
	}
}