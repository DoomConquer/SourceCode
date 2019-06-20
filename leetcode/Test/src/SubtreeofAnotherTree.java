
/**
 * @author li_zhe
 * 思路简单,但想的比较久
 */
public class SubtreeofAnotherTree {

	public boolean isSubtree(TreeNode s, TreeNode t) {
		if(s == null || t == null) return false;
		if(isSub(s, t)) return true;
		else return isSubtree(s.left, t) || isSubtree(s.right, t);
	}
	private boolean isSub(TreeNode s, TreeNode t){
		if(s == null && t == null) return true;
		if(s == null || t == null) return false;
		if(s.val == t.val && isSub(s.left, t.left) && isSub(s.right, t.right)) return true;
		return false;
	}
	
	public static void main(String[] args) {
		SubtreeofAnotherTree tree = new SubtreeofAnotherTree();
//		TreeNode s = new TreeNode(3);
//		TreeNode snode1 = new TreeNode(4);
//		TreeNode snode2 = new TreeNode(5);
//		TreeNode snode3 = new TreeNode(1);
//		TreeNode snode4 = new TreeNode(2);
//		TreeNode snode5 = new TreeNode(2);
//		s.left = snode1;
//		s.right = snode2;
//		snode1.left = snode3;
//		snode1.right = snode4;
//		snode4.left = snode5;
//		TreeNode t = new TreeNode(4);
//		TreeNode tnode1 = new TreeNode(1);
//		TreeNode tnode2 = new TreeNode(2);
//		t.left = tnode1;
//		t.right = tnode2;
		
		TreeNode s = new TreeNode(3);
		TreeNode snode1 = new TreeNode(4);
		TreeNode snode2 = new TreeNode(5);
		TreeNode snode3 = new TreeNode(1);
		TreeNode snode4 = new TreeNode(2);
		s.left = snode1;
		s.right = snode2;
		snode1.left = snode3;
		snode2.left = snode4;
		TreeNode t = new TreeNode(3);
		TreeNode tnode1 = new TreeNode(1);
		TreeNode tnode2 = new TreeNode(2);
		t.left = tnode1;
		t.right = tnode2;
		System.out.println(tree.isSubtree(s, t));
	}

}
