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
		for(int i = 0; i < 1000; i++) {
			test = new Stock("TEST", 10, 1, 1, 1);
			for(int j = 0; j < 150; j++) {
				test.nextCandleStick();
			}
			if(test.getPriceHistory().getLast().getClosePrice() > 10) {
				winners++;
			}
			else {
				losers++;
			}
		}
		System.out.println(winners + " winners");
		System.out.println(losers + " losers");
	}

}
