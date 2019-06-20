import java.util.ArrayList;
import java.util.List;

public class PermutationSequence {

	public String getPermutation(int n, int k) {
		List<Integer> nums = new ArrayList<Integer>();
		int[] f = new int[n + 1];
		f[0] = 1;
		int temp = 1;
		for(int i = 1; i <= n; i++){
			nums.add(i);
			f[i] = i * temp;
			temp = f[i];
		}
		k = k -1;
		StringBuilder sb = new StringBuilder();
		for(int i = 1; i <= n; i++){
			int index = k / f[n - i];
			sb.append(nums.get(index));
			nums.remove(index);
			k -= index * f[n - i];
		}
		return sb.toString();
    }
	
	public static void main(String[] args) {
		PermutationSequence sequence = new PermutationSequence();
		System.out.println(sequence.getPermutation(1, 1));
	}

}
