
public class ReverseString {

	public String reverseString(String s) {
		if(s.isEmpty()) return s;
		char[] str = s.toCharArray();
		for(int left = 0, right = str.length - 1; left < right; left++, right--){
			char ch = str[left];
			str[left] = str[right];
			str[right] = ch;
		}
		return String.valueOf(str);
	}
	
	public static void main(String[] args) {
		ReverseString reverse = new ReverseString();
		System.out.println(reverse.reverseString("ste"));
	}

}
