
/**
 * @author li_zhe
 * 思路来源之前做的一道题,O(n)时间,O(1)空间
 * 利用原数组记录应该在正确位置的数,负数和超过数组长度的数不需要考虑
 */
public class FirstMissingPositive {

	public int firstMissingPositive(int[] nums) {
		if(nums == null || nums.length == 0) return 1;
		int index = 0, len = nums.length;
		while(index < len){
			if(nums[index] > len || nums[index] <= 0 || nums[index] == index + 1){ index++; continue; }
			int next = nums[index];
			while(next > 0 && next <= len && nums[next - 1] != next){
				int temp = nums[next - 1];
				nums[next - 1] = next;
				next = temp;
			}
			index++;
		}
		for(int i = 0; i < len; i++) if(nums[i] !=  i + 1) return i + 1;
		return len + 1;
	}
	
	public static void main(String[] args) {
		FirstMissingPositive first = new FirstMissingPositive();
		System.out.println(first.firstMissingPositive(new int[]{1,1,1,2}));
		System.out.println(first.firstMissingPositive(new int[]{3,4,-1,1}));
	}

}
