import java.util.ArrayList;
import java.util.List;

public class FindAllNumbersDisappearedinanArray {

	public List<Integer> findDisappearedNumbers(int[] nums) {
		List<Integer> res = new ArrayList<Integer>();
		for(int i = 0; i < nums.length; i++){
			while(nums[i] != nums[nums[i] -1]){
				if(i + 1 != nums[i]){
					int temp = nums[i];
					nums[i] = nums[temp - 1];
					nums[temp - 1] = temp;
				}
			}
		}
		for(int i = 0; i < nums.length; i++){
			if(i + 1 != nums[i])
				res.add(i + 1);
		}
		return res;
	}
	
	public List<Integer> findDisappearedNumbers2(int[] nums) {
        List<Integer> ret = new ArrayList<Integer>();
        for(int i = 0; i < nums.length; i++) {
            int val = Math.abs(nums[i]) - 1;
            if(nums[val] > 0) {
                nums[val] = -nums[val];
            }
        }
        for(int i = 0; i < nums.length; i++) {
            if(nums[i] > 0) {
                ret.add(i+1);
            }
        }
        return ret;
    }
	
	public static void main(String[] args) {
		FindAllNumbersDisappearedinanArray findAll = new FindAllNumbersDisappearedinanArray();
		List<Integer> res = findAll.findDisappearedNumbers(new int[]{4,3,2,7,8,2,3,1});
		for(int num : res)
			System.out.println(num);
	}

}
