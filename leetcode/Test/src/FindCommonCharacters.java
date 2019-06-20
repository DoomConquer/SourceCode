import java.util.ArrayList;
import java.util.List;

public class FindCommonCharacters {

    public List<String> commonChars(String[] A) {
    	List<String> res = new ArrayList<>();
    	int n = A.length;
    	int[][] alphabet = new int[n][26];
    	for(int i = 0; i < n; i++){
    		for(char ch : A[i].toCharArray()){
    			alphabet[i][ch - 'a']++;
    		}
    	}
    	for(int i = 0; i < 26; i++){
    		int min = alphabet[0][i];
    		for(int j = 1; j < n; j++){
    			if(min > alphabet[j][i]) min = alphabet[j][i];
    		}
    		for(int k = 0; k < min; k++) res.add(String.valueOf((char)(i + 'a')));
    	}
    	return res;
    }
    
	public static void main(String[] args) {
		FindCommonCharacters findCommonCharacters = new FindCommonCharacters();
		System.out.println(findCommonCharacters.commonChars(new String[]{"bella","label","roller"}));
		System.out.println(findCommonCharacters.commonChars(new String[]{"cool","lock","cook"}));
		System.out.println(findCommonCharacters.commonChars(new String[]{"cool","lock",""}));
		System.out.println(findCommonCharacters.commonChars(new String[]{"aaaa","aaa","aaaaaaa"}));
		System.out.println(findCommonCharacters.commonChars(new String[]{"aaaa"}));
	}

}
