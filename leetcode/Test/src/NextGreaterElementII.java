import java.util.Arrays;
import java.util.Stack;

public class NextGreaterElementII {

    public int[] nextGreaterElements(int[] nums) {
        Stack<Integer> stack = new Stack<>();
        int n = nums.length;
        int[] res = new int[n];
        Arrays.fill(res, -1);
        for(int i = 0; i < 2 * n; i++){
        	while(!stack.isEmpty() && nums[i % n] > nums[stack.peek() % n]) res[stack.pop() % n] = nums[i % n];
        	stack.push(i);
        }
        return res;
    }
    
	public static void main(String[] args) {
		NextGreaterElementII nextGreaterElementII = new NextGreaterElementII();
		System.out.println(Arrays.toString(nextGreaterElementII.nextGreaterElements(new int[]{1,2,1})));
	}

}
