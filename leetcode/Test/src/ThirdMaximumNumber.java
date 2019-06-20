
public class ThirdMaximumNumber {

	public int thirdMax(int[] nums) {
		if(nums.length == 1) return nums[0];
		int max1 = nums[0], max2 = Integer.MIN_VALUE, max3 = Integer.MIN_VALUE;
		for(int i = 1; i < nums.length; i++){
			if(nums[i] > max1){
				max1 = nums[i];
			}
		}
		for(int i = 0; i < nums.length; i++){
			if(nums[i] != max1 && nums[i] > max2){
				max2 = nums[i];
			}
		}
		boolean flag = false;
		for(int i = 0; i < nums.length; i++){
			if(nums[i] != max1 && nums[i] != max2 && nums[i] >= max3){
				max3 = nums[i];
				flag = true;
			}
		}
		if(!flag) return max1;
		return max3;
	}
	public int thirdMax1(int[] nums) {
        Integer max1 = null;
        Integer max2 = null;
        Integer max3 = null;
        for (Integer n : nums) {
            if (n.equals(max1) || n.equals(max2) || n.equals(max3)) continue;
            if (max1 == null || n > max1) {
                max3 = max2;
                max2 = max1;
                max1 = n;
            } else if (max2 == null || n > max2) {
                max3 = max2;
                max2 = n;
            } else if (max3 == null || n > max3) {
                max3 = n;
            }
        }
        return max3 == null ? max1 : max3;
    }
	
	public static void main(String[] args) {
		ThirdMaximumNumber max = new ThirdMaximumNumber();
		System.out.println(max.thirdMax(new int[]{2, 2, 3, 1}));
		System.out.println(max.thirdMax(new int[]{2, 1}));
		System.out.println(max.thirdMax(new int[]{1}));
		System.out.println(max.thirdMax(new int[]{1, 3, 1, 5, 1, 2}));
		System.out.println(max.thirdMax(new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE + 1, 5}));
		System.out.println(max.thirdMax(new int[]{Integer.MIN_VALUE, 1, 2}));
	}

}
