package classFiles;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class Save implements Comparable<Save>{

	private static Font body = new Font("Arial", Font.PLAIN, 40);
	private static Font small = new Font("Arial", Font.PLAIN, 26);
	private static FontMetrics smallFM;
	private static ArrayList<Save> saves = new ArrayList<Save>();
	
	private int saveNumber;
	private String name;
	private double money;
	private double portfolioValue;
	private long lastLoginMillis;

	public Save (int saveNumber, String name, double money, double portfolioValue, long lastLoginMillis) {
		this.saveNumber = saveNumber;
		this.name = name;
		this.money = money;
		this.portfolioValue = portfolioValue;
		this.lastLoginMillis = lastLoginMillis;
	}
	
	public static ArrayList<Save> getSaves() {
		return saves;
	}
	
	public String getName() {
		return name;
	}
	
	public double getMoney() {
		return money;
	}
	
	public double getPortfolioValue() {
		return portfolioValue;
	}
	
	public long getLastLoginMillis() {
		return lastLoginMillis;
	}
	
	//default sort by newest log in time descending
	public int compareTo(Save p) {
		if(lastLoginMillis == p.getLastLoginMillis()) {
			return 0;
		}
		else if (lastLoginMillis - p.getLastLoginMillis() < 0) {
			return 1;
		}
		else {
			return 0;
		}
	}

	//create save objects and load data into them
	public static void cacheSaves() {
		try {
			Scanner countChecker = new Scanner(new File("gameFiles/saves/saveData.txt"));
			double money;
			double portfolioValue;
			long lastLoginMillis;
			int saveCount = Integer.parseInt(countChecker.nextLine());
			for(int i = 0; i <= saveCount; i++) {
				//open file 
				BufferedReader fileIn = new BufferedReader(new FileReader("gameFiles/saves/playerData/" + i + ".txt"));
				String name = fileIn.readLine();
				money = Double.parseDouble(fileIn.readLine());
				portfolioValue = Double.parseDouble(fileIn.readLine());
				fileIn.readLine(); //skip hardMode line
				lastLoginMillis = Long.parseLong(fileIn.readLine());
				saves.add(new Save(i, name, money, portfolioValue, lastLoginMillis));
				fileIn.close();
			}
			countChecker.close();
		} catch (FileNotFoundException e) {
			System.out.println("Save file not found!");
		} catch (IOException e) {
			System.out.println("Reading error");
		}

		Collections.sort(saves); //default sort by last login time
	}

	
	//pageNum starts counting at 0
	public static void drawSaves(int pageNum, Graphics g) {
		g.setColor(Color.WHITE);
		g.setFont(body);
		Save currentSave;
		Long lastPlayed;
		StringBuilder result;
		//it just so happens that days, hours, minutes, seconds are alphabetically consecutive
		TreeMap<String, Integer> formattedDate = new TreeMap<String, Integer>();
		for(int i = 0; i < saves.size() - (6 * pageNum) && i < 6; i++) {
			formattedDate.clear(); //clear for every save
			currentSave = saves.get(i + 6 * pageNum);
			g.drawRect(525, 140 + i * 115, 850, 115);
			g.drawString(currentSave.name, 535, 180 + i * 115);
			g.drawString(String.format("$%.2f", currentSave.money + currentSave.portfolioValue), 535, 240 + i * 115);
			
			//get last played date
			lastPlayed = System.currentTimeMillis() - currentSave.lastLoginMillis;
			if(lastPlayed / 86400000 != 0) { //more than 1 day ago
				formattedDate.put("Day(s)", (int) (lastPlayed / 1440000));
			}
			if(lastPlayed % 86400000 / 3600000 != 0) { //more than 1 hour ago
				formattedDate.put("Hour(s)", (int) (lastPlayed % 86400000 / 3600000));
			}
			if(lastPlayed % 3600000 / 60000 != 0) {
				formattedDate.put("Minute(s)", (int) (lastPlayed % 3600000 / 60000));
			}
			result = new StringBuilder("Last Played ");
			for(Entry<String, Integer> entry : formattedDate.entrySet()) {
				result.append(entry.getValue() + " " + entry.getKey() + ", ");
			}
			result.deleteCharAt(result.length() - 2);
			result.append("ago");
			
			//draw last played date
			g.setFont(small);
			smallFM = g.getFontMetrics(small);
			g.drawString(result.toString(), 1365 - smallFM.stringWidth(result.toString()), 170 + i * 115);
		}
	}
}
