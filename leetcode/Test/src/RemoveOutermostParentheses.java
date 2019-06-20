import java.util.Stack;

public class RemoveOutermostParentheses {

    public String removeOuterParentheses(String S) {
        Stack<Character> stack = new Stack<>();
        StringBuilder sb = new StringBuilder();
        for(char ch : S.toCharArray()){
        	if(ch == '('){
        		if(!stack.isEmpty()) sb.append(ch);
        		stack.push(ch);
        	}else{
        		stack.pop();
        		if(!stack.isEmpty()) sb.append(ch);
        	}
        }
        return sb.toString();
    }
    
    public String removeOuterParentheses1(String S) {
        StringBuilder s = new StringBuilder();
        int opened = 0;
        for (char c : S.toCharArray()) {
            if (c == '(' && opened++ > 0) s.append(c);
            if (c == ')' && opened-- > 1) s.append(c);
        }
        return s.toString();
    }
    
	public static void main(String[] args) {
		RemoveOutermostParentheses removeOutermostParentheses = new RemoveOutermostParentheses();
		System.out.println(removeOutermostParentheses.removeOuterParentheses("(()())(())"));
		System.out.println(removeOutermostParentheses.removeOuterParentheses("(()())(())(()(()))"));
		System.out.println(removeOutermostParentheses.removeOuterParentheses("()()"));
		System.out.println(removeOutermostParentheses.removeOuterParentheses("(((()())))"));
	}

}
