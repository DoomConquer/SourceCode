
public class ValidPalindrome {

	public boolean isPalindrome(String s) {
		if(s.isEmpty()) return true;
		for(int left = 0, right = s.length() - 1; left < right;){
			if(!isAlph(s.charAt(left))) left++;
			else if(!isAlph(s.charAt(right))) right--;
			else if(Character.toUpperCase(s.charAt(left)) == Character.toUpperCase(s.charAt(right))){
				left++;
				right--;
			}else return false;
		}
		return true;
	}
	private boolean isAlph(char ch){
		return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z' || (ch >= '0' && ch <= '9')) ? true : false;
	}
	
	public static void main(String[] args) {
		ValidPalindrome palindrome = new ValidPalindrome();
		System.out.println(palindrome.isPalindrome(""));
	}

}
