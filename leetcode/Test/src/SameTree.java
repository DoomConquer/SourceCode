
public class SameTree {

	public boolean isSameTree(TreeNode p, TreeNode q) {
		 if(p == null && q == null) return true;
		 else if((p == null && q != null) || (p != null && q == null)) return false;
		 if(p.val != q.val) return false;
		 return isSameTree(p.left, q.left) && isSameTree(p.right, q.right);
	}
	 
	public static void main(String[] args) {
		SameTree tree = new SameTree();
		TreeNode p = new TreeNode(2);
		TreeNode node1 = new TreeNode(2);
		p.left = node1;
		
		TreeNode q = new TreeNode(2);
		TreeNode node2 = new TreeNode(2);
		q.right = node2;
		
		System.out.println(tree.isSameTree(p, q));
	}

}
