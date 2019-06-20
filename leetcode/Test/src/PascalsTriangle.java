import java.util.ArrayList;
import java.util.List;

public class PascalsTriangle {

	public List<List<Integer>> generate(int numRows) {
		List<List<Integer>> res = new ArrayList<List<Integer>>();
		if(numRows == 0) return res;
		int[] nums = new int[numRows];
		for(int i = 0; i < numRows; i++){
			nums[i] = 1;
			int temp1 = 0, temp2 = nums[0];
			for(int j = 1; j < i; j++){
				temp1 = nums[j];
				nums[j] = temp1 + temp2;
				temp2 = temp1;
			}
			List<Integer> list = new ArrayList<>();
			for(int j = 0; j < i + 1; j++)
				list.add(nums[j]);
			res.add(list);
		}
		return res;
	}
	
	public static void main(String[] args) {
		PascalsTriangle triangle = new PascalsTriangle();
		System.out.println(triangle.generate(10));
		System.out.println(triangle.generate(1));
	}

}
