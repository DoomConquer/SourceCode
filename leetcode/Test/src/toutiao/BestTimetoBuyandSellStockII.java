package toutiao;

public class BestTimetoBuyandSellStockII {

    public int maxProfit(int[] prices) {
        if(prices == null || prices.length <= 1) return 0;
        int max = 0, buyPrice = prices[0], sellPrice = prices[0];
        for(int i = 1; i < prices.length; i++){
        	if(sellPrice < prices[i]) sellPrice = prices[i];
        	else{
        		max += sellPrice - buyPrice;
        		sellPrice = buyPrice = prices[i];
        	}
        }
        max += sellPrice - buyPrice;
        return max;
    }
    
	public static void main(String[] args) {
		BestTimetoBuyandSellStockII bestTimetoBuyandSellStock = new BestTimetoBuyandSellStockII();
		System.out.println(bestTimetoBuyandSellStock.maxProfit(new int[]{7,1,5,3,6,4}));
		System.out.println(bestTimetoBuyandSellStock.maxProfit(new int[]{1,2,3,4,5}));
		System.out.println(bestTimetoBuyandSellStock.maxProfit(new int[]{7,6,4,3,1}));
		System.out.println(bestTimetoBuyandSellStock.maxProfit(new int[]{7}));
		System.out.println(bestTimetoBuyandSellStock.maxProfit(new int[]{7,10}));
	}

}
