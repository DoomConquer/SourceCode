
public class DiameterofBinaryTree {

	public int diameterOfBinaryTree(TreeNode root) {
		diameter(root);
		return max;
	}
	int max = 0;
	private int diameter(TreeNode root){
		if(root == null) return 0;
		int diameterLeft = diameter(root.left);
		int diameterRight = diameter(root.right);
		max = Math.max(max, diameterLeft + diameterRight);
		return Math.max(diameterLeft, diameterRight) + 1;
	}
	
	public static void main(String[] args) {
		DiameterofBinaryTree tree = new DiameterofBinaryTree();
		TreeNode root = new TreeNode(1);
		TreeNode node1 = new TreeNode(2);
		TreeNode node2 = new TreeNode(3);
		TreeNode node3 = new TreeNode(4);
		TreeNode node4 = new TreeNode(5);
		root.left = node1;
		root.right = node2;
		node1.left = node3;
		node1.right = node4;
		System.out.println(tree.diameterOfBinaryTree(root));
	}

}
