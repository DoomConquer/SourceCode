import java.util.Arrays;
import java.util.Random;

public class ShuffleanArray {

	public static void main(String[] args) {
		int[] nums = new int[]{1,2,3};
		Solution2 sol = new Solution2(nums);
		sol.shuffle();
		sol.reset();
		sol.shuffle();
	}

}

class Solution2 {

	private int[] nums;
	private int[] copy;
	private Random ran = new Random();
    public Solution2(int[] nums) {
        this.nums = nums;
        this.copy = Arrays.copyOf(nums, nums.length);
    }
    
    /** Resets the array to its original configuration and return it. */
    public int[] reset() {
    	this.nums = Arrays.copyOf(copy, nums.length);
        return nums;
    }
    
    /** Returns a random shuffling of the array. */
    public int[] shuffle() {
        for(int i = nums.length - 1; i >= 0; i--){
        	int pos = ran.nextInt(i + 1);
        	swap(i, pos);
        }
        return nums;
    }
    
    private void swap(int x, int y){
    	int temp = nums[x];
    	nums[x] = nums[y];
    	nums[y] = temp;
    }
}