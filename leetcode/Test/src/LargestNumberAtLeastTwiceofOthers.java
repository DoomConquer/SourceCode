public class LargestNumberAtLeastTwiceofOthers {

    public int dominantIndex(int[] nums) {
    	int max = Integer.MIN_VALUE, secondMax = Integer.MIN_VALUE, index = 0;
        for(int i = 0; i < nums.length; i++){
        	if(nums[i] > max){
        		secondMax = max;
        		max = nums[i];
        		index = i;
        	}else if(nums[i] > secondMax) secondMax = nums[i];
        }
        return max >= 2 * secondMax ? index : -1;
    }
    
	public static void main(String[] args) {
		LargestNumberAtLeastTwiceofOthers largestNumberAtLeastTwiceofOthers = new LargestNumberAtLeastTwiceofOthers();
		System.out.println(largestNumberAtLeastTwiceofOthers.dominantIndex(new int[]{3, 6, 1, 0}));
		System.out.println(largestNumberAtLeastTwiceofOthers.dominantIndex(new int[]{1, 2, 3, 4}));
		System.out.println(largestNumberAtLeastTwiceofOthers.dominantIndex(new int[]{1, 1, 2}));
		System.out.println(largestNumberAtLeastTwiceofOthers.dominantIndex(new int[]{0, 0, 3, 2}));
		System.out.println(largestNumberAtLeastTwiceofOthers.dominantIndex(new int[]{Integer.MAX_VALUE - 1, Integer.MAX_VALUE, Integer.MIN_VALUE}));
	}

}
