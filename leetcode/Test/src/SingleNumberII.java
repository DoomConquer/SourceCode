
public class SingleNumberII {

	public int singleNumber(int[] nums) {
        int ones = 0, twos = 0, three = 0;
        for(int i = 0; i < nums.length; i++){
        	twos |= ones & nums[i];
        	ones ^= nums[i];
        	three = ~(twos & ones);
        	twos &= three;
        	ones &= three;
        }
        return ones;
    }
	public static void main(String[] args) {
		SingleNumberII number = new SingleNumberII();
		System.out.println(number.singleNumber(new int[]{1,2,1,1,3,3,3,4,5,4,5,4,5}));
	}
}
