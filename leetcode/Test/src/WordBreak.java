import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WordBreak {

	public boolean wordBreak(String s, List<String> wordDict) {
		if(s == null || s.length() == 0) return false;
		Set<String> set = new HashSet<String>();
		for(String word : wordDict) set.add(word);
		boolean[] flag = new boolean[s.length()];
		for(int i = 0; i < s.length(); i++){
			for(int j = i; j >= 0; j--){
				String ss = s.substring(j, i + 1);
				if(set.contains(ss) && (j == 0 || (j > 0 && flag[j - 1]))){
					flag[i] = true;
					break;
				}
			}
		}
		return flag[s.length() - 1];
	}
	
	public static void main(String[] args) {
		WordBreak word = new WordBreak();
		System.out.println(word.wordBreak("catsandog", Arrays.asList(new String[]{"cats", "dog", "sand", "and", "cat"})));
		System.out.println(word.wordBreak("applepenapple", Arrays.asList(new String[]{"apple", "pen"})));
		System.out.println(word.wordBreak("leetcode", Arrays.asList(new String[]{"leet", "code"})));
		System.out.println(word.wordBreak("leetcc ", Arrays.asList(new String[]{"leet","cc"})));
		System.out.println(word.wordBreak("", Arrays.asList(new String[]{""})));
	}

}
