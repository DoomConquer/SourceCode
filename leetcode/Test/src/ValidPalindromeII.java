
public class ValidPalindromeII {

	public boolean validPalindrome(String s) {
		return isPalindrome(s, 0, s.length() - 1, 0);
	}
	private boolean isPalindrome(String s, int left, int right, int flag){
		if(flag == 2) return false;
		while(left < right){
			if(s.charAt(left) == s.charAt(right)){
				left++;
				right--;
			}else{
				flag++;
				return isPalindrome(s, left + 1, right, flag) || isPalindrome(s, left, right - 1, flag);
			}
		}
		return true;
	}
	
	public static void main(String[] args) {
		ValidPalindromeII palindrome = new ValidPalindromeII();
		System.out.println(palindrome.validPalindrome("cba"));
	}

}
