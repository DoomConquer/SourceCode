
public class MinimumDistanceBetweenBSTNodes {

	public int minDiffInBST(TreeNode root) {
		min(root);
		return mini;
	}
	int mini = Integer.MAX_VALUE;
	int lastVal = Integer.MAX_VALUE;
	private void min(TreeNode root){
		if(root == null) return;
		min(root.left);
		mini = Math.min(Math.abs(root.val - lastVal), mini);
		lastVal = root.val;
		min(root.right);
	}
	
	public static void main(String[] args) {
		MinimumDistanceBetweenBSTNodes mini = new MinimumDistanceBetweenBSTNodes();
		TreeNode root = new TreeNode(7);
		TreeNode node1 = new TreeNode(2);
		TreeNode node2 = new TreeNode(8);
		root.left = node1;
		root.right = node2;
		System.out.println(mini.minDiffInBST(root));
	}

}
