
/**
 * @author li_zhe
 * 解题思路来源leetcode网友分析
 * 找到长的数组中的一个位置，满足nums1和num2中左半部分和右半部分元素个数一样
 */
public class MedianofTwoSortedArrays {

	public double findMedianSortedArrays(int[] nums1, int[] nums2) {
		int len1 = nums1.length;
		int len2 = nums2.length;
		if(len1 > len2) return findMedianSortedArrays(nums2, nums1);
		int half = (len1 + len2 + 1) >>> 1;
		int left = 0, right = len1;
		while(left < right){
			int pos1 = left + (right - left) / 2;
			int pos2 = half - pos1 - 1;
			if(nums1[pos1] > nums2[pos2]) right = pos1;
			else left = pos1 + 1;
		}
		int pos1 = left;
		int pos2 = half - left;
		int res1 = Math.max(pos1 <= 0 ? Integer.MIN_VALUE : nums1[pos1 - 1], pos2 <= 0 ? Integer.MIN_VALUE : nums2[pos2 - 1]);
		if((len1 + len2) % 2 == 1) return res1;
		int res2 = Math.min(pos1 >= len1 ? Integer.MAX_VALUE : nums1[pos1], pos2 >= len2 ? Integer.MAX_VALUE : nums2[pos2]);
		return (res1 + res2) / 2.0;
	}
	
	public static void main(String[] args) {
		MedianofTwoSortedArrays median = new MedianofTwoSortedArrays();
		System.out.println(median.findMedianSortedArrays(new int[]{1,3}, new int[]{2}));
		System.out.println(median.findMedianSortedArrays(new int[]{1}, new int[]{2}));
	}

}
