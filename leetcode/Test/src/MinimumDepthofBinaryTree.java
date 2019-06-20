
public class MinimumDepthofBinaryTree {

	public int minDepth(TreeNode root) {
		return miniDepth(root);
	}
	private int miniDepth(TreeNode root){
		if(root == null) return 0;
		if(root.left == null) return miniDepth(root.right) + 1;
		if(root.right == null) return miniDepth(root.left) + 1;
		return Math.min(miniDepth(root.right) + 1, miniDepth(root.left) + 1);
	}
	
	public static void main(String[] args) {
		MinimumDepthofBinaryTree tree = new MinimumDepthofBinaryTree();
		TreeNode root = new TreeNode(1);
		TreeNode node1 = new TreeNode(2);
		root.left = node1;
		
		System.out.println(tree.minDepth(root));
	}

}
