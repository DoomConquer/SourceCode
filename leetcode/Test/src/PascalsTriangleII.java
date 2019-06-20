import java.util.Arrays;
import java.util.List;

public class PascalsTriangleII {

	public List<Integer> getRow(int rowIndex) {
		int numRows = rowIndex + 1;
		Integer[] nums = new Integer[numRows];
		for(int i = 0; i < numRows; i++){
			nums[i] = 1;
			int temp1 = 0, temp2 = nums[0];
			for(int j = 1; j < i; j++){
				temp1 = nums[j];
				nums[j] = temp1 + temp2;
				temp2 = temp1;
			}
		}
		return Arrays.asList(nums);
	}
	
	public static void main(String[] args) {
		PascalsTriangleII triangle = new PascalsTriangleII();
		System.out.println(triangle.getRow(10));
	}

}
