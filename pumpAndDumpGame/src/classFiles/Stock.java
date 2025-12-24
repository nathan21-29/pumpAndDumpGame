package classFiles;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.*;

public class Stock {
	
	public static ArrayList<Stock> market = new ArrayList<>();
	private static Font price = new Font("Arial", Font.PLAIN, 20);

	private final static boolean POSITIVE = true;
	private final static boolean NEGATIVE = false;
	private ArrayList<Candlestick> priceHistory;
	private String symbol;
	private double volatility;
	private double baselineSum;
	private double momentum;
	private double maxPrice;
	private double minPrice;
	private int recentMax;
	private int recentMin;
	private double targetGrowth;
	private double stability;

	public Stock(String symbol, int initialPrice, double volatility, double targetGrowth, double stability) {
		maxPrice = initialPrice;
		minPrice = initialPrice;
		recentMax = 0;
		recentMin = 0;
		this.symbol = symbol;
		this.volatility = volatility;
		this.targetGrowth = targetGrowth;
		this.stability = stability;
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
	
	public int getRecentMax() {
		return recentMax;
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
		(priceHistory.subList(Math.max(priceHistory.size() - (int)(25 * stability),  0), priceHistory.size()));
		
		int recentSize = Math.min(priceHistory.size(), (int)(25 * stability));
		
		//baselinePrice is the average price of the last 25 available candlesticks + 10%
		double baselinePrice = baselineSum / recentSize * targetGrowth;
		//find pressure as %difference from baselinePrice
		double percentDiff = (baselinePrice - this.getValue()) / baselinePrice;
		double pressure;
		if(percentDiff > 0) { //undervalued; there should be positive pressure
			//when volatility is 1, max pressure is reached when stock is 20% under target
			pressure = Math.min(percentDiff / (0.2 * Math.pow(volatility, 1.2)), 1);
		}
		else {
			pressure = Math.max(percentDiff / (0.2 * Math.pow(volatility, 1.2)), -1);
		}
		//roll positive or negative candlestick
		if(Math.random() * 4 <= momentum + pressure) { //positive roll
			generateCandleStick(POSITIVE);
		}
		else { //negative roll
			generateCandleStick(NEGATIVE);
		}
		
		//check for out of bounds min/max
		if(priceHistory.size() - recentMax > 141) { //recent max is out of bounds of the current graph
			double maxValue = priceHistory.get(priceHistory.size() - 141).getClosePrice();
			int maxIndex = priceHistory.size() - 141;
			for(int i = priceHistory.size() - 141; i < priceHistory.size(); i++) {
				if(priceHistory.get(i).getClosePrice() > maxValue) {
					maxValue = priceHistory.get(i).getClosePrice();
					maxIndex = i;
				}
			}
			recentMax = maxIndex;
		}
		
		if(priceHistory.size() - recentMin > 141) { //recent max is out of bounds of the current graph
			double minValue = priceHistory.get(priceHistory.size() - 140).getClosePrice();
			int minIndex = priceHistory.size() - 141;
			for(int i = priceHistory.size() - 141; i < priceHistory.size(); i++) {
				if(priceHistory.get(i).getClosePrice() < minValue) {
					minValue = priceHistory.get(i).getClosePrice();
					minIndex = i;
				}
			}
			recentMin = minIndex;
		}
		
		//correct baselineSum
		if(recentSize == 25 * stability) {
			baselineSum -= priceHistory.get(priceHistory.size() - (int)(25 * stability)).getClosePrice();
		}
	}
	
	//pass in sublist so that we can still access values after changing it
	public void generateCandleStick(boolean type) {
		double change = Math.random() * ((0.095 * this.getValue()) * volatility) + (0.005 * this.getValue());
		if(type == POSITIVE) {
			if(priceHistory.getLast().getType() == NEGATIVE) { //swing from negative -> positive
//				momentum = 2; //reset momentum
				//maxroll momentum in the other direction
				momentum = Math.min(momentum + 0.25, 2.7);
			}
			momentum = Math.min(momentum + Math.sqrt(Math.random()) * 0.2 + 0.05, 2.55);
			baselineSum += this.getValue() + change;
			if(this.getValue() + change > maxPrice) {
				maxPrice = this.getValue() + change;
			}
			if(this.getValue() + change > priceHistory.get(recentMax).getClosePrice()) {
				recentMax = priceHistory.size(); //set recent max index to new candlestick
			}
			priceHistory.add(new Candlestick(this.getValue(), this.getValue() + change));
		}
		else { //type == NEGATIVE
			if(priceHistory.getLast().getType() == POSITIVE) { //swing from positive -> negative
//				momentum = 2; //reset momentum
				//maxroll momentum in the other direction
				momentum = Math.max(momentum - 0.25, 1.3);
			}
			momentum = Math.max(momentum - Math.sqrt(Math.random()) * 0.2 + 0.05, 1.3);
			baselineSum += this.getValue() - change;
			if(this.getValue() - change < minPrice) {
				minPrice = this.getValue() - change;
			}
			if(this.getValue() - change < priceHistory.get(recentMin).getClosePrice()) {
				recentMin = priceHistory.size(); //set recent max index to new candlestick
			}
			priceHistory.add(new Candlestick(this.getValue(), this.getValue() - change));
		}
	}
	
	public void drawDemo(long startTime, int candlestickCount, int candlestickWidth, Graphics g) {
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
			g.fillRect(i * candlestickWidth, Math.min(openPixel, closePixel), candlestickWidth, Math.abs(openPixel - closePixel));
		}
	}
	
	public void drawGraph(Graphics g) {
		double topBound = Math.max(priceHistory.get(recentMax).getClosePrice(), 
				priceHistory.get(recentMax).getOpenPrice()) * 1.1;
		double bottomBound = Math.min(priceHistory.get(recentMin).getClosePrice(), 
				priceHistory.get(recentMin).getOpenPrice()) * 0.85;
		
		drawGraphLines(g);
		
		int openPixel;
		int closePixel;
		int start = priceHistory.size() - 141;
		for (int i = 0; i < 140; i++) {
			openPixel = getDrawPixel(100, 700, topBound, bottomBound, priceHistory.get(i + start).getOpenPrice());
			closePixel = getDrawPixel(100, 700, topBound, bottomBound, priceHistory.get(i + start).getClosePrice());
			if(openPixel >= closePixel) { //upward candle
				g.setColor(Color.GREEN);
			}
			else { //downward candle
				g.setColor(Color.RED);
			}
			g.fillRect(10 + i * 10, Math.min(openPixel, closePixel), 10, Math.abs(openPixel - closePixel));
		}
	}
	
	public void drawGraphLines(Graphics g) {
		g.setColor(Color.WHITE);
		g.setFont(price);
		double top = Math.max(priceHistory.get(recentMax).getClosePrice(), 
				priceHistory.get(recentMax).getOpenPrice());
		double bottom = Math.min(priceHistory.get(recentMin).getClosePrice(), 
				priceHistory.get(recentMin).getOpenPrice());
		double valueIncrement = (top - bottom) / 4; //round to nearest 100th
		
		int yCoord;
		for(int i = 0; i <= 4; i++) {
			yCoord = getDrawPixel(100, 700, top * 1.1, bottom * 0.85, bottom + (i * valueIncrement));
			g.drawLine(10, yCoord, 1450, yCoord);
			g.drawString(String.format("%.2f", bottom + (i * valueIncrement)), 1455, yCoord + 2);
		}
	}
	
	public int getDrawPixel(int fieldTop, int fieldBottom, double topBound, double bottomBound, double target) {
		return (int) (fieldTop + (fieldBottom - fieldTop) * (1 - (target - bottomBound) / (topBound - bottomBound)));
	}
}