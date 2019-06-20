import java.util.Arrays;

/**
 * @author li_zhe
 * ˼·�ο�leetcode��O��nlgn���Ľⷨ��ֱ̫�ۣ�DP + ���ֲ��ң���
 * ˼·��ά��һ������õ����飬ÿ�θ��±�ĩβ�Ĵ�ͼ��ں��棬������ֲ���Ӧ�ò����λ���滻ԭ����Ԫ��
 */
public class LongestIncreasingSubsequence {

	public int lengthOfLIS(int[] nums) {
		if(nums == null || nums.length == 0) return 0;
		int[] sorted = new int[nums.length];
		sorted[0] = nums[0];
		int index = 0;
		for(int i = 1; i < nums.length; i++){
			if(nums[i] > sorted[index]){
				index++;
				sorted[index] = nums[i];
			}else{
				int pos = Arrays.binarySearch(sorted, 0, index, nums[i]);
				if(pos < 0){
					sorted[- pos - 1] = nums[i];
				}
			}
		}
		return index + 1;
	}
	
	public static void main(String[] args) {
		LongestIncreasingSubsequence longest = new LongestIncreasingSubsequence();
		System.out.println(longest.lengthOfLIS(new int[]{10,9,2,5,3,7,101,18}));
		System.out.println(longest.lengthOfLIS(new int[]{18,9,2,5,3,7,1,3}));
		System.out.println(longest.lengthOfLIS(new int[]{10,9,2,5,3,4}));
	}

}
