import java.util.ArrayList;
import java.util.List;

public class LetterCasePermutation {

	public List<String> letterCasePermutation(String S) {
		List<String> res = new ArrayList<String>();
		if(S == null)
			return res;
		letter(res, new ArrayList<Integer>(), 0, S);
		return res;
			
	}
	private void letter(List<String> res, List<Integer> one, int start, String s){
		gernerate(res, s, one);
		for(int i = start; i < s.length(); i++){
			if((s.charAt(i) >= 'a' && s.charAt(i) <= 'z') || (s.charAt(i) >= 'A' && s.charAt(i) <= 'Z')){
				one.add(i);
				letter(res, one, i + 1, s);
				one.remove(one.size() - 1);
			}
		}
	}
	private void gernerate(List<String> res, String s, List<Integer> one){
		char[] ch = s.toCharArray();
		for(int num : one){
			if(ch[num] >= 'a' && ch[num] <= 'z')
				ch[num] -= 32;
			else if(ch[num] >= 'A' && ch[num] <= 'Z')
				ch[num] += 32;
		}
		res.add(new String(ch));
	}
	
	public static void main(String[] args) {
		LetterCasePermutation letter = new LetterCasePermutation();
		System.out.println(letter.letterCasePermutation("abvb2"));
	}

}
