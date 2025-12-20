package classFiles;

import java.util.*;

public class Player {

	private double money;
	private double quota;
	private double quotaProgress;
	private ArrayList<Holding> portfolio;

	public Player (double money, double quota) {
		this.money = money;
		this.quota = quota;
		this.quotaProgress = 0;
		this.portfolio = new ArrayList<>();
	}

	public void buyStock (Stock stock, int shares) {
		portfolio.add(new Holding(stock, shares));
		this.money -= stock.getValue() * shares;
	}	
}
