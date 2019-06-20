
public class MergeSortedArray {

	public void merge(int[] nums1, int m, int[] nums2, int n) {
		while(m > 0 && n > 0){
			if(nums2[n - 1] < nums1[m - 1]){
				nums1[m + n - 1] = nums1[m - 1];
				m--;
			}else{
				nums1[m + n - 1] = nums2[n - 1];
				n--;
			}
		}
		while(n > 0){
			nums1[m + n - 1] = nums2[n - 1];
			n--;
		}
		for(int i = 0; i < nums1.length; i++){
			System.out.print(nums1[i] + "  ");
		}
	}
	
	public static void main(String[] args) {
		MergeSortedArray merge = new MergeSortedArray();
		merge.merge(new int[]{0,1,2}, 0, new int[]{}, 0);
	}

}
