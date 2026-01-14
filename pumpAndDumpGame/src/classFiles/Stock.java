package classFiles;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import javax.swing.JOptionPane;

import pumpAndDumpGame.PumpAndDumpGame;

public class Stock implements Comparable<Stock> {

	private static ArrayList<Stock> testMarket = new ArrayList<>();
	private static ArrayList<Stock> market = new ArrayList<>();
	private static ArrayList<Stock> selectedMarket = market;
	private static Font price = new Font("Arial", Font.PLAIN, 20);
	private static Font small = new Font("Arial", Font.PLAIN, 26);
	private static Font body = new Font("Arial", Font.PLAIN, 40);
	private static int time = 0; //int # of hours since "day" started
	private static String[] ratings = {"Strong sell", "Sell", "Neutral", "Buy", "Strong buy"};
	private static int[] views = {7, 35, 140, 1400}; //array of different view history options, in # of candles (hours)
	private static Color darkGreen = new Color(66, 176, 78);
	private static Color darkRed = new Color(187, 46, 40);
	private static HashMap<Stock, Integer> buyOrders = new HashMap<>(); //yet-to-be-filled buy orders, should be cleared upon each stock refresh
	private static HashMap<Stock, Integer> sellOrders = new HashMap<>(); //yet-to-be-filled buy orders, should be cleared upon each stock refresh
	private static int tinyCandleCount = 35; //stores candlecount when list view overwrites it
	private final static boolean POSITIVE = true;
	private final static boolean NEGATIVE = false;

	private ArrayList<Candlestick> priceHistory;
	private String symbol;
	private double volatility; //1.3 to 2.55
	private double baselineSum; //sum used for rolling average
	private double momentum; //-1 to 1
	private double maxPrice;
	private double minPrice;
	private double seedPrice; //the initial price of the stock, used for regenerating stocks
	private int recentMax; //stores the INDEX of the recent max value
	private int recentMin; //stores the INDEX of the recent min value
	private int candleCount; //makes it possible to save a different view for each stock
	private double targetGrowth; //as a % (e.g. 1.005 means target gain of 0.5%)
	private double stability; //modifies the pool history for rolling average
	private double chanceFactor; //0.3 to 3.55 sum of volatility and momentum
	private double targetFlipChance; //decimal chance for target to flip on any given refresh
	private Image icon; //stock/company logo
	
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
	public Stock(String symbol, int initialPrice, 
			double volatility, double targetGrowth, 
			double stability, double targetFlipChance) {
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
	
	public static int getTime() {
		return time;
	}
	
	public double getVolatility() {
		return volatility;
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
	
	public Image getIcon() {
		return icon;
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
	
	public void setIcon(Image icon) {
		this.icon = icon;
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
	
	public static void loadStocks() {
		try {
			BufferedReader fileIn = new BufferedReader(new FileReader("gameFiles/stockList.txt"));
			while(fileIn.readLine() != null) { //skip the comment line
				Stock tempStock;
				StringTokenizer temp = new StringTokenizer(fileIn.readLine(), " ");
				tempStock = (new Stock(temp.nextToken(), //symbol
						Integer.parseInt(temp.nextToken()), //initial price
						Double.parseDouble(temp.nextToken()), //volatility
						Double.parseDouble(temp.nextToken()), //growthTarget
						Double.parseDouble(temp.nextToken()), //stability
						Double.parseDouble(temp.nextToken()))); //targetFlipChance
				tempStock.setIcon(Toolkit.getDefaultToolkit().getImage(
						"gameFiles/stockIcons/" + tempStock.getSymbol() + ".png"));
				market.add(tempStock);
			}
			fileIn.close();
		} catch (FileNotFoundException e) {
			System.out.println("file not found");
		} catch (IOException e) {
			System.out.println("stock reading error");
		}
	}
	
	//returns true if timechange rolls into a new day
	//returns false otherwise
	public static boolean incrementTime() {
		time++;
		if(time > 7) {
			time = 0; //reset time
			//chance for stocks w/ targetFlipping = true to have their trend bias inverted
			for(Stock stock : selectedMarket) {
				if(Math.random() < stock.getTargetFlipChance()) {
					stock.invertTarget();
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
			g.setFont(small);
			if(selectedMarket.get(i).getProfitLoss(NEGATIVE) >= 0) { //positive position
				g.setColor(darkGreen);
			}
			else {
				g.setColor(darkRed);
			}
			g.drawImage(selectedMarket.get(i).getIcon(), 1520, 50 * i + 185, 30, 30, null);
			g.drawString(selectedMarket.get(i).getSymbol(), 1560, 50 * i + 210);
			g.drawString(String.format("%+.1f%% (%+.2f)", selectedMarket.get(i).getProfitLoss(NEGATIVE),
					selectedMarket.get(i).getProfitLoss(POSITIVE)),
					1650, 50 * i + 210);
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
		int recentSize = Math.min(priceHistory.size(), (int)(25 * stability));

		//baselinePrice is the average price of the last stability * 25 number of candlesticks * targetGrowth
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
			for(int i = priceHistory.size() - candleCount - 1; i < priceHistory.size(); i++) {
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
			for(int i = priceHistory.size() - candleCount - 1; i < priceHistory.size(); i++) {
				//second condition can happen when lowest item is an upward candle
				if(priceHistory.get(i).getClosePrice() < minValue || priceHistory.get(i).getOpenPrice() < minValue) {
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
		double change = Math.pow(Math.random(), 1.6) * ((0.0999 * this.getValue()) * volatility) + (0.0001 * this.getValue());
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
	
	public void drawTinyGraph(int startX, int startY, Graphics g) {
		//calculate min and max
		int tinyMax = priceHistory.size() - 36, tinyMin = tinyMax;
		for(int i = priceHistory.size() - 35; i < priceHistory.size(); i++) {
			if(priceHistory.get(i).getClosePrice() > priceHistory.get(tinyMax).getClosePrice()) {
				tinyMax = i;
			}
			else if(priceHistory.get(i).getClosePrice() < priceHistory.get(tinyMin).getClosePrice()) {
				tinyMin = i;
			}
		}
		double topBound = Math.max(priceHistory.get(tinyMax).getClosePrice(), 
				priceHistory.get(tinyMax).getOpenPrice());
		double bottomBound = Math.min(priceHistory.get(tinyMin).getClosePrice(), 
				priceHistory.get(tinyMin).getOpenPrice());
		
		int openPixel;
		int closePixel;
		int start = priceHistory.size() - tinyCandleCount;
		for (int i = 0; i < tinyCandleCount; i++) {
			openPixel = getDrawPixel(startY + 15, startY + 105, topBound, bottomBound, priceHistory.get(i + start).getOpenPrice());
			closePixel = getDrawPixel(startY + 15, startY + 105, topBound, bottomBound, priceHistory.get(i + start).getClosePrice());
			if(openPixel >= closePixel) { //upward candle
				g.setColor(Color.GREEN);
			}
			else { //downward candle
				g.setColor(Color.RED);
			}
			g.fillRect(startX + 345 + i * (4), Math.min(openPixel, closePixel), (4), Math.abs(openPixel - closePixel));
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
		if(amountHeld > 0 && getAveragePurchasePrice() > bottom && getAveragePurchasePrice() < top) {
			g.setColor(Color.CYAN);
			yCoord = getDrawPixel(175, 670, top, bottom, getAveragePurchasePrice());
			g.drawLine(10, yCoord, 1425, yCoord);
			g.drawString("AVG", 1430, yCoord + 2);
		}
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

		//draw stock icon
		g.drawImage(icon, 15, 105, 60, 60, null);
		
		//draw stock ticker (symbol)
		g.setColor(Color.WHITE);
		g.drawString(symbol, 90, 150);

		//draw amount held
		if(amountHeld == 0) {
			g.setColor(Color.WHITE);
			g.drawString("$0.00 (+0)", 1090, 860);
			return; //to avoid zero division later on
		}
		else if(amountHeld * getValue() >= totalPurchasePrice) { //positive position
			g.setColor(Color.GREEN);
		}
		else { //negative position
			g.setColor(Color.RED);
		}

//		g.drawString(String.format("%d @ $%.2f (%+.2f%%)", amountHeld, getAveragePurchasePrice(),
//				getProfitLoss(false)), 1090, 860);
		g.drawString(String.format("$%.2f (%+.2f%%)", amountHeld * getValue(), getProfitLoss(NEGATIVE)), 1090, 860);
	}
	
	public void drawListIndicator(int x, int y, Graphics g) {
		double refPrice = getPastPrice(35);
		if(getValue() >= refPrice) {
			g.setColor(Color.GREEN);
		}
		else {
			g.setColor(Color.RED);
		}
		g.drawString(String.format("$%.2f (%+.2f%%)", getValue(), 100 * (getValue() - refPrice) / refPrice),
				x + 10, y + 110);
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
	
	public void drawIndicator(double currentValue, double pastValue, int x, int y, Font font, Graphics g) {
		g.setFont(font);
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
		int lastIndex = 2 + candleCount;
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
	
	public static void clearHoldings() {
		for(Stock stock : selectedMarket) {
			stock.setAmountHeld(0);
		}
	}

	public static void saveStocks(int saveNumber) {
		//save stock data
		for(Stock stock : selectedMarket) { //for every stock
			try {
				PrintWriter fileOut = new PrintWriter
						("gameFiles/saves/stockData/" + stock.getSymbol() + "/" + saveNumber + ".txt");
				
				//write player variables
				fileOut.println(stock.amountHeld + " " + stock.totalPurchasePrice);
				
				Candlestick temp = stock.getPriceHistory().get(stock.getPriceHistory().size() - 1401);
				fileOut.println(temp.getOpenPrice() + " " + temp.getClosePrice());
				for(int i = stock.getPriceHistory().size() - 1400; i < stock.getPriceHistory().size(); i++) { //last 1400 candles
					temp = stock.getPriceHistory().get(i);
					fileOut.println(temp.getClosePrice());
				}
				
				fileOut.flush();
				fileOut.close();
			} catch (FileNotFoundException e) {
				System.out.println("Error writing stock " + stock.getSymbol());
			} 
		}
	}
	
	public static void loadStocksFromSave(int saveNumber) {
		for(Stock stock : selectedMarket) { //for every stock
			stock.priceHistory.clear(); //clear previous candlesticks
			try {
				BufferedReader fileIn = new BufferedReader(new FileReader("gameFiles/saves/stockData/" + stock.getSymbol() + "/" + saveNumber + ".txt"));
				StringTokenizer st = new StringTokenizer(fileIn.readLine(), " ");
				//read in user-specific data
				stock.amountHeld = Integer.parseInt(st.nextToken());
				stock.totalPurchasePrice = Double.parseDouble(st.nextToken());
				//add initial candleStick
				st = new StringTokenizer(fileIn.readLine(), " "); //data for first candlestick which has both open and end
				double prevPrice; //saves the price of the last candlestick since the while loop isn't indexed
				stock.priceHistory.add(new Candlestick(Double.parseDouble(st.nextToken()), prevPrice = Double.parseDouble(st.nextToken())));
				
				//read in previous price history
				String data;
				while((data = fileIn.readLine()) != null) {
					stock.priceHistory.add(new Candlestick(prevPrice, Double.parseDouble(data)));
					prevPrice = stock.priceHistory.getLast().getClosePrice();
				}
				
				stock.recalculateRecents(true);
			} catch (FileNotFoundException e) {
				System.out.println("stock file not found!");
			} catch (IOException e) {
				System.out.println("Reading error");
			}
		}
	}
		
		
	// part of the chatbot
	public static String reply(String str) { // returns reply
		ArrayList<String> numberKeyWords = new ArrayList<>();
		
		String action = "";
		Stock stock = null;
		String stockName = "";
		int amount;
		
		HashMap<String, Double> actionSentiment = new HashMap<String, Double>();
		actionSentiment.put("buy", 0.0d); // -1 to 1
		actionSentiment.put("sell", 0.0d);
		//actionSentiment.put("info", 0.0d);
		
		HashMap<String, Double> stockSentiment = new HashMap<String, Double>();
		for (int i = 0; i < market.size(); i++) {
			stockSentiment.put(market.get(i).symbol, 0.0d);
		}
		
		str.toLowerCase();
		
		String[] tokens = str.split(" ");
		
		//XXX data type
		HashMap<Integer, Double> numberPlausibility = new HashMap<>();
		for (int i = 0; i < tokens.length; i++) {
			for (String s : actionSentiment.keySet()) {
				//System.out.printf("%f: %s + %s%n", Chatbot.similarity(tokens[i], s), tokens[i], s);
				actionSentiment.put(s, Math.max(actionSentiment.get(s), Chatbot.similarity(s, tokens[i])));
			}
			for (String s : stockSentiment.keySet()) {
				stockSentiment.put(s, stockSentiment.get(s) + Chatbot.similarity(s.toLowerCase(), tokens[i]));
			}
			
			try {
				numberPlausibility.put(Integer.parseInt(tokens[i]), 0.0);
			} catch (NumberFormatException e) {
				// not a number
			}
		}
		
		
		double maxSentiment = -99999; //XXX
		for (String symbol : stockSentiment.keySet()) {
			//System.out.printf("%s: %f/%f%n", symbol, stockSentiment.get(symbol), maxSentiment);
		    if (stockSentiment.get(symbol) > maxSentiment) {
		    	//System.out.println("Symbol change");
		        maxSentiment = stockSentiment.get(symbol);
		        stockName = symbol;
		    }
		}
		
		
		//TODO implement "keywords" (half, all, quarter, etc)
		int sum = 0;
		for (Integer i : numberPlausibility.keySet()) {
			sum += i;
		}
		amount = sum/numberPlausibility.values().size();
		
		
		//stock = Collections.binarySearch(market, new Stock());
		ArrayList<String> names = new ArrayList<>();
		for (Stock s : market) {
			if (s.getSymbol().equalsIgnoreCase(stockName)) {
				stock = s;
			}
		}
		
		if (actionSentiment.get("buy") > actionSentiment.get("sell")) {
			stock.addOrder(buyOrders, amount);
			return String.format("Buying %d shares of %s", amount, stockName);
		} else {
			stock.addOrder(sellOrders, amount);
			return String.format("Selling %d shares of %s", amount, stockName);
		}
	}
}