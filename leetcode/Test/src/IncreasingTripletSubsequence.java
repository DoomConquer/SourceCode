import java.util.Arrays;

/**
 * @author li_zhe ÀàËÆLongest Increasing SubsequenceË¼Â·
 */
public class IncreasingTripletSubsequence {

	public boolean increasingTriplet(int[] nums) {
		if (nums == null || nums.length < 3)
			return false;
		int[] triple = new int[3];
		triple[0] = nums[0];
		int index = 0;
		for (int i = 1; i < nums.length; i++) {
			if (nums[i] > triple[index]) {
				index++;
				if (index >= 2)
					return true;
				triple[index] = nums[i];
			} else {
				int pos = Arrays.binarySearch(triple, 0, index, nums[i]);
				if (pos < 0) {
					triple[-pos - 1] = nums[i];
				}
			}
		}
		return false;
	}

	public boolean increasingTriplet1(int[] nums) {
		int first = Integer.MAX_VALUE, second = Integer.MAX_VALUE;
		for (int i = 0; i < nums.length; i++) {
			if (nums[i] <= first)
				first = nums[i];
			else if (nums[i] <= second)
				second = nums[i];
			else
				return true;
		}
		return false;
	}

	public static void main(String[] args) {
		IncreasingTripletSubsequence triple = new IncreasingTripletSubsequence();
		System.out.println(triple.increasingTriplet(new int[] { 1, 2, 3, 4, 5 }));
		System.out.println(triple.increasingTriplet(new int[] { 5, 4, 3, 2, 1 }));
		System.out.println(triple.increasingTriplet(new int[] { 5, 4, 1, 2, 3 }));
		System.out.println(triple.increasingTriplet(new int[] { 5, 1, 1, 2, 2 }));
		System.out.println(triple.increasingTriplet(new int[] { 1, 1, 1 }));
		System.out.println(triple
				.increasingTriplet(new int[] { Integer.MIN_VALUE, Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2 }));
	}

}
