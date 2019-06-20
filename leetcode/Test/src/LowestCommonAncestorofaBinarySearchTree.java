
public class LowestCommonAncestorofaBinarySearchTree {

	public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
		if(root.val == p.val) return p;
		if(root.val == q.val) return q;
		if((root.val > p.val && root.val < q.val) || (root.val < p.val && root.val > q.val)) return root;
		if(root.val > p.val && root.val > q.val) return lowestCommonAncestor(root.left, p, q);
		return lowestCommonAncestor(root.right, p, q);
	}
	
	public static void main(String[] args) {
		LowestCommonAncestorofaBinarySearchTree ancestor = new LowestCommonAncestorofaBinarySearchTree();
		TreeNode root = new TreeNode(4);
		TreeNode node1 = new TreeNode(2);
		TreeNode node2 = new TreeNode(6);
		TreeNode node3 = new TreeNode(1);
		TreeNode node4 = new TreeNode(3);
		root.left = node1;
		root.right = node2;
		node1.left = node3;
		node1.right = node4;
		TreeNode res = ancestor.lowestCommonAncestor(root, node1, node4);
		System.out.println(res.val);
	}

}
