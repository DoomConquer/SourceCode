
public class SymmetricTree {
	public boolean isSymmetric(TreeNode root) {
		StringBuffer preOld = new StringBuffer();
		StringBuffer orderOld = new StringBuffer();
		preTraverse(root, preOld);
		orderTraverse(root, orderOld);
		exchange(root);
		StringBuffer preNew = new StringBuffer();
		StringBuffer orderNew = new StringBuffer();
		preTraverse(root, preNew);
		orderTraverse(root, orderNew);
		return preOld.toString().equals(preNew.toString()) && orderOld.toString().equals(orderNew.toString());
	}
	private void exchange(TreeNode root){
		if(root == null)
			return;
		if(root.left != null || root.right != null){
			TreeNode node = root.left;
			root.left = root.right;
			root.right = node;
		}
		exchange(root.left);
		exchange(root.right);
	}
	private void preTraverse(TreeNode root, StringBuffer s){
		if(root == null)
			return;
		preTraverse(root.left, s);
		preTraverse(root.right, s);
		s.append(root.val);
	}
	private void orderTraverse(TreeNode root, StringBuffer s){
		if(root == null)
			return;
		orderTraverse(root.left, s);
		s.append(root.val);
		orderTraverse(root.right, s);
	}
	public static void main(String[] args) {
		TreeNode root = new TreeNode(1);
		TreeNode node1 = new TreeNode(2);
		TreeNode node2 = new TreeNode(2);
		TreeNode node3 = new TreeNode(3);
		TreeNode node4 = new TreeNode(4);
		TreeNode node5 = new TreeNode(4);
		TreeNode node6 = new TreeNode(3);
		root.left = node1;
		root.right = node2;
		node1.left = node3;
		node1.right = node4;
		node2.left = node5;
		node2.right = node6;
		
		SymmetricTree tree = new SymmetricTree();
		System.out.println(tree.isSymmetric(root));
	}

}
