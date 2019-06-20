import java.util.Stack;

public class DecodeString {

	public String decodeString(String s) {
		if(s == null || s.length() == 0) return s;
		char[] ch = s.toCharArray();
		Stack<Integer> numStack = new Stack<>();
		Stack<Character> charStack = new Stack<>();
		int k = 0;
		boolean isDigit = false;
		for(int i = 0; i < ch.length; i++){
			if(Character.isDigit(ch[i])) {
				k = k * 10 + (ch[i] - '0');
				isDigit = true;
				continue;
			}
			if(isDigit){
				numStack.push(k);
				k = 0;
				isDigit = false;
			}
			if(ch[i] != ']'){
				charStack.push(ch[i]);
				continue;
			}
			if(ch[i] == ']'){
				StringBuilder sb = new StringBuilder();
				while(!charStack.isEmpty() && charStack.peek() != '['){
					sb.append(charStack.pop());
				}
				charStack.pop();
				int time = numStack.pop();
				String temp = sb.reverse().toString();
				while(time-- > 0){
					for(int j = 0; j < temp.length(); j++)
						charStack.push(temp.charAt(j));
				}
			}
		}
		StringBuilder sb = new StringBuilder();
		while(!charStack.isEmpty()){
			sb.append(charStack.pop());
		}
		return sb.reverse().toString();
	}
	
	public static void main(String[] args) {
		DecodeString decode = new DecodeString();
		System.out.println(decode.decodeString("3[a2[c]]"));
		System.out.println(decode.decodeString("2[abc]3[cd]ef"));
		System.out.println(decode.decodeString("10[2[abc]3[cd]ef]"));
		System.out.println(decode.decodeString("asdes"));
	}

}
