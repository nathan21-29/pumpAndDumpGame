/*
 * Jerry Li
 * Jan 5, 2025
 * Helper class for the chatbot
 */

package classFiles;

import java.util.*;
import java.io.*;

public class Chatbot {
	/*
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
	static HashMap<String, String[]> aliases = new HashMap<>();

	public Chatbot() {
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

	// XXX W3 school
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
		double result = Math.min(m, n)-dp[m][n];
		//		if (result > 0) 
		//			System.out.printf("%.1f: %s and %s%n", result, str1, str2);
		return result;
	}
}
