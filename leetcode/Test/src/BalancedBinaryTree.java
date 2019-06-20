
public class BalancedBinaryTree {

	public boolean isBalanced(TreeNode root) {
		if(root == null) return true;
		if(Math.abs(height(root.left) - height(root.right)) > 1) return false;
		if(!isBalanced(root.left) || !isBalanced(root.right)) return false;
		return true;
	}
	private int height(TreeNode root){
		if(root == null) return 0;
		return Math.max(height(root.left), height(root.right)) + 1;
	}
	
	public static void main(String[] args) {
		BalancedBinaryTree tree = new BalancedBinaryTree();
		TreeNode root = new TreeNode(5);
		TreeNode node1 = new TreeNode(4);
		TreeNode node2 = new TreeNode(8);
		TreeNode node3 = new TreeNode(11);
		TreeNode node4 = new TreeNode(13);
		TreeNode node5 = new TreeNode(4);
		TreeNode node6 = new TreeNode(7);
		TreeNode node7 = new TreeNode(2);
		TreeNode node8 = new TreeNode(1);
		root.left = node1;
		root.right = node2;
		node1.left = node3;
		node2.left = node4;
		node2.right = node5;
		node3.left = node6;
		node3.right = node7;
		node5.right = node8;
		System.out.println(tree.isBalanced(root));
	}

}
