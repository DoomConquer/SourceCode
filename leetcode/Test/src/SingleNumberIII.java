
public class SingleNumberIII {
	public int[] singleNumber(int[] nums) {
        int res = 0;
        for(int num : nums)
        	res ^= num;
        res &= -res;
        int[] find = new int[2];
        for(int num : nums){
        	if((num & res) == 0)
        		find[0] ^= num;
        	else
        		find[1] ^= num;
        }
        return find;
    }
	
	public static void main(String[] args) {
		SingleNumberIII number = new SingleNumberIII();
		int[] res = number.singleNumber(new int[]{1, 2, 1, 3, 2, 5});
		System.out.println(res[0] + "  " + res[1]);
	}

}
