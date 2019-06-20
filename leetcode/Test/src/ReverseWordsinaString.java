public class ReverseWordsinaString {

    public String reverseWords(String s) {
    	if(s == null) return s;
    	s = s.trim();
        char[] sch = s.toCharArray();
        reverse(sch, 0, sch.length - 1);
        int start = -1, end = -1;
        int index = 0;
        for(int i = 0; i < sch.length; i++){
        	if(sch[i] == ' '){
        		continue;
        	}else{
        		if(start == -1){
        			start = i;
        			end = start;
        		}else end++;
        		if((i + 1 < sch.length && sch[i + 1] == ' ') || i == sch.length - 1){
        			if(start <= end){
        				reverse(sch, start, end);
        				for(int j = start; j <= end; j++)
        					sch[index++] = sch[j];
        				if(i != sch.length - 1)
        					sch[index++] = ' ';
        				start = -1; end = -1;
        			}
        		}
        	}
        }
        return new String(sch, 0, index);
    }
    private void reverse(char[] sch, int start, int end){
    	while(start < end){
    		char ch = sch[start];
    		sch[start++] = sch[end];
    		sch[end--] = ch;
    	}
    }
    
	public static void main(String[] args) {
		ReverseWordsinaString ReverseWordsinaString = new ReverseWordsinaString();
		System.out.println(ReverseWordsinaString.reverseWords(" "));
		System.out.println(ReverseWordsinaString.reverseWords("   a   b "));
		System.out.println(ReverseWordsinaString.reverseWords("   a   b   ccc"));
		System.out.println(ReverseWordsinaString.reverseWords("  theskyisblue "));
		System.out.println(ReverseWordsinaString.reverseWords("the sky is blue"));
		System.out.println(ReverseWordsinaString.reverseWords("the sky is blue  "));
		System.out.println(ReverseWordsinaString.reverseWords("  the   sky   is blue  "));
		System.out.println(ReverseWordsinaString.reverseWords("  the   sky   is blue"));
		System.out.println(ReverseWordsinaString.reverseWords("  the   sky   is      blue"));
	}

}
