import java.util.Stack;

/**
 * @author li_zhe
 * 思路来源YouTube leetcode解题
 * 双指针,栈
 */
public class TrappingRainWater {

	public int trap1(int[] height) {
		int sum = 0;
		int secMax = 0;
		for(int left = 0, right = height.length - 1; left < right;){
			if(height[left] < height[right]){
				secMax = Math.max(secMax, height[left]);
				sum += secMax - height[left];
				left++;
			}else{
				secMax = Math.max(secMax, height[right]);
				sum += secMax - height[right];
				right--;
			}
		}
		return sum;
	}
	
	// Stack方法
	public int trap(int[] A) {
        if (A == null) return 0;
        Stack<Integer> s = new Stack<Integer>();
        int i = 0, maxWater = 0, maxBotWater = 0;
        while (i < A.length){
            if (s.isEmpty() || A[i]<=A[s.peek()]){
                s.push(i++);
            }
            else {
                int bot = s.pop();
                maxBotWater = s.isEmpty() ? 0 : (Math.min(A[s.peek()], A[i]) - A[bot]) * (i - s.peek() - 1);
                maxWater += maxBotWater;
            }
        }
        return maxWater;
    }
	
	public static void main(String[] args) {
		TrappingRainWater water = new TrappingRainWater();
		System.out.println(water.trap(new int[]{0,1,0,2,1,0,1,3,2,1,2,1}));
		System.out.println(water.trap(new int[]{4,2,1,3}));
	}

}
