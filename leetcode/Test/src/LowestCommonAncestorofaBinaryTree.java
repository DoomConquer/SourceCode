import java.util.Stack;

public class LowestCommonAncestorofaBinaryTree {

	public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
		Stack<TreeNode> path1 = new Stack<>();
		Stack<TreeNode> path2 = new Stack<>();
		traverse(root, p, path1);
		traverse(root, q, path2);
		if(path1.size() > path2.size()){
			Stack<TreeNode> temp = path1;
			path1 = path2;
			path2 = temp;
		}
		int diff = path2.size() - path1.size();
		while(diff-- > 0) path2.pop();
		while(!path1.isEmpty() && !path2.isEmpty()){
			if(path1.peek().val == path2.peek().val) return path1.peek();
			path1.pop(); path2.pop();
		}
		return null;
	}
	private boolean traverse(TreeNode root, TreeNode node, Stack<TreeNode> path){
		if(root == null) return false;
		path.push(root);
		if(root.val == node.val) return true;
		if(traverse(root.left, node, path)) return true;
		if(traverse(root.right, node, path)) return true;
		path.pop();
		return false;
	}
	
	// µÝ¹é·½·¨
	public TreeNode lowestCommonAncestor1(TreeNode root, TreeNode p, TreeNode q) {
        if(root == null) return null;
        if(root == p || root == q) return root;
        TreeNode left_lca = lowestCommonAncestor1(root.left, p, q);
        TreeNode right_lca = lowestCommonAncestor1(root.right, p, q);
        if(left_lca != null && right_lca != null) return root;
        return (left_lca != null) ? left_lca : right_lca;
            
    }
	
	public static void main(String[] args) {
		LowestCommonAncestorofaBinaryTree ancestor = new LowestCommonAncestorofaBinaryTree();
		TreeNode root = new TreeNode(1);
		TreeNode node1 = new TreeNode(2);
		TreeNode node2 = new TreeNode(3);
		TreeNode node3 = new TreeNode(4);
		TreeNode node4 = new TreeNode(5);
		root.left = node1;
		root.right = node2;
		node1.left = node3;
		node1.right = node4;
		TreeNode res = ancestor.lowestCommonAncestor(root, node1, node4);
		System.out.println(res.val);
	}

}
