import java.util.Arrays;
import java.util.Stack;

public class DailyTemperatures {

    public int[] dailyTemperatures(int[] T) {
    	if(T == null || T.length == 0) return new int[]{};
        Stack<Integer> stack = new Stack<>();
        int[] res = new int[T.length];
        for(int i = 0; i < T.length; i++){
        	while(!stack.isEmpty() && T[i] > T[stack.peek()])
        		res[stack.peek()] = i - stack.pop();
        	stack.push(i);
        }
        return res;
    }
    
	public static void main(String[] args) {
		DailyTemperatures dailyTemperatures = new DailyTemperatures();
		System.out.println(Arrays.toString(dailyTemperatures.dailyTemperatures(new int[]{73, 74, 75, 71, 69, 72, 76, 73})));
		System.out.println(Arrays.toString(dailyTemperatures.dailyTemperatures(new int[]{1,2,3,1})));
	}

}
