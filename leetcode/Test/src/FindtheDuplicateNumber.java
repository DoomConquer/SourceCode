
public class FindtheDuplicateNumber {

	public int findDuplicate(int[] nums) {
		int i = nums[0], j = nums[nums[0]];
		while(i != j){
			i = nums[i];
			j = nums[nums[j]];
		}
		j = 0;
		while(i != j){
			i = nums[i];
			j = nums[j];
		}
		return i;
	}
	
	public static void main(String[] args) {
		FindtheDuplicateNumber duplicate = new FindtheDuplicateNumber();
		System.out.println(duplicate.findDuplicate(new int[]{2,3,4,2,2}));
	}

}
