
public class TwoSumII {

	public int[] twoSum(int[] numbers, int target) {
		for(int left = 0, right = numbers.length - 1;left < right;){
			if(numbers[left] + numbers[right] == target){
				return new int[]{left + 1, right + 1};
			}else if(numbers[left] + numbers[right] < target) left++;
			else right--;
		}
		return null;
	}
	
	public static void main(String[] args) {
		TwoSumII sum = new TwoSumII();
		System.out.println(sum.twoSum(new int[]{2, 7, 11, 15}, 9)[0]);
	}

}
