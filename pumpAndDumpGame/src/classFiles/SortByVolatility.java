package classFiles;

import java.util.Comparator;

public class SortByVolatility implements Comparator<Stock> {

	public int compare(Stock o1, Stock o2) {
		if(o1.getVolatility() == o2.getVolatility()) {
			return 0;
		}
		else if(o1.getVolatility() - o2.getVolatility() > 0) { //o1 has a higher volatility
			return 1;
		}
		else {
			return -1;
		}
	}
}
