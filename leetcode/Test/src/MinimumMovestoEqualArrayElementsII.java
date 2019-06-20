import java.util.Arrays;

// 解题思路，求解sum(|x - numi|)的最小值，求导让导数等于0，计算出取n/2位置的元素高度时，需要变化的次数最少
public class MinimumMovestoEqualArrayElementsII {
	
    public int minMoves2(int[] nums) {
        Arrays.sort(nums);
        int height = nums[nums.length / 2];
        long sum = 0;
        for(int num : nums){
        	sum += Math.abs(height - num);
        }
        return (int)sum;
    }

	public static void main(String[] args) {
		MinimumMovestoEqualArrayElementsII minimumMovestoEqualArrayElementsII = new MinimumMovestoEqualArrayElementsII();
		System.out.println(minimumMovestoEqualArrayElementsII.minMoves2(new int[]{1,2,3}));
		System.out.println(minimumMovestoEqualArrayElementsII.minMoves2(new int[]{1,1,10}));
		System.out.println(minimumMovestoEqualArrayElementsII.minMoves2(new int[]{1,1,1}));
		System.out.println(minimumMovestoEqualArrayElementsII.minMoves2(new int[]{1,2,2}));
	}
}
