import java.util.ArrayList;
import java.util.List;

/**
 * @author li_zhe
 * DP思路
 * dp[i][j] = k in (i,j),以k为root，dp[i][k - 1]和dp[k + 1][j]分别为左右子树
 * 
 * 分治思路
 */
public class UniqueBinarySearchTreesII {

	public List<TreeNode> generateTrees(int n) {
		if(n <= 0) return new ArrayList<>();
		@SuppressWarnings("unchecked")
		List<TreeNode>[][] dp = new ArrayList[n + 1][n + 1];
		for(int i = 1; i <= n; i++){
			dp[i][i] = new ArrayList<>();
			dp[i][i].add(new TreeNode(i));
		}
		for(int i = 1; i <= n; i++){
			for(int j = 1; j + i <= n; j++){
				if(dp[j][j + i] == null)
					dp[j][j + i] = new ArrayList<>();
				for(int k = j; k <= j + i; k++){
					List<TreeNode> left = null;
					if(k > j) left = dp[j][k - 1];
					else{ left = new ArrayList<>(); left.add(null); }
					List<TreeNode> right = null;
					if(k < j + i) right = dp[k + 1][j + i];
					else{ right = new ArrayList<>(); right.add(null); }
					if(left != null && right != null){
						for(TreeNode lnode : left){
							for(TreeNode rnode : right){
								TreeNode root = new TreeNode(k);
								root.left = lnode;
								root.right = rnode;
								dp[j][j + i].add(root);
							}
						}
					}
				}
			}
		}
		return dp[1][n];
	}
	
	public List<TreeNode> generateTrees1(int n) {
		if(n <= 0) return new ArrayList<>();
		return generate(1, n);
	}
	private List<TreeNode> generate(int start, int end){
		List<TreeNode> res = new ArrayList<>();
		if(start > end){
			res.add(null);
			return res;
		}
		for(int i = start; i <= end; i++){
			List<TreeNode> left = generate(start, i - 1);
			List<TreeNode> right = generate(i + 1, end);
			for(TreeNode lnode : left){
				for(TreeNode rnode : right){
					TreeNode node = new TreeNode(i);
					node.left = lnode;
					node.right = rnode;
					res.add(node);
				}
			}
		}
		return res;
	}
	
	public static void main(String[] args) {
		UniqueBinarySearchTreesII unique = new UniqueBinarySearchTreesII();
		unique.generateTrees(4);
	}

}
