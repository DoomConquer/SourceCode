
public class TrimaBinarySearchTree {

	public TreeNode trimBST(TreeNode root, int L, int R) {
		if(root == null) return root;
		if(root.val < L){
			return trimBST(root.right, L, R);
		}else if(root.val > R){
			return trimBST(root.left, L, R);
		}
		root.left = trimBST(root.left, L, R);
		root.right = trimBST(root.right, L, R);
		return root;
	}
	
	public static void main(String[] args) {
		TrimaBinarySearchTree tree = new TrimaBinarySearchTree();
		TreeNode root = new TreeNode(2);
		TreeNode node1 = new TreeNode(1);
		TreeNode node2 = new TreeNode(3);
		root.left = node1;
		root.right = node2;
		tree.trimBST(root, 1, 2);
	}

}
