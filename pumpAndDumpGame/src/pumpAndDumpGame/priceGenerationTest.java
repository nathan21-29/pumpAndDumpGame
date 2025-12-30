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
		for(int i = 0; i < 1000; i++) {
			test = new Stock("DEMO", initPrice, 0.02, 1.004, 10);
			for(int j = 0; j < 2920; j++) {
				test.nextCandleStick();
			}
			if(test.getPriceHistory().getLast().getClosePrice() > 10) {
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
