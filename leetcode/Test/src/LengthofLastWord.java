public class LengthofLastWord {

    public int lengthOfLastWord(String s) {
        if(s == null || s.length() == 0) return 0;
        int n = s.length() - 1;
        while(n >= 0){
        	if(s.charAt(n) == ' ') n--;
        	else break;
        }
        int res = 0;
        while(n >= 0){
        	if(s.charAt(n) != ' '){
        		n--; res++;
        	}else break;
        }
        return res;
    }
    
	public static void main(String[] args) {
		LengthofLastWord lengthofLastWord = new LengthofLastWord();
		System.out.println(lengthofLastWord.lengthOfLastWord("Hello World"));
		System.out.println(lengthofLastWord.lengthOfLastWord("Hello World   "));
		System.out.println(lengthofLastWord.lengthOfLastWord("Hello World  q "));
		System.out.println(lengthofLastWord.lengthOfLastWord(""));
		System.out.println(lengthofLastWord.lengthOfLastWord("  "));
		System.out.println(lengthofLastWord.lengthOfLastWord("q  "));
	}

}
