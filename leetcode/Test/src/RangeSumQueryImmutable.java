
public class RangeSumQueryImmutable {

	public static void main(String[] args) {
		int[] num = new int[]{-2, 0, 3, -5, 2, -1};
		NumArray1 numArray = new NumArray1(num);
		System.out.println(numArray.sumRange(0, 2));
		System.out.println(numArray.sumRange(2, 5));
		System.out.println(numArray.sumRange(0, 5));
		System.out.println(numArray.sumRange(5, 5));
	}

}

class NumArray1 {

	private int[] sum;
    public NumArray1(int[] nums) {
    	if(nums.length == 0) return;
        sum = new int[nums.length];
        sum[0] = nums[0];
        for(int i = 1; i < nums.length; i++){
        	sum[i] = sum[i - 1] + nums[i];
        }
    }
    
    public int sumRange(int i, int j) {
    	if(i == 0) return sum[j];
        return sum[j] - sum[i - 1];
    }
}