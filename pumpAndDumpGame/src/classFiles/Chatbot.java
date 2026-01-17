/*
 * Jerry Li
 * Jan 5, 2025
 * Helper class for the chatbot
 */

package classFiles;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Chatbot {
	
	static HashMap<String, String[]> aliases = new HashMap<>();

	private boolean isTyping;
	private ArrayList<Character> currentMessage;
	
	public void startTyping() {
		this.isTyping = true;
	}
	
	public void endTyping() {
		this.isTyping = false;
	}
	
	public boolean isTyping() {
		return this.isTyping;
	}
	
	public void userType(Character c) {
		currentMessage.add(c);
	}
	
	public String getCurrentMessage() {
		String s = "";
		for (Character c : currentMessage) {
			s += c;
		}
		return s;
	}
	
	public Chatbot() {
		this.isTyping = false;
		
		try {
			Scanner dictionaryScanner = new Scanner(new File("gameFiles/sentimentList"));
			while (dictionaryScanner.hasNextLine()) {
				String[] raw = dictionaryScanner.nextLine().toLowerCase().split(" ");
				aliases.put(raw[0], raw);
			}
		} catch (FileNotFoundException e) {
			System.out.println("A minor file in the chatbot was not found");
		}
	}
	
	/*
	 * This was my initial attempt at making a
	 * method that can detect how similar two
	 * words are to one another
	 * 
	 * It fails for words like "sell", as it
	 * sees the leading S and tailing L and
	 * interprets it as similar to "SOL" (Name of a stock)
	 * 
	public static double similarity (String str1, String str2) {

		result -= Math.abs(str1.length() - str2.length()) / 10;

		char head1 = str1.charAt(0);
		char tail1 = str1.charAt(str1.length() - 1);

		char head2 = str2.charAt(0);
		char tail2 = str2.charAt(str2.length() - 1);

		if (head1 == head2 && tail1 == tail2) {
			result += 0.8 - Math.abs(str1.length() - str2.length() * 0.05);
		} else if (head1 == head2) {
			result += 0.2;
		} else if (tail1 == tail2) {
			result += 0.2;
		}

		return result - 0.05;

	}
	 */
	
	/*
	 * 
	 */
	public static double similarity(String str1, String str2) { //XXX only works if the first parameter is the one with aliases
		double result = 0.0;
		if (aliases.get(str1) != null) {
			for (String alias : aliases.get(str1)) {
				double temp = levenshtein (alias, str2);
				result = Math.max(temp, result);
			}
		} else
			result = levenshtein(str1, str2);

		return result;
	}

	/* takes in two strings and returns a double 
	 * based on how correlated they are
	 * 
	 * XXX Not my code; copy paste from internet at: 
	 * https://www.geeksforgeeks.org/dsa/introduction-to-levenshtein-distance/
	 * 
	 * This is my own understanding of what this algorithm is:
	 * 
	 * The Levenshtein distance between two strings is 
	 * the amount of characters that must be 
	 * changed/added/removed in order for the two strings 
	 * to match
	 * 
	 * This implementation uses dynamic programming
	 * (splitting up the algorithm into smaller
	 * sub problems). In this case, it finds the
	 * Levenshtein distance between substrings from the
	 * larger strings, and stores them in a 2d array
	 * (sacrificing memory complexity for better time
	 * complexity) and then uses a sort of prefix
	 * sum array (?) to calculate the final Levenshtein
	 * distance
	 * 
	 * This is ostensibly a more robust solution than
	 * a hardcoded method with weights
	 */
	public static double levenshtein(String str1, String str2) {
		int m = str1.length();
		int n = str2.length();

		// Create a 2D array to store the dynamic programming results
		int[][] dp = new int[m + 1][n + 1];

		// Initialize the base cases
		for (int i = 0; i <= m; i++) {
			dp[i][0] = i;
		}

		for (int j = 0; j <= n; j++) {
			dp[0][j] = j;
		}

		// Fill in the DP array using the recurrence relation
		for (int i = 1; i <= m; i++) {
			for (int j = 1; j <= n; j++) {
				if (str1.charAt(i - 1) == str2.charAt(j - 1)) {
					// Characters match, no operation needed
					dp[i][j] = dp[i - 1][j - 1];
				} else {
					// Characters don't match, consider the minimum of insert, remove, and replace
					dp[i][j] = 1 + Math.min(
							// Insert
							dp[i][j - 1],
							Math.min(
									// Remove
									dp[i - 1][j],
									// Replace
									dp[i - 1][j - 1]));
				}
			}
		}

		// Result is stored in the bottom-right cell of the DP array
		
		// at the end, WE make it a negative value and tie it to length;
		// as it stands in the stock.reply() class, a negative number
		// means negative correlation
		double result = Math.min(m, n)-dp[m][n];
		//		if (result > 0) 
		//			System.out.printf("%.1f: %s and %s%n", result, str1, str2);
		return result;
	}
}
