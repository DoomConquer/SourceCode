import java.util.Random;

public class RandomPickIndex {

	public static void main(String[] args) {
		int[] nums = new int[]{1,2,3,1,4};
		Solution1 sol = new Solution1(nums);
		System.out.println(sol.pick(1));
		System.out.println(sol.pick(2));
	}

}

class Solution1 {

	private int[] nums;
	private Random ran = null;
    public Solution1(int[] nums) {
        this.nums = nums;
        this.ran = new Random();
    }
    
    public int pick(int target) {
        int res = -1;
        int count = 0;
        for(int i = 0; i < nums.length; i++){
        	if(nums[i] != target) continue;
        	count++;
        	int random = ran.nextInt(count);
        	if(random == 0) res = i;
        }
        return res;
    }
}