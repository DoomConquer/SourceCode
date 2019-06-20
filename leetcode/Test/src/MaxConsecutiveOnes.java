
public class MaxConsecutiveOnes {

	public int findMaxConsecutiveOnes(int[] nums) {
        if(nums == null || nums.length == 0) return 0;
        int max = 0, curr = 0;
        for(int num : nums){
        	if(num == 1){
        		curr++;
        		max = Math.max(max, curr);
        	}else curr = 0;
        }
        return max;
    }

	public static void main(String[] args) {
		MaxConsecutiveOnes ones = new MaxConsecutiveOnes();
		System.out.println(ones.findMaxConsecutiveOnes(new int[]{1,1,0,1,1,1}));
		System.out.println(ones.findMaxConsecutiveOnes(new int[]{1,1,1,1,1}));
		System.out.println(ones.findMaxConsecutiveOnes(new int[]{1,0,1,1,0}));
	}

}
