/*
 * Jerry Li
 * Jan 5, 2025
 * Helper class for the chatbot
 */

package classFiles;

public class PalantirSarumanGPT {
	public static double similarity (String str1, String str2) {
		double result = 0.0;
//		int lengthPriority = 3;
//		int headTailPriority = 1;
//		int middlePriority = 2;
		
		// 
		result -= Math.abs(str1.length() - str2.length()) / 10;
		
		char head1 = str1.charAt(0);
		char tail1 = str1.charAt(str1.length() - 1);
		
		char head2 = str2.charAt(0);
		char tail2 = str2.charAt(str2.length() - 1);
		
		if (head1 == head2 && tail1 == tail2) {
			result += 0.8;
		} else if (head1 == head2) {
			result += 0.2;
		} else if (tail1 == tail2) {
			result += 0.2;
		}
		
		
		
		return result;
	}
}
