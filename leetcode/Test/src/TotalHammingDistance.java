
public class TotalHammingDistance {

	public int totalHammingDistance(int[] nums) {
		int sum = 0;
		int len = nums.length;
		for(int i = 0; i < 32; i++){
			int ones = 0;
			for(int j = 0; j < len; j++){
				ones += (nums[j] >>> i) & 1; 
			}
			sum += ones * (len - ones);
		}
		return sum;
	}
	
	public static void main(String[] args) {
		TotalHammingDistance hamming = new TotalHammingDistance();
		System.out.println(hamming.totalHammingDistance(new int[]{4, 12, 2}));
	}

}
