package pumpAndDumpGame;
import java.awt.event.*;
import java.awt.*;

import javax.swing.*;

import classFiles.Stock;

@SuppressWarnings("serial") //funky warning, just suppress it. It's not gonna do anything.
public class PumpAndDumpGame extends JPanel implements Runnable, KeyListener, MouseListener{
	
	//self explanatory variables
	int FPS = 100;
	Thread thread;
	int screenWidth = 1900;
	int screenHeight = 1000;
	
	long startTime, timeElapsed, demoStartTime;
	int frameCount = 0;
	
	int gameState = 0;
	final int MAINMENU = 0, TRADINGSCREEN = 1;
	
	Color darkGray = new Color(22, 22, 22);
	Font title = new Font ("Arial", Font.BOLD, 100);
	FontMetrics fmTitle;
	
	Image menuTitle, menuPlayButton, menuSettingsButton;
	Image tradingView, selectionPill;
	
	Stock demo = new Stock("DEMO", 10, 2, 1, 1);
	Stock test = new Stock("DEMO", 10, 0.01, 1.008, 3);
	
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
		startTime = System.currentTimeMillis();
		
		timeElapsed = 0;
		FPS = 100;
		//generate first candlesticks for demo and test
		for(int i = 0; i < 200; i++) {
			demo.nextCandleStick();
			test.nextCandleStick();
		}
		
		for(int i = 0; i < 2920; i++) {
			test.nextCandleStick();
		}
		System.out.println(demo.getRecentMax());
		gameState = MAINMENU;
		demoStartTime = System.currentTimeMillis();
		
		//populate market
		
		menuTitle = Toolkit.getDefaultToolkit().getImage("gameFiles/menuTitle.png");
		menuPlayButton = Toolkit.getDefaultToolkit().getImage("gameFiles/playButton.png");
		menuSettingsButton = Toolkit.getDefaultToolkit().getImage("gameFiles/settingsButton.png");
		
		tradingView = Toolkit.getDefaultToolkit().getImage("gameFiles/tradingScreen.png");
		selectionPill = Toolkit.getDefaultToolkit().getImage("gameFiles/selectionPill.png");
		
		System.out.println("Thread: Done initializing game");
	}
	
	public void update() {
		//update stuff
		timeElapsed = System.currentTimeMillis() - startTime;
		frameCount++;
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g; //We can use g2d if we need something g doesn't have
		if(gameState == MAINMENU) {
			drawMenu(g);
		}
		else if(gameState == TRADINGSCREEN) {
			drawTradingScreen(test, g);
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

	//Draws the trading screen
	//Stock s is the stock to be drawn
	//Graphics g is the graphics object used to draw elements
	public void drawTradingScreen(Stock s, Graphics g) {
		g.drawImage(tradingView, 0, 0, 1900, 1000, this);
		s.drawGraph(g);
		s.getIndicators(g);
		
		//draw timeline selection
		if(s.getCandleCount() == 140) {
			g.drawImage(selectionPill, 500, 40, 120, 50, this);
		}
		else if(s.getCandleCount() == 7) {
			g.drawImage(selectionPill, 172, 40, 120, 50, this);
		}
		else if(s.getCandleCount() == 35) {
			g.drawImage(selectionPill, 330, 40, 120, 50, this);
		}
		else if(s.getCandleCount() == 1400) {
			g.drawImage(selectionPill, 710, 40, 120, 50, this);
		}
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void keyPressed(KeyEvent e) {
		//esc moves from trading screen back to menu and resets animation
		if(e.getKeyCode() == KeyEvent.VK_ESCAPE && gameState == TRADINGSCREEN) {
			demo = new Stock("DEMO", 10, 2, 1, 1);
			//generate first 100 candlesticks for demo
			for(int i = 0; i < 300; i++) {
				demo.nextCandleStick();
			}
			System.out.println(demo.getRecentMax());
			gameState = MAINMENU;
			demoStartTime = System.currentTimeMillis();
		}
		if(e.getKeyCode() == KeyEvent.VK_SPACE) {
			gameState = TRADINGSCREEN;
		}
		if(e.getKeyCode() == KeyEvent.VK_D) {
			Stock.incrementTime();
			test.nextCandleStick();
			Stock.incrementTime();
			test.nextCandleStick();
			Stock.incrementTime();
			test.nextCandleStick();
			Stock.incrementTime();
			test.nextCandleStick();
//			String temp = JOptionPane.showInputDialog("HI");
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
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
	
	public static void main(String[] args) {
		
		//The following lines creates your window
		System.out.println("  Main: Booting game");
		//makes a brand new JFrame
		JFrame frame = new JFrame ("Example");
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