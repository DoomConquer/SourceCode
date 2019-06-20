import java.util.Stack;

/**
 * @author li_zhe
 * 思路来源leetcode，自己思路存在问题
 * stack，O(n),O(n)记录和)匹配的前一个位置，最开始push -1
 * 
 * DP思路 O(n),O(n)
 * 
 * 双指针 O(n),O(1)
 */
public class LongestValidParentheses {

	public int longestValidParentheses(String s) {
		if(s == null || s.length() == 0) return 0;
		Stack<Integer> stack = new Stack<>();
		stack.push(-1);
		char[] sch = s.toCharArray();
		int longest = 0;
		for(int i = 0; i < sch.length; i++){
			if(sch[i] == '(') stack.push(i);
			else{
				stack.pop();
				if(stack.isEmpty()) stack.push(i);
				else longest = Math.max(longest, i - stack.peek());
			}
		}
		return longest;
	}
	
	public int longestValidParentheses1(String s) {
		if(s == null || s.length() == 0) return 0;
		int[] dp = new int[s.length()];
		char[] sch = s.toCharArray();
		int longest = 0;
		for(int i = 1; i < sch.length; i++){
			if(sch[i] == ')'){
				if(sch[i - 1] == '(') 
					dp[i] = (i - 2 >= 0 ? dp[i - 2] : 0) + 2;
				if(i - dp[i - 1] > 0 && sch[i - dp[i - 1] - 1] == '(') 
					dp[i] = dp[i - 1] + (i - dp[i - 1] - 2 >= 0 ? dp[i - dp[i - 1] - 2] : 0) + 2;
			}
			longest = Math.max(longest, dp[i]);
		}
		return longest;
	}
	
	public int longestValidParentheses2(String s) {
        int left = 0, right = 0, maxlength = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '(') {
                left++;
            } else {
                right++;
            }
            if (left == right) {
                maxlength = Math.max(maxlength, 2 * right);
            } else if (right >= left) {
                left = right = 0;
            }
        }
        left = right = 0;
        for (int i = s.length() - 1; i >= 0; i--) {
            if (s.charAt(i) == '(') {
                left++;
            } else {
                right++;
            }
            if (left == right) {
                maxlength = Math.max(maxlength, 2 * left);
            } else if (left >= right) {
                left = right = 0;
            }
        }
        return maxlength;
    }
	
	public static void main(String[] args) {
		LongestValidParentheses longest = new LongestValidParentheses();
		System.out.println(longest.longestValidParentheses("()(()"));
		System.out.println(longest.longestValidParentheses("))()(())"));
		System.out.println(longest.longestValidParentheses1("(()()"));
	}

}
