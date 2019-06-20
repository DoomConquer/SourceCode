import java.util.Arrays;

public class ThreeSumClosest {

	public int threeSumClosest(int[] nums, int target) {
		Arrays.sort(nums);
		int min = Integer.MAX_VALUE;
		int res = 0;
		for(int i = 0; i < nums.length - 2; i++){
			if(i == 0 || (i > 0 && nums[i] != nums[i - 1])){
				int left = i + 1;
				int right = nums.length - 1;
				while(left < right){
					int sum = nums[left] + nums[right] + nums[i];
					if(sum == target){
						return target;
					}else if(sum > target) {
						int num = Math.abs(target - sum);
						if(num < min){
							min = num;
							res = sum;
						}
						right--;
					}
					else {
						int num = Math.abs(target - sum);
						if(num < min){
							min = num;
							res = sum;
						}
						left++;
					}
				}
			}
		}
		return res;
	}
	
	public static void main(String[] args) {
		ThreeSumClosest closest = new ThreeSumClosest();
		System.out.println(closest.threeSumClosest(new int[]{1,1,-1,-1,3}, 1));
	}
}
