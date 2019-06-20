public class BuySellStockIII {

	public int maxProfit(int[] prices) {
		if(prices.length < 2)
			return 0;
		int[] leftIndexProfit = new int[prices.length];
		int[] rightIndexProfit = new int[prices.length];
		
		int leftMin = prices[0];
		for(int i = 1; i < prices.length; i++){
			if(prices[i] < leftMin){
				leftMin = prices[i];
			}
			leftIndexProfit[i] = Math.max(leftIndexProfit[i - 1], prices[i] - leftMin);
		}
		
		int rightMax = prices[prices.length - 1];
		for(int i = prices.length - 2; i >= 0; i--){
			if(prices[i] > rightMax){
				rightMax = prices[i];
			}
			rightIndexProfit[i] = Math.max(rightIndexProfit[i + 1], rightMax - prices[i]);
		}
		
		int maxProfit = 0;
		for(int i = 0; i < prices.length; i++){
			if(maxProfit < (leftIndexProfit[i] + rightIndexProfit[i])){
				maxProfit = leftIndexProfit[i] + rightIndexProfit[i];
			}
		}
		
		return maxProfit;
	}
	
	public static void main(String[] args) {
		int[] prices = new int[]{2,1,2,0,1};
		BuySellStockIII stock = new BuySellStockIII();
		System.out.println(stock.maxProfit(prices));
	}

}
