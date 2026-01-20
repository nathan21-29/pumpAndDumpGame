package classFiles;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.sound.sampled.*;

//Nathan Chan Jan 18, 2026
//This is the object class for the notification object, which is used
//to relay information to the player, including buy/sell orders,
//when orders are successful/fail etc.
public class Notification {
	
	private static Queue<Notification> notifications = new LinkedList<Notification>();
	
	private static Font titleFont = new Font("Arial", Font.BOLD, 35);
	private static Font bodyFont = new Font("Arial", Font.PLAIN, 20);
	private static Color offYellow = new Color(255, 247, 196);

	private String title;
	private String body;
	private String soundPath;
	private long startMillis;
	private long duration = 3000;
	
	public Notification(String title, String body, String soundPath) {
		this.title = title;
		this.body = body;
		playAudio(soundPath);
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
	
	public String getSoundPath() {
		return soundPath;
	}
	
	//setters
	public static void addNotification(String title, String body, String soundPath) {
		notifications.add(new Notification(title, body, soundPath));
	}
	
	public static void drawNotifications(Graphics g, boolean viewAll) {
		//remove all expired notifications
		while(notifications.peek() != null && notifications.peek().getProgress() == 1) {
			notifications.poll();
		}
		
		int number = 0; //index
		for(Notification n : notifications) {
			if(viewAll || number < 1) {
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
	}
	
	//returns the % progress, as a decimal, of the notification
	public double getProgress() {
		return Math.min((System.currentTimeMillis() - startMillis) * 1.0 / duration, 1);
	}
	
	//clears notification queue
	//useful for ensuring notification queue is empty upon save load
	public static void clearNotifications() {
		notifications.clear();
	}

	//plays an audio by creating a new AudioInputStrem
	//Parameter Clip clip is the clip to start
	//returns nothing
	//code borrowed from my last year osu game
	public static void playAudio(String path) {
		try {
			AudioInputStream player = AudioSystem.getAudioInputStream(new File (path));
			Clip clip = AudioSystem.getClip();
			clip.open(player);
			clip.start();
			clip.addLineListener(event -> {
				if(event.getType() == LineEvent.Type.STOP) {
					clip.drain();
					clip.close(); //close the audio thread when it finishes to free ram
				}
			});
		} catch (UnsupportedAudioFileException e) {
			System.out.println("Unsupported file");
		} catch (IOException e) {
			System.out.println("File error");
		} catch (LineUnavailableException e) {
			System.out.println("Line unavailable");
		}

	}
}