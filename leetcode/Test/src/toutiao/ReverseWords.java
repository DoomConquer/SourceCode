package toutiao;

public class ReverseWords {

    public String reverseWords(String s) {
    	if(s == null || s.length() == 0) return s;
    	char[] sch = s.trim().toCharArray();
    	reverse(sch, 0, sch.length - 1);
    	int start = -1;
    	for(int i = 0; i < sch.length; i++){
    		if(sch[i] == ' '){
    			if(start != -1 && start != i){
    				reverse(sch, start, i - 1);
    				start = -1;
    			}
    		}else{
    			if(start == -1) start = i;
    			if(i == sch.length - 1 && start != -1) reverse(sch, start, i);
    		}
    	}
    	StringBuilder sb = new StringBuilder();
    	sb.append(sch);
    	for(int i = 0; i < sb.length(); i++){
    		if(sb.charAt(i) == ' ' && i + 1 < sb.length() && sb.charAt(i + 1) == ' '){
    			sb.deleteCharAt(i + 1); i--;
    		}
    	}
    	
    	return sb.toString();
    }
    private void reverse(char[] sch, int start, int end){
    	while(start < end){
    		char ch = sch[start];
    		sch[start++] = sch[end];
    		sch[end--] = ch;
    	}
    }
    
	public static void main(String[] args) {
		ReverseWords reverseWords = new ReverseWords();
		System.out.println(reverseWords.reverseWords("  hello world!  "));
		System.out.println(reverseWords.reverseWords("a good   example"));
		System.out.println(reverseWords.reverseWords("   a  good   example   "));
	}

}
