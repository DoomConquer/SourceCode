
public class ReverseVowelsofaString {

	public String reverseVowels(String s) {
		if(s.isEmpty()) return s;
		char[] str = s.toCharArray();
		for(int left = 0, right = str.length - 1; left < right;){
			while(left < right && !isVowels(str[left])) left++;
			while(left < right && !isVowels(str[right])) right--;
			if(left < right){
				char ch = str[left];
				str[left] = str[right];
				str[right] = ch;
				left++;
				right--;
			}
		}
		return String.valueOf(str);
	}
	private boolean isVowels(char ch){
		return (ch == 'a' || ch == 'e' || ch == 'i' || ch == 'o' || ch == 'u' 
				|| ch == 'A' || ch == 'E' || ch == 'I' || ch == 'O' || ch == 'U') ? true : false;
	}
	
	public static void main(String[] args) {
		ReverseVowelsofaString reverse = new ReverseVowelsofaString();
		System.out.println(reverse.reverseVowels("hell"));
	}

}
