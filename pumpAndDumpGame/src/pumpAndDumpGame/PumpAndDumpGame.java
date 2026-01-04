package pumpAndDumpGame;
import java.awt.event.*;
import java.io.*;
import java.security.PublicKey;
import java.util.*;
import java.util.Map.Entry;
import java.awt.*;

import javax.sound.sampled.*;
import javax.swing.*;

import classFiles.*; //import all files from classFiles package

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
	final int MAINMENU = 0, SAVESELECT = 1, TRADINGSCREEN = 3;
	
	Color darkGray = new Color(22, 22, 22);
	Color darkGreen = new Color(66, 176, 78);
	Color darkRed = new Color(187, 46, 40);
	Font title = new Font ("Arial", Font.BOLD, 100);
	FontMetrics fmTitle;
	
	Image menuTitle, menuPlayButton, menuSettingsButton;
	ArrayList<Image> tutorial = new ArrayList<>(); //AL to hold the images for the tutorial slideshow
	Image saveSelect;
	Image tradingView, selectionPill;
	
	String hitSoundPath = "gameFiles/soft-hitnormal.wav";
	Clip temp;
	
	ArrayList<Stock> selectedMarket;
	
	Stock demo = new Stock("DEMO", 10, 2, 1, 1, 0);
	Stock test = new Stock("TEST", 10, 0.01, 1.008, 3, 0);
	Stock test2 = new Stock("TST2", 100, 0.5, 1.0005, 1, 0.1);
	
	//player variables
	double money;
	String name;
	Stock currentStock = test2;
	
	double quota;
	double quotaProgress; //as an absolute amount, NOT a percentage
	
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
		
		selectedMarket = Stock.getTestMarket();
		selectedMarket.add(test);
		selectedMarket.add(test2);
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
		
		//populate market
		
		menuTitle = Toolkit.getDefaultToolkit().getImage("gameFiles/menuTitle.png");
		menuPlayButton = Toolkit.getDefaultToolkit().getImage("gameFiles/playButton.png");
		menuSettingsButton = Toolkit.getDefaultToolkit().getImage("gameFiles/settingsButton.png");
		
		saveSelect = Toolkit.getDefaultToolkit().getImage("gameFiles/saveSelect.png");
		
		tradingView = Toolkit.getDefaultToolkit().getImage("gameFiles/tradingScreen.png");
		selectionPill = Toolkit.getDefaultToolkit().getImage("gameFiles/selectionPill.png");
		
		
		System.out.println("Thread: Done initializing game");
	}
	
	public void update() {
		//update stuff
		timeElapsed = System.currentTimeMillis() - startTime;
		if(gameState == TRADINGSCREEN && System.currentTimeMillis() - lastCycle > 3000) {
			refreshMarket();
		}
		frameCount++;
	}
	
	public void refreshMarket() {
		currentStock.nextCandleStick();
		
		if(Stock.incrementTime()) { //if end of day, pause for user to check stocks and create orders for next morning
			
		}
		
		lastCycle = System.currentTimeMillis();
		
		fillOrders(Stock.getBuyOrders(), true);
		fillOrders(Stock.getSellOrders(), false);
		
		//sorting here means 1 sort per 3 seconds as opposed to every repaint
		Collections.sort(selectedMarket);
	}
	
	//mode == true is buy, mode == false is sell
	public void fillOrders(HashMap<Stock, Integer> orders, boolean mode) {
		for(Entry<Stock, Integer> entry : orders.entrySet()) {
			Stock stock = entry.getKey();
			int amount = entry.getValue();
			//check sufficient funds / shares
			if(mode) { //buy orders; check sufficient funds
				if(stock.getValue() * amount > money) {
//					JOptionPane.showMessageDialog(this, "Order to buy " + entry.getValue() + "shares\n"
//							+ "of " + entry.getKey().getSymbol() + " has failed. \n"
//							+ "Reason: Insufficient money");
					Notification.addNotification("ORDER FAILED",
							"Order for " + amount + " shares of " + stock.getSymbol() + " has" +  
							"\nfailed. Reason: Insufficient funds", hitSoundPath);
				}
				else {
					Notification.addNotification("ORDER FILLED", String.format(
							"Successfully purchased " + amount + " shares of\n" + stock.getSymbol()
							+ "for $%.2f/share. ($%.2f)", stock.getValue(),
							stock.getValue() * amount), hitSoundPath);
					
					money -= stock.getValue() * amount; //update money
					stock.incrementTotalPurchasePrice(stock.getValue() * amount);
					stock.incrementAmountHeld(amount);
				}
			}
			else { //!buy; sell orders
				if(amount > stock.getAmountHeld()) {
//					JOptionPane.showMessageDialog(this, "Order to sell " + entry.getValue() + "shares\n"
//							+ "of " + entry.getKey().getSymbol() + " has failed. \n"
//							+ "Reason: Insufficient shares");
					Notification.addNotification("ORDER FAILED",
							"Order to sell " + amount + " shares of " + stock.getSymbol() + " has" +  
							"\nfailed. Reason: Insufficient shares", hitSoundPath);
				}
				else {
					Notification.addNotification("ORDER FILLED", String.format(
							"Successfully sold " + amount + " shares of\n" + stock.getSymbol()
							+ "for $%.2f/share. ($%.2f)", stock.getValue(),
							stock.getValue() * amount), hitSoundPath);
					
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
		else if(gameState == SAVESELECT) {
			drawSaveSelect(g);
		}
		else if(gameState == TRADINGSCREEN) {
			drawTradingScreen(g);
		}
	}
	
	//Draws the main menu
	//Graphics g is the graphics object used to draw elements
	public void drawMenu(Graphics g) {
		g.setColor(darkGray);
		g.fillRect(0, 0, screenWidth, screenHeight);

		demo.drawDemo(demoStartTime, 200, 10, g);
		
		g.drawImage(menuTitle, 0, 0, 1900, 1000, this);
		g.drawImage(menuPlayButton, 600, 500, 700,  300, this);
		g.drawImage(menuSettingsButton, 600, 730, 700,  300, this);
	}
	
	public void drawSaveSelect(Graphics g) {
		g.drawImage(saveSelect, 0, 0, 1900, 1000, this);
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
		g.drawString(String.format("$%.2f", money), 530, 65);
		
		//draw current holdings
		Stock.drawHoldings(g);
		
		//draw notifications
		Notification.drawNotifications(g);
	}

	public void startGame() {
		//load sounds into memory
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

		gameState = TRADINGSCREEN;
		lastCycle = System.currentTimeMillis();
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void keyPressed(KeyEvent e) {
		//esc moves from trading screen back to menu and resets animation
		if(e.getKeyCode() == KeyEvent.VK_ESCAPE && gameState == TRADINGSCREEN) {
			demo = new Stock("DEMO", 10, 2, 1, 1, 0);
			//generate first 100 candlesticks for demo
			for(int i = 0; i < 300; i++) {
				demo.nextCandleStick();
			}
			System.out.println(demo.getRecentMax());
			gameState = MAINMENU;
			demoStartTime = System.currentTimeMillis();
		}
		if(e.getKeyCode() == KeyEvent.VK_SPACE) {
			System.out.println(currentStock.getProfitLoss(false));
		}
		if(e.getKeyCode() == KeyEvent.VK_D) {
			for(int i = 0; i < selectedMarket.size(); i++) {
				selectedMarket.get(i).nextCandleStick();
			}
			Stock.incrementTime();
//			String temp = JOptionPane.showInputDialog("HI");
		}
		if(e.getKeyCode() == KeyEvent.VK_F11) {
			Notification.addNotification("TESTING", "this is a test\ntesting....", hitSoundPath);
		}
		if(e.getKeyCode() == KeyEvent.VK_F12) {
			for(int i = 0; i < 1400; i++) {
				currentStock.nextCandleStick();
				Stock.incrementTime();
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		int x = e.getX(), y = e.getY() - 30; //offset y b/c y counts window header pixels
		
		if(gameState == MAINMENU) {
			if(checkHit(x, y, 600, 500, 1300, 800)) { //play button
				//move to save select
				gameState = SAVESELECT;
			}
		}
		else if (gameState == SAVESELECT) {
			name = null; //reset in case multiple saves loaded in the same session
			if(checkHit(x, y, 700, 20, 1200, 95)) {
				name = JOptionPane.showInputDialog("Name:");
				if(name == null) { //handle cancel button
					//ignore
				}
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
				startGame();
			}
		}
		else if(gameState == TRADINGSCREEN) {
			//view changing
			if(checkHit(x, y, 100, 14, 169, 86)) { //1D
				System.out.println();
				currentStock.setCandleCount(0);
				currentStock.recalculateRecents(true);
			}
			else if(checkHit(x, y, 70, 14, 256, 86)) { //5D
				System.out.println();
				currentStock.setCandleCount(1);
				currentStock.recalculateRecents(true);
			}
			else if(checkHit(x, y, 257, 14, 377, 86)) { //20D
				System.out.println();
				currentStock.setCandleCount(2);
				currentStock.recalculateRecents(true);
			}
			else if(checkHit(x, y, 378, 14, 512, 86)) { //MAX
				System.out.println();
				currentStock.setCandleCount(3);
				currentStock.recalculateRecents(true);
			}
			
			//pump button
			if(checkHit(x, y, 20, 896, 750, 986)) {
				do {
					validInput = true;
					try {
						String amount = JOptionPane.showInputDialog("How many shares would you like to buy?\n"
								+ "Approx. max is " + (int)(money / currentStock.getValue()));
						
						if(amount == null) { //handle cancel
							break;
						}
						//convert count into an integer
						int convert = Integer.parseInt(amount);
						if(convert <= 0) {
							throw new NumberFormatException();
						}
						//add buy order
//						Stock.getBuyOrders().put(currentStock, convert);
						currentStock.addOrder(Stock.getBuyOrders(), convert);
						
					} catch (NumberFormatException e2) {
						validInput = false;
						JOptionPane.showMessageDialog(this, "INVALID. Please enter a positive integer.");
					}
				} while (!validInput);
			}
			else if(checkHit(x, y, 770, 896, 1500, 986)) {
			}
			
			if(checkHit(x, y, 770, 896, 1500, 986)) { //dump button
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
						//convert count into an integer
						int convert = Integer.parseInt(amount);
						if(convert <= 0) {
							throw new NumberFormatException();
						}
						//add buy order
//						Stock.getSellOrders().put(currentStock, convert);
						currentStock.addOrder(Stock.getSellOrders(), convert);
						
					} catch (NumberFormatException e2) {
						validInput = false;
						JOptionPane.showMessageDialog(this, "INVALID. Please enter a positive integer.");
					}
				} while (!validInput);
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
		
	}
}