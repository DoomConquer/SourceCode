
public class SetMismatch {

	public int[] findErrorNums(int[] nums) {
		int[] map = new int[10000];
		int missing = 0;
		int dup = 0;
		for(int num : nums){
			if(map[num] != 0) dup = num;
			map[num]++;
		}
		for(int i = 1; i <= nums.length; i++){
			if(map[i] == 0) missing = i;
		}
		return new int[]{dup, missing};
	}
	
	public static void main(String[] args) {
		SetMismatch miss = new SetMismatch();
		System.out.println(miss.findErrorNums(new int[]{1,2,2,3})[1]);
	}

}
