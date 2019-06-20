import java.util.Arrays;

public class HeightChecker {

    public int heightChecker(int[] heights) {
        int[] nums = new int[heights.length];
        System.arraycopy(heights, 0, nums, 0, heights.length);
        Arrays.sort(nums);
        int res = 0;
        for(int i = 0; i < heights.length; i++){
        	if(nums[i] != heights[i]) res++;
        }
        return res;
    }
    
	public static void main(String[] args) {
		HeightChecker heightChecker = new HeightChecker();
		System.out.println(heightChecker.heightChecker(new int[]{1,1,4,2,1,3}));
		System.out.println(heightChecker.heightChecker(new int[]{1,1,4,2,3,1}));
		System.out.println(heightChecker.heightChecker(new int[]{1,1,4,3,2,1}));
		System.out.println(heightChecker.heightChecker(new int[]{1,2,1,2,1,1,1,2,1}));
	}

}
