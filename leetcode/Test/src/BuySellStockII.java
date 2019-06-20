
public class BuySellStockII {

	public int maxProfit(int[] prices) {
        int sumPrice = 0;
        for(int i = 1; i < prices.length; i++){
        	if((prices[i] - prices[i -1]) > 0)
        		sumPrice += prices[i] - prices[i -1];
        }
        return sumPrice;
    }
	public static void main(String[] args) {
		int[] prices = new int[]{};
		BuySellStockII stock = new BuySellStockII();
		System.out.println(stock.maxProfit(prices));
	}
}
