
public class WiggleSortII {

	public void wiggleSort(int[] nums) {
		int mid = findKLargest(nums, 0, nums.length - 1, nums.length >> 1);
		int len = nums.length;
		int left = 0, curr = 0, right = len - 1;
		while(curr <= right){
			if(nums[index(curr, len)] > mid){
				swap(nums, index(left, len), index(curr, len));
				left++;
				curr++;
			}else if(nums[index(curr, len)] < mid){
				swap(nums, index(curr, len), index(right, len));
				right--;
			}else{
				curr++;
			}
		}
	}
	private int index(int x, int n){
		return (2 * x + 1) % (n | 1);
	}

	private int findKLargest(int[] nums, int l, int r, int k) {
		int pivot = nums[l];
		int i = l + 1, j = r;
		while (i <= j) {
			while (i <= j && nums[i] <= pivot) {
				++i;
			}
			while (i <= j && nums[j] > pivot) {
				--j;
			}
			if (i < j) {
				swap(nums, i, j);
				++i;
				--j;
			}
		}
		swap(nums, l, j);
		if (j == k)
			return nums[j];
		if (j < k) {
			return findKLargest(nums, j + 1, r, k);
		} else {
			return findKLargest(nums, l, j - 1, k);
		}
	}

	private void swap(int[] nums, int i, int j) {
		int temp = nums[i];
		nums[i] = nums[j];
		nums[j] = temp;
	}
	
	public static void main(String[] args) {
		WiggleSortII sort = new WiggleSortII();
		sort.wiggleSort(new int[]{1,2,1,2,5,3});
	}

}
