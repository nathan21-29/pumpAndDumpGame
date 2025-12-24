package pumpAndDumpGame;

import java.util.Scanner;

import classFiles.Candlestick;
import classFiles.Stock;

public class priceGenerationTest {

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		Stock test;
//		for(int i = 0; i < 10; i++) {
//			test = new Stock("TEST", 10, 1);
//			System.out.println(test);
//			test.nextCandleStick();
//		}
		while(true) {
			test = new Stock("TEST", 10, 3);
			for(int i = 0; i < 25; i++) {
				test.nextCandleStick();
			}
			System.out.println(test);
			in.nextLine();
		}
	}

}
