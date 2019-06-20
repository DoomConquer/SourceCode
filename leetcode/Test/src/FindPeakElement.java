
public class FindPeakElement {

	public int findPeakElement(int[] nums) {
		int len = nums.length;
		int left = 0, right = len - 1;
		while(left <= right){
			int mid = (left + right) >>> 1;
			if(mid - 1 >= 0 && mid + 1 < len){
				if(nums[mid] > nums[mid - 1] && nums[mid] > nums[mid + 1]) return mid;
				else if(nums[mid] < nums[mid - 1]) right = mid - 1;
				else left = mid + 1;
			}else if(mid - 1 >= 0){
				if(nums[mid] > nums[mid - 1]) return mid;
				else right = mid - 1;
			}else if(mid + 1 < len){
				if(nums[mid] > nums[mid + 1]) return mid;
				else left = mid + 1;
			}else return mid;
		}
		return 0;
	}
	
	public static void main(String[] args) {
		FindPeakElement find = new FindPeakElement();
		System.out.println(find.findPeakElement(new int[]{1,2,1,3,5,6,4}));
		System.out.println(find.findPeakElement(new int[]{2,3,1}));
		System.out.println(find.findPeakElement(new int[]{1,2}));
		System.out.println(find.findPeakElement(new int[]{1}));
	}

}
