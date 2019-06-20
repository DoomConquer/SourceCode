package other;

public class MergeSort {
	private void merge(int[] nums, int left, int mid, int right, int[] temp){
		int l1 = left;
		int r1 = mid;
		int l2 = mid + 1;
		int r2 = right;
		int i = 0;
		while(l1 <= r1 && l2 <= r2){
			if(nums[l1] > nums[l2])
				temp[i++] = nums[l2++];
			else
				temp[i++] = nums[l1++];
		}
		while(l1 <= r1){
			temp[i++] = nums[l1++];
		}
		while(l2 <= r2){
			temp[i++] = nums[l2++];
		}
		System.arraycopy(temp, 0, nums, left, i);
	}
	public void mergeSort(int[] nums, int left, int right, int[] temp){
		if(left < right){
			int mid = (left + right) / 2;
			mergeSort(nums, left, mid, temp);
			mergeSort(nums, mid + 1, right, temp);
			merge(nums, left, mid, right, temp);
		}
	}
	public void sort(int[] nums){
		mergeSort(nums, 0, nums.length - 1, new int[nums.length]);
		for(int i = 0; i < nums.length; i++)
			System.out.print(nums[i] + " ");
		System.out.println();
	}
	
	public static void main(String[] args) {
		MergeSort mergeSort = new MergeSort();
		mergeSort.sort(new int[]{3,1,2,5,3,2,5,7,9,0,1,6,0,2,1,10,11});
		mergeSort.sort(new int[]{3,1,2});
		mergeSort.sort(new int[]{3,1});
		mergeSort.sort(new int[]{1});
		mergeSort.sort(new int[]{3,1,2,2,1,0,5});
		mergeSort.sort(new int[]{3,1,2,1,1,1,2,2,2,4,5,6,11,0,10});
	}
}
