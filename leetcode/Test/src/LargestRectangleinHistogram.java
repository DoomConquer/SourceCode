import java.util.Stack;

/**
 * @author li_zhe
 * 解题思路来源于leetcode
 * 利用stack保存递增的bar
 */
public class LargestRectangleinHistogram {

	public int largestRectangleArea(int[] heights) {
		if(heights == null || heights.length == 0) return 0;
		int max = 0;
		Stack<Integer> stack = new Stack<>();
		int i = 0, len = heights.length;
		while(i <= len){
			int curr = (i == len ? 0 : heights[i]);
			if(stack.isEmpty() || curr >= heights[stack.peek()]){
				stack.push(i);
			}else{
				int top = stack.pop();
				max = Math.max(max, heights[top] * (stack.isEmpty() ? i : i - stack.peek() - 1));
				i--;
			}
			i++;
		}
		return max;
	}
	
	public static void main(String[] args) {
		LargestRectangleinHistogram histogram = new LargestRectangleinHistogram();
		System.out.println(histogram.largestRectangleArea(new int[]{2,1,5,6,2,3}));
		System.out.println(histogram.largestRectangleArea(new int[]{4,2,1,3}));
		System.out.println(histogram.largestRectangleArea(new int[]{1}));
		System.out.println(histogram.largestRectangleArea(new int[]{1,10}));
	}

}
