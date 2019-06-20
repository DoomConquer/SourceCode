import java.util.Deque;
import java.util.LinkedList;

/**
 * @author li_zhe
 * O(logK*n)的思路不难想,O(n)的思路不太容易，参考leetcode
 * 利用Deque维护滑动窗口k以内的有序数字
 */
public class SlidingWindowMaximum {

	public int[] maxSlidingWindow(int[] nums, int k) {
		if(nums.length == 0) return new int[]{};
		int len = nums.length;
		int[] res = new int[len - k + 1];
		Deque<Integer> deq = new LinkedList<>();
		for(int i = 0; i < len; i++){
			// 每当新数进来时，如果发现队列头部的数的下标，是窗口最左边数的下标，则扔掉
			if(!deq.isEmpty() && deq.peekFirst() == i - k) deq.pollFirst();
			// 把队列尾部所有比新数小的都扔掉，保证队列是降序的
			while(!deq.isEmpty() && nums[deq.peekLast()] < nums[i]) deq.removeLast();
			deq.addLast(i);
			// 队列头部就是该窗口内第一大的
			if(i >= k - 1) res[i - k + 1] = nums[deq.peekFirst()];
		}
		return res;
	}
	
	private static void print(int[] nums){
		for(int i = 0; i < nums.length; i++)
			System.out.print(nums[i] + " ");
		System.out.println();
	}
	public static void main(String[] args) {
		SlidingWindowMaximum slid = new SlidingWindowMaximum();
		int[] res = slid.maxSlidingWindow(new int[]{1,3,-1,-3,5,3,6,7}, 3);
		print(res);
	}

}
