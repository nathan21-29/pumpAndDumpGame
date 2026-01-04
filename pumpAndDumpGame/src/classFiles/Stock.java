package classFiles;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.*;

import javax.swing.JOptionPane;

import pumpAndDumpGame.PumpAndDumpGame;

public class Stock implements Comparable<Stock> {

	private static ArrayList<Stock> testMarket = new ArrayList<>();
	private static ArrayList<Stock> market = new ArrayList<>();
	private static ArrayList<Stock> selectedMarket = testMarket;
	private static Font price = new Font("Arial", Font.PLAIN, 20);
	private static Font body = new Font("Arial", Font.PLAIN, 40);
	private static int time = 0; //int # of hours since "day" started
	private static String[] ratings = {"Strong sell", "Sell", "Neutral", "Buy", "Strong buy"};
	private static int[] views = {7, 35, 140, 1400}; //array of different view history options, in # of candles (hours)
	private static Color darkGreen = new Color(66, 176, 78);
	private static Color darkRed = new Color(187, 46, 40);
	private static HashMap<Stock, Integer> buyOrders = new HashMap<>(); //yet-to-be-filled buy orders, should be cleared upon each stock refresh
	private static HashMap<Stock, Integer> sellOrders = new HashMap<>(); //yet-to-be-filled buy orders, should be cleared upon each stock refresh
	private final static boolean POSITIVE = true;
	private final static boolean NEGATIVE = false;

	private ArrayList<Candlestick> priceHistory;
	private String symbol;
	private double volatility;
	private double baselineSum;
	private double momentum;
	private double maxPrice;
	private double minPrice;
	private double seedPrice; //the initial price of the stock, used for regenerating stocks
	private int recentMax; //stores the INDEX of the recent max value
	private int recentMin; //stores the INDEX of the recent min value
	private int candleCount; //makes it possible to save a different view for each stock
	private double targetGrowth;
	private double stability;
	private double chanceFactor;
	private double targetFlipChance;
	
	//player variables
	private int amountHeld;
	private double totalPurchasePrice;

	//constructor
	//String symbol is the symbol or "ticker" of the stock, int initialPrice is the seed price of the stock,
	//double volatility is a double from 0 exclusive to inf that dictates how big swings are
	//, but works best in the range of 0.1 - 3. Double targetGrowth is the percent above the baseline that
	//the stock "wants" to be at (e.g. targetGrowth of 1.1 means the stock wants to grow 10% above the baseline
	//however targetGrowth is most realistic in the range 1 +- 0.07. Double stability is a factor of the
	//sample size defining the baseline, as a multiple of 25 (if stability is 2, then the baseline price will
	//be defined using the last 2 * 25 = 50 close prices). Double targetFlipChance is the % chance as a decimal
	//that a stock will invert its targetGrowth (i.e. growth to decay & vice versa) on the start of any given day.
	public Stock(String symbol, int initialPrice, double volatility, double targetGrowth, double stability, double targetFlipChance) {
		maxPrice = initialPrice;
		minPrice = initialPrice;
		seedPrice = initialPrice;
		recentMax = 0;
		recentMin = 0;
		this.symbol = symbol;
		this.volatility = volatility;
		this.targetGrowth = targetGrowth;
		this.stability = stability;
		momentum = 2; //default
		priceHistory = new ArrayList<>();
		generateInitialCandlestick(initialPrice);
		candleCount = views[2];
		this.targetFlipChance = targetFlipChance;
				
		amountHeld = 0;
		totalPurchasePrice = 0;
	}

	//getters
	public static HashMap<Stock, Integer> getBuyOrders() {
		return buyOrders;
	}
	
	public static HashMap<Stock, Integer> getSellOrders() {
		return sellOrders;
	}
	
	public String getSymbol () {
		return symbol;
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
	
	public double getSeedPrice() {
		return seedPrice;
	}

	public ArrayList<Candlestick> getPriceHistory() {
		return priceHistory;
	}

	public int getRecentMax() {
		return recentMax;
	}
	
	public int getCandleCount() {
		return candleCount;
	}
	
	public int getAmountHeld() {
		return amountHeld;
	}
	
	public double getTotalPurchasePrice() {
		return totalPurchasePrice;
	}
	
	public static ArrayList<Stock> getMarket() {
		return market;
	}
	
	public static ArrayList<Stock> getTestMarket() {
		return testMarket;
	}
	
	public double getAveragePurchasePrice() {
		if(amountHeld == 0) {
			return 0;
		}
		return totalPurchasePrice / amountHeld;
	}
	
	//if absolute is true, return the absolute profitloss (dollar amount)
	//otherwise return %
	public double getProfitLoss(boolean absolute) {
		if(absolute) {
			return (getValue() - getAveragePurchasePrice()) * amountHeld;
		}
		else {
			return (getValue() - getAveragePurchasePrice()) / getValue() * 100;
		}
	}
	
	public double getTargetFlipChance() {
		return targetFlipChance;
	}
	
	//setters
	public void setValue(double newPrice) {
		priceHistory.addLast(new Candlestick(priceHistory.getLast().getClosePrice(), newPrice));
	}

	public void setValue(double openPrice, double closePrice) {
		priceHistory.addLast(new Candlestick(openPrice, closePrice));
	}
	
	public void setCandleCount(int data) {
		candleCount = views[data];
	}
	
	public void setAmountHeld(int amountHeld) {
		this.amountHeld = amountHeld;
	}
	
	public void setTotalPurchasePrice(int totalPurchasePrice) {
		this.totalPurchasePrice = totalPurchasePrice;
	}
	
	public void incrementAmountHeld(int increment) {
		amountHeld += increment;
	}
	
	public void incrementTotalPurchasePrice(double increment) {
		totalPurchasePrice += increment;
	}
	
	public String toString() {
		return priceHistory.toString();
	}
	
	public boolean equals(Object obj) {
		Stock compare = (Stock) obj;
		return symbol.equals(compare.getSymbol());
	}
	
	//returns true if timechange rolls into a new day
	//returns false otherwise
	public static boolean incrementTime() {
		time++;
		if(time > 7) {
			time = 0; //reset time
			//chance for stocks w/ targetFlipping = true to have their trend bias inverted
			for(int i = 0; i < selectedMarket.size(); i++) {
				if(Math.random() < selectedMarket.get(i).getTargetFlipChance()) {
					selectedMarket.get(i).invertTarget();
				}
			}
			return true;
		}
		return false;
	}
	
	private void invertTarget() {
		targetGrowth = 1 / targetGrowth;
	}
	
	public static void drawHoldings(Graphics g) {
		for(int i = 0; i < selectedMarket.size(); i++) {
			//if reached stocks with 0 shares held
			if(selectedMarket.get(i).amountHeld == 0) { 
				break;
			}
			g.setFont(body);
			g.drawString(selectedMarket.get(i).getSymbol() + 
					String.format("    (%+.4f%%)", selectedMarket.get(i).getProfitLoss(NEGATIVE)),
					1525, 80 * i + 240);
		}
	}

	//Generates the first candlestick for a stock
	//double openPrice is the openPrice of this first candleStick
	//volatility is a float from 0 exclusive to inf, values above 1 create larger swings
	public void generateInitialCandlestick(double openPrice) {
		//for a volatility of 1, generate 
		double change = Math.random() * ((0.085 * openPrice) * volatility) + (0.015 * openPrice);
		if(Math.random() >= 0.5) {
			momentum += Math.random() * 0.1 + 0.05;
			baselineSum += openPrice + change;
			maxPrice = openPrice + change;
			priceHistory.add(new Candlestick(openPrice, openPrice + change));
		}
		else {
			momentum -= Math.random() * 0.1 + 0.05;
			baselineSum += openPrice - change;
			minPrice = openPrice - change;
			priceHistory.add(new Candlestick(openPrice, openPrice - change));
		}
	}

	//generates the next candlestick for a non-empty stock
	//using factors momentum and pressure in order to weigh the probability
	//of positive or negative change
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
		chanceFactor = momentum + pressure;
		if(Math.random() * 4 <= chanceFactor) { //positive roll
			generateCandleStick(POSITIVE);
		}
		else { //negative roll
			generateCandleStick(NEGATIVE);
		}

		//check for out of bounds min/max
		if(priceHistory.size() - recentMax > 1 + candleCount) { //recent max is out of bounds of the current graph
			double maxValue = priceHistory.get(priceHistory.size() - candleCount).getClosePrice();
			int maxIndex = priceHistory.size() - candleCount;
			for(int i = priceHistory.size() - candleCount; i < priceHistory.size(); i++) {
				if(priceHistory.get(i).getClosePrice() > maxValue) {
					maxValue = priceHistory.get(i).getClosePrice();
					maxIndex = i;
				}
			}
			recentMax = maxIndex;
		}

		if(priceHistory.size() - recentMin > candleCount) { //recent max is out of bounds of the current graph
			double minValue = priceHistory.get(priceHistory.size() - candleCount).getClosePrice();
			int minIndex = priceHistory.size() - candleCount;
			for(int i = priceHistory.size() - candleCount; i < priceHistory.size(); i++) {
				if(priceHistory.get(i).getClosePrice() < minValue) {
					minValue = priceHistory.get(i).getClosePrice();
					minIndex = i;
				}
			}
			recentMin = minIndex;
		}

		//correct baselineSum
		if(recentSize == (int)(25 * stability)) {
			baselineSum -= priceHistory.get(priceHistory.size() - (int)(25 * stability)).getClosePrice();
		}
	}

	//Creates a candlestick using random change value
	//boolean type is the type of the candlestick (i.e. true = positive, false = negative)
	public void generateCandleStick(boolean type) {
		double change = Math.pow(Math.random(), 1.4) * ((0.0999 * this.getValue()) * volatility) + (0.0001 * this.getValue());
		if(type == POSITIVE) {
			if(priceHistory.getLast().getType() == NEGATIVE) { //swing from negative -> positive
				//momentum = 2; //reset momentum
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
				//momentum = 2; //reset momentum
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

	//animates the demo stock
	//long startTime is the time since the start of rendering the demo, used to calculate how many candlesticks
	//to render. Int candlestickCount is the max number of candlesticks rendered, int candlestickWidth is the
	//width, in pixels, of an individual candlestick. Graphics g is the graphics object used to draw elements
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

	//draws the last 140 candlesticks of the instance stock in the bounds of the trading menu
	//Graphics g is the graphics object used to draw elements
	public void drawGraph(Graphics g) {
		double topBound = Math.max(priceHistory.get(recentMax).getClosePrice(), 
				priceHistory.get(recentMax).getOpenPrice());
		double bottomBound = Math.min(priceHistory.get(recentMin).getClosePrice(), 
				priceHistory.get(recentMin).getOpenPrice());
		
		drawGraphLines(g);

		int openPixel;
		int closePixel;
		int start = priceHistory.size() - candleCount;
		for (int i = 0; i < candleCount; i++) {
			openPixel = getDrawPixel(175, 670, topBound, bottomBound, priceHistory.get(i + start).getOpenPrice());
			closePixel = getDrawPixel(175, 670, topBound, bottomBound, priceHistory.get(i + start).getClosePrice());
			if(openPixel >= closePixel) { //upward candle
				g.setColor(Color.GREEN);
			}
			else { //downward candle
				g.setColor(Color.RED);
			}
			g.fillRect(10 + i * (1400 / candleCount), Math.min(openPixel, closePixel), (1400 / candleCount), Math.abs(openPixel - closePixel));
		}
	}

	//draws 5 graph lines from the bottom value to the top value
	//Graphics g is the graphics object used to draw elements
	public void drawGraphLines(Graphics g) {
		//draw graph lines
		g.setColor(Color.WHITE);
		g.setFont(price);
		double top = Math.max(priceHistory.get(recentMax).getClosePrice(), 
				priceHistory.get(recentMax).getOpenPrice());
		double bottom = Math.min(priceHistory.get(recentMin).getClosePrice(), 
				priceHistory.get(recentMin).getOpenPrice());
		double valueIncrement = (top - bottom) / 4; //round to nearest 100th

		int yCoord;
		for(int i = 0; i <= 4; i++) {
			yCoord = getDrawPixel(175, 670, top, bottom, bottom + (i * valueIncrement));
			g.drawLine(10, yCoord, 1425, yCoord);
			g.drawString(String.format("%.2f", bottom + (i * valueIncrement)), 1430, yCoord + 2);
		}
		
		//draw break even line
		g.setColor(Color.CYAN);
		yCoord = getDrawPixel(175, 670, top, bottom, getAveragePurchasePrice());
		g.drawLine(10, yCoord, 1425, yCoord);
		g.drawString("AVG", 1430, yCoord + 2);
	}

	//gets the y pixel to draw an element
	//int fieldTop is the y pixel of the top of the field, fieldBottom is the y pixel of the bottom of the field,
	//double topBound is the top value of the graph's range, double bottomBound is the bottom value of the graph's
	//range. Double target is the value which we are looking for the y pixel of
	public int getDrawPixel(int fieldTop, int fieldBottom, double topBound, double bottomBound, double target) {
		return (int) (fieldTop + (fieldBottom - fieldTop) * (1 - (target - bottomBound) / (topBound - bottomBound)));
	}
	
	public void drawAllIndicators(Graphics g) {
		//draw time indicators (past 5 days etc)
		double currentValue = getValue();
		double oneDay = getPastPrice(time);
		double fiveDay = getPastPrice(time + 35); //5 days * 7 hours (9am open -> 4pm close)
		double twentyDay = getPastPrice(time + 140);
		drawIndicator(currentValue, oneDay, 430, 755, g);
		drawIndicator(currentValue, fiveDay, 430, 807, g);
		drawIndicator(currentValue, twentyDay, 430, 860, g);
		
		//draw current price
		g.setColor(Color.WHITE);
		g.drawString(String.format("$%.4f", currentValue),  1090, 755);
		
		//draw "analyst rating" based on our chanceFactor
		int rating = 0;
		if(chanceFactor < 1.8) {
			g.setColor(Color.RED);
			rating = 1;
			if(chanceFactor < 0.6) {
				rating = 0;
			}
		}
		else if(chanceFactor < 2.2) {
			rating = 2;
		}
		else { //chanceFactor > 2.2
			g.setColor(Color.GREEN);
			rating = 3;
			if(chanceFactor > 3.15) {
				rating = 4;
			}
		}
		g.drawString(ratings[rating], 1090, 807);

		//draw stock ticker (symbol)
		g.setColor(Color.WHITE);
		g.drawString(symbol, 20, 160);

		//draw amount held
		if(amountHeld == 0) {
			g.setColor(Color.WHITE);
			g.drawString("0.00% (0)", 1090, 860);
			return; //to avoid zero division later on
		}
		else if(amountHeld * getValue() >= totalPurchasePrice) { //positive position
			g.setColor(darkGreen);
		}
		else { //negative position
			g.setColor(darkRed);
		}

//		g.drawString(String.format("%d @ $%.2f (%+.2f%%)", amountHeld, getAveragePurchasePrice(),
//				getProfitLoss(false)), 1090, 860);
		g.drawString(String.format("%.4f%% (%+.2f)", getProfitLoss(false), getProfitLoss(true)), 1090, 860);
	}
	
	public double getPastPrice(int hoursAgo) {
		return priceHistory.get(priceHistory.size() - 1 - hoursAgo).getClosePrice();
	}
	
	//compares stock objects by a default order of amount held decreasing
	public int compareTo(Stock compare) {
		return compare.getAmountHeld() - amountHeld;
	}
	
	public void drawIndicator(double currentValue, double pastValue, int x, int y, Graphics g) {
		g.setFont(body);
		double diff = currentValue - pastValue;
		String result = "";
		if(diff >= 0) {
			g.setColor(Color.GREEN);
			result += "(";
		}
		else { //diff < 0
			g.setColor(Color.RED);
			result += "(";
		}
		g.drawString(String.format("%.4f%% %s", diff / pastValue * 100, result + String.format("%+.2f", diff) + ")"), x, y);
	}
	
	//boolean bypass dictates whether to bypass the checked recount (i.e. only check if recentMax
	//has left the bounds of the graph) if true, this check will be skipped
	public void recalculateRecents(boolean bypass) {
		int lastIndex = 1 + candleCount;
		if(priceHistory.size() - recentMax > 1 + lastIndex || bypass) { //recent max is out of bounds of the current graph
			double maxValue = priceHistory.get(priceHistory.size() - lastIndex).getClosePrice();
			int maxIndex = priceHistory.size() - lastIndex;
			for(int i = priceHistory.size() - lastIndex; i < priceHistory.size(); i++) {
				if(priceHistory.get(i).getClosePrice() > maxValue) {
					maxValue = priceHistory.get(i).getClosePrice();
					maxIndex = i;
				}
			}
			recentMax = maxIndex;
		}

		if(priceHistory.size() - recentMin > lastIndex || bypass) { //recent max is out of bounds of the current graph
			double minValue = priceHistory.get(priceHistory.size() - candleCount).getClosePrice();
			int minIndex = priceHistory.size() - lastIndex;
			for(int i = priceHistory.size() - lastIndex; i < priceHistory.size(); i++) {
				if(priceHistory.get(i).getClosePrice() < minValue) {
					minValue = priceHistory.get(i).getClosePrice();
					minIndex = i;
				}
			}
			recentMin = minIndex;
		}
	}
	
	public void addOrder(HashMap<Stock, Integer> destination, int amount) {
		if(destination.get(this) != null) { //order is already present
			destination.put(this, destination.get(this) + amount); //add to the order
		}
		else {
			destination.put(this, amount); //add to destination as normal
		}
	}
}