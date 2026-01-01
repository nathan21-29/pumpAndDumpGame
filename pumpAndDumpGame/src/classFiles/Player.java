package classFiles;

import java.util.*;

public class Player {

	private String name;
	private double money;
	private double quota;
	private double quotaProgress;

	public Player (String name, double money, double quota) {
		this.name = name;
		this.money = money;
		this.quota = quota;
		this.quotaProgress = 0;
	}

	public void buyStock (Stock stock, int shares) {
		this.money -= stock.getValue() * shares;
	}	
}
