import java.util.Deque;
import java.util.LinkedList;

/**
 * @author li_zhe
 * O(logK*n)��˼·������,O(n)��˼·��̫���ף��ο�leetcode
 * ����Dequeά����������k���ڵ���������
 */
public class SlidingWindowMaximum {

	public int[] maxSlidingWindow(int[] nums, int k) {
		if(nums.length == 0) return new int[]{};
		int len = nums.length;
		int[] res = new int[len - k + 1];
		Deque<Integer> deq = new LinkedList<>();
		for(int i = 0; i < len; i++){
			// ÿ����������ʱ��������ֶ���ͷ���������±꣬�Ǵ�������������±꣬���ӵ�
			if(!deq.isEmpty() && deq.peekFirst() == i - k) deq.pollFirst();
			// �Ѷ���β�����б�����С�Ķ��ӵ�����֤�����ǽ����
			while(!deq.isEmpty() && nums[deq.peekLast()] < nums[i]) deq.removeLast();
			deq.addLast(i);
			// ����ͷ�����Ǹô����ڵ�һ���
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
