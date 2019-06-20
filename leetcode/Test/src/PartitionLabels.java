import java.util.ArrayList;
import java.util.List;

public class PartitionLabels {

	public List<Integer> partitionLabels(String S) {
		List<Integer> res = new ArrayList<Integer>();
		int[] map = new int[26];
		int len = S.length();
		for(int i = 0; i < len; i++){
			map[S.charAt(i) - 'a'] = i;
		}
		int maxRight = 0;
		for(int left = 0, right = 0; left < S.length(); left++){
			maxRight = Math.max(maxRight, map[S.charAt(left) - 'a']);
			while(right < maxRight){
				maxRight = Math.max(maxRight, map[S.charAt(right) - 'a']);
				right++;
			}
			res.add(right - left + 1);
			left = right;
		}
		return res;
	}
	
	public static void main(String[] args) {
		PartitionLabels partition = new PartitionLabels();
		List<Integer> res = partition.partitionLabels("abac");
		for(int num : res)
			System.out.print(num + "  ");
	}

}
