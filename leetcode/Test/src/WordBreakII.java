import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author li_zhe
 * 思路参考leetcode
 * backtracking
 */
public class WordBreakII {

	public List<String> wordBreak(String s, List<String> wordDict) {
		List<String> res = new ArrayList<>();
		if(!canWordBreak(s, wordDict)) return res;
		Set<String> set = new HashSet<String>();
		for(String word : wordDict) set.add(word);
		find(s, set, new ArrayList<>(), res, 0);
		return res;
	}
	private void find(String s, Set<String> set, List<String> one, List<String> res, int index){
		if(index == s.length()){
			if(!one.isEmpty()){
				StringBuilder sb = new StringBuilder();
				for(int i = 0; i < one.size() - 1; i++) sb.append(one.get(i)).append(" ");
				sb.append(one.get(one.size() - 1));
				res.add(sb.toString());
			}
		}else{
			for(int i = index + 1; i <= s.length(); i++){
				String ss = s.substring(index, i);
				if(set.contains(ss)){
					one.add(ss);
					find(s, set, one, res, i);
					one.remove(one.size() - 1);
				}
			}
		}
	}
	// 先判断能否break，防止超时
	private boolean canWordBreak(String s, List<String> wordDict) {
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
		WordBreakII word = new WordBreakII();
		System.out.println(word.wordBreak("catsanddog", Arrays.asList(new String[]{"cat", "cats", "and", "sand", "dog"})));
		System.out.println(word.wordBreak("pineapplepenapple", Arrays.asList(new String[]{"apple", "pen", "applepen", "pine", "pineapple"})));
		System.out.println(word.wordBreak("catsandog", Arrays.asList(new String[]{"cats", "dog", "sand", "and", "cat"})));
	}

}
