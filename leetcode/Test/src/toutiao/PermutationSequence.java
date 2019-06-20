package toutiao;

import java.util.ArrayList;
import java.util.List;

public class PermutationSequence {

    public String getPermutation(int n, int k) {
        int[] f = new int[n + 1];
        f[0] = 1;
        List<Integer> list = new ArrayList<>();
        for(int i = 1; i <= n; i++){
        	list.add(i);
        	f[i] = i * f[i - 1];
        }
        StringBuilder sb = new StringBuilder();
        int deep = n - 1; k--;
        while(deep >= 0){
        	int index = k / f[deep];
        	sb.append(list.get(index));
        	list.remove(index);
        	k = k % f[deep]; deep--;
        }
        return sb.toString();
    }
    
	public static void main(String[] args) {
		PermutationSequence permutationSequence = new PermutationSequence();
		System.out.println(permutationSequence.getPermutation(3, 2));
		System.out.println(permutationSequence.getPermutation(3, 3));
		System.out.println(permutationSequence.getPermutation(4, 9));
		System.out.println(permutationSequence.getPermutation(9, 1));
		System.out.println(permutationSequence.getPermutation(9, 9*8*7*6*5*4*3*2));
	}

}
