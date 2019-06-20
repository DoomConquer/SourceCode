
public class MaximumDepthofBinaryTree {

	public int maxDepth(TreeNode root) {
		return maxiDepth(root);
	}
	private int maxiDepth(TreeNode root){
		if(root == null) return 0;
		if(root.left == null) return maxiDepth(root.right) + 1;
		if(root.right == null) return maxiDepth(root.left) + 1;
		return Math.max(maxiDepth(root.right) + 1, maxiDepth(root.left) + 1);
	}
	
	public static void main(String[] args) {
		MaximumDepthofBinaryTree tree = new MaximumDepthofBinaryTree();
		TreeNode root = new TreeNode(1);
		TreeNode node1 = new TreeNode(2);
		TreeNode node2 = new TreeNode(4);
		root.left = node1;
		node1.right = node2;
		System.out.println(tree.maxDepth(root));
	}

}
