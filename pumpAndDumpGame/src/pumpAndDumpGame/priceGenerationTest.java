package pumpAndDumpGame;

import java.util.ArrayList;
import java.util.Scanner;

import classFiles.Candlestick;
import classFiles.Stock;

//Jerry Li and Nathan Chan Jan 18, 2026
//this is the file used for basic testing of stocks to see average
//returns and likelihood of profit in the long term
public class priceGenerationTest {

	public static void main(String[] args) {
		Stock test;
//		for(int i = 0; i < 10; i++) {
//			test = new Stock("TEST", 10, 1);
//			System.out.println(test);
//			test.nextCandleStick();
//		}
		int winners = 0;
		int losers = 0;
		double totalSum = 0;
		int initPrice = 250;
		for(int i = 0; i < 100; i++) {
			test = new Stock("TEST", initPrice, 0.004, 1.02, 10, 0.03);
			Stock.getTestMarket().add(test);
			for(int j = 0; j < 2555; j++) {
				test.nextCandleStick();
				Stock.incrementTime();
			}
			if(test.getPriceHistory().getLast().getClosePrice() > initPrice) {
				winners++;
			}
			else {
				losers++;
			}
			
			totalSum += test.getValue();
		}
		System.out.println(winners + " winners");
		System.out.println(losers + " losers");
		System.out.println(totalSum / 100);
		System.out.println((totalSum / 100 - initPrice) / (totalSum / 100000) + "%");
	}

}
