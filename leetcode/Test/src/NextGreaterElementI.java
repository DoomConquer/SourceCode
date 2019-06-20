import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class NextGreaterElementI {

    public int[] nextGreaterElement(int[] nums1, int[] nums2) {
        Stack<Integer> stack = new Stack<>();
        Map<Integer, Integer> map = new HashMap<>();
        for(int i = 0; i < nums2.length; i++){
        	while(!stack.isEmpty() && nums2[i] > nums2[stack.peek()]){
        		map.put(nums2[stack.pop()], nums2[i]);
        	}
        	stack.push(i);
        }
        int[] res = new int[nums1.length];
        for(int i = 0; i < nums1.length; i++){
        	res[i] = map.getOrDefault(nums1[i], -1);
        }
        return res;
    }
    
	public static void main(String[] args) {
		NextGreaterElementI nextGreaterElementI = new NextGreaterElementI();
		System.out.println(Arrays.toString(nextGreaterElementI.nextGreaterElement(new int[]{4,1,2}, new int[]{1,3,4,2})));
		System.out.println(Arrays.toString(nextGreaterElementI.nextGreaterElement(new int[]{2,4}, new int[]{1,2,3,4})));
	}

}
