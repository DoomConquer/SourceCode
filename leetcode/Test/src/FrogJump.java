import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

// 思路来源leetcode
public class FrogJump {

    public boolean canCross(int[] stones) {
    	int len = stones.length;
    	if(stones[1] > 1) return false;
		@SuppressWarnings("unchecked")
		Set<Integer>[] lastJump = new Set[len];
        for(int i = 0; i < len; i++) lastJump[i] = new HashSet<Integer>();
        lastJump[1].add(1);
        for(int i = 2; i < len; i++){
        	for(int j = 1; j < i; j++){
        		if(lastJump[j].size() > 0){
        			int currJump = stones[i] - stones[j];
        			if(lastJump[j].contains(currJump) || lastJump[j].contains(currJump - 1) || lastJump[j].contains(currJump + 1)){
        				lastJump[i].add(currJump);
        			}
        		}
        	}
        }
        return lastJump[len - 1].size() > 0;
    }
    
    // dp思路，dp[i][j]表示当前走到i处走了j步
    public boolean canCross1(int[] stones) {
    	int len = stones.length;
    	if(stones[1] > 1) return false;
    	boolean[][] dp = new boolean[len][len + 1];
    	dp[0][0] = true;
    	for(int i = 1; i < len; i++){
    		for(int j = 1; j <= i; j++){
    			int k = Arrays.binarySearch(stones, 0, i, stones[i] - j);
    			if(k < 0) continue;
    			dp[i][j] = dp[k][j - 1] | dp[k][j] | dp[k][j + 1];
    		}
    	}
    	for(int i = 0; i < len; i++){
    		if(dp[len - 1][i]) return true;
    	}
    	return false;
    }
    
	public static void main(String[] args) {
		FrogJump frogJump = new FrogJump();
		System.out.println(frogJump.canCross(new int[]{0,1,3,5,6,8,12,17}));
		System.out.println(frogJump.canCross(new int[]{0,1,2,3,4,8,9,11}));
		System.out.println(frogJump.canCross(new int[]{0,1,3,4,5,7,9,10,12}));
		System.out.println(frogJump.canCross(new int[]{0,2}));
		
		System.out.println(frogJump.canCross1(new int[]{0,1,3,5,6,8,12,17}));
		System.out.println(frogJump.canCross1(new int[]{0,1,2,3,4,8,9,11}));
		System.out.println(frogJump.canCross1(new int[]{0,1,3,4,5,7,9,10,12}));
		System.out.println(frogJump.canCross1(new int[]{0,2}));
	}

}
