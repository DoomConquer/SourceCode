
public class MissingNumber {
	public int missingNumber(int[] nums) {
		int sum = 0;
		for(int num : nums)
			sum += num;
		int length = nums.length;
		int total = (length * (length + 1)) / 2;
		return total - sum;
	}
	public static void main(String[] args) {
		MissingNumber number = new MissingNumber();
		System.out.println(number.missingNumber(new int[]{0, 1, 3, 4, 2, 6}));
	}
}
