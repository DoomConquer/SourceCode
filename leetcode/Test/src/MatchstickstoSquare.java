import java.util.Arrays;

public class MatchstickstoSquare {

	public boolean makesquare(int[] nums) {
		if(nums == null || nums.length == 0) return false;
		int sum = 0;
		for(int num : nums) sum += num;
		if(sum % 4 != 0) return false;
		Arrays.sort(nums);
		return square(nums, new int[4], nums.length - 1, sum / 4);
	}
	private boolean square(int[] nums, int[] res, int layer, int max){
		if(layer < 0){
			for(int i = 1; i < 4; i++){
				if(res[i - 1] != res[i]){
					return false;
				}
			}
			return true;
		}else{
			for(int i = 0; i < 4; i++){
				if(res[i] + nums[layer] <= max){
					res[i] += nums[layer];
					if(square(nums, res, layer - 1, max)) return true;
					res[i] -= nums[layer];
				}
			}
		}
		return false;
	}
	
	public static void main(String[] args) {
		MatchstickstoSquare matchsticks = new MatchstickstoSquare();
		System.out.println(matchsticks.makesquare(new int[]{2,3,3,3,2,2,2}));
		System.out.println(matchsticks.makesquare(new int[]{10,6,5,5,5,3,3,3,2,2,2,2}));
		System.out.println(matchsticks.makesquare(new int[]{12,12,12,16,20,24,28,32,36,40,44,48,52,56,60}));
		System.out.println(matchsticks.makesquare(new int[]{3,3,3,3,4}));
	}

}
