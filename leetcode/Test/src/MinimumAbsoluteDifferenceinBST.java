
public class MinimumAbsoluteDifferenceinBST {

	public int getMinimumDifference(TreeNode root) {
		preOrder(root);
		return min;
	}
	int min = Integer.MAX_VALUE;
	int preVal = Integer.MAX_VALUE;
	private void preOrder(TreeNode root){
		if(root == null) return;
		preOrder(root.left);
		int diff = Math.abs(root.val - preVal);
		if(diff < min) min = diff;
		preVal = root.val;
		preOrder(root.right);
	}
	
	public static void main(String[] args) {
		MinimumAbsoluteDifferenceinBST mini = new MinimumAbsoluteDifferenceinBST();
		TreeNode root = new TreeNode(1);
		TreeNode node1 = new TreeNode(3);
		TreeNode node2 = new TreeNode(2);
		root.right = node1;
		node1.left = node2;
		System.out.println(mini.getMinimumDifference(root));
	}

}
