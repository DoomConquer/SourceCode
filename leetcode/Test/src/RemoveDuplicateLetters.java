import java.util.Stack;

// 参考leetcode思路，贪心策略：排序小的字母尽量放左边
public class RemoveDuplicateLetters {

    public String removeDuplicateLetters(String s) {
        int[] alphabets = new int[26];
        char[] sch = s.toCharArray();
        for(char ch : sch) alphabets[ch - 'a']++;
        Stack<Character> stack = new Stack<>();
        boolean[] flag = new boolean[26];
        for(char ch : sch){
        	alphabets[ch - 'a']--;
        	if(flag[ch - 'a']) continue;
        	while(!stack.isEmpty() && stack.peek() > ch && alphabets[stack.peek() - 'a'] > 0){
        		flag[stack.pop() - 'a'] = false;
        	}
        	stack.push(ch);
        	flag[ch - 'a'] = true;
        }
        
        StringBuilder sb = new StringBuilder();
        while(!stack.isEmpty()) sb.append(stack.pop());
        return sb.reverse().toString();
    }
    
	public static void main(String[] args) {
		RemoveDuplicateLetters removeDuplicateLetters = new RemoveDuplicateLetters();
		System.out.println(removeDuplicateLetters.removeDuplicateLetters("cbacdcbc"));
		System.out.println(removeDuplicateLetters.removeDuplicateLetters("bcabc"));
	}

}
