import java.util.Arrays;

public class SortanArray {

    public int[] sortArray(int[] nums) {
        Arrays.sort(nums);
        return nums;
    }
    
	public static void main(String[] args) {
		SortanArray sortanArray = new SortanArray();
		System.out.println(Arrays.toString(sortanArray.sortArray(new int[]{5,2,3,1})));
	}

}
