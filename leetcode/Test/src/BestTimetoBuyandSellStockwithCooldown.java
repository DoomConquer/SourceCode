
/**
 * @author li_zhe
 * DP˼·,�Լ����������,�ο�leetcode
 * buy[i]��ʾ��i��������ߵ�i��û���뵫��֮ǰĳ�������˵��������
 * sell[i]��ʾ��i���������ߵ�i��������������֮ǰĳ�������˵��������
 */
public class BestTimetoBuyandSellStockwithCooldown {

	public int maxProfit(int[] prices) {
		if(prices == null || prices.length <= 1) return 0;
		int len = prices.length;
		int[] buy = new int[len];
		buy[0] = -prices[0];
		int[] sell = new int[len];
		for(int i = 1; i < len; i++){
			if(i == 1) buy[i] = Math.max(buy[i - 1], -prices[i]);
			else buy[i] = Math.max(buy[i - 1], sell[i - 2] - prices[i]);
			sell[i] = Math.max(sell[i - 1], buy[i - 1] + prices[i]);
		}
		return sell[len - 1];
	}
	
	public static void main(String[] args) {
		BestTimetoBuyandSellStockwithCooldown best = new BestTimetoBuyandSellStockwithCooldown();
		System.out.println(best.maxProfit(new int[]{1,2,3,0,2}));
	}

}
