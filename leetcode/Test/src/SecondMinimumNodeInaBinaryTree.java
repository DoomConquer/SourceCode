
public class SecondMinimumNodeInaBinaryTree {

	public int findSecondMinimumValue(TreeNode root) {
		int res = find(root, root.val);
		if(res == root.val) return -1;
		return res;
	}
	private int find(TreeNode root, int val){
		if(root.left == null && root.right == null) return root.val;
		int left = 0;
		if(root.val == root.left.val) left = find(root.left, val);
		else left = root.left.val;
		int right = 0;
		if(root.val == root.right.val) right = find(root.right, val);
		else right = root.right.val;
		if(left == val) return right;
		if(right == val) return left;
		return left > right ? right : left;
	}
	
	public static void main(String[] args) {
		SecondMinimumNodeInaBinaryTree tree = new SecondMinimumNodeInaBinaryTree();
		TreeNode root = new TreeNode(2);
		TreeNode node1 = new TreeNode(4);
		TreeNode node2 = new TreeNode(2);
		TreeNode node3 = new TreeNode(2);
		TreeNode node4 = new TreeNode(5);
		root.left = node1;
		root.right = node2;
		node1.left = node3;
		node1.right = node4;
		System.out.println(tree.findSecondMinimumValue(root));
	}

}
