
public class HouseRobber {
	public int rob(int[] nums) {
		int preMaxRob = 0;
		int preTwoMaxRob = 0;
		int maxRob = 0;
		for(int num : nums){
			maxRob = Math.max(preMaxRob, preTwoMaxRob + num);
			preTwoMaxRob = preMaxRob;
			preMaxRob = maxRob;
		}
		return maxRob;
	}
	
	public static void main(String[] args) {
		HouseRobber robber = new HouseRobber();
		System.out.println(robber.rob(new int[]{1,1,1,1,1,1,1}));
	}

}
