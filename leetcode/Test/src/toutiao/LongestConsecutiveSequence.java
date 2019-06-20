package toutiao;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class LongestConsecutiveSequence {

    public int longestConsecutive(int[] nums) {
        if(nums == null || nums.length == 0) return 0;
        Set<Integer> set = new HashSet<Integer>();
        for(int num: nums) set.add(num);
        int max = 1;
        for(int num: nums) {
            if(set.remove(num)) {
                int val = num;
                int sum = 1;
                while(set.remove(val - 1)) val--;
                sum += num - val;
                
                val = num;
                while(set.remove(val + 1)) val++;
                sum += val - num;
                
                max = Math.max(max, sum);
            }
        }
        return max;
    }
    
	public static void main(String[] args) throws IOException {
		LongestConsecutiveSequence longestConsecutiveSequence = new LongestConsecutiveSequence();
		System.out.println(longestConsecutiveSequence.longestConsecutive(new int[]{100, 4, 200, 1, 3, 2}));
		System.out.println(longestConsecutiveSequence.longestConsecutive(new int[]{2, 4, 2, 1, 3, 2}));
		System.out.println(longestConsecutiveSequence.longestConsecutive(new int[]{2}));
		System.out.println(longestConsecutiveSequence.longestConsecutive(new int[]{-1, 0, 0}));
		System.out.println(longestConsecutiveSequence.longestConsecutive(new int[]{2, 4, 6, 7, 5}));
		System.out.println(longestConsecutiveSequence.longestConsecutive(new int[]{1, 2, 3, 4, 5, 6}));
		System.out.println(longestConsecutiveSequence.longestConsecutive(new int[]{1, 2, 3, -1, -2, -3, -4}));
	}

}
