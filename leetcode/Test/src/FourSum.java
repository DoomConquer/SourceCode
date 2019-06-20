import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FourSum {

	public List<List<Integer>> fourSum(int[] nums, int target) {
		List<List<Integer>> res = new ArrayList<List<Integer>>();
		Arrays.sort(nums);
		for(int i = 0; i < nums.length - 3; i++){
			if(i == 0 || (i > 0 && nums[i] != nums[i -1])){
				for(int j = i + 1; j < nums.length - 2; j++){
					if(j == i + 1 || (j > i + 1 && nums[j] != nums[j - 1])){
						int left = j + 1;
						int right = nums.length - 1;
						while(left < right){
							int sum = target - (nums[i] + nums[j]);
							if(nums[left] + nums[right] == sum){
								res.add(Arrays.asList(new Integer[]{nums[i], nums[j], nums[left], nums[right]}));
								while(left < right && nums[left] == nums[left + 1]) left++;
								while(left < right && nums[right] == nums[right -1]) right--;
								left++; right--;
							}else if(nums[left] + nums[right] < sum) left++;
							else right--;
						}
					}
				}
			}
		}
		return res;
	}
	
	public static void main(String[] args) {
		FourSum sum = new FourSum();
		System.out.println(sum.fourSum(new int[]{-494,-474,-425,-424,-391,-371,-365,-351,-345,-304,-292,-289,-283,-256,-236,-236,-236,-226,-225,-223,-217,-185,-174,-163,-157,-148,-145,-130,-103,-84,-71,-67,-55,-16,-13,-11,1,19,28,28,43,48,49,53,78,79,91,99,115,122,132,154,176,180,185,185,206,207,272,274,316,321,327,327,346,380,386,391,400,404,424,432,440,463,465,466,475,486,492}, -1211));
	}

}
