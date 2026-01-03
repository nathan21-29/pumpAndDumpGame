package pumpAndDumpGame;

import java.util.Scanner;

import classFiles.Candlestick;
import classFiles.Stock;

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
		int initPrice = 100;
		for(int i = 0; i < 1; i++) {
			test = new Stock("TST2", 100, 0.02, 1.004, 10, 0.02);
			Stock.getTestMarket().add(test);
			for(int j = 0; j < 2000; j++) {
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
		System.out.println(totalSum / 1000);
		System.out.println((totalSum / 1000 - initPrice) / (totalSum / 100000) + "%");
	}

}
