public class DIStringMatch {

	public int[] diStringMatch(String S) {
        int[] res = new int[S.length() + 1];
        int left = 0, right = S.length(), index = 0;
        for(int i = 0; i < S.length(); i++){
        	if(S.charAt(i) == 'I') res[index++] = left++;
        	else if(S.charAt(i) == 'D') res[index++] = right--;
        }
        res[index++] = right--;
        return res;
    }
    
	public static void main(String[] args) {
		DIStringMatch dIStringMatch = new DIStringMatch();
		for(int num : dIStringMatch.diStringMatch("IDID")) System.out.print(num + "  ");
		System.out.println();
		for(int num : dIStringMatch.diStringMatch("DDI")) System.out.print(num + "  ");
	}

}
