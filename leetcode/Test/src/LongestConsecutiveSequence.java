import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LongestConsecutiveSequence {

	public int longestConsecutive(int[] nums) {
		int maxLen = 0;
		unionFind(nums.length); // init union find array
		Map<Long, Integer> map = new HashMap<>();
		for(int i = 0; i < nums.length; i++){
			if(!map.containsKey((long)nums[i]))
				map.put((long)nums[i], i);
		}
		Set<Integer> set = new HashSet<>(); // remove duplicate elements
		for(int i = 0; i < nums.length; i++){
			if(set.contains(nums[i])) continue;
			set.add(nums[i]);
			if(map.containsKey((long)(nums[i] - 1))){
				union(i, map.get((long)(nums[i] - 1)));
			}else if(map.containsKey((long)(nums[i] + 1))){
				union(i, map.get((long)(nums[i] + 1)));
			}
		}
		for(int i = 0; i < count.length; i++)
			maxLen = Math.max(maxLen, count[i]);
		return maxLen;
	}
	int[] parent;
	int[] count; // record very union set's count
	private void unionFind(int n){
		parent = new int[n];
		count = new int[n];
		for(int i = 0; i < n; i++){
			parent[i] = i;
			count[i] = 1;
		}
	}
	private int find(int x){
		while(x != parent[x]){
			x = parent[parent[x]];
		}
		return x;
	}
	private void union(int x, int y){
		int xx = find(x);
		int yy = find(y);
		if(xx != yy){
			parent[yy] = xx;
			count[xx] += count[yy];
		}
	}
	
	// HashSet·½·¨
    public int longestConsecutive1(int[] nums) {
        if(nums == null || nums.length == 0) return 0;
        Set<Integer> set = new HashSet<Integer>();
        for(int num: nums) set.add(num);
        int max = 1;
        for(int num: nums) {
            if(set.remove(num)) {
                int val = num;
                int sum = 1;
                while(set.remove(val - 1)) val--;
                sum += num - val;
                
                val = num;
                while(set.remove(val + 1)) val++;
                sum += val - num;
                
                max = Math.max(max, sum);
            }
        }
        return max;
    }
	
	public static void main(String[] args) {
		LongestConsecutiveSequence longest = new LongestConsecutiveSequence();
		System.out.println(longest.longestConsecutive(new int[]{100, 4, 200, 1, 3, 2}));
		System.out.println(longest.longestConsecutive(new int[]{0,0,-1}));
	}

}
