package classFiles;

import java.awt.*;
import java.util.*;

public class Notification {
	
	private static Queue<Notification> notifications = new LinkedList<Notification>();
	
	private static Font titleFont = new Font("Arial", Font.BOLD, 35);
	private static Font bodyFont = new Font("Arial", Font.PLAIN, 20);
	private static Color offYellow = new Color(255, 247, 196);

	private String title;
	private String body;
	
	private long startMillis;
	private long duration = 5000;
	
	public Notification(String title, String body) {
		this.title = title;
		this.body = body;
		startMillis = System.currentTimeMillis();
	}
	
	//getters
	public static Queue<Notification> getNotifications() {
		return notifications;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getBody() {
		return body;
	}
	
	//setters
	public static void addNotification(String title, String body) {
		notifications.add(new Notification(title, body));
	}
	
	public static void drawNotifications(Graphics g) {
		//remove all expired notifications
		while(notifications.peek() != null && notifications.peek().getProgress() == 1) {
			notifications.poll();
		}
		
		int number = 0; //index
		for(Notification n : notifications) {
			//draw box
			g.setColor(new Color(41, 41, 41));
//			g.fillRect(1525, 880 - number * 120, 355, 100);
			g.fillRoundRect(1525, 880 - number * 120, 355, 100, 15, 15);
			
			//draw text
			g.setColor(Color.WHITE);
			g.setFont(titleFont);
			g.drawString(n.getTitle(), 1535, 917 - number * 120);
			
			g.setColor(new Color(200, 200, 200));
			g.setFont(bodyFont);
			int lineNumber = 0;
			for(String data : n.getBody().split("\n")) { //force recognition of \n
				g.drawString(data, 1540, 945 - number * 120 + lineNumber++ * 20);
			}
			number++;
			//draw progress bar
			g.setColor(offYellow);
			g.fillRect(1525, 1095 - number * 120, (int)(355 * (1 - n.getProgress())), 5);
		}
	}
	
	//returns the % progress, as a decimal, of the notification
	public double getProgress() {
		return Math.min((System.currentTimeMillis() - startMillis) * 1.0 / duration, 1);
	}
}
