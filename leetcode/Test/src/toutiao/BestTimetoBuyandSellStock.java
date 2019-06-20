package toutiao;

public class BestTimetoBuyandSellStock {

    public int maxProfit(int[] prices) {
    	if(prices == null || prices.length <= 1) return 0;
    	int max = 0, maxPrice = prices[prices.length - 1];
    	for(int i = prices.length - 2; i >= 0; i--){
    		if(prices[i] > maxPrice) maxPrice = prices[i];
    		if(prices[i] < maxPrice) max =  Math.max(max, maxPrice - prices[i]);
    	}
    	return max;
    }
    
	public static void main(String[] args) {
		BestTimetoBuyandSellStock bestTimetoBuyandSellStock = new BestTimetoBuyandSellStock();
		System.out.println(bestTimetoBuyandSellStock.maxProfit(new int[]{7,1,5,3,6,4}));
		System.out.println(bestTimetoBuyandSellStock.maxProfit(new int[]{7,6,4,3,1}));
		System.out.println(bestTimetoBuyandSellStock.maxProfit(new int[]{7}));
		System.out.println(bestTimetoBuyandSellStock.maxProfit(new int[]{7,10}));
	}

}
