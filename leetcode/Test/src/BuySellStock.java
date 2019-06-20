public class BuySellStock {
	public int maxProfit(int[] prices) {
		if(prices.length <= 0)
			return 0;
		int min = prices[0];
		int maxPrice = 0;
		for(int i = 0; i < prices.length; i++){
			if((prices[i] - min) > maxPrice){
				maxPrice = prices[i] - min;
			}
			if(min > prices[i]){
				min = prices[i];
			}
		}
		return maxPrice;
	}
	public static void main(String[] args) {
		BuySellStock stock = new BuySellStock();
		int[] prices = new int[]{};
		System.out.println(stock.maxProfit(prices));
	}
}
