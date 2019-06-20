import java.util.Arrays;

/**
 * @author li_zhe
 * 解题思路来自leetcode,用类完全背包解有问题,没想明白
 * DP思想,dp[i]表示价格为i需要最少的coin数，则状态转移方程：dp[i] = min(dp[i], dp[i - coins[j]] + 1)
 */
public class CoinChange {
	//迭代
	public int coinChange(int[] coins, int amount) {
		if(amount == 0) return 0;
		if(coins == null || coins.length == 0) return -1;
		int[] price = new int[amount + 1];
		Arrays.fill(price, Integer.MAX_VALUE - 1);
		price[0] = 0;
		for(int i = 1; i <= amount; i++){
			for(int j = 0; j < coins.length; j++){
				if(i >= coins[j]){
					price[i] = Math.min(price[i], price[i - coins[j]] + 1);
				}
			}
		}
		return price[amount] == Integer.MAX_VALUE - 1 ? -1 : price[amount];
	}
	// 递归
	public int coinChange1(int[] coins, int amount) {
		if(amount < 1) return 0;
		return coin(coins, amount, new int[amount]);
	}
	private int coin(int[] coins, int left, int[] price){
		if(left < 0) return -1;
		if(left == 0) return 0;
		if(price[left - 1] != 0) return price[left - 1];
		int min = Integer.MAX_VALUE;
		for(int i = 0; i < coins.length; i++){
			if(left >= coins[i]){
				int res = coin(coins, left - coins[i], price);
				if(res >= 0 && res < min)
					min = res + 1;
			}
		}
		price[left - 1] = min == Integer.MAX_VALUE ? -1 : min;
		return price[left - 1];
	}
	
	public static void main(String[] args) {
		CoinChange coin = new CoinChange();
		System.out.println(coin.coinChange(new int[]{1,2,5}, 11));
		System.out.println(coin.coinChange(new int[]{1,2,5}, 1));
		System.out.println(coin.coinChange(new int[]{1,2,5}, 2));
		System.out.println(coin.coinChange(new int[]{1,2,5}, 5));
		System.out.println(coin.coinChange(new int[]{1,2,5}, 8));
		System.out.println(coin.coinChange(new int[]{1,2,5}, 100));
		System.out.println(coin.coinChange(new int[]{2}, 2));
		System.out.println(coin.coinChange(new int[]{2}, 3));
		System.out.println(coin.coinChange(new int[]{2}, 11));
		System.out.println(coin.coinChange(new int[]{1}, 0));
		System.out.println(coin.coinChange(new int[]{186,419,83,408}, 6249));
	}
}
