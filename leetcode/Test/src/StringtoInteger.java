
/**
 * @author li_zhe
 * 题目非常不清晰
 */
public class StringtoInteger {

	public int myAtoi(String str) {
		if(str.isEmpty()) return 0;
		boolean isPositive = true;
		long res = 0;
		int start = 0;
		for(; start < str.length(); start++){
			if(str.charAt(start) == ' ') continue;
			break;
		}
		for(int i = start; i < str.length(); i++){
			if(str.charAt(i) == '-') {
				isPositive = false;
				if(i + 1 < str.length() && !Character.isDigit(str.charAt(i + 1))) return 0;
			}
			else if(str.charAt(i) == '+') {
				isPositive = true;
				if(i + 1 < str.length() && !Character.isDigit(str.charAt(i + 1))) return 0;
			}
			else if(Character.isDigit(str.charAt(i))){
				res = res * 10 + str.charAt(i) - '0';
				if(isPositive && res > Integer.MAX_VALUE) return Integer.MAX_VALUE;
				if(!isPositive && -res < Integer.MIN_VALUE) return Integer.MIN_VALUE;
				if(i + 1 < str.length() && !Character.isDigit(str.charAt(i + 1))) break;
			}else break;
		}
		if(!isPositive) res = -res;
		return (int)res;
	}
	
	public static void main(String[] args) {
		StringtoInteger to = new StringtoInteger();
		System.out.println(to.myAtoi("+-1"));
		System.out.println(to.myAtoi("42"));
		System.out.println(to.myAtoi("    -42"));
		System.out.println(to.myAtoi("4193 with words"));
		System.out.println(to.myAtoi("words and 987"));
		System.out.println(to.myAtoi("-91283472332"));
		System.out.println(to.myAtoi("   +0 123"));
		System.out.println(to.myAtoi("-   234"));
		System.out.println(to.myAtoi("0-1"));
		System.out.println(to.myAtoi("3.14159"));
		System.out.println(to.myAtoi("      -11919730356x"));
	}

}
