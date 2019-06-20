import java.util.Arrays;

// ����˼·�����sum(|x - numi|)����Сֵ�����õ�������0�������ȡn/2λ�õ�Ԫ�ظ߶�ʱ����Ҫ�仯�Ĵ�������
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
