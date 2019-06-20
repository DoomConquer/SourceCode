import java.util.HashSet;
import java.util.Stack;

public class RemoveKDigits {

	public String removeKdigits(String num, int k) {
		if(num == null || num.length() == 0) return "0";
		if(k == 0) return num;
		char[] nums = num.toCharArray();
		Stack<Integer> stack = new Stack<>();
		StringBuilder sb = new StringBuilder();
		HashSet<Integer> set = new HashSet<>();
		for(int i = 0; i < nums.length;){
			if(stack.isEmpty() || nums[i] >= nums[stack.peek()]){ 
				stack.push(i); 
				i++;
			}else{
				set.add(stack.pop());
				k--;
				if(k <= 0) break;
			}
		}
		while(k-- > 0) set.add(stack.pop());
		for(int i = 0; i < nums.length ; i++){
			if(!set.contains(i)){
				if(sb.length() == 0 && nums[i] == '0') continue;
				sb.append(nums[i]);
			}
		}
		return sb.toString().equals("") ? "0" : sb.toString();
	}
	
	public static void main(String[] args) {
		RemoveKDigits remove = new RemoveKDigits();
		System.out.println(remove.removeKdigits("1432219", 3));
		System.out.println(remove.removeKdigits("1432219", 1));
		System.out.println(remove.removeKdigits("2222219", 6));
		System.out.println(remove.removeKdigits("10200", 1));
		System.out.println(remove.removeKdigits("10", 2));
		System.out.println(remove.removeKdigits("112", 1));
	}

}
