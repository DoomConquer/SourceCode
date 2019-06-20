import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubstringwithConcatenationofAllWords {

    public List<Integer> findSubstring(String s, String[] words) {
        List<Integer> res = new ArrayList<>();
        if(s.length() == 0 || words.length == 0) return res;
        Map<String, Integer> map = new HashMap<>();
        for(String word : words) map.put(word, map.getOrDefault(word, 0) + 1);
        int size = words.length, len = words[0].length();
        for(int i = 0; i <= s.length() - size * len; i++){
        	Map<String, Integer> count = new HashMap<>();
        	for(int j = 0; j < size; j++){
        		String word = s.substring(i + j * len, i + j * len + len);
        		count.put(word, count.getOrDefault(word, 0) + 1);
        		if(count.get(word) > map.getOrDefault(word, 0)) break;
        		if(j == size - 1) res.add(i);
        	}
        }
        return res;
    }
    
	public static void main(String[] args) {
		SubstringwithConcatenationofAllWords substringwithConcatenationofAllWords = new SubstringwithConcatenationofAllWords();
		System.out.println(substringwithConcatenationofAllWords.findSubstring("barfoothefoobarman", new String[]{"foo","bar"}));
		System.out.println(substringwithConcatenationofAllWords.findSubstring("wordgoodgoodgoodbestword", new String[]{"word","good","best","word"}));
	}

}
