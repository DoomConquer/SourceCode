import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

// dp思路，dp[i][j]表示key中前i个字符当前轮盘12点方向正对着ring.charAt(j)字符转动的次数
public class FreedomTrail {

    public int findRotateSteps(String ring, String key) {
        int lenR = ring.length();
        int lenK = key.length();
        int[][] dp = new int[lenK + 1][lenR];
        int min = Integer.MAX_VALUE;
        for(int i = 0; i <= lenK; i++){
        	Arrays.fill(dp[i], Integer.MAX_VALUE);
        	if(i == 0){ dp[0][0] = 0; continue; }
        	for(int j = 0; j < lenR; j++){
        		if(key.charAt(i - 1) == ring.charAt(j)){
	        		for(int k = 0; k < lenR; k++){
	        			if(dp[i - 1][k] != Integer.MAX_VALUE){
	        				int dis = Math.abs(j - k);
	        				dis = Math.min(dis, lenR - dis);
	        				dp[i][j] = Math.min(dp[i][j], dis + dp[i - 1][k]);
	        				if(i == lenK) 
	        					min = Math.min(min, dp[lenK][j]);
	        			}
        			}
        		}
    		}
        }
        return min + lenK;
    }
    
    // dfs + memorization
    public int findRotateSteps1(String ring, String key) {
        Map<String,Integer> map = new HashMap<>();
        return dfs(ring, key, 0, map);
    }
    private int dfs(String ring, String key, int index, Map<String,Integer> map){
	    if(index == key.length()){
	        return 0;
	    }
	    char c = key.charAt(index);
	    String hashKey = ring + index;
	    if(map.containsKey(hashKey)) return map.get(hashKey);
	     
	    int minSteps = Integer.MAX_VALUE;
	    for(int i = 0; i < ring.length(); i ++){
	        if(ring.charAt(i) == c){
	            String s = ring.substring(i, ring.length()) + ring.substring(0, i);
	            int steps = 1 + Math.min(i, ring.length() - i);
	            steps += dfs(s, key, index + 1, map);
	            minSteps = Math.min(minSteps, steps);
	        }
	    }   
	    map.put(hashKey, minSteps);
	    return minSteps;
 	}
    
	public static void main(String[] args) {
		FreedomTrail freedomTrail = new FreedomTrail();
		System.out.println(freedomTrail.findRotateSteps("godding", "gd"));
		System.out.println(freedomTrail.findRotateSteps("godding", "g"));
		System.out.println(freedomTrail.findRotateSteps("edcba", "abcde"));
	}

}
