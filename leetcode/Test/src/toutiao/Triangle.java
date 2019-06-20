package toutiao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Triangle {

	// O(n)空间，可以在原list上操作，O(1)空间
    public int minimumTotal(List<List<Integer>> triangle) {
        if(triangle.size() == 0) return 0;
        int[] dp = new int[triangle.size()];
        int minPath = Integer.MAX_VALUE;
        int pre = 0;
        for(int i = 0; i < triangle.size(); i++){
        	for(int j = 0; j < triangle.get(i).size(); j++){
        		int curr = dp[j];
        		if(j == 0) dp[j] = curr + triangle.get(i).get(j);
        		else if(j == triangle.get(i).size() - 1) dp[j] = pre + triangle.get(i).get(j);
        		else dp[j] = Math.min(pre + triangle.get(i).get(j), curr + triangle.get(i).get(j));
        		if(i == triangle.size() - 1) minPath = Math.min(minPath, dp[j]);
        		pre = curr;
        	}
        }
        return minPath;
    }
    
    // 自底向上
    public int minimumTotal1(List<List<Integer>> triangle) {
    	int[] A = new int[triangle.size() + 1];
	    for(int i= triangle.size() - 1; i >= 0; i--){
	        for(int j = 0; j < triangle.get(i).size(); j++){
	            A[j] = Math.min(A[j], A[j + 1]) + triangle.get(i).get(j);
	        }
	    }
	    return A[0];
    }
    
	public static void main(String[] args) {
		Triangle triangle = new Triangle();
		List<List<Integer>> list = new ArrayList<List<Integer>>();
		list.add(Arrays.asList(2));
		list.add(Arrays.asList(3,4));
		list.add(Arrays.asList(6,5,7));
		list.add(Arrays.asList(4,1,8,3));
		System.out.println(triangle.minimumTotal(list));
	}

}
