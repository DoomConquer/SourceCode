
public class MergeTwoBinaryTrees {

	public TreeNode mergeTrees(TreeNode t1, TreeNode t2) {
		if(t1 == null) return t2;
		if(t2 == null) return t1;
		TreeNode root = new TreeNode(t1.val + t2.val);
		root.left = mergeTrees(t1.left, t2.left);
		root.right = mergeTrees(t1.right, t2.right);
		return root;
	}
	
	public static void main(String[] args) {
		MergeTwoBinaryTrees tree = new MergeTwoBinaryTrees();
		TreeNode t1 = new TreeNode(1);
		TreeNode node1 = new TreeNode(2);
		TreeNode node2 = new TreeNode(3);
		TreeNode node3 = new TreeNode(4);
		TreeNode node4 = new TreeNode(5);
		t1.left = node1;
		t1.right = node2;
		node1.left = node3;
		node1.right = node4;
		TreeNode t2 = new TreeNode(1);
		TreeNode node5 = new TreeNode(2);
		TreeNode node6 = new TreeNode(3);
		TreeNode node7 = new TreeNode(4);
		TreeNode node8 = new TreeNode(5);
		t2.left = node5;
		t2.right = node6;
		node5.left = node7;
		node6.right = node8;
		tree.mergeTrees(t1, t2);
	}

}
