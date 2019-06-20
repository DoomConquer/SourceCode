
public class BuySellStockIIV {
	public int maxProfit(int k, int[] prices) {
		int n = prices.length;
		if(k >= n/2){
			return findMaxProfit(prices);
		}
		return findMaxProfit(k, prices);
	}
	
	private int findMaxProfit(int[] prices){
		int max = 0;
		for(int i = 1; i < prices.length; i++){
			if((prices[i] - prices[i -1]) > 0)
				max += prices[i] - prices[i -1];
		}
		return max;
	}
	
	private int findMaxProfit(int k, int[] prices){
		int n = prices.length;
		int[] buy = new int[k+1];  // 当前最多进行j次交易最大利润且当前买入
		int[] sell = new int[k+1]; // 当前最多进行j次交易最大利润且当前卖出
		for(int i = 0; i <= k; i++)
			buy[i] = Integer.MIN_VALUE;
		for(int i = 0; i < n; i++){
			for(int j = 1; j <= k; j++){
				buy[j] = Math.max(buy[j], sell[j-1] - prices[i]);
				sell[j] = Math.max(sell[j], buy[j] + prices[i]);
			}
		}
		return sell[k];
	}
	
	public static void main(String[] args) {
		BuySellStockIIV stock = new BuySellStockIIV();
		System.out.println(stock.maxProfit(2, new int[]{4,4,6,1,1,4,2,5}));
	}

}
