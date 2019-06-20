
public class MoveZeroes {

	public void moveZeroes(int[] nums) {
		if(nums == null || nums.length == 0) return;
		int len = nums.length;
		for(int zero = 0, nonZero = 0; zero < len && nonZero < len;){
			if(nums[zero] == 0 && nums[nonZero] != 0 && zero < nonZero){
				nums[zero] = nums[nonZero];
				nums[nonZero] = 0;
				zero++;
				nonZero++;
			}else if(zero > nonZero){
				nonZero++;
			}else if(nums[zero] != 0) zero++;
			else if(nums[nonZero] == 0) nonZero++;
		}
		for(int num : nums)
			System.out.print(num + "  ");
	}
	
	public static void main(String[] args) {
		MoveZeroes move = new MoveZeroes();
		move.moveZeroes(new int[]{0,0,0,0,3,1});
	}

}
