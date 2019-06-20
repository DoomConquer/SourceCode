public class BackspaceStringCompare {

    public boolean backspaceCompare(String S, String T) {
        int i = S.length() - 1, j = T.length() - 1;
        int ii = 0, jj = 0;
        while(i >= 0 || j >= 0){
        	if(i >= 0 && S.charAt(i) == '#'){
        		ii++; i--; continue;
        	}
        	if(j >= 0 && T.charAt(j) == '#'){
        		jj++; j--; continue;
        	}
        	if(i >= 0 && ii > 0){
        		ii--; i--; continue;
        	}
        	if(j >= 0 && jj > 0){
        		jj--; j--; continue;
        	}
        	if(i >= 0 && j >= 0 && S.charAt(i) != T.charAt(j)) return false;
        	else if((i < 0 && j >= 0) || (i >= 0 && j < 0)) return false;
        	i--; j--;
        }
        return true;
    }
    
	public static void main(String[] args) {
		BackspaceStringCompare backspaceStringCompare = new BackspaceStringCompare();
		System.out.println(backspaceStringCompare.backspaceCompare("ab#c", "ad#c"));
		System.out.println(backspaceStringCompare.backspaceCompare("ab##", "c#d#"));
		System.out.println(backspaceStringCompare.backspaceCompare("a##c", "#a#c"));
		System.out.println(backspaceStringCompare.backspaceCompare("a#c", "b"));
		System.out.println(backspaceStringCompare.backspaceCompare("###", "b"));
		System.out.println(backspaceStringCompare.backspaceCompare("###", "###########"));
		System.out.println(backspaceStringCompare.backspaceCompare("##a#######", "a#"));
		System.out.println(backspaceStringCompare.backspaceCompare("##a#####a##", "a#"));
		System.out.println(backspaceStringCompare.backspaceCompare("a##a#####a##", "a#a"));
		System.out.println(backspaceStringCompare.backspaceCompare("", ""));
	}

}
