
public class HouseRobberII {
	public int rob(int[] nums) {
		if(nums.length == 0) return 0;
		if(nums.length == 1) return nums[0];
		if(nums.length == 2) return Math.max(nums[0], nums[1]);
        return Math.max(rob(nums, 0, nums.length - 1), rob(nums, 1, nums.length));
    }
	public int rob(int[] nums, int start, int end) {
		int preMaxRob = 0;
		int preTwoMaxRob = 0;
		int maxRob = 0;
		for(int i = start; i < end; i++){
			maxRob = Math.max(preMaxRob, preTwoMaxRob + nums[i]);
			preTwoMaxRob = preMaxRob;
			preMaxRob = maxRob;
		}
		return maxRob;
	}
	
	public static void main(String[] args) {
		HouseRobberII robber = new HouseRobberII();
		System.out.println(robber.rob(new int[]{}));
	}

}
