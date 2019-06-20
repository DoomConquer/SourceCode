import java.util.Arrays;
import java.util.Comparator;

public class LongestStringChain {

    public int longestStrChain(String[] words) {
    	if(words.length == 0) return 0;
        int[] dp = new int[words.length];
        Arrays.sort(words, new Comparator<String>(){
			@Override
			public int compare(String o1, String o2) {
				return o1.length() - o2.length();
			}});
        
        dp[0] = 1; int max = 1;
        for(int i = 1; i < words.length; i++){
        	dp[i] = 1;
        	for(int j = 0; j < i; j++){
        		if(words[j].length() != words[i].length() - 1) continue;
        		if(isMatch(words[j], words[i])) dp[i] = Math.max(dp[i], dp[j] + 1);
        	}
        	max = Math.max(max, dp[i]);
        }
        return max;
    }
    private boolean isMatch(String word1, String word2){
    	for(int i = 0; i < word2.length(); i++){
    		StringBuilder sb = new StringBuilder(word2);
    		sb.deleteCharAt(i);
    		if(word1.equals(sb.toString())) return true;
    	}
    	return false;
    }
    
	public static void main(String[] args) {
		LongestStringChain longestStringChain = new LongestStringChain();
		System.out.println(longestStringChain.longestStrChain(new String[]{"a","b","ba","bca","bda","bdca"}));
		System.out.println(longestStringChain.longestStrChain(new String[]{"sgtnz","sgtz","sgz","ikrcyoglz","ajelpkpx","ajelpkpxm","srqgtnz","srqgotnz","srgtnz","ijkrcyoglz"}));
		System.out.println(longestStringChain.longestStrChain(new String[]{"ksqvsyq","ks","kss","czvh","zczpzvdhx","zczpzvh","zczpzvhx","zcpzvh","zczvh","gr","grukmj","ksqvsq","gruj","kssq","ksqsq","grukkmj","grukj","zczpzfvdhx","gru"}));
	}

}
