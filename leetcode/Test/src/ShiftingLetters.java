public class ShiftingLetters {

    public String shiftingLetters(String S, int[] shifts) {
    	StringBuilder sb = new StringBuilder(S.length());
    	int count = 0;
        for(int i = shifts.length - 1; i >= 0; i--){
        	count += shifts[i] % 26;
        	sb.append((char)('a' + (S.charAt(i) - 'a' + count) % 26));
        }
        return sb.reverse().toString();
    }
    
	public static void main(String[] args) {
		ShiftingLetters shiftingLetters = new ShiftingLetters();
		System.out.println(shiftingLetters.shiftingLetters("abc", new int[]{3,5,9}));
		System.out.println(shiftingLetters.shiftingLetters("abc", new int[]{0,0,0}));
		System.out.println(shiftingLetters.shiftingLetters("abc", new int[]{26,26,26}));
	}

}
