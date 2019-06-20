import java.util.Stack;

public class RemoveAllAdjacentDuplicatesInString {

    public String removeDuplicates(String S) {
        if(S == null || S.length() == 0) return S;
        Stack<Character> stack = new Stack<>();
        for(char ch : S.toCharArray()){
        	if(stack.isEmpty()) stack.push(ch);
        	else{
        		if(stack.peek() == ch) stack.pop();
        		else stack.push(ch);
        	}
        }
        StringBuilder sb = new StringBuilder();
        while(!stack.isEmpty()) sb.append(stack.pop());
        return sb.reverse().toString();
    }
    
	public static void main(String[] args) {
		RemoveAllAdjacentDuplicatesInString removeAllAdjacentDuplicatesInString = new RemoveAllAdjacentDuplicatesInString();
		System.out.println(removeAllAdjacentDuplicatesInString.removeDuplicates("abbaca"));
		System.out.println(removeAllAdjacentDuplicatesInString.removeDuplicates("abca"));
		System.out.println(removeAllAdjacentDuplicatesInString.removeDuplicates("aaaaaaaa"));
		System.out.println(removeAllAdjacentDuplicatesInString.removeDuplicates("aaaaaaaaa"));
		System.out.println(removeAllAdjacentDuplicatesInString.removeDuplicates("abba"));
	}

}
