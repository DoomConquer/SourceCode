// 参考leetcode思路，diff = sum - 2 * neg，使diff最小化，则neg最大化，但neg要小于sum / 2
public class LastStoneWeightII {

    public int lastStoneWeightII(int[] stones) {
        if(stones == null || stones.length == 0) return 0;
        int sum = 0;
        for(int stone : stones) sum += stone;
        boolean[] dp = new boolean[sum / 2 + 1];
        dp[0] = true;
        int min = Integer.MAX_VALUE;
        for(int stone : stones){
        	for(int neg = sum / 2; neg >= stone; neg--){
        		dp[neg] = dp[neg] | dp[neg - stone];
        		if(dp[neg]) min = Math.min(min, sum - 2 * neg);
        	}
        }
        return min;
    }
    
	public static void main(String[] args) {
		LastStoneWeightII lastStoneWeightII = new LastStoneWeightII();
		System.out.println(lastStoneWeightII.lastStoneWeightII(new int[]{2,7,4,1,8,1}));
		System.out.println(lastStoneWeightII.lastStoneWeightII(new int[]{3,3,3,1}));
	}

}
