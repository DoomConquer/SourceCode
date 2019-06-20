
public class MinCostClimbingStairs {

	public int minCostClimbingStairs(int[] cost) {
		if(cost == null || cost.length == 0) return 0;
		if(cost.length == 1) return cost[0];
		int len = cost.length;
		int[] costs = new int[len + 1];
		for(int i = 2; i <= len; i++){
			costs[i] = Math.min(costs[i -1] + cost[i - 1], costs[i - 2] + cost[i - 2]);
		}
		return costs[len];
	}
	
	public static void main(String[] args) {
		MinCostClimbingStairs climb= new MinCostClimbingStairs();
		System.out.println(climb.minCostClimbingStairs(new int[]{1, 100, 1, 1, 1, 100, 1, 1, 100, 1}));
		System.out.println(climb.minCostClimbingStairs(new int[]{10, 15, 20}));
		System.out.println(climb.minCostClimbingStairs(new int[]{10}));
	}

}