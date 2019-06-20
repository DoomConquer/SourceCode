public class TargetSum {

	public int findTargetSumWays(int[] nums, int S) {
		if(nums == null || nums.length == 0) return 0;
		find(nums, S, 0);
		return count;
	}
	int count = 0;
	int[] signal = new int[]{1, -1};
	private void find(int[] nums, int sum, int layer){
		if(layer == nums.length){
			if(sum == 0) count++;
			return;
		}
		for(int i = 0; i < 2; i++){
			sum -= nums[layer] * signal[i];
			find(nums, sum, layer + 1);
			sum += nums[layer] * signal[i];
		}
	}
	
//	public int findTargetSumWays(int[] nums, int S) {
//		if(nums == null || nums.length == 0) return 0;
//		int[] map = new int[nums.length];
//		return find(nums, S, map, 0);
//	}
//	int[] signal = new int[]{1, -1};
//	private int find(int[] nums, int sum, int[] map, int layer){
//		if(layer == nums.length) {
//			if(sum == 0) return 1;
//			return 0;
//		}
//		map[layer] = 0;
//		for(int i = 0; i < 2; i++){
//			map[layer] += find(nums, sum + nums[layer] * signal[i], map, layer + 1);
//		}
//		return map[layer];
//	}
	
	public static void main(String[] args) {
		TargetSum sum = new TargetSum();
		System.out.println(sum.findTargetSumWays(new int[]{1, 1, 1, 1, 1}, 3));
		System.out.println(sum.findTargetSumWays(new int[]{1, 2, 3, 1, 5}, 10));
	}

}
