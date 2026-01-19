package pumpAndDumpGame;
import java.awt.event.*;
import java.awt.geom.Arc2D;
import java.io.*;
import java.security.PublicKey;
import java.util.*;
import java.util.Map.Entry;
import java.awt.*;

import javax.sound.sampled.*;
import javax.swing.*;

import classFiles.*; //import all files from classFiles package

//Nathan Chan and Jerry Li Jan 14, 2026
//Pump and Dump game!
//this file is the "driver" file for our game, which is essentially a paper trading/crypto trading sim.
//In our game, you start with a certain amount of money, and your goal is to amass as much money as possible
//by making profitable trades. The game provides the player with 12 stocks to invest in, each with their own
//risk levels and entry levels. Keep in mind that you can also lose money, so invest wisely!
@SuppressWarnings("serial") //funky warning, just suppress it. It's not gonna do anything.
public class PumpAndDumpGame extends JPanel implements Runnable, KeyListener, MouseListener{
	
	//self explanatory variables
	boolean validInput;
	int FPS = 100;
	Thread thread;
	int screenWidth = 1900;
	int screenHeight = 1000;
	
	long startTime, timeElapsed, demoStartTime, lastCycle;
	int frameCount = 0;
	
	int gameState = 0;
	final int TUTORIAL = -1, MAINMENU = 0, SAVESELECT = 1, STOCKLIST = 2, TRADINGSCREEN = 3;
	
	Color darkGray = new Color(22, 22, 22);
	Color darkGrayTransparent = new Color(22, 22, 22, 240); //transparent layer for tutorial
	Color backGround = new Color(48, 49, 51);
	Font title = new Font ("Arial", Font.BOLD, 100);
	Font header = new Font("Arial", Font.BOLD, 50);
	Font body = new Font("Arial", Font.PLAIN, 25);
	Font body2 = new Font("Arial", Font.PLAIN, 40);
	Font wormTongue = new Font("Courier New", Font.PLAIN, 18);
	FontMetrics fmTitle;
	FontMetrics fmBody;
	
	Image menuTitle, menuPlayButton, menuTutorialButton;
	Image backButton;
	Image tutorialArrows;
	ArrayList<Image> tutorial = new ArrayList<>(); //AL to hold the images for the tutorial slideshow
	int tutorialPage = 0;
	Image loadingCircle;
	Image saveSelect;
	Image stockList;
	Image tradingView, selectionPill;
	
	String hitSoundPath = "gameFiles/soft-hitnormal.wav";
	String orderFilledPath = "gameFiles/orderFilled.wav";
	Clip temp;
	
	ArrayList<Stock> selectedMarket;
	
	//testing
	Stock demo = new Stock("DEMO", 10, 2, 1, 1, 0);
	Stock test = new Stock("TEST", 10, 0.01, 1.008, 3, 0);
	Stock test2 = new Stock("TST2", 100, 0.5, 1.0005, 1, 0.1);
	
	Chatbot saruman = new Chatbot();
	
	int savePageNum = 0;
	
	//player variables
	int saveNumber = 0;
	String name;
	double money;
	double initialMoney;
	double portfolioValue;
	Stock currentStock = test2;
	boolean hardMode = true;
	
	//constructor
	public PumpAndDumpGame() {
		//sets up JPanel
		setPreferredSize(new Dimension(screenWidth, screenHeight));
		setVisible(true);
		
		//starting the thread
		thread = new Thread(this);
		thread.start();
	}
	
	@Override
	public void run() {
		System.out.println("Thread: Starting thread");
		initialize();
		while(true) {
			//main game loop
			update();
			this.repaint();
			try {
				Thread.sleep(1000/FPS);
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void initialize() {
		//setups before the game starts running
		System.out.println("Thread: Initializing game");
		
		Stock.loadStocks();
		
		selectedMarket = Stock.getMarket();
//		selectedMarket.add(test);
//		selectedMarket.add(test2);
		startTime = System.currentTimeMillis();
		
		timeElapsed = 0;
		FPS = 100;
		
		//for every stock
		for(int i = 0; i < selectedMarket.size(); i++) {
			for(int j = 0; j < 2555; j++) { //generate 1 year of candlesticks
				selectedMarket.get(i).nextCandleStick();
				Stock.incrementTime();
			}
		}
		
		//generate first candlesticks for demo and test
		for(int i = 0; i < 200; i++) {
			demo.nextCandleStick();
			test.nextCandleStick();
		}
		
		gameState = MAINMENU;
		demoStartTime = System.currentTimeMillis();
		
		//load tutorial
		tutorialArrows = Toolkit.getDefaultToolkit().getImage("gameFiles/tutorial/arrows.png");
		for(int i = 1; i <= 5; i++) {
			tutorial.add(Toolkit.getDefaultToolkit().getImage
					("gameFiles/tutorial/" + String.format("%02d", i) + ".png"));
		}
		//load other images
		backButton = Toolkit.getDefaultToolkit().getImage("gameFiles/backButton.png");
		menuTitle = Toolkit.getDefaultToolkit().getImage("gameFiles/menuTitle.png");
		menuPlayButton = Toolkit.getDefaultToolkit().getImage("gameFiles/playButton.png");
		menuTutorialButton = Toolkit.getDefaultToolkit().getImage("gameFiles/tutorialButton.png");
		stockList = Toolkit.getDefaultToolkit().getImage("gameFiles/stockList.png");
		saveSelect = Toolkit.getDefaultToolkit().getImage("gameFiles/saveSelect.png");
		loadingCircle = Toolkit.getDefaultToolkit().getImage("gameFiles/loadingCircle.png"); 
		tradingView = Toolkit.getDefaultToolkit().getImage("gameFiles/tradingScreen.png");
		selectionPill = Toolkit.getDefaultToolkit().getImage("gameFiles/selectionPill.png");
		
		//load saves
		Save.cacheSaves();
		System.out.println("Thread: Done initializing game");
	}
	
	public void update() {
		//update timeElapsed and check if market refresh is due
		timeElapsed = System.currentTimeMillis() - startTime;
		if((gameState == TRADINGSCREEN || gameState == STOCKLIST) && System.currentTimeMillis() - lastCycle > 3000) {
			refreshMarket();
		}
		frameCount++;
	}
	
	//1. Generates one new candlestick for every stock
	//2. increments time (which is used for %change indicators)
	//3. updates portfolio value and sorts by amount held
	//returns nothing
	public void refreshMarket() {
		portfolioValue = 0;
		//generate new candlestick for every stock
		for(Stock s : selectedMarket) {
			s.nextCandleStick();
		}
		
		Stock.incrementTime();
		
		//update lastCycle to now
		lastCycle = System.currentTimeMillis();
		
		//fill buy/sell orders
		fillOrders(Stock.getBuyOrders(), true);
		fillOrders(Stock.getSellOrders(), false);
		
		for(Stock s : selectedMarket) {
			portfolioValue += s.getAmountHeld() * s.getValue(); //increment portfolioValue
		}
		
		//sorting here means 1 sort per 3 seconds as opposed to every repaint
		if(gameState == TRADINGSCREEN) {
			Collections.sort(selectedMarket); //default sort by amount held
		}
	}
	
	//fills the orders in the given HashMap
	//HashMap<Stock, Integer> orders is the hashmap storing the orders,
	//in a stock/amount key-value pair
	//mode == true is buy, mode == false is sell
	//returns nothing
	public void fillOrders(HashMap<Stock, Integer> orders, boolean mode) {
		//read in orders as pairs to keep information together
		for(Entry<Stock, Integer> entry : orders.entrySet()) {
			Stock stock = entry.getKey();
			int amount = entry.getValue();
			//check sufficient funds / shares
			if(mode) { //buy orders; check sufficient funds
				if(stock.getValue() * amount > money) {
					Notification.addNotification("ORDER FAILED",
							"Order for " + amount + " shares of " + stock.getSymbol() + " has" +  
							"\nfailed. Reason: Insufficient funds", hitSoundPath);
				}
				else {
					Notification.addNotification("ORDER FILLED", String.format(
							"Successfully purchased " + amount + " shares of\n" + stock.getSymbol()
							+ " for $%.2f/share. ($%.2f)", stock.getValue(),
							stock.getValue() * amount), orderFilledPath);
					
					money -= stock.getValue() * amount; //update money
					stock.incrementTotalPurchasePrice(stock.getValue() * amount);
					stock.incrementAmountHeld(amount);
				}
			}
			else { //!buy; sell orders
				if(amount > stock.getAmountHeld()) {
					Notification.addNotification("ORDER FAILED",
							"Order to sell " + amount + " shares of " + stock.getSymbol() + " has" +  
							"\nfailed. Reason: Insufficient shares", hitSoundPath);
				}
				else {
					Notification.addNotification("ORDER FILLED", String.format(
							"Successfully sold " + amount + " shares of\n" + stock.getSymbol()
							+ " for $%.2f/share. ($%.2f)", stock.getValue(),
							stock.getValue() * amount), orderFilledPath);
					
					money += stock.getValue() * amount; //update money
					//increment total first using old amount held
					stock.incrementTotalPurchasePrice(stock.getAveragePurchasePrice() * -amount);
					stock.incrementAmountHeld(-amount); //increment amount held
				}
			}
		}
		orders.clear(); //clear the orders as they have all been reviewed
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g; //We can use g2d if we need something g doesn't have
		if(gameState == MAINMENU) {
			drawMenu(g);
		}
		else if(gameState == TUTORIAL) {
			drawTutorial(g);
		}
		else if(gameState == SAVESELECT) {
			drawSaveSelect(g);
		}
		else if(gameState == STOCKLIST) {
			drawStockList(g);
		}
		else if(gameState == TRADINGSCREEN) {
			drawTradingScreen(g);
		}
	}
	
	//loads main menu by refreshing demo stock and changing gamestate
	//returns nothing
	public void loadMenu() {
		demo = new Stock("DEMO", 10, 2, 1, 1, 0);
		for(int i = 0; i < 200; i++) {
			demo.nextCandleStick();
		}
		gameState = MAINMENU;
		demoStartTime = System.currentTimeMillis();
		//change gamestate
		gameState = MAINMENU;
	}
	
	//Draws the main menu
	//Graphics g is the graphics object used to draw elements
	public void drawMenu(Graphics g) {
		//draw background 
		g.setColor(darkGray);
		g.fillRect(0, 0, screenWidth, screenHeight);

		//draw demo graph
		demo.drawDemo(demoStartTime, 200, 10, g);
		
		g.drawImage(menuTitle, 0, 0, 1900, 1000, this);
		g.drawImage(menuPlayButton, 600, 500, 700,  300, this);
		g.drawImage(menuTutorialButton, 600, 730, 700,  300, this);
	}
	
	//Draws the tutorial OVERTOP of the main menu
	//Graphics g is the graphics object used to draw elements
	public void drawTutorial(Graphics g) {
		drawMenu(g); //main menu will be the background for the tutorial
		//darken the main menu by using a semi-transparent layer
		g.setColor(darkGrayTransparent);
		g.fillRect(0, 0, screenWidth, screenHeight);
		
		//draw tutorial page
		g.drawImage(tutorial.get(tutorialPage), 0, 0, 1900, 1000, this);
		
		//draw page number
		g.setFont(title);
		g.setColor(Color.WHITE);
		g.drawString(tutorialPage + 1 + " / " + tutorial.size(), 860, 960);
		
		//draw back button
		g.drawImage(backButton, 10, 10, 80, 80, this);
		
		//draw arrows
		g.drawImage(tutorialArrows, 0, 0, 1900, 1000, this);
	}
	
	
	//updates tutorialPage, wrapping back to zero after last image
	//boolean increase defines the direction of change, increase == true
	//means page++, !increase means page--
	public void incrementTutorial(boolean increase) {
		if(increase) {
			if(tutorialPage == tutorial.size() - 1) { //wrap back to front
				tutorialPage = 0;
			}
			else {
				tutorialPage++;
			}
		}
		else { //decrease
			if(tutorialPage == 0) {
				tutorialPage = tutorial.size() - 1;
			}
			else {
				tutorialPage--;
			}
		}
	}
	
	//loads save select by loading save objects from disk then changing gamestate
	//returns nothing
	public void loadSaveSelect() {
		Save.cacheSaves(); //update save list
		gameState = SAVESELECT;
	}
	
	//draws the save select screen, where the user can either make a new save
	//or open an old save.
	//Graphics g is the graphics object used to draw elements
	public void drawSaveSelect(Graphics g) {
		g.drawImage(saveSelect, 0, 0, 1900, 1000, this);
		g.drawImage(backButton, 10, 10, 80, 80, this);
		Save.drawSaves(savePageNum, g);

		//draw page number
		g.setFont(title);
		g.setColor(Color.WHITE);
		g.drawString(savePageNum + 1 + " / " + ((Save.getSaves().size() - 1) / 6 + 1), 860, 960);
		
		//draw arrows
		g.drawImage(tutorialArrows, 0, 0, 1900, 1000, this);
	}
	
	//increments the save page list if there are >6 saves on disk
	//boolean increase defines direction of increment, true means page++
	public void incrementSavePage(boolean increase) {
		int maxPage = (Save.getSaves().size() - 1) / 6;
		if(increase) {
			if(savePageNum == maxPage) { //wrap back to front
				savePageNum = 0;
			}
			else {
				savePageNum++;
			}
		}
		else { //decrease
			if(savePageNum == 0) {
				savePageNum = maxPage;
			}
			else {
				savePageNum--;
			}
		}
	}
	
	//loads the stock list by sorting the market by volatility and changing gamestate
	public void loadStockList() {
		Collections.sort(selectedMarket, new SortByVolatility());
		gameState = STOCKLIST;
	}
	
	//draws the stockList and chatbot window
	//Graphics g is the graphics object used to draw elements
	public void drawStockList(Graphics g) {
		fmBody = g.getFontMetrics(body);
		g.drawImage(stockList, 0, 0, 1900, 1000, this);
		g.setColor(Color.WHITE);
		g.setFont(body2);
		//draw money
		g.drawString(String.format("$%,.2f (+$%,.2f)", money, portfolioValue), 120, 65);
		//draw name
		g.drawString(name, 790, 65);
		
		//System.out.println(saruman.isTyping());
		ArrayList<String> subMessages = new ArrayList<>(saruman.getMessages().subList(Math.max(saruman.getMessages().size() - 17, 0), saruman.getMessages().size()));
		g.setColor(Color.WHITE);
		g.setFont(wormTongue);
		for (int i = 0; i < subMessages.size(); i++) {
			g.drawString(subMessages.get(i), 1580, 270 + i * 30);
		}
		
		if (saruman.isTyping()) { // draw current message + pointer

			g.drawString(saruman.getCurrentMessage(), 1580, 775);
			g.fillRect(1580 + saruman.getCurrentMessage().length() * 11, 750, 1, 45);
		}
		
		int startX, startY;
		int index = 0;
		for(Stock s : selectedMarket) {
			startX = 50 + (index / 4) * 500;
			startY = 300 + (index % 4) * 120;
			//draw graph background behind all text just in case
			g.setColor(darkGray);
			g.fillRoundRect(startX + 335, startY + 5, 160, 110, 10, 10);
			g.setColor(Color.WHITE);
			g.drawRect(startX, startY, 500, 120);
			//draw back button
			//draw icon
			g.drawImage(s.getIcon(), startX + 5, startY + 5, 70, 70, this);
			//draw symbol
			g.setFont(header);
			g.drawString(s.getSymbol(), startX + 80, startY + 60);
			g.setFont(body);
			//draw amount held
			if(s.getAmountHeld() > 0) {
				g.drawString("" + s.getAmountHeld(), startX + 320 - fmBody.stringWidth("" + s.getAmountHeld()), startY + 70);
			}
			//draw today's change
//			s.drawIndicator(s.getValue(), s.getPastPrice(Stock.getTime() + 35), startX + 10, startY + 110, body, g);
			s.drawListIndicator(startX, startY, g);
			index++;
			//draw graph
			s.drawTinyGraph(startX, startY, g);
			//if shares held, draw P&L indicator
			if(s.getAmountHeld() > 0) {
				int[] xCoordinates = {startX + 305, startX + 290, startX + 320};
				int[] yCoordinates;
				if(s.getProfitLoss(true) >= 0) { //positive position
					g.setColor(Color.GREEN);
					int[] tempYCoordinates = {startY + 10, startY + 40, startY + 40};
					yCoordinates = tempYCoordinates;
				}
				else { //negative position
					g.setColor(Color.RED);
					int[] tempYCoordinates = {startY + 40, startY + 10, startY + 10};
					yCoordinates = tempYCoordinates;
				}
				g.fillPolygon(xCoordinates, yCoordinates, 3);
			}
		}
		
		Notification.drawNotifications(g, false);
	}
	
	//loads the trading screen by sorting the market by amount held ($)
	//and changing gamestate
	//Stock s is the stock to load the trading screen of
	public void loadTradingScreen(Stock s) {
		currentStock = s;
		Collections.sort(selectedMarket); //default sort by amount held
		gameState = TRADINGSCREEN;
	}
	
	//Draws the trading screen
	//Stock s is the stock to be drawn
	//Graphics g is the graphics object used to draw elements
	public void drawTradingScreen(Graphics g) { 
		g.drawImage(tradingView, 0, 0, 1900, 1000, this);
		currentStock.drawGraph(g);
		currentStock.drawAllIndicators(g);
		
		//draw timeline selection
		if(currentStock.getCandleCount() == 140) { //20D
			g.drawImage(selectionPill, 269, 40, 100, 50, this);
		}
		else if(currentStock.getCandleCount() == 7) { //1D
			g.drawImage(selectionPill, 106, 40, 60, 50, this);
		}
		else if(currentStock.getCandleCount() == 35) { //5D
			g.drawImage(selectionPill, 180, 40, 70, 50, this);
		}
		else if(currentStock.getCandleCount() == 1400) { //MAX
			g.drawImage(selectionPill, 390, 40, 116, 50, this);
		}
		
		//draw money
		g.setColor(Color.WHITE);
		g.drawString(String.format("$%,.2f (+$%,.2f)", money, portfolioValue), 530, 65);
		
		//draw name
		g.drawString(name, 1185, 65);
		
		//draw current holdings
		Stock.drawHoldings(g);
		
		if(hardMode) {
			//draw hard mode
			g.setFont(body2);
			g.setColor(backGround);
			g.fillRect(800, 765, 600, 55);
			g.setColor(Color.WHITE);
			g.drawString("(Press H for easy mode)", 800, 805);
		}
		
		
		//draw notifications
		Notification.drawNotifications(g, true);
	}

	//starts a new game and updates the current save number file
	public void startGame() {
		//clear holdings from any other saves loaded this session
		Stock.clearHoldings();
		Notification.clearNotifications();
		saruman = new Chatbot(); //reload saruman
		
		//load sounds
		loadSounds();
		
		//sort stocks by volatility
		Collections.sort(selectedMarket, new SortByVolatility());
		
		//get next save number
		try {
			Scanner fileIn = new Scanner(new File("gameFiles/saves/saveData.txt"));
			saveNumber = Integer.parseInt(fileIn.nextLine()) + 1;
			fileIn.close();
			PrintWriter fileOut = new PrintWriter(new File("gameFiles/saves/saveData.txt"));
			fileOut.print(saveNumber);
			fileOut.flush();
			fileOut.close();
		} catch (FileNotFoundException e) {
			System.out.println("file not found");
		}
		gameState = STOCKLIST;
		lastCycle = System.currentTimeMillis();
	}
	
	//starts the game from save
	public void startGameFromSave() {
		Stock.clearHoldings(); //clear holdings from other saves
		Notification.clearNotifications(); //clear notifications
		saruman = new Chatbot();
		
		//load sounds
		loadSounds();
		
		//load player data
		try {
			BufferedReader playerData = new BufferedReader(new FileReader("gameFiles/saves/playerData/" + saveNumber + ".txt"));
			name = playerData.readLine();
			money = Double.parseDouble(playerData.readLine());
			initialMoney = Double.parseDouble(playerData.readLine());
			portfolioValue = Double.parseDouble(playerData.readLine());
			hardMode = Boolean.parseBoolean(playerData.readLine());
			playerData.close();
		} catch (FileNotFoundException e) {
			System.out.println("Player data not found!");
		} catch (IOException e) {
			System.out.println("Reading error");
		}
		
		//load stock data
		Stock.loadStocksFromSave(saveNumber);
		
		Collections.sort(selectedMarket, new SortByVolatility());
		
		gameState = STOCKLIST;
		lastCycle = System.currentTimeMillis();
		
		Notification.addNotification("GAME LOADED", "Welcome back, " + name + "!", hitSoundPath);
	}
	
	//loads sounds into memory to remove delay on first playback
	public void loadSounds() {
		//load sounds into memory (so that first play isn't delayed)
		try {
			AudioInputStream player;
			BufferedReader soundLoader = new BufferedReader(new FileReader("gameFiles/soundFilePaths.txt"));
			String data;
			while((data = soundLoader.readLine()) != null) {
				player = AudioSystem.getAudioInputStream(new File (data));
				temp = AudioSystem.getClip();
				temp.open(player);
			}
			soundLoader.close();
		} catch (UnsupportedAudioFileException e) {
			System.out.println("Unsupported file");
		} catch (IOException e) {
			System.out.println("File error");
		} catch (LineUnavailableException e) {
			System.out.println("Line unavailable");
		}
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void keyPressed(KeyEvent e) {
		//System.out.println("Here1");
		
		//System.out.println(gameState);
		if (gameState == STOCKLIST) {
			//System.out.println("Here2");
			//System.out.println("Current msg:" + saruman.getCurrentMessage());
			//System.out.println(saruman.isTyping());
			if (saruman.isTyping()) {
				//System.out.println("Here3");
				
				
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					saruman.addMessage(String.format("%s", saruman.getCurrentMessage()));
					saruman.addMessage(String.format("%s", Stock.reply(saruman.getCurrentMessage())));
					
					saruman.clearText();
				} else {
					saruman.userType(e.getKeyChar());
				}
				
			}
		}
		
		//esc moves from trading screen back to menu and resets animation
		if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			if(gameState == SAVESELECT) {
				loadMenu();
			}
			else if(gameState == STOCKLIST) { //exit to menu
				loadMenu();
			}
			else if(gameState == TRADINGSCREEN) { //exit to trading screen
				loadStockList();
			}
			if(gameState == TUTORIAL) {
				gameState = MAINMENU;
			}
		}
		else if(gameState == SAVESELECT) {
			//change page
			if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
				incrementSavePage(true);
			}
			else if(e.getKeyCode() == KeyEvent.VK_LEFT) {
				incrementSavePage(false);
			}
		}
		else if(gameState == TRADINGSCREEN) {
			String data = e.getKeyChar() + "";
			try {
				if(Integer.parseInt(data) < 5 && !data.equals("0")) { //no negative 
					currentStock.setCandleCount(Integer.parseInt(data) - 1);
					currentStock.recalculateRecents(true);
				}
			} catch (NumberFormatException e2) {
				//do nothing, expected
			}
			//hard mode
			if(e.getKeyCode() == KeyEvent.VK_H) {
				hardMode = !hardMode;
			}
		}
		else if(gameState == TUTORIAL) { //arrow key binds
			if(e.getKeyCode() == KeyEvent.VK_RIGHT) { //increase page
				incrementTutorial(true);
			}
			else if(e.getKeyCode() == KeyEvent.VK_LEFT) { //decrease page
				incrementTutorial(false);
			}
		}
		//testing
		if(e.getKeyCode() == KeyEvent.VK_F1 && (gameState == TRADINGSCREEN || gameState == STOCKLIST)) {
			saveGame();
		}
		else if(e.getKeyCode() == KeyEvent.VK_F2) {
			Notification.addNotification("TESTING", "this is a test\ntesting....", hitSoundPath);
			startGameFromSave();
		}
		else if(e.getKeyCode() == KeyEvent.VK_F3) {
			System.out.println(name);
		}
		else if(e.getKeyCode() == KeyEvent.VK_F4) {
			System.out.println(initialMoney);
//			System.out.println((money + portfolioValue - initialMoney) / initialMoney);
//			System.out.println(money + portfolioValue - initialMoney);
			
		}
		else if(e.getKeyCode() == KeyEvent.VK_F12) {
			refreshMarket();
		}
		
	}

	//prompts the user for stock buy
	//error checks for invalid inputs
	public void promptBuyOrder() {
		do {
			validInput = true;
			try {
				String amount = JOptionPane.showInputDialog("How many shares would you like to buy?\n"
						+ "Approx. max is " + (int)(money / currentStock.getValue()));
				
				if(amount == null) { //handle cancel
					break;
				}
				if(amount.equalsIgnoreCase("max")) { //attempt to buy with approximate max shares
					currentStock.addOrder(Stock.getBuyOrders(), (int) Math.floor((money / currentStock.getValue()) * 0.97));
					Notification.addNotification("ORDER CREATED", 
							"Buy " + (int) Math.floor((money / currentStock.getValue()) * 0.97) + " shares of " + currentStock.getSymbol(), 
							hitSoundPath);
				}
				else {
					//convert count into an integer
					int convert = Integer.parseInt(amount);
					if(convert <= 0) {
						throw new NumberFormatException();
					}
					//add buy order
					currentStock.addOrder(Stock.getBuyOrders(), convert);
					Notification.addNotification("ORDER CREATED", 
							"Buy " + convert + " shares of " + currentStock.getSymbol(), 
							hitSoundPath);
				}
				
			} catch (NumberFormatException e2) {
				validInput = false;
				JOptionPane.showMessageDialog(this, "INVALID. Please enter a positive integer.");
			}
		} while (!validInput);
	}
	
	//prompts the user for stock sell
	//error checks for invalid inputs
	public void promptSellOrder() {
		//check shares held
		if(currentStock.getAmountHeld() < 1) {
			return;
		}
		do {
			validInput = true;
			try {
				String amount = JOptionPane.showInputDialog("How many shares would you like to sell?\n"
						+ "Max is " + currentStock.getAmountHeld());
				
				if(amount == null) { //handle cancel
					break;
				}
				if(amount.equalsIgnoreCase("max")) {
					currentStock.addOrder(Stock.getSellOrders(), currentStock.getAmountHeld());
					Notification.addNotification("ORDER CREATED", 
							"Sell " + currentStock.getAmountHeld() + " shares of " + currentStock.getSymbol(), 
							hitSoundPath);
				}
				else {
					//convert count into an integer
					int convert = Integer.parseInt(amount);
					if(convert <= 0) {
						throw new NumberFormatException();
					}
					//add buy order
//				Stock.getSellOrders().put(currentStock, convert);
					currentStock.addOrder(Stock.getSellOrders(), convert);
					Notification.addNotification("ORDER CREATED", 
							"Sell " + convert + " shares of " + currentStock.getSymbol(),
							hitSoundPath);
				}
			} catch (NumberFormatException e2) {
				validInput = false;
				JOptionPane.showMessageDialog(this, "INVALID. Please enter a positive integer.");
			}
		} while (!validInput);
	}
	
	//saves the game to disk
	public void saveGame() {
		//save money and name and mode
		try {
			PrintWriter playerData = new PrintWriter(new File("gameFiles/saves/playerData/" + saveNumber + ".txt"));
			playerData.print(name + "\n" + money + "\n" + initialMoney + "\n" + portfolioValue + "\n" + 
					hardMode + "\n" + System.currentTimeMillis());
			playerData.flush();
			playerData.close();
		} catch (IOException e) {
			System.out.println("Player data writing error");
		}
		Stock.saveStocks(saveNumber);
		Notification.addNotification("GAME SAVED", "Game has been saved.", hitSoundPath);
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		if(gameState == TRADINGSCREEN) {
			//buy/sell keybinds
			if(e.getKeyCode() == KeyEvent.VK_P) { //pump
				promptBuyOrder();
			}
			else if(e.getKeyCode() == KeyEvent.VK_D) { //dump
				promptSellOrder();
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		int x = e.getX(), y = e.getY() - 30; //offset y b/c y counts window header pixels
		//System.out.printf("%d : %d%n", x, y);
		
		if(gameState == MAINMENU) {
			if(checkHit(x, y, 600, 500, 1300, 800)) { //play button
				//move to save select
				loadSaveSelect();
			}
			if(checkHit(x, y, 600, 730, 1300, 1030)) { //tutorial button
				gameState = TUTORIAL;
			}
		}
		else if (gameState == SAVESELECT) {
			if(checkHit(x, y, 10, 10, 90, 90)) { //back button
				loadMenu();
			}
			//arrows
			if(checkHit(x, y, 32, 460, 205, 540)) { //left arrow
				incrementSavePage(false);
			}
			else if(checkHit(x, y, 1700, 460, 1875, 540)) {
				incrementSavePage(true);
			}
			if(checkHit(x, y, 525, 140, 1375, 830)) { //in the saves field
				saveNumber = Save.getSaveNumberFromGUI(savePageNum, (y - 140) / 115);
				if(saveNumber != -1) {
					startGameFromSave();
					return;
				}
			}
			if(checkHit(x, y, 700, 20, 1200, 95)) {
				name = JOptionPane.showInputDialog("Name:");
				if(name == null) { //handle cancel button
					//ignore
				}
				if(name != null) { //valid name has been inputted
					//prompt mods
					//custom money
					if(JOptionPane.showOptionDialog(null, "Custom money?", "Custom money", 0, 3, null, null, null) == 0) {
						do {
							validInput = true;
							try {
								money = Integer.parseInt(JOptionPane.showInputDialog("How much? (Default 1000)"));
								if(money < 50) {
									throw new NumberFormatException();
								}
							} catch (NumberFormatException e2) {
								validInput = false;
								JOptionPane.showMessageDialog(this, "INVALID. Please enter a positive integer above 50.");
							}
						} while (!validInput);
					}
					else { //default money
						money = 1000;
					}
					initialMoney = money; //set initial money for lifetime P&L
					startGame();
				}
			}
		}
		else if(gameState == STOCKLIST) {
			
			//System.out.printf("%d : %d%n", x, y);
			
			//back button
			if(checkHit(x, y, 10, 10, 90, 90)) { //back button
				loadMenu();
			}
			if(checkHit(x, y, 1790, 10, 1890, 110)) { //save button
				saveGame();
			}
			if(checkHit(x, y, 50, 300, 1550, 780)) { //if in the grid box
				loadTradingScreen(selectedMarket.get((x - 50) / 500 * 4 + (y - 300) / 120));  //add rows
			}
			
			if (checkHit(x, y, 1577, 750, 1861, 805)) { // user clicked in the chatbot field
				//System.out.println("Started Typing");
				saruman.startTyping();
			} else {
				//System.out.println("Stopped Typing");
				saruman.endTyping();
			}
			//System.out.println(saruman.isTyping());
			
		}
		else if(gameState == TRADINGSCREEN) {
			//back button
			if(checkHit(x, y, 10, 10, 90, 90)) {
				loadStockList();
			}
			//view changing
			if(checkHit(x, y, 100, 14, 169, 86)) { //1D
				currentStock.setCandleCount(0);
				currentStock.recalculateRecents(true);
			}
			else if(checkHit(x, y, 180, 14, 256, 86)) { //5D
				currentStock.setCandleCount(1);
				currentStock.recalculateRecents(true);
			}
			else if(checkHit(x, y, 257, 14, 377, 86)) { //20D
				currentStock.setCandleCount(2);
				currentStock.recalculateRecents(true);
			}
			else if(checkHit(x, y, 378, 14, 512, 86)) { //MAX
				currentStock.setCandleCount(3);
				currentStock.recalculateRecents(true);
			}
			
			//pump button
			if(checkHit(x, y, 20, 896, 750, 986)) {
				promptBuyOrder();
			}
			
			if(checkHit(x, y, 770, 896, 1500, 986)) { //dump button
				promptSellOrder();
			}
		}
		else if(gameState == TUTORIAL) {
			//back button
			if(checkHit(x, y, 10, 10, 90, 90)) {
				gameState = MAINMENU;
			}
			//arrows
			if(checkHit(x, y, 32, 460, 205, 540)) { //left arrow
				incrementTutorial(false);
			}
			else if(checkHit(x, y, 1700, 460, 1875, 540)) {
				incrementTutorial(true);
			}
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	//checks if the mouse coordinates are within a rectangular bound
	//mouseX and mouseY are the coordinates of the cursor/click
	//x1 and y1 are the coordinates of the top left point of the rect
	//x2 and y2 are the coordinates of the bottom right point of the rect
	public boolean checkHit(int mouseX, int mouseY, int x1, int y1, int x2, int y2) {
		return mouseX > x1 && mouseX < x2 && mouseY > y1 && mouseY < y2;
	}
	
	public static void main(String[] args) {
		
		//The following lines creates your window
		System.out.println("  Main: Booting game");
		//makes a brand new JFrame
		JFrame frame = new JFrame ("🤑Pump and Dump🤑");
		//makes a new copy of your "game" that is also a JPanel
		PumpAndDumpGame myPanel = new PumpAndDumpGame ();
		//so your JPanel to the frame so you can actually see it
		System.out.println("  Main: Initializing Jframe 1/3");
		frame.add(myPanel);
		//so you can actually get keyboard input
		frame.addKeyListener(myPanel);
		//so you can actually get mouse input
		frame.addMouseListener(myPanel);
		//self explanatory. You want to see your frame
		System.out.println("  Main: Initializing Jframe 2/3");
		frame.setVisible(true);
		//some weird method that you must run
		frame.pack();
		//place your frame in the middle of the screen
		frame.setLocationRelativeTo(null);
		System.out.println("  Main: Initializing Jframe 3/3");
		//without this, your thread will keep running even when you windows is closed!
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//self explanatory. You don't want to resize your window because
		//it might mess up your graphics and collisions
		frame.setResizable(false);
		System.out.println("  Main: Done initializing Jframe");
		try {
			AudioInputStream player;
			Clip background;
			player = AudioSystem.getAudioInputStream(new File ("gameFiles/background.wav"));
			background = AudioSystem.getClip();
			background.open(player);
			background.setFramePosition (0); //<-- play sound file again from beginning
			background.loop(Clip.LOOP_CONTINUOUSLY);
		} catch (UnsupportedAudioFileException e) {
			System.out.println("Unsupported file");
		} catch (IOException e) {
			System.out.println("File error");
		} catch (LineUnavailableException e) {
			System.out.println("Line unavailable");
		}
		
		Scanner sc = new Scanner(System.in);
		System.out.println(Stock.reply(sc.nextLine()));
	}
}