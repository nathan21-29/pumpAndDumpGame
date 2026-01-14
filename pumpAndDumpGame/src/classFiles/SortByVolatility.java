package classFiles;

import java.util.Comparator;

//Nathan Chan Jan 14, 2026
//This is the Comparator object for stocks sorting by volatility ascending. 
public class SortByVolatility implements Comparator<Stock> {

	public int compare(Stock o1, Stock o2) {
		if(o1.getVolatility() == o2.getVolatility()) {
			return 0;
		}
		else if(o1.getVolatility() > o2.getVolatility()) { //o1 has a higher volatility
			return 1;
		}
		else {
			return -1;
		}
	}
}
