
/**
 * @author li_zhe
 * 参考leetcode思路,自己dp有问题
 * DP思路, dp[i]表示加入第i个元素时的BST数，新加入一个节点，这个节点可以作为前面任意一个集合的根节点
 * We define the STATE as result[i] as the number of unique structurally BST that stores 1...i, then the END STATE as result[n].
	STATE TRANSFER as below :

    Each one among 1..i can be the root. 
    If we take r as root (1 <= r <= i), then its left subtree is [0, r-1], and its right subtree is [r+1, i].
	If we define another STATE dp[s][e] as the number of unique structurally BST that stores s...e,
	the number of unique structurally BST that stores 1...i with r as root for 1 <= r <= i will be dp[1][r-1] * dp[r+1][i].
	Since each one among 1..i can be the root, we try them one by one and get the sum. The the sum will be result[i].

        result[i] = sum(dp[1][r-1] * dp[r+1][i])
                  = sum(result[r-1]* result[i-r]) 注result[i-r] r+1到i的BST的数目和1到i-r的BST数目相同
 */
public class UniqueBinarySearchTrees {

	public int numTrees(int n) {
		if(n <= 0) return 0;
		int[] dp = new int[n + 1];
		dp[0] = 1;
		for(int i = 1; i <= n; i++){
			int sum = 0;
			for(int j = 1; j <= i; j++){
				sum += dp[j - 1] * dp[i - j];
			}
			dp[i] = sum;
		}
		return dp[n];
	}
	
	public static void main(String[] args) {
		UniqueBinarySearchTrees unique = new UniqueBinarySearchTrees();
		System.out.println(unique.numTrees(3));
		System.out.println(unique.numTrees(4));
		System.out.println(unique.numTrees(10));
		System.out.println(unique.numTrees(18));
	}

}
