
public class InvertBinaryTree {

	public TreeNode invertTree(TreeNode root) {
		if(root == null) return null;
		TreeNode leftInvert = invertTree(root.left);
		TreeNode rightInvert = invertTree(root.right);
		root.left = rightInvert;
		root.right = leftInvert;
		return root;
	}
	
	public static void main(String[] args) {
		InvertBinaryTree tree = new InvertBinaryTree();
		TreeNode root = new TreeNode(4);
		System.out.println(tree.invertTree(root).val);
	}

}
