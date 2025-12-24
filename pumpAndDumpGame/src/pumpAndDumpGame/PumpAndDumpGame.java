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
	
	long startTime, timeElapsed;
	int frameCount = 0;
	
	int gameState = 0;
	final int MAINMENU = 0;
	
	Color darkGray = new Color(22, 22, 22);
	Font title = new Font ("Arial", Font.BOLD, 100);
	FontMetrics fmTitle;
	
	Image menuTitle, menuPlayButton, menuSettingsButton;
	
	Stock demo = new Stock("DEMO", 10, 1);
	
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
//		for(int i = 0; i < 100000; i++) {
//			// this is just to delay time
//			String s = "set up stuff blah blah blah";
//			s.toUpperCase();
//		}
		//generate first 100 candlesticks for demo
		for(int i = 0; i < 200; i++) {
			demo.nextCandleStick();
		}
		System.out.println("max");
		System.out.println(demo.getMaxPrice());
		System.out.println(demo.getMinPrice());
		System.out.println(demo.getPriceHistory());
		
		menuTitle = Toolkit.getDefaultToolkit().getImage("gameFiles/menuTitle.png");
		menuPlayButton = Toolkit.getDefaultToolkit().getImage("gameFiles/playButton.png");
		menuSettingsButton = Toolkit.getDefaultToolkit().getImage("gameFiles/settingsButton.png");
		
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
	}
	
	public void drawMenu(Graphics g) {
		g.setColor(darkGray);
		g.fillRect(0, 0, screenWidth, screenHeight);

		demo.drawDemo(200, 10, g);
		
		g.drawImage(menuTitle, 0, 0, 1900, 1000, this);
		g.drawImage(menuPlayButton, 600, 500, 700,  300, this);
		g.drawImage(menuSettingsButton, 600, 730, 700,  300, this);
	}
	
	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_D) {
			System.out.println("hi");
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