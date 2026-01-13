package classFiles;

import java.util.*;

public class Save implements Comparable<Save>{

	private String name;
	private double money;
	private double portfolioValue;
	private long lastLoginMillis;

	public Save (String name, double money, double portfolioValue, long lastLoginMillis) {
		this.name = name;
		this.money = money;
		this.portfolioValue = portfolioValue;
		this.lastLoginMillis = lastLoginMillis;
	}
	
	public long getLastLoginMillis() {
		return lastLoginMillis;
	}
	
	//default sort by last log in time
	public int compareTo(Save p) {
		if(lastLoginMillis == p.getLastLoginMillis()) {
			return 0;
		}
		else if (lastLoginMillis - p.getLastLoginMillis() > 0) {
			return 1;
		}
		else {
			return 0;
		}
	}
}
