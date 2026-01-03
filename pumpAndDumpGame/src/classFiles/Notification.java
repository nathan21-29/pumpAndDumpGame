package classFiles;

import java.awt.Font;
import java.awt.Graphics;

public class Notification {
	
	Font titleFont = new Font("Arial", Font.BOLD, 40);

	private String title;
	private String body;
	
	private long startMillis;
	private long duration;
	
	public Notification(String title, String body, long duration) {
		this.title = title;
		this.body = body;
		startMillis = System.currentTimeMillis();
		this.duration = duration;
	}
	
	//int number is sequential (i.e. the first notification has a number of 0, second has 1 etc.)
	public void drawNotification(int number, Graphics g) {
		g.drawRect(880 - number * 120, number, number, number);
	}
	
	//returns the % progress, as a decimal, of the notification
	public double getProgress() {
		return Math.min((System.currentTimeMillis() - startMillis) / duration, 1);
	}
}
