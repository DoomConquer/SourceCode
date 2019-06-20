import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GeneralizedAbbreviation {

	public List<String> generateAbbreviations(String word) {
		List<String> res = new ArrayList<String>();
		Set<String> set = new HashSet<String>();
		generate(set, new ArrayList<Integer>(), word.length(), 0, word);
		res.addAll(set);
		return res;
	}
	private void generate(Set<String> res, List<Integer> one, int n, int curr, String word){
		if(one.size() <= n){
			char[] ch = word.toCharArray();
			for(int num : one){
				ch[num] = '1';
			}
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < n;){
				int sum = 0;
				while(i < n && ch[i] == '1'){
					i++;
					sum++;
				}
				if(sum == 0){
					sb.append(ch[i]);
					i++;
				}
				else
					sb.append(sum);
			}
			res.add(sb.toString());
		}
		for(int i = curr; i < n; i++){
			if(!one.contains(i)){
				one.add(i);
				generate(res, one, n, curr + 1, word);
				one.remove(one.size() - 1);
			}
		}
	}
	
	public List<String> generateAbbreviations1(String word) {
        List<String> result = new ArrayList<String>();
        
        backtrack(result, word, 0, "", 0);
        
        return result;
    }
    
    void backtrack(List<String> result, String word, int position, String current, int count) {
        if(position == word.length()) {
            if(count > 0) {
                current += count;   
            }
            
            result.add(current);
        } else {
            backtrack(result, word, position + 1, current, count + 1);
            backtrack(result, word, position + 1, current + (count > 0 ? count : "") + word.charAt(position), 0);
        }
    }
	
	public static void main(String[] args) {
		GeneralizedAbbreviation abbreviation = new GeneralizedAbbreviation();
		System.out.println(abbreviation.generateAbbreviations1("word"));
	}

}
